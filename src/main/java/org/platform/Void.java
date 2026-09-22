package org.platform;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Void system:
 *   - If a player falls below the configured Y level, teleport to spawn.
 *   - Full heal, kit restore, hunger refill.
 *
 * Set with /platform setvoid [y]
 */
public class Void implements Listener {

    private final JavaPlugin plugin;
    private final PlayerJoin playerJoin;

    private double killHeight;

    // Prevent double-teleport
    private final Set<UUID> teleporting = new HashSet<UUID>();

    public Void(JavaPlugin plugin, PlayerJoin playerJoin) {
        this.plugin = plugin;
        this.playerJoin = playerJoin;
        loadConfiguration();
    }

    public void loadConfiguration() {
        FileConfiguration config = this.plugin.getConfig();
        this.killHeight = config.getDouble("void.kill-height", -13.0D);
    }

    public void reloadConfig() {
        loadConfiguration();
    }

    public double getKillHeight() {
        return this.killHeight;
    }

    public void setKillHeight(double y) {
        this.killHeight = y;
        FileConfiguration config = this.plugin.getConfig();
        config.set("void.kill-height", Double.valueOf(y));
        this.plugin.saveConfig();
    }

    // ============================================================
    //  Detect falling below kill-height
    // ============================================================
    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.isDead()) return;
        if (player.getHealth() <= 0) return;
        if (this.teleporting.contains(player.getUniqueId())) return;

        Location to = event.getTo();
        if (to == null) return;

        if (to.getY() < this.killHeight) {
            this.teleporting.add(player.getUniqueId());
            handleVoidFall(player);
        }
    }

    // ============================================================
    //  Handle void fall
    // ============================================================
    private void handleVoidFall(final Player player) {
        // Full heal
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20.0F);
        player.setExhaustion(0.0F);
        player.setFireTicks(0);
        player.setFallDistance(0.0F);

        // Clear effects
        for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        // Clear inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        // Teleport to spawn
        Location spawn = getSpawnLocation();
        if (spawn != null) {
            player.teleport(spawn);
        } else {
            player.teleport(player.getWorld().getSpawnLocation());
        }

        // Give kit after teleport
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    playerJoin.giveKit(player);
                }
            }
        }, 2L);

        // Remove from teleporting set after 1 second
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                teleporting.remove(player.getUniqueId());
            }
        }, 20L);
    }

    // ============================================================
    //  Spawn location from config
    // ============================================================
    private Location getSpawnLocation() {
        FileConfiguration config = this.plugin.getConfig();
        if (!config.contains("spawn.world")) return null;

        String worldName = config.getString("spawn.world");
        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = config.getDouble("spawn.x");
        double y = config.getDouble("spawn.y");
        double z = config.getDouble("spawn.z");
        float yaw = (float) config.getDouble("spawn.yaw");
        float pitch = (float) config.getDouble("spawn.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }
}
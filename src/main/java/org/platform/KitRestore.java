package org.platform;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Restores the cosmetic kit when the inventory is empty.
 *
 * Runs two ways:
 *   1. On /clear command  (immediate response)
 *   2. On a repeating task (safety net — catches any other way
 *      the inventory can be emptied: /clear from another plugin,
 *      /kit reset, admin clear, etc.)
 */
public class KitRestore implements Listener {

    private final JavaPlugin plugin;
    private final PlayerJoin playerJoin;

    // Prevent spamming the message
    private long lastRestoreTime = 0L;
    private static final long RESTORE_COOLDOWN = 3000L;

    public KitRestore(JavaPlugin plugin, PlayerJoin playerJoin) {
        this.plugin = plugin;
        this.playerJoin = playerJoin;

        startSafetyTask();
    }

    // ============================================================
    //  1. Detect /clear and restore
    // ============================================================
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String raw = event.getMessage().toLowerCase();
        if (raw.startsWith("/")) raw = raw.substring(1);

        String[] parts = raw.split(" ");
        String cmd = parts[0];

        // Strip namespace (minecraft:clear -> clear)
        if (cmd.contains(":")) {
            cmd = cmd.substring(cmd.indexOf(':') + 1);
        }

        if (!cmd.equals("clear")) return;

        final Player player = event.getPlayer();

        // Wait 20 ticks (1 second) — plenty of time for /clear to finish
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                restore(player, true);
            }
        }, 20L);
    }

    // ============================================================
    //  2. Safety net — check every 20 ticks (1 second)
    //     If inventory is empty and player is in survival, restore.
    // ============================================================
    private void startSafetyTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getGameMode() == GameMode.CREATIVE) continue;
                    if (!isEmpty(player)) continue;
                    restore(player, false);
                }
            }
        }.runTaskTimer(this.plugin, 40L, 20L); // start after 2s, every 1s
    }

    // ============================================================
    //  Restore helper
    // ============================================================
    private void restore(Player player, boolean notifyPlayer) {
        if (player == null || !player.isOnline()) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        // Cooldown to prevent spamming
        long now = System.currentTimeMillis();
        if (now - lastRestoreTime < RESTORE_COOLDOWN) return;
        lastRestoreTime = now;

        playerJoin.giveKit(player);

        if (notifyPlayer) {
            player.sendMessage(colorize("&aYour cosmetic kit has been restored."));
        }
    }

    // ============================================================
    //  isEmpty — true if main inventory + armor are ALL empty
    // ============================================================
    private boolean isEmpty(Player player) {
        if (player.getInventory().getHelmet() != null) return false;
        if (player.getInventory().getChestplate() != null) return false;
        if (player.getInventory().getLeggings() != null) return false;
        if (player.getInventory().getBoots() != null) return false;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
package org.platform;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Cosmetic kit:
 *   - Leather Helmet     (Protection III, color 16711680 = pure red, Unbreakable)
 *   - Leather Chestplate (Protection III, color 16711680 = pure red, Unbreakable)
 *   - Iron Leggings      (Protection III, Unbreakable)
 *   - Iron Boots         (Protection III, Unbreakable)
 *   - Wooden Sword       (Sharpness I, Unbreakable)
 */
public class PlayerJoin implements Listener {

    private final JavaPlugin plugin;

    // 16711680 = 0xFF0000 = pure red
    private static final int LEATHER_COLOR = 16711680;

    public PlayerJoin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                giveKit(player);
                teleportToSpawn(player);
            }
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                giveKit(player);
            }
        }, 5L);
    }

    // ============================================================
    //  COSMETIC KIT
    // ============================================================
    public void giveKit(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        player.getInventory().setHelmet(dyedLeather(Material.LEATHER_HELMET));
        player.getInventory().setChestplate(dyedLeather(Material.LEATHER_CHESTPLATE));
        player.getInventory().setLeggings(protectionIron(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(protectionIron(Material.IRON_BOOTS));

        ItemStack sword = new ItemStack(Material.WOOD_SWORD);
        sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
        player.getInventory().setItem(0, unbreakable(sword));

        player.setFoodLevel(20);
        player.setSaturation(20.0F);
        player.setExhaustion(0.0F);

        player.updateInventory();
    }

    private ItemStack dyedLeather(Material mat) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(Color.fromRGB(LEATHER_COLOR));
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
        meta.spigot().setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack protectionIron(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
        meta.spigot().setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack unbreakable(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.spigot().setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    // ============================================================
    //  SPAWN TELEPORT
    // ============================================================
    public void teleportToSpawn(Player player) {
        Location spawn = getSpawnLocation();
        if (spawn == null) return;
        player.teleport(spawn);
    }

    public Location getSpawnLocation() {
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

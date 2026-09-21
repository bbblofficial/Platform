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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Watches for /clear (and similar commands) and restores the
 * cosmetic kit automatically after the command runs.
 */
public class KitRestore implements Listener {

    private final JavaPlugin plugin;
    private final PlayerJoin playerJoin;

    public KitRestore(JavaPlugin plugin, PlayerJoin playerJoin) {
        this.plugin = plugin;
        this.playerJoin = playerJoin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String raw = event.getMessage().toLowerCase();
        if (raw.startsWith("/")) raw = raw.substring(1);

        String[] parts = raw.split(" ");
        String cmd = parts[0];

        boolean isClear = cmd.equals("clear")
                || cmd.endsWith(":clear")
                || cmd.equals("minecraft:clear");

        if (!isClear) return;

        final Player player = event.getPlayer();

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                if (player.getGameMode() == GameMode.CREATIVE) return;
                if (!isEmpty(player)) return;

                playerJoin.giveKit(player);
                player.sendMessage(colorize("&aYour cosmetic kit has been restored."));
            }
        }, 3L);
    }

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

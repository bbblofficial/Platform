package org.platform;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Watches for /clear and restores the cosmetic kit automatically.
 * Always re-gives the kit (no isEmpty check) so it works with any
 * clear command variant.
 */
public class KitRestore implements Listener {

    private final JavaPlugin plugin;
    private final PlayerJoin playerJoin;

    public KitRestore(JavaPlugin plugin, PlayerJoin playerJoin) {
        this.plugin = plugin;
        this.playerJoin = playerJoin;
    }

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

        // Give /clear a few ticks to finish, then re-give kit
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                if (player == null || !player.isOnline()) return;
                if (player.getGameMode() == GameMode.CREATIVE) return;

                playerJoin.giveKit(player);
                player.sendMessage(colorize("&aYour cosmetic kit has been restored."));
            }
        }, 5L);
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
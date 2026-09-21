package org.platform;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Custom join/quit messages (like BuildFFA).
 *
 *   - Sets player gamemode to SURVIVAL on join
 *   - Broadcasts configurable join/quit messages
 *   - Replaces %player%, %online% and %max_online% placeholders
 *
 * No kill/death messages — this plugin has no PvP deaths (cosmetic only).
 */
public class Welcome implements Listener {

    private final JavaPlugin plugin;

    public Welcome(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // ============================================================
    //  JOIN
    // ============================================================
    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Force survival (so the cosmetic kit armor works normally)
        if (player.getGameMode() != GameMode.SURVIVAL) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        // Remove vanilla join message
        event.setJoinMessage(null);

        FileConfiguration config = this.plugin.getConfig();
        String joinMessage = config.getString("join-message",
                "&b%player% &7joined the game &8(&b%online%&7/&b%max_online%&8)");

        String rendered = colorize(joinMessage)
                .replace("%player%", player.getName())
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_online%", String.valueOf(Bukkit.getMaxPlayers()));

        Bukkit.broadcastMessage(rendered);
    }

    // ============================================================
    //  QUIT
    // ============================================================
    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove vanilla quit message
        event.setQuitMessage(null);

        FileConfiguration config = this.plugin.getConfig();
        String quitMessage = config.getString("quit-message",
                "&b%player% &7left the game &8(&b%online%&7/&b%max_online%&8)");

        // Subtract 1 because the player is still counted while leaving
        int onlineAfter = Math.max(0, Bukkit.getOnlinePlayers().size() - 1);

        String rendered = colorize(quitMessage)
                .replace("%player%", player.getName())
                .replace("%online%", String.valueOf(onlineAfter))
                .replace("%max_online%", String.valueOf(Bukkit.getMaxPlayers()));

        Bukkit.broadcastMessage(rendered);
    }

    // ============================================================
    //  Helper
    // ============================================================
    private String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
package org.platform;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Protection rules:
 *   - Players cannot break map blocks  (permission: platform.break)
 *   - Players cannot place blocks      (permission: platform.place)
 *   - Players cannot drop items        (permission: platform.drop)
 *
 *   The "platform.bypass" permission bypasses ALL of the above.
 *   OP players have all these permissions by default.
 */
public class Protection implements Listener {

    @SuppressWarnings("unused")
    private final JavaPlugin plugin;

    private static final String PERM_BYPASS = "platform.bypass";
    private static final String PERM_BREAK  = "platform.break";
    private static final String PERM_PLACE  = "platform.place";
    private static final String PERM_DROP   = "platform.drop";

    public Protection(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean hasBypass(Player player) {
        return player.hasPermission(PERM_BYPASS);
    }

    // ============================================================
    //  Block Break Protection
    // ============================================================
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (hasBypass(player)) return;
        if (player.hasPermission(PERM_BREAK)) return;

        event.setCancelled(true);
        player.sendMessage(colorize("&cYou cannot break blocks here!"));
    }

    // ============================================================
    //  Block Place Protection
    // ============================================================
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (hasBypass(player)) return;
        if (player.hasPermission(PERM_PLACE)) return;

        event.setCancelled(true);
        player.sendMessage(colorize("&cYou cannot place blocks here!"));
    }

    // ============================================================
    //  Item Drop Protection
    // ============================================================
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (hasBypass(player)) return;
        if (player.hasPermission(PERM_DROP)) return;

        event.setCancelled(true);
        player.sendMessage(colorize("&cYou cannot drop items here!"));
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}

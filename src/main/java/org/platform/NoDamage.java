package org.platform;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Cosmetic PvP rules:
 *   - NO health is ever lost (all damage cancelled, except void)
 *   - Fall damage is always off
 *   - PvP damage is always off (but knockback still applies)
 *   - Infinite food - players never get hungry
 */
public class NoDamage implements Listener {

    @SuppressWarnings("unused")
    private final JavaPlugin plugin;

    public NoDamage(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // ============================================================
    //  Cancel ALL damage to players (except void)
    // ============================================================
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnyDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        EntityDamageEvent.DamageCause cause = event.getCause();

        // Let void handle itself (spawn teleport / void kill logic)
        if (cause == EntityDamageEvent.DamageCause.VOID) return;

        event.setCancelled(true);
        event.setDamage(0);
    }

    // ============================================================
    //  Explicit Fall Damage blocker (extra safety net)
    // ============================================================
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
            event.setDamage(0);
        }
    }

    // ============================================================
    //  Extra safety: cancel direct PvP damage at MONITOR too
    // ============================================================
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        event.setCancelled(true);
        event.setDamage(0);
    }

    // ============================================================
    //  Infinite Food - players never get hungry
    // ============================================================
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (event.getFoodLevel() < 20) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            player.setSaturation(20.0F);
            player.setExhaustion(0.0F);
        }
    }
}

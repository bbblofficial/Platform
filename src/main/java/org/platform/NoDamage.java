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
 *   - PvP is fully functional (hits register, knockback works)
 *   - But HP is never lost - damage is set to 0, not cancelled
 *   - Fall damage is fully off
 *   - Infinite food - players never get hungry
 */
public class NoDamage implements Listener {

    @SuppressWarnings("unused")
    private final JavaPlugin plugin;

    public NoDamage(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // ============================================================
    //  MAIN: Zero out all damage to players (except void)
    //  We use setDamage(0) instead of setCancelled(true) so that:
    //    - Knockback still happens
    //    - Hit sounds + animation still play
    //    - PvP feels fully real, just no HP loss
    // ============================================================
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnyDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.VOID) return;

        event.setDamage(0);
    }

    // ============================================================
    //  PvP damage - zero out, keep knockback
    // ============================================================
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!event.isCancelled()) {
            event.setDamage(0);
        }
    }

    // ============================================================
    //  Fall damage - fully disabled
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

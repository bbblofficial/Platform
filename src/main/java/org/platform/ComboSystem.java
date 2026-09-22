package org.platform;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Combo System
 *
 * Every time a player hits an opponent, their combo counter goes up by 1.
 * If the player doesn't land a hit for X seconds, their combo resets.
 * If the player takes a hit, the opponent's combo resets.
 *
 * At specific combo milestones (e.g. 10, 20, 30...) a broadcast is sent.
 */
public class ComboSystem implements Listener {

    private final JavaPlugin plugin;

    // Current combo per player
    private final Map<UUID, Integer> combos = new HashMap<UUID, Integer>();

    // Timestamp of last hit per player (used for auto-reset)
    private final Map<UUID, Long> lastHitTime = new HashMap<UUID, Long>();

    // Config values
    private boolean enabled;
    private int comboStep;
    private long comboResetTime;
    private String broadcastMessage;
    private boolean soundEnabled;

    public ComboSystem(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfiguration();
        startResetTask();
    }

    // ============================================================
    //  CONFIG
    // ============================================================
    public void loadConfiguration() {
        FileConfiguration config = this.plugin.getConfig();
        this.enabled        = config.getBoolean("combo.enabled", true);
        this.comboStep      = config.getInt("combo.step", 10);
        this.comboResetTime = config.getLong("combo.reset-time", 3000L);
        this.broadcastMessage = config.getString("combo.broadcast-message",
                "&8&m-------------------------------\n"
              + "&6&l⚔ COMBO &e&l%combo%x\n"
              + "&e%attacker% &7got a combo on &c%victim% &7(&6%combo% &7combo)\n"
              + "&8&m-------------------------------");
        this.soundEnabled   = config.getBoolean("combo.sound-enabled", true);

        if (this.comboStep < 1) this.comboStep = 10;
        if (this.comboResetTime < 500L) this.comboResetTime = 3000L;

        this.plugin.getLogger().info("Platform combo system loaded: "
                + (this.enabled ? "ENABLED (step " + this.comboStep + ")" : "DISABLED"));
    }

    public void reloadConfig() {
        loadConfiguration();
    }

    // ============================================================
    //  DAMAGE DETECTION
    // ============================================================
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!this.enabled) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        Player attacker = (Player) event.getDamager();
        Player victim   = (Player) event.getEntity();

        UUID attackerId = attacker.getUniqueId();
        UUID victimId   = victim.getUniqueId();

        // Victim got hit -> their own combo resets
        resetCombo(victimId);

        // Attacker's combo +1
        int combo = this.combos.containsKey(attackerId)
                ? this.combos.get(attackerId).intValue() + 1
                : 1;

        this.combos.put(attackerId, Integer.valueOf(combo));
        this.lastHitTime.put(attackerId, Long.valueOf(System.currentTimeMillis()));

        // Announce combo milestone
        if (combo % this.comboStep == 0) {
            announceCombo(attacker, victim, combo);
        }
    }

    // ============================================================
    //  ANNOUNCE COMBO
    // ============================================================
    private void announceCombo(Player attacker, Player victim, int combo) {
        String message = colorize(
                this.broadcastMessage
                        .replace("%combo%", String.valueOf(combo))
                        .replace("%attacker%", attacker.getName())
                        .replace("%victim%", victim.getName())
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(message);
        }

        if (this.soundEnabled) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (combo >= this.comboStep * 2) {
                    p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 0.8F, 1.2F);
                } else {
                    p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.5F);
                }
            }
        }
    }

    // ============================================================
    //  RESET COMBO
    // ============================================================
    private void resetCombo(UUID playerId) {
        this.combos.remove(playerId);
        this.lastHitTime.remove(playerId);
    }

    // ============================================================
    //  AUTO RESET TASK (if player doesn't hit for X seconds)
    // ============================================================
    private void startResetTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();

                Iterator<Map.Entry<UUID, Long>> it = lastHitTime.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, Long> entry = it.next();
                    if (now - entry.getValue().longValue() > comboResetTime) {
                        combos.remove(entry.getKey());
                        it.remove();
                    }
                }
            }
        }.runTaskTimer(this.plugin, 20L, 20L);
    }

    // ============================================================
    //  CLEANUP ON QUIT
    // ============================================================
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        this.combos.remove(id);
        this.lastHitTime.remove(id);
    }

    // ============================================================
    //  PUBLIC API (optional)
    // ============================================================
    public int getCombo(Player player) {
        if (!this.combos.containsKey(player.getUniqueId())) return 0;
        return this.combos.get(player.getUniqueId()).intValue();
    }

    public void clearCombo(Player player) {
        resetCombo(player.getUniqueId());
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
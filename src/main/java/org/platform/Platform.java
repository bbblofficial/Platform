package org.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Platform extends JavaPlugin {

    private UnstableConnection unstableConnection;
    private PlayerJoin playerJoin;
    private ScoreboardManager scoreboardManager;
    private Void voidSystem;
    private ComboSystem comboSystem;

    @Override
    public void onEnable() {

        // ---- folders ----
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // ---- config auto-merge ----
        createConfigIfMissing();
        saveDefaultConfig();
        reloadConfig();

        // ---- listeners ----
        this.playerJoin = new PlayerJoin(this);
        getServer().getPluginManager().registerEvents(this.playerJoin, this);
        getServer().getPluginManager().registerEvents(new NoDamage(this), this);
        getServer().getPluginManager().registerEvents(new Protection(this), this);
        getServer().getPluginManager().registerEvents(new KitRestore(this, this.playerJoin), this);
        getServer().getPluginManager().registerEvents(new Welcome(this), this);

        // ---- void ----
        this.voidSystem = new Void(this, this.playerJoin);
        getServer().getPluginManager().registerEvents(this.voidSystem, this);

        // ---- combo ----
        this.comboSystem = new ComboSystem(this);
        getServer().getPluginManager().registerEvents(this.comboSystem, this);

        // ---- unstable connection ----
        this.unstableConnection = new UnstableConnection(this);

        // ---- scoreboard ----
        this.scoreboardManager = new ScoreboardManager(this);

        // ---- command ----
        PlatformCommand cmd = new PlatformCommand(this, this.unstableConnection,
                this.playerJoin, this.scoreboardManager, this.voidSystem);
        getCommand("platform").setExecutor(cmd);
        getCommand("platform").setTabCompleter(cmd);

        getLogger().info("=================================================");
        getLogger().info("  Platform v1.0 - Enabled");
        getLogger().info("  Simple cosmetic PvP plugin");
        getLogger().info("=================================================");
    }

    @Override
    public void onDisable() {
        if (this.scoreboardManager != null) {
            this.scoreboardManager.shutdown();
        }
        getLogger().info("Platform disabled.");
    }

    // ============================================================
    //  CONFIG AUTO-MERGE
    // ============================================================
    private void createConfigIfMissing() {
        File configFile = new File(getDataFolder(), "config.yml");
        boolean isNew = !configFile.exists();

        if (isNew) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                getLogger().warning("Could not create config.yml: " + e.getMessage());
                return;
            }
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);

        InputStream defStream = this.getResource("config.yml");
        if (defStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            cfg.setDefaults(defaults);
        }

        // ---- spawn ----
        setIfMissing(cfg, "spawn.world", "world");
        setIfMissing(cfg, "spawn.x", Double.valueOf(0.5D));
        setIfMissing(cfg, "spawn.y", Double.valueOf(100.0D));
        setIfMissing(cfg, "spawn.z", Double.valueOf(0.5D));
        setIfMissing(cfg, "spawn.yaw", Float.valueOf(0.0F));
        setIfMissing(cfg, "spawn.pitch", Float.valueOf(0.0F));

        // ---- void ----
        setIfMissing(cfg, "void.kill-height", Double.valueOf(-13.0D));

        // ---- connection check ----
        setIfMissing(cfg, "connection-check.enabled", Boolean.valueOf(true));
        setIfMissing(cfg, "connection-check.ping-threshold", Integer.valueOf(150));
        setIfMissing(cfg, "connection-check.check-interval", Integer.valueOf(20));
        setIfMissing(cfg, "connection-check.grace-seconds", Integer.valueOf(30));
        setIfMissing(cfg, "connection-check.warn-cooldown", Integer.valueOf(5));
        setIfMissing(cfg, "connection-check.kick-message",
                "&cUnstable connection\\n&fYour ping is too high: &e%ping%ms&7/&e%max%ms");
        setIfMissing(cfg, "connection-check.broadcast-message",
                "&c%player% &7was kicked for &eUnstable Connection &7(&c%ping%ms&7)");

        // ---- messages ----
        setIfMissing(cfg, "join-message",
                "&b%player% &7joined the game &8(&b%online%&7/&b%max_online%&8)");
        setIfMissing(cfg, "quit-message",
                "&b%player% &7left the game &8(&b%online%&7/&b%max_online%&8)");

        // ---- combo ----
        setIfMissing(cfg, "combo.enabled", Boolean.valueOf(true));
        setIfMissing(cfg, "combo.step", Integer.valueOf(10));
        setIfMissing(cfg, "combo.reset-time", Long.valueOf(3000L));
        setIfMissing(cfg, "combo.sound-enabled", Boolean.valueOf(true));
        setIfMissing(cfg, "combo.broadcast-message",
                "&8&m-------------------------------\\n&6&lCOMBO &e&l%combo%x\\n&e%attacker% &7got a combo on &c%victim% &7(&6%combo% &7combo)\\n&8&m-------------------------------");

        try {
            cfg.save(configFile);
            if (isNew) {
                getLogger().info("Created default config.yml");
            } else {
                getLogger().info("Config.yml merged (existing values preserved).");
            }
        } catch (IOException e) {
            getLogger().warning("Could not save config.yml: " + e.getMessage());
        }
    }

    private void setIfMissing(FileConfiguration cfg, String path, Object value) {
        if (!cfg.contains(path)) {
            cfg.set(path, value);
        }
    }

    public UnstableConnection getUnstableConnection() {
        return this.unstableConnection;
    }

    public PlayerJoin getPlayerJoin() {
        return this.playerJoin;
    }

    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    public Void getVoidSystem() {
        return this.voidSystem;
    }

    public ComboSystem getComboSystem() {
        return this.comboSystem;
    }
}
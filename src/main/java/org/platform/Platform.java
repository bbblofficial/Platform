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

        // ---- unstable connection ----
        this.unstableConnection = new UnstableConnection(this);

        // ---- command ----
        PlatformCommand cmd = new PlatformCommand(this, this.unstableConnection, this.playerJoin);
        getCommand("platform").setExecutor(cmd);
        getCommand("platform").setTabCompleter(cmd);

        getLogger().info("=================================================");
        getLogger().info("  Platform v1.0 - Enabled");
        getLogger().info("  Simple cosmetic PvP plugin");
        getLogger().info("=================================================");
    }

    @Override
    public void onDisable() {
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

        // ---- connection check ----
        setIfMissing(cfg, "connection-check.enabled", Boolean.valueOf(true));
        setIfMissing(cfg, "connection-check.ping-threshold", Integer.valueOf(150));
        setIfMissing(cfg, "connection-check.check-interval", Integer.valueOf(20));
        setIfMissing(cfg, "connection-check.grace-seconds", Integer.valueOf(30));
        setIfMissing(cfg, "connection-check.warn-cooldown", Integer.valueOf(5));
        setIfMissing(cfg, "connection-check.kick-message",
                "&cUnstable connection\n&fYour ping is too high: &e%ping%ms&7/&e%max%ms");
        setIfMissing(cfg, "connection-check.broadcast-message",
                "&c%player% &7was kicked for &eUnstable Connection &7(&c%ping%ms&7)");

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
}

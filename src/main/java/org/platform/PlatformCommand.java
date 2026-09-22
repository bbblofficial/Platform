package org.platform;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlatformCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final PlayerJoin playerJoin;
    private final ScoreboardManager scoreboardManager;
    private final Void voidSystem;

    public PlatformCommand(JavaPlugin plugin,
                           PlayerJoin playerJoin, ScoreboardManager scoreboardManager,
                           Void voidSystem) {
        this.plugin = plugin;
        this.playerJoin = playerJoin;
        this.scoreboardManager = scoreboardManager;
        this.voidSystem = voidSystem;
    }

    // ============================================================
    //  onCommand
    // ============================================================
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        // ---------- /platform setspawn ----------
        if (sub.equals("setspawn")) {
            if (!sender.hasPermission("platform.setspawn")) {
                sendNoPerm(sender);
                return true;
            }
            return handleSetSpawn(sender);
        }

        // ---------- /platform setvoid [y] ----------
        if (sub.equals("setvoid")) {
            if (!sender.hasPermission("platform.setvoid")) {
                sendNoPerm(sender);
                return true;
            }
            return handleSetVoid(sender, args);
        }

        // ---------- /platform kit [player] ----------
        if (sub.equals("kit")) {
            if (!sender.hasPermission("platform.kit")) {
                sendNoPerm(sender);
                return true;
            }
            return handleKit(sender, args);
        }

        // ---------- /platform creator ----------
        if (sub.equals("creator")) {
            return handleCreator(sender);
        }

        // ---------- /platform sb ... ----------
        if (sub.equals("sb") || sub.equals("scoreboard")) {
            if (!sender.hasPermission("platform.scoreboard.toggle")) {
                sendNoPerm(sender);
                return true;
            }
            return handleScoreboard(sender, args);
        }

        // ---------- /platform reload ----------
        if (sub.equals("reload")) {
            if (!sender.hasPermission("platform.reload")) {
                sendNoPerm(sender);
                return true;
            }
            this.plugin.reloadConfig();
            if (this.scoreboardManager != null) {
                this.scoreboardManager.reloadConfig();
            }
            if (this.voidSystem != null) {
                this.voidSystem.reloadConfig();
            }
            sender.sendMessage(colorize("&aPlatform configuration reloaded."));
            return true;
        }

        if (sub.equals("help")) {
            sendHelp(sender);
            return true;
        }

        sender.sendMessage(colorize("&cUnknown subcommand. Use /platform help"));
        return true;
    }

    // ============================================================
    //  /platform setspawn
    // ============================================================
    private boolean handleSetSpawn(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(colorize("&cOnly players can use setspawn."));
            return true;
        }

        Player player = (Player) sender;
        Location loc = player.getLocation();

        FileConfiguration config = this.plugin.getConfig();
        config.set("spawn.world", loc.getWorld().getName());
        config.set("spawn.x", Double.valueOf(loc.getX()));
        config.set("spawn.y", Double.valueOf(loc.getY()));
        config.set("spawn.z", Double.valueOf(loc.getZ()));
        config.set("spawn.yaw", Float.valueOf(loc.getYaw()));
        config.set("spawn.pitch", Float.valueOf(loc.getPitch()));
        this.plugin.saveConfig();

        player.sendMessage(colorize("&aSpawn set to &e"
                + loc.getWorld().getName() + " "
                + loc.getBlockX() + " "
                + loc.getBlockY() + " "
                + loc.getBlockZ() + "&a."));
        return true;
    }

    // ============================================================
    //  /platform setvoid [y]
    // ============================================================
    private boolean handleSetVoid(CommandSender sender, String[] args) {
        if (this.voidSystem == null) {
            sender.sendMessage(colorize("&cError: Void system not found."));
            return true;
        }

        double y;

        if (args.length >= 2) {
            try {
                y = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(colorize("&cInvalid number: &e" + args[1]));
                sender.sendMessage(colorize("&7Usage: &e/platform setvoid [y]"));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(colorize("&cYou must be a player to use setvoid without a value."));
                sender.sendMessage(colorize("&7From console: &e/platform setvoid <y>"));
                return true;
            }
            Player player = (Player) sender;
            y = player.getLocation().getY();
        }

        this.voidSystem.setKillHeight(y);
        this.voidSystem.reloadConfig();

        sender.sendMessage(colorize("&aVoid kill height set to &e" + y + " &a(Y level)."));
        return true;
    }

    // ============================================================
    //  /platform kit [player]
    // ============================================================
    private boolean handleKit(CommandSender sender, String[] args) {
        Player target;

        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(colorize("&cPlayer not found: &e" + args[1]));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(colorize("&cUsage from console: /platform kit <player>"));
                return true;
            }
            target = (Player) sender;
        }

        try {
            this.playerJoin.giveKit(target);
        } catch (Throwable t) {
            sender.sendMessage(colorize("&cFailed to give kit: " + t.getMessage()));
            return true;
        }

        if (sender.equals(target)) {
            sender.sendMessage(colorize("&aYour cosmetic kit has been restored."));
        } else {
            sender.sendMessage(colorize("&aGave cosmetic kit to &e" + target.getName() + "&a."));
            target.sendMessage(colorize("&aYour cosmetic kit has been restored."));
        }
        return true;
    }

    // ============================================================
    //  /platform creator
    // ============================================================
    private boolean handleCreator(CommandSender sender) {
        sender.sendMessage(colorize("&8&m----------------------------------"));
        sender.sendMessage(colorize("&6&lPlatform &7- &fCreated by &bMuvixo"));
        sender.sendMessage(colorize("&7Version: &f1.0"));
        sender.sendMessage(colorize("&8&m----------------------------------"));
        return true;
    }

    // ============================================================
    //  /platform sb [reload]
    // ============================================================
    private boolean handleScoreboard(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(colorize("&cOnly players can use the scoreboard command."));
            return true;
        }
        Player player = (Player) sender;

        if (args.length >= 2 && args[1].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("platform.reload")) {
                sendNoPerm(sender);
                return true;
            }
            if (this.scoreboardManager == null) {
                player.sendMessage(colorize("&cError: ScoreboardManager not found."));
                return true;
            }
            this.scoreboardManager.reloadConfig();
            player.sendMessage(colorize("&aScoreboard configuration reloaded."));
            return true;
        }

        if (this.scoreboardManager == null) {
            player.sendMessage(colorize("&cError: ScoreboardManager not found."));
            return true;
        }

        boolean nowVisible = this.scoreboardManager.toggleScoreboard(player);
        if (nowVisible) {
            player.sendMessage(colorize("&aScoreboard &lENABLED&a."));
        } else {
            player.sendMessage(colorize("&cScoreboard &lDISABLED&c."));
        }
        return true;
    }

    // ============================================================
    //  Help
    // ============================================================
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(colorize("&8&m----------------------------------"));
        sender.sendMessage(colorize("&6&lPlatform &7- &fCommands"));
        sender.sendMessage(colorize("&8&m----------------------------------"));

        sender.sendMessage(colorize("&e/platform creator &7- Show plugin credits"));

        if (sender.hasPermission("platform.kit")) {
            sender.sendMessage(colorize("&e/platform kit [player] &7- Give the cosmetic kit"));
        }

        if (sender.hasPermission("platform.setspawn")) {
            sender.sendMessage(colorize("&e/platform setspawn &7- Set the spawn point"));
        }

        if (sender.hasPermission("platform.setvoid")) {
            sender.sendMessage(colorize("&e/platform setvoid [y] &7- Set void Y level"));
        }

        if (sender.hasPermission("platform.scoreboard.toggle")) {
            sender.sendMessage(colorize("&e/platform sb &7- Toggle scoreboard visibility"));
        }

        if (sender.hasPermission("platform.reload")) {
            sender.sendMessage(colorize("&e/platform reload &7- Reload configuration"));
            sender.sendMessage(colorize("&e/platform sb reload &7- Reload scoreboard.yml"));
        }

        sender.sendMessage(colorize("&8&m----------------------------------"));
    }

    // ============================================================
    //  Tab Complete
    // ============================================================
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<String>();

        if (args.length == 1) {
            List<String> subs = new ArrayList<String>();
            subs.add("creator");
            subs.add("help");
            if (sender.hasPermission("platform.kit")) subs.add("kit");
            if (sender.hasPermission("platform.setspawn")) subs.add("setspawn");
            if (sender.hasPermission("platform.setvoid")) subs.add("setvoid");
            if (sender.hasPermission("platform.scoreboard.toggle")) subs.add("sb");
            if (sender.hasPermission("platform.reload")) subs.add("reload");

            String partial = args[0].toLowerCase();
            for (String s : subs) {
                if (s.startsWith(partial)) out.add(s);
            }
            return out;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("kit")) {
            String partial = args[1].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(partial)) {
                    out.add(p.getName());
                }
            }
            return out;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("sb") || args[0].equalsIgnoreCase("scoreboard"))) {
            if (sender.hasPermission("platform.reload")) {
                out.add("reload");
            }
            return out;
        }

        return out;
    }

    // ============================================================
    //  Helpers
    // ============================================================
    private void sendNoPerm(CommandSender sender) {
        sender.sendMessage(colorize("&cYou do not have permission to do this."));
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
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
    private final UnstableConnection connection;
    private final PlayerJoin playerJoin;
    private final ScoreboardManager scoreboardManager;

    public PlatformCommand(JavaPlugin plugin, UnstableConnection connection,
                           PlayerJoin playerJoin, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.connection = connection;
        this.playerJoin = playerJoin;
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("setspawn")) {
            if (!sender.hasPermission("platform.setspawn")) {
                sendNoPerm(sender);
                return true;
            }
            return handleSetSpawn(sender);
        }

        if (sub.equals("kit")) {
            if (!sender.hasPermission("platform.kit")) {
                sendNoPerm(sender);
                return true;
            }
            return handleKit(sender, args);
        }

        if (sub.equals("creator")) {
            return handleCreator(sender);
        }

        if (sub.equals("cc") || sub.equals("connectioncheck")) {
            if (!sender.hasPermission("platform.connection")) {
                sendNoPerm(sender);
                return true;
            }
            return handleConnection(sender, args);
        }

        if (sub.equals("sb") || sub.equals("scoreboard")) {
            if (!sender.hasPermission("platform.scoreboard.toggle")) {
                sendNoPerm(sender);
                return true;
            }
            return handleScoreboard(sender, args);
        }

        if (sub.equals("reload")) {
            if (!sender.hasPermission("platform.reload")) {
                sendNoPerm(sender);
                return true;
            }
            this.plugin.reloadConfig();
            this.connection.reloadConfig();
            if (this.scoreboardManager != null) {
                this.scoreboardManager.reloadConfig();
            }
            if (this.plugin instanceof Platform) {
                Platform pl = (Platform) this.plugin;
                if (pl.getComboSystem() != null) {
                    pl.getComboSystem().reloadConfig();
                }
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
    //  SETSPAWN
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
    //  KIT
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
    //  CREATOR
    // ============================================================
    private boolean handleCreator(CommandSender sender) {
        sender.sendMessage(colorize("&8&m----------------------------------"));
        sender.sendMessage(colorize("&6&lPlatform &7- &fCreated by &bMuvixo"));
        sender.sendMessage(colorize("&7Version: &f1.0"));
        sender.sendMessage(colorize("&8&m----------------------------------"));
        return true;
    }

    // ============================================================
    //  SCOREBOARD
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
    //  CONNECTION CHECK
    // ============================================================
    private boolean handleConnection(CommandSender sender, String[] args) {
        if (this.connection == null) {
            sender.sendMessage(colorize("&cError: Connection listener not found."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(colorize("&8&m----------------------------------"));
            sender.sendMessage(colorize("&6&lConnection Check Status"));
            sender.sendMessage(colorize("&7Enabled: " + (this.connection.isEnabled() ? "&aYES" : "&cNO")));
            sender.sendMessage(colorize("&7Ping Threshold: &e" + this.connection.getPingThreshold() + "ms"));
            sender.sendMessage(colorize("&7Usage: &e/platform cc on|off|toggle"));
            sender.sendMessage(colorize("&7Usage: &e/platform cc bypass <player>"));
            sender.sendMessage(colorize("&7Usage: &e/platform cc unbypass <player>"));
            sender.sendMessage(colorize("&7Usage: &e/platform cc forceaddping <player> <ping>"));
            sender.sendMessage(colorize("&7Usage: &e/platform cc ping <player> default"));
            sender.sendMessage(colorize("&8&m----------------------------------"));
            return true;
        }

        String arg = args[1].toLowerCase();

        if (arg.equals("on")) {
            this.connection.setEnabled(true);
            sender.sendMessage(colorize("&aConnection check &lENABLED&a."));
            return true;
        }

        if (arg.equals("off")) {
            this.connection.setEnabled(false);
            sender.sendMessage(colorize("&cConnection check &lDISABLED&c."));
            return true;
        }

        if (arg.equals("toggle")) {
            boolean state = !this.connection.isEnabled();
            this.connection.setEnabled(state);
            sender.sendMessage(colorize(state
                    ? "&aConnection check &lENABLED&a."
                    : "&cConnection check &lDISABLED&c."));
            return true;
        }

        if (arg.equals("bypass")) {
            if (args.length < 3) {
                sender.sendMessage(colorize("&cUsage: /platform cc bypass <player>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(colorize("&cPlayer not found: &e" + args[2]));
                return true;
            }
            this.connection.addBypass(target.getUniqueId());
            sender.sendMessage(colorize("&a" + target.getName() + " is now bypassing connection check."));
            return true;
        }

        if (arg.equals("unbypass")) {
            if (args.length < 3) {
                sender.sendMessage(colorize("&cUsage: /platform cc unbypass <player>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(colorize("&cPlayer not found: &e" + args[2]));
                return true;
            }
            this.connection.removeBypass(target.getUniqueId());
            sender.sendMessage(colorize("&a" + target.getName() + " is no longer bypassing."));
            return true;
        }

        if (arg.equals("forceaddping")) {
            if (args.length < 4) {
                sender.sendMessage(colorize("&cUsage: /platform cc forceaddping <player> <ping>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(colorize("&cPlayer not found: &e" + args[2]));
                return true;
            }
            int pingAmount;
            try {
                pingAmount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(colorize("&cInvalid ping amount: &e" + args[3]));
                return true;
            }
            if (pingAmount < 0) {
                sender.sendMessage(colorize("&cPing amount cannot be negative."));
                return true;
            }
            int realPing = getRealPing(target);
            if (pingAmount <= realPing) {
                sender.sendMessage(colorize("&cThe forced ping must be higher than the player's current ping."));
                sender.sendMessage(colorize("&7" + target.getName() + "'s current ping: &e" + realPing + "ms"));
                return true;
            }
            if (pingAmount < this.connection.getPingThreshold()) {
                sender.sendMessage(colorize("&cThe forced ping must be at least the threshold (&e"
                        + this.connection.getPingThreshold() + "ms&c)."));
                return true;
            }
            this.connection.setForcedPing(target.getUniqueId(), pingAmount);
            sender.sendMessage(colorize("&aForced ping for &e" + target.getName() + " &aset to &e" + pingAmount + "ms&a."));
            sender.sendMessage(colorize("&7Real ping: &e" + realPing + "ms &7| Threshold: &e"
                    + this.connection.getPingThreshold() + "ms"));
            return true;
        }

        if (arg.equals("ping")) {
            if (args.length < 4) {
                sender.sendMessage(colorize("&cUsage: /platform cc ping <player> default"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(colorize("&cPlayer not found: &e" + args[2]));
                return true;
            }
            String mode = args[3].toLowerCase();
            if (!mode.equals("default")) {
                sender.sendMessage(colorize("&cUsage: /platform cc ping <player> default"));
                return true;
            }
            if (!this.connection.hasForcedPing(target.getUniqueId())) {
                sender.sendMessage(colorize("&e" + target.getName() + " &7does not have a forced ping."));
                return true;
            }
            this.connection.clearForcedPing(target.getUniqueId());
            this.connection.removeBypass(target.getUniqueId());
            sender.sendMessage(colorize("&aForced ping removed for &e" + target.getName() + "&a. Using real ping now."));
            return true;
        }

        sender.sendMessage(colorize("&cUnknown argument. Use /platform cc"));
        return true;
    }

    // ============================================================
    //  REAL PING
    // ============================================================
    private int getRealPing(Player player) {
        try {
            Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return ((Integer) craftPlayer.getClass().getField("ping").get(craftPlayer)).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    // ============================================================
    //  HELP
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

        if (sender.hasPermission("platform.scoreboard.toggle")) {
            sender.sendMessage(colorize("&e/platform sb &7- Toggle scoreboard visibility"));
        }

        if (sender.hasPermission("platform.connection")) {
            sender.sendMessage(colorize("&e/platform cc &7- Connection check status"));
            sender.sendMessage(colorize("&e/platform cc on|off|toggle &7- Toggle connection check"));
            sender.sendMessage(colorize("&e/platform cc bypass <player> &7- Bypass a player"));
            sender.sendMessage(colorize("&e/platform cc unbypass <player> &7- Remove bypass"));
            sender.sendMessage(colorize("&e/platform cc forceaddping <player> <ping> &7- Force ping"));
            sender.sendMessage(colorize("&e/platform cc ping <player> default &7- Remove forced ping"));
        }

        if (sender.hasPermission("platform.reload")) {
            sender.sendMessage(colorize("&e/platform reload &7- Reload configuration"));
            sender.sendMessage(colorize("&e/platform sb reload &7- Reload scoreboard.yml"));
        }

        sender.sendMessage(colorize("&8&m----------------------------------"));
    }

    // ============================================================
    //  TAB COMPLETE
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
            if (sender.hasPermission("platform.connection")) subs.add("cc");
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

        if (args.length == 2 && args[0].equalsIgnoreCase("cc")) {
            String partial = args[1].toLowerCase();
            String[] subs = {"on", "off", "toggle", "bypass", "unbypass", "forceaddping", "ping"};
            for (String s : subs) {
                if (s.startsWith(partial)) out.add(s);
            }
            return out;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("cc")) {
            String action = args[1].toLowerCase();
            if (action.equals("bypass") || action.equals("unbypass")
                    || action.equals("forceaddping") || action.equals("ping")) {
                String partial = args[2].toLowerCase();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(partial)) {
                        out.add(p.getName());
                    }
                }
            }
            return out;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("cc") && args[1].equalsIgnoreCase("ping")) {
            out.add("default");
            return out;
        }

        return out;
    }

    // ============================================================
    //  HELPERS
    // ============================================================
    private void sendNoPerm(CommandSender sender) {
        sender.sendMessage(colorize("&cYou do not have permission to do this."));
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
package com.antispam;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommands implements CommandExecutor {

    private final AntiSpamPlugin plugin;
    private final SpamManager spamManager;

    public AdminCommands(AntiSpamPlugin plugin, SpamManager spamManager) {
        this.plugin = plugin;
        this.spamManager = spamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("asunmute")) {
            if (args.length == 0) { sender.sendMessage(colorize("&cUsage: /asunmute <player>")); return true; }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) { sender.sendMessage(colorize("&cPlayer not found or offline.")); return true; }
            if (!spamManager.isMuted(target)) { sender.sendMessage(colorize("&e" + target.getName() + " is not muted.")); return true; }
            spamManager.unmutePlayer(target);
            sender.sendMessage(colorize("&aUnmuted &f" + target.getName() + "&a."));
            target.sendMessage(colorize("&aYou were manually unmuted by &f" + sender.getName() + "&a."));
            plugin.getLogger().info("[AntiSpam] " + target.getName() + " unmuted by " + sender.getName());
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                plugin.getDiscordLogger().sendUnmute(target.getName(), sender.getName()));
            return true;
        }

        if (command.getName().equalsIgnoreCase("asmute")) {
            if (args.length < 2) { sender.sendMessage(colorize("&cUsage: /asmute <player> <minutes>")); return true; }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) { sender.sendMessage(colorize("&cPlayer not found or offline.")); return true; }
            int mins;
            try { mins = Integer.parseInt(args[1]); }
            catch (NumberFormatException e) { sender.sendMessage(colorize("&cInvalid number.")); return true; }
            spamManager.mutePlayer(target, mins, "Manual mute by " + sender.getName());
            sender.sendMessage(colorize("&aMuted &f" + target.getName() + " &afor &f" + mins + " &aminute(s)."));
            return true;
        }

        if (command.getName().equalsIgnoreCase("spamcheck")) {
            if (args.length == 0) { sender.sendMessage(colorize("&cUsage: /spamcheck <player>")); return true; }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) { sender.sendMessage(colorize("&cPlayer not found or offline.")); return true; }
            sender.sendMessage(colorize("&7=== &cAntiSpam: &f" + target.getName() + " &7==="));
            sender.sendMessage(colorize("&7Currently muted: &f" + spamManager.isMuted(target)));
            if (spamManager.isMuted(target))
                sender.sendMessage(colorize("&7Time remaining: &f" + spamManager.getRemainingTime(target)));
            return true;
        }

        if (command.getName().equalsIgnoreCase("asreset")) {
            if (args.length == 0) { sender.sendMessage(colorize("&cUsage: /asreset <player>")); return true; }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) { sender.sendMessage(colorize("&cPlayer not found or offline.")); return true; }
            spamManager.resetPlayer(target);
            sender.sendMessage(colorize("&aReset &f" + target.getName() + "&a's spam data."));
            plugin.getLogger().info("[AntiSpam] " + target.getName() + "'s data reset by " + sender.getName());
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                plugin.getDiscordLogger().sendReset(target.getName(), sender.getName()));
            return true;
        }

        return false;
    }

    private String colorize(String msg) {
        return msg.replace("&", "\u00a7");
    }
}

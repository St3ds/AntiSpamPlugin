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

        if (command.getName().equalsIgnoreCase("unmute")) {
            if (args.length == 0) {
                sender.sendMessage(colorize("&cUsage: /unmute <player>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(colorize("&cPlayer not found or offline."));
                return true;
            }

            if (!spamManager.isMuted(target)) {
                sender.sendMessage(colorize("&e" + target.getName() + " is not muted."));
                return true;
            }

            spamManager.unmutePlayer(target);
            sender.sendMessage(colorize("&aUnmuted &f" + target.getName() + "&a."));
            target.sendMessage(colorize("&aYou were manually unmuted by &f" + sender.getName() + "&a."));
            plugin.getLogger().info("[AntiSpam] " + target.getName() + " manually unmuted by " + sender.getName());
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                DiscordLogger logger = new DiscordLogger(plugin);
                logger.sendUnmute(target.getName(), sender.getName());
            });
            return true;
        }

        if (command.getName().equalsIgnoreCase("spamcheck")) {
            if (args.length == 0) {
                sender.sendMessage(colorize("&cUsage: /spamcheck <player>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(colorize("&cPlayer not found or offline."));
                return true;
            }

            sender.sendMessage(colorize("&7=== &cAntiSpam: &f" + target.getName() + " &7==="));
            sender.sendMessage(colorize("&7Total offences: &f" + spamManager.getOffences(target)));
            sender.sendMessage(colorize("&7Currently muted: &f" + spamManager.isMuted(target)));
            return true;
        }

        return false;
    }

    private String colorize(String msg) {
        return msg.replace("&", "\u00a7");
    }
}

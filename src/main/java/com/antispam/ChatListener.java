package com.antispam;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final AntiSpamPlugin plugin;
    private final SpamManager spamManager;

    public ChatListener(AntiSpamPlugin plugin, SpamManager spamManager) {
        this.plugin = plugin;
        this.spamManager = spamManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("antispam.bypass")) {
            return;
        }

        if (spamManager.isMuted(player)) {
            event.setCancelled(true);
            String remaining = spamManager.getRemainingTime(player);
            player.sendMessage(colorize("&c&lYou are muted! &7Time remaining: &c" + remaining));
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            spamManager.incrementSpam(player);
        });
    }

    private String colorize(String msg) {
        return msg.replace("&", "\u00a7");
    }
}

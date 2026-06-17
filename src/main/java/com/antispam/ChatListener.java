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

        // Bypass for staff
        if (player.hasPermission("antispam.bypass")) {
            return;
        }

        // Block muted players
        if (spamManager.isMuted(player)) {
            event.setCancelled(true);
            String msg = plugin.getConfig().getString("messages.still-muted", "&c&lYou are muted!");
            player.sendMessage(msg.replace("&", "\u00a7"));
            return;
        }

        // Check spam
        event.setCancelled(false);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            spamManager.incrementSpam(player);
        });
    }
}

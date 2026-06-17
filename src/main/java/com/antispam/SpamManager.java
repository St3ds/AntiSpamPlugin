package com.antispam;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpamManager {

    private final AntiSpamPlugin plugin;
    private final DiscordLogger discordLogger;

    private final Map<UUID, Integer> spamCount = new HashMap<>();
    private final Map<UUID, Integer> offences = new HashMap<>();
    private final Map<UUID, Boolean> muted = new HashMap<>();

    public SpamManager(AntiSpamPlugin plugin, DiscordLogger discordLogger) {
        this.plugin = plugin;
        this.discordLogger = discordLogger;
    }

    public boolean isMuted(Player player) {
        return muted.getOrDefault(player.getUniqueId(), false);
    }

    public void incrementSpam(Player player) {
        UUID uuid = player.getUniqueId();
        int count = spamCount.getOrDefault(uuid, 0) + 1;
        spamCount.put(uuid, count);

        int spamWindow = plugin.getConfig().getInt("spam-window", 2);

        // Reset counter after spam window
        if (count == 1) {
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                spamCount.remove(uuid);
            }, spamWindow * 20L);
        }

        int spamLimit = plugin.getConfig().getInt("spam-limit", 3);

        if (count >= spamLimit) {
            spamCount.remove(uuid);

            // First offence — warn only
            if (!offences.containsKey(uuid)) {
                offences.put(uuid, 0);
                String warnMsg = plugin.getConfig().getString("messages.warning", "&e&l[AntiSpam] &eStop spamming!");
                player.sendMessage(colorize(warnMsg));

                String broadcastMsg = plugin.getConfig().getString("messages.broadcast-warn", "&8[&eAntiSpam&8] &e{player} &7was warned.")
                        .replace("{player}", player.getName());
                plugin.getServer().broadcastMessage(colorize(broadcastMsg));

                plugin.getLogger().info("[AntiSpam] WARNING — " + player.getName() + " spammed " + spamLimit + " messages in " + spamWindow + " seconds.");
                discordLogger.sendWarn(player.getName());
                return;
            }

            // Escalating mute
            int offenceCount = offences.getOrDefault(uuid, 0) + 1;
            offences.put(uuid, offenceCount);

            int mins;
            if (offenceCount == 1) mins = 1;
            else if (offenceCount == 2) mins = 3;
            else if (offenceCount == 3) mins = 5;
            else mins = 10;

            mutePlayer(player, mins, offenceCount);
        }
    }

    public void mutePlayer(Player player, int mins, int offenceCount) {
        UUID uuid = player.getUniqueId();
        muted.put(uuid, true);

        String muteMsg = plugin.getConfig().getString("messages.muted", "&c&lMuted for &f{duration} min(s)!")
                .replace("{duration}", String.valueOf(mins))
                .replace("{offence}", String.valueOf(offenceCount));
        player.sendMessage(colorize(muteMsg));

        String broadcastMsg = plugin.getConfig().getString("messages.broadcast-mute", "&8[&cAntiSpam&8] &c{player} &7muted for &f{duration} min(s).")
                .replace("{player}", player.getName())
                .replace("{duration}", String.valueOf(mins))
                .replace("{offence}", String.valueOf(offenceCount));
        plugin.getServer().broadcastMessage(colorize(broadcastMsg));

        plugin.getLogger().info("[AntiSpam] MUTED — " + player.getName() + " | Duration: " + mins + " min(s) | Offence #" + offenceCount);
        discordLogger.sendMute(player.getName(), mins, offenceCount);

        // Unmute after duration
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (muted.getOrDefault(uuid, false)) {
                muted.remove(uuid);
                Player online = plugin.getServer().getPlayer(uuid);
                if (online != null) {
                    String unmutedMsg = plugin.getConfig().getString("messages.unmuted", "&aYou have been unmuted!");
                    online.sendMessage(colorize(unmutedMsg));
                }
            }
        }, mins * 60 * 20L);
    }

    public void unmutePlayer(Player player) {
        muted.remove(player.getUniqueId());
    }

    public int getOffences(Player player) {
        return offences.getOrDefault(player.getUniqueId(), 0);
    }

    public boolean hasMuteEntry(Player player) {
        return muted.containsKey(player.getUniqueId());
    }

    private String colorize(String msg) {
        return msg.replace("&", "\u00a7");
    }
}

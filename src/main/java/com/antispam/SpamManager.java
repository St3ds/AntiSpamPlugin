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

        if (count == 1) {
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                spamCount.remove(uuid);
            }, spamWindow * 20L);
        }

        int spamLimit = plugin.getConfig().getInt("spam-limit", 3);

        if (count >= spamLimit) {
            spamCount.remove(uuid);

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

            int offenceCount = offences.getOrDefault(uuid, 0) + 1;
            offences.put(uuid, offenceCount);

            int mins = getMuteDuration(offenceCount);
            mutePlayer(player, mins, offenceCount);
        }
    }

    public int getMuteDuration(int offenceCount) {
        if (offenceCount == 1) return 1;
        else if (offenceCount == 2) return 3;
        else if (offenceCount == 3) return 5;
        else if (offenceCount == 4) return 10;
        else if (offenceCount == 5) return 20;
        else return 30;
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

    public void manualMute(Player player, int mins, String admin) {
        UUID uuid = player.getUniqueId();
        muted.put(uuid, true);

        player.sendMessage(colorize("&c&lYou have been manually muted for &f" + mins + " minute(s) &cby &f" + admin + "&c!"));
        plugin.getServer().broadcastMessage(colorize("&8[&cAntiSpam&8] &c" + player.getName() + " &7was manually muted for &f" + mins + " min(s) &7by &f" + admin));
        plugin.getLogger().info("[AntiSpam] " + player.getName() + " manually muted for " + mins + " min(s) by " + admin);
        discordLogger.sendManualMute(player.getName(), mins, admin);

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (muted.getOrDefault(uuid, false)) {
                muted.remove(uuid);
                Player online = plugin.getServer().getPlayer(uuid);
                if (online != null) {
                    online.sendMessage(colorize("&aYou have been unmuted!"));
                }
            }
        }, mins * 60 * 20L);
    }

    public void unmutePlayer(Player player) {
        muted.remove(player.getUniqueId());
    }

    public void resetOffences(Player player) {
        offences.remove(player.getUniqueId());
        spamCount.remove(player.getUniqueId());
    }

    public int getOffences(Player player) {
        return offences.getOrDefault(player.getUniqueId(), 0);
    }

    public boolean isMutedRaw(UUID uuid) {
        return muted.getOrDefault(uuid, false);
    }

    private String colorize(String msg) {
        return msg.replace("&", "\u00a7");
    }
}

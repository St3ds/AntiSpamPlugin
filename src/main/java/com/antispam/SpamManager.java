package com.antispam;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpamManager {

    private final AntiSpamPlugin plugin;
    private final DiscordLogger discordLogger;

    private final Map<UUID, Integer> spamCount = new HashMap<>();
    private final Map<UUID, Boolean> warned = new HashMap<>();
    private final Map<UUID, Boolean> muted = new HashMap<>();
    private final Map<UUID, Long> muteEndTime = new HashMap<>();

    public SpamManager(AntiSpamPlugin plugin, DiscordLogger discordLogger) {
        this.plugin = plugin;
        this.discordLogger = discordLogger;
    }

    public boolean isMuted(Player player) {
        return muted.getOrDefault(player.getUniqueId(), false);
    }

    public String getRemainingTime(Player player) {
        UUID uuid = player.getUniqueId();
        if (!muteEndTime.containsKey(uuid)) return "unknown";
        long remaining = muteEndTime.get(uuid) - System.currentTimeMillis();
        if (remaining <= 0) return "0 seconds";
        long mins = remaining / 60000;
        long secs = (remaining % 60000) / 1000;
        if (mins > 0) return mins + " minute(s) and " + secs + " second(s)";
        return secs + " second(s)";
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

            // Not yet warned — warn first
            if (!warned.getOrDefault(uuid, false)) {
                warned.put(uuid, true);

                String warnMsg = plugin.getConfig().getString("messages.warning",
                        "&e&l[AntiSpam] &eStop spamming! Next time you will be muted for 3 minutes.");
                player.sendMessage(colorize(warnMsg));

                String broadcastMsg = plugin.getConfig().getString("messages.broadcast-warn",
                        "&8[&eAntiSpam&8] &e{player} &7was warned for spamming.")
                        .replace("{player}", player.getName());
                plugin.getServer().broadcastMessage(colorize(broadcastMsg));

                plugin.getLogger().info("[AntiSpam] WARNING — " + player.getName());
                discordLogger.sendWarn(player.getName());

                // Reset warn after 30 seconds so they get warned again next session
                plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                    warned.remove(uuid);
                }, 30 * 20L);
                return;
            }

            // Already warned — mute for 3 minutes
            mutePlayer(player, 3, "Spamming");
        }
    }

    public void mutePlayer(Player player, int mins, String reason) {
        UUID uuid = player.getUniqueId();
        muted.put(uuid, true);
        muteEndTime.put(uuid, System.currentTimeMillis() + (mins * 60 * 1000L));
        warned.remove(uuid);

        String muteMsg = colorize("&c&lYou have been muted for &f" + mins + " minute(s)&c! Reason: &f" + reason);
        player.sendMessage(muteMsg);

        String broadcastMsg = colorize("&8[&cAntiSpam&8] &c" + player.getName() + " &7was muted for &f" + mins + " min(s)&7. Reason: &f" + reason);
        plugin.getServer().broadcastMessage(broadcastMsg);

        plugin.getLogger().info("[AntiSpam] MUTED — " + player.getName() + " | " + mins + " min(s) | Reason: " + reason);
        discordLogger.sendMute(player.getName(), mins, reason);

        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (muted.getOrDefault(uuid, false)) {
                muted.remove(uuid);
                muteEndTime.remove(uuid);
                Player online = plugin.getServer().getPlayer(uuid);
                if (online != null) {
                    online.sendMessage(colorize("&aYou have been unmuted!"));
                }
            }
        }, mins * 60 * 20L);
    }

    public void unmutePlayer(Player player) {
        muted.remove(player.getUniqueId());
        muteEndTime.remove(player.getUniqueId());
    }

    public void resetPlayer(Player player) {
        warned.remove(player.getUniqueId());
        spamCount.remove(player.getUniqueId());
        muted.remove(player.getUniqueId());
        muteEndTime.remove(player.getUniqueId());
    }

    private String colorize(String msg) {
        return msg.replace("&", "\u00a7");
    }
}

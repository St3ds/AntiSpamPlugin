package com.antispam;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordLogger {

    private final AntiSpamPlugin plugin;

    public DiscordLogger(AntiSpamPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendWarn(String playerName) {
        send("{\"type\":\"warn\",\"player\":\"" + playerName + "\"}");
    }

    public void sendMute(String playerName, int mins, String reason) {
        send("{\"type\":\"mute\",\"player\":\"" + playerName + "\",\"duration\":\"" + mins + "\",\"reason\":\"" + reason + "\"}");
    }

    public void sendUnmute(String playerName, String admin) {
        send("{\"type\":\"unmute\",\"player\":\"" + playerName + "\",\"admin\":\"" + admin + "\"}");
    }

    public void sendReset(String playerName, String admin) {
        send("{\"type\":\"reset\",\"player\":\"" + playerName + "\",\"admin\":\"" + admin + "\"}");
    }

    private void send(String body) {
        String botUrl = plugin.getConfig().getString("bot-url", "");
        String authKey = plugin.getConfig().getString("auth-key", "");
        if (botUrl.isEmpty()) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(botUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("x-auth-key", authKey);
                con.setDoOutput(true);
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }
                con.getResponseCode();
                con.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("[AntiSpam] Discord log failed: " + e.getMessage());
            }
        });
    }
}

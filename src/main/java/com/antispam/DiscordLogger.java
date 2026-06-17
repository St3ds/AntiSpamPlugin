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
        String body = "{\"type\":\"warn\",\"player\":\"" + playerName + "\",\"reason\":\"3 messages in 2 seconds\"}";
        sendRequest(body);
    }

    public void sendMute(String playerName, int mins, int offence) {
        String body = "{\"type\":\"mute\",\"player\":\"" + playerName + "\",\"reason\":\"3 messages in 2 seconds\",\"duration\":\"" + mins + "\",\"offence\":\"" + offence + "\"}";
        sendRequest(body);
    }

    public void sendManualMute(String playerName, int mins, String admin) {
        String body = "{\"type\":\"manualmute\",\"player\":\"" + playerName + "\",\"duration\":\"" + mins + "\",\"admin\":\"" + admin + "\"}";
        sendRequest(body);
    }

    public void sendUnmute(String playerName, String admin) {
        String body = "{\"type\":\"unmute\",\"player\":\"" + playerName + "\",\"reason\":\"" + admin + "\"}";
        sendRequest(body);
    }

    public void sendReset(String playerName, String admin) {
        String body = "{\"type\":\"reset\",\"player\":\"" + playerName + "\",\"admin\":\"" + admin + "\"}";
        sendRequest(body);
    }

    private void sendRequest(String body) {
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
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = con.getResponseCode();
                if (responseCode != 200) {
                    plugin.getLogger().warning("[AntiSpam] Discord log failed! Response: " + responseCode);
                }

                con.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("[AntiSpam] Could not send Discord log: " + e.getMessage());
            }
        });
    }
}

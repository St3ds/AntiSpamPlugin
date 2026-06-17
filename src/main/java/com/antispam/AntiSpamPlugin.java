package com.antispam;

import org.bukkit.plugin.java.JavaPlugin;

public class AntiSpamPlugin extends JavaPlugin {

    private SpamManager spamManager;
    private DiscordLogger discordLogger;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        discordLogger = new DiscordLogger(this);
        spamManager = new SpamManager(this, discordLogger);

        getServer().getPluginManager().registerEvents(new ChatListener(this, spamManager), this);

        AdminCommands adminCmds = new AdminCommands(this, spamManager);
        getCommand("unmute").setExecutor(adminCmds);
        getCommand("mute").setExecutor(adminCmds);
        getCommand("spamcheck").setExecutor(adminCmds);
        getCommand("resetwarns").setExecutor(adminCmds);

        getLogger().info("AntiSpamPlugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AntiSpamPlugin disabled!");
    }

    public DiscordLogger getDiscordLogger() {
        return discordLogger;
    }

    public SpamManager getSpamManager() {
        return spamManager;
    }
}

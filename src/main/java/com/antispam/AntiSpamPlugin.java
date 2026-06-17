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

        getCommand("unmute").setExecutor(new AdminCommands(this, spamManager));
        getCommand("spamcheck").setExecutor(new AdminCommands(this, spamManager));

        getLogger().info("AntiSpamPlugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AntiSpamPlugin disabled!");
    }
}

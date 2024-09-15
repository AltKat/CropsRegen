package io.github.altkat.cropsRegen;

import io.github.altkat.cropsRegen.Farm.Scheduler;
import io.github.altkat.cropsRegen.PAPI.CountdownPlaceholder;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class CropsRegen extends JavaPlugin {
    FileConfiguration config = getConfig();
    public CountdownPlaceholder countdownPlaceholder;
    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, 23370);
        loadConfig();
        if(getServer().getPluginManager().getPlugin("WorldGuard") != null){
            getServer().getConsoleSender().sendMessage("§a[CropsRegen] WorldGuard found! enabling CropsRegen.");
        }else {
            getServer().getConsoleSender().sendMessage("§a[CropsRegen] §cWorldGuard not found! disabling CropsRegen.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        long countdownTime = (config.getInt("Farm.refreshInterval", 60) * 1000L);
        countdownPlaceholder = new CountdownPlaceholder(countdownTime);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getServer().getConsoleSender().sendMessage("§a[CropsRegen] PlaceholderAPI found! Enabling Placeholder support.");
            countdownPlaceholder.register();
        } else {
            getServer().getConsoleSender().sendMessage("§a[CropsRegen] §cPlaceholderAPI not found! Placeholder support will not work.");
        }

        Scheduler sc = new Scheduler(this);
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        getServer().getConsoleSender().sendMessage("§a[CropsRegen] is active!");

    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("§a[CropsRegen] §cdisabling CropsRegen...");
        if (countdownPlaceholder != null) {
            countdownPlaceholder.unregister();
        }
    }


    public void loadConfig(){
        reloadConfig();
        saveDefaultConfig();
    }
}

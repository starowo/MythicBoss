package icu.xiyoumc.mmboss.util;

import icu.xiyoumc.mmboss.MMBoss;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public class DataManager {

    public YamlConfiguration data;

    public BukkitTask saveTask;

    public DataManager() {
        File dataFile = new File(MMBoss.getInstance().getDataFolder(), "Data.yml");
        if (dataFile.exists()) {
            data = YamlConfiguration.loadConfiguration(dataFile);
        } else {
            data = new YamlConfiguration();
        }
        saveTask = Bukkit.getScheduler().runTaskTimer(MMBoss.getInstance(), () -> {
            MMBoss.getInstance().killRewardManager.save(data);
            MMBoss.getInstance().bossManager.save(data);
            save();
        }, 0, 20 * 60);
    }

    public void save() {
        File dataFile = new File(MMBoss.getInstance().getDataFolder(), "Data.yml");
        try {
            data.save(dataFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

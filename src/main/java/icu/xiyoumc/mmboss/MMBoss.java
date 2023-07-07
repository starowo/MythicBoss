package icu.xiyoumc.mmboss;

import icu.xiyoumc.mmboss.boss.BossManager;
import icu.xiyoumc.mmboss.command.MMBossCommand;
import icu.xiyoumc.mmboss.killreward.KillRewardManager;
import icu.xiyoumc.mmboss.listener.EntityCombatListener;
import icu.xiyoumc.mmboss.util.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MMBoss extends JavaPlugin {

    public static MMBoss Instance;
    public BossManager bossManager;
    public KillRewardManager killRewardManager;
    public DataManager dataManager;

    @Override
    public void onEnable() {
        Instance = this;
        saveDefaultConfig();
        bossManager = new BossManager();
        killRewardManager = new KillRewardManager();
        dataManager = new DataManager();
        bossManager.loadBossesAndDataFromFiles();
        killRewardManager.loadRewardsAndDataFromFiles();
        getCommand("mmboss").setExecutor(new MMBossCommand());
        Bukkit.getPluginManager().registerEvents(new EntityCombatListener(), this);
        getLogger().info("MMBoss插件已加载");
    }

    public void reload() {
        reloadConfig();
        dataManager.saveTask.cancel();
        dataManager = new DataManager();
        bossManager.loadBossesAndDataFromFiles();
        killRewardManager.loadRewardsAndDataFromFiles();
    }

    public static MMBoss getInstance() {
        return Instance;
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    @Override
    public void onDisable() {
        dataManager.save();
    }
}

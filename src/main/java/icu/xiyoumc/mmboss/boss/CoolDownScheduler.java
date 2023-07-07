package icu.xiyoumc.mmboss.boss;

import icu.xiyoumc.mmboss.MMBoss;
import icu.xiyoumc.mmboss.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class CoolDownScheduler implements ISpawnScheduler {

    private String coolDownStr;
    private int coolDown;
    private BukkitTask task;

    public CoolDownScheduler(String coolDown) {
        // coolDown: like 1d2h3M4s
        // convert string to second
        this.coolDown = TimeUtils.str2seconds(coolDown);
        this.coolDownStr = coolDown;
        start();
    }

    @Override
    public void reset() {
        task.cancel();
        coolDown = TimeUtils.str2seconds(coolDownStr);
    }

    @Override
    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(MMBoss.getInstance(), () -> {
            if (coolDown > 0) {
                coolDown--;
            }
        }, 0, 20);
    }

    @Override
    public boolean isRunning() {
        return !task.isCancelled();
    }

    @Override
    public boolean shouldSpawn() {
        return coolDown == 0;
    }

}

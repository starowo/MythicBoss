package icu.xiyoumc.mmboss.boss;

public interface ISpawnScheduler {

    void reset();

    void start();

    boolean isRunning();

    boolean shouldSpawn();

}

package icu.xiyoumc.mmboss.boss;

import icu.xiyoumc.mmboss.util.TimeUtils;

import java.util.Calendar;

public class TimeScheduler implements ISpawnScheduler {

    private Calendar scheduledTime;
    private int interval; // in seconds

    public TimeScheduler(String baseTime, String interval) {
        // baseTime: like 2020-01-01 12:00:00
        // interval: like 1d2h3M4s
        this.scheduledTime = TimeUtils.str2local(baseTime);
        this.interval = TimeUtils.str2seconds(interval);
        // push scheduledTime to next time
        while (scheduledTime.before(Calendar.getInstance())) {
            scheduledTime.add(Calendar.SECOND, this.interval);
        }
    }

    @Override
    public void reset() {
        while (scheduledTime.before(Calendar.getInstance())) {
            scheduledTime.add(Calendar.SECOND, this.interval);
        }
    }

    @Override
    public void start() {
        // do nothing
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public boolean shouldSpawn() {
        return scheduledTime.before(Calendar.getInstance());
    }
}

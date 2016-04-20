package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.time;

import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.prop.PropertyHolder;

/**
 * @author kalmarr
 */
public class TimeHelper implements ITimeHelper {

    private static volatile ITimeHelper instance = null;

    protected static final long TIME_INTERVAL = PropertyHolder.getOneRoundTime();

    private long starttime = 0L;

    private int currentUnit = -1;

    private TimeHelper(){
    }

    public static ITimeHelper getInstance() {
        if(instance == null){
            synchronized (TimeHelper.class) {
                if (instance == null) {
                    instance = new TimeHelper();
                }
            }
        }
        return instance;
    }

    @Override
    public boolean isInTime() throws Exception {
        return System.currentTimeMillis() < starttime + TIME_INTERVAL;
    }

    @Override
    public void printTime() {
        System.out.println("startTime: " + starttime + ", currentTime: " + System.currentTimeMillis() + ", maxTime: " + (starttime + TIME_INTERVAL));
    }

    @Override
    public void setStartTime(int unit) {
        if (currentUnit != unit) {
            currentUnit = unit;
            starttime = System.currentTimeMillis();
        }
    }
}

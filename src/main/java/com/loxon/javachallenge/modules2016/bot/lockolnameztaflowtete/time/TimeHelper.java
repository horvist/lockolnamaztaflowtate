package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.time;

/**
 * @author kalmarr
 */
public class TimeHelper implements ITimeHelper {

    private static ITimeHelper instance = null;

    protected static final long TIME_INTERVAL = 850L;

    private long starttime = 0L;

    private TimeHelper(){
    }

    public static ITimeHelper getInstance() {
        if(instance == null) {
            instance = new TimeHelper();
        }
        return instance;
    }

    @Override
    public boolean isInTime() throws Exception {
        return System.currentTimeMillis() < starttime + TIME_INTERVAL;
    }

    @Override
    public void setStartTime() {
        starttime = System.currentTimeMillis();
    }
}

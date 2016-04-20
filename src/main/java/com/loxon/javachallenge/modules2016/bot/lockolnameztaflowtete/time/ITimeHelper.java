package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.time;

public interface ITimeHelper {
	public boolean isInTime() throws Exception;

    void setStartTime(int unit);

    public void printTime();
}

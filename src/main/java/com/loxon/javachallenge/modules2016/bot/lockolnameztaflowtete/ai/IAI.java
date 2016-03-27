package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.ai;

import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.Field;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;

public interface IAI {

    public Field getNextStepForUnit(int unit, IMapCache map);

    public void lastMovementWasExecutedSuccessfully(int unit);
}

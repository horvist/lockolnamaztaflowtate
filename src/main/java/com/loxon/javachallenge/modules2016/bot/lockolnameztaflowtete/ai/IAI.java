package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.ai;

import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.IActionCostProvider;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.Field;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;

public interface IAI {

    public void lastMovementWasExecutedSuccessfully(int unit);

    Field getNextStepForUnit(int unit, IMapCache map, int round, IActionCostProvider actionCostProvider);

    void clearBotPath(int unit);
}

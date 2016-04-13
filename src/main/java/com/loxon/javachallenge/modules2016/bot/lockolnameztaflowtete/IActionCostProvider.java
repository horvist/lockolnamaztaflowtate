package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete;

public interface IActionCostProvider {

    public int getCostDrill();
    public int getCostMove();
    public int getCostRadar();
    public int getCostExplode();
    public int getCostWatch();
    public int getNumberOfActionPoints();
    public int getNumberOfExplosives();

}

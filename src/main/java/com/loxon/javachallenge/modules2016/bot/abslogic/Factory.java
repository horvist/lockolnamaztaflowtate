package com.loxon.javachallenge.modules2016.bot.abslogic;

import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.ai.AI_3;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.ai.IAI;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.MapCache;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.time.ITimeHelper;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.time.TimeHelper;
import com.loxon.javachallenge.modules2016.gui.controller.GuiController;
import com.loxon.javachallenge.modules2016.gui.controller.IGuiController;

/**
 * @author kalmarr
 */
public final class Factory {

    private Factory(){}

    public static ITimeHelper createTimeHelper(){
        return TimeHelper.getInstance();
    }

    public static IMapCache createMap(){
        return MapCache.getInstance();
    }

    public static IGuiController createGuiController(){
        return GuiController.getInstance();
    }

    public static IAI createAI() {
        return AI_3.getInstance();
    }
}

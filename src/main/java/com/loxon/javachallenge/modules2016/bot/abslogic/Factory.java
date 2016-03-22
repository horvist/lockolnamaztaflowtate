package com.loxon.javachallenge.modules2016.bot.abslogic;

import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.MapCache;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.time.ITimeHelper;

/**
 * @author kalmarr
 */
public final class Factory {

    private Factory(){}

    public static ITimeHelper createTimeHelper(){
        return null;
    }

    public static IMapCache getMap(){
        return MapCache.getInstance();
    }
}

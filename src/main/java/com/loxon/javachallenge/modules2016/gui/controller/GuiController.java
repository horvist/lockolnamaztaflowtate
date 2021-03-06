package com.loxon.javachallenge.modules2016.gui.controller;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsScore;
import com.loxon.javachallenge.modules2016.bot.abslogic.Factory;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;
import com.loxon.javachallenge.modules2016.gui.view.MapWindow;
import com.loxon.javachallenge.modules2016.gui.view.ScoreWindow;

/**
 * @author kalmarr
 */
public final class GuiController implements IGuiController {

    private static volatile IGuiController instance = null;

    private MapWindow mapWindow = null;

    private ScoreWindow scoreWindow = null;

    private final IMapCache mapCache = Factory.createMap();

    public static IGuiController getInstance() {
        if (instance == null) {
            synchronized (GuiController.class) {
                if (instance == null) {
                    instance = new GuiController();
                }
            }
        }
        return instance;
    }

    @Override
    public void updateElements(WsCoordinate... coordinates) {
        for (WsCoordinate coord : coordinates) {
            mapWindow.modifyField(coord, mapCache.getField(coord));
        }
    }

    @Override
    public void initAndStartGui(WsCoordinate size) {
        mapWindow = new MapWindow(size.getX(), size.getY());
        mapWindow.showWindow();

        scoreWindow = new ScoreWindow();
        scoreWindow.showWindow();
    }

    @Override
    public void refreshScore(WsScore score, int actionPointLeft, int explLeft, int roundsLeft) {
        scoreWindow.refreshScore(score, actionPointLeft, explLeft, roundsLeft);

    }
}

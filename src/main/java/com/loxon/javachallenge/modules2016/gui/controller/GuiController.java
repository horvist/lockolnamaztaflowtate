package com.loxon.javachallenge.modules2016.gui.controller;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsScore;
import com.loxon.javachallenge.modules2016.gui.view.MapWindow;
import com.loxon.javachallenge.modules2016.gui.view.ScoreWindow;

import java.util.Collection;

/**
 *
 * @author kalmarr
 */
public final class GuiController implements IGuiController {

    private static IGuiController instance = null;

    private MapWindow mapWindow = null;

    private ScoreWindow scoreWindow = null;

    private static IGuiController getInstance(){
        if(getInstance() == null){
            instance = new GuiController();
        }
        return instance;
    }

    @Override
    public void updateElements(Collection<WsCoordinate> coordinates) {

    }

    @Override
    public void initAndStartGui(WsCoordinate size) {
        mapWindow = new MapWindow(size.getX(), size.getY());
        mapWindow.showWindow();

        scoreWindow = new ScoreWindow();
        scoreWindow.showWindow();
    }

    @Override
    public void refreshScore(WsScore score) {
        scoreWindow.refreshScore(score);
    }
}

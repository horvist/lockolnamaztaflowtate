package com.loxon.javachallenge.modules2016.gui.controller;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsScore;
import com.loxon.javachallenge.modules2016.bot.abslogic.Factory;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;
import com.loxon.javachallenge.modules2016.gui.view.MapWindow;
import com.loxon.javachallenge.modules2016.gui.view.ScoreWindow;

/**
 *
 * @author kalmarr
 */
public final class GuiController implements IGuiController {

    private static IGuiController instance = null;

    private MapWindow mapWindow = null;

    private ScoreWindow scoreWindow = null;

    private IMapCache mapCache = Factory.createMap();

    public static IGuiController getInstance(){
        if(getInstance() == null){
            instance = new GuiController();
        }
        return instance;
    }

    @Override
    public void updateElements(WsCoordinate... coordinates) {
    	new Thread() {
    		public void run() {
    			for(WsCoordinate coord : coordinates){
    				mapWindow.modifyField(coord, mapCache.getField(coord));
    			}    	   
    		}
    	}.start();
    }

    @Override
    public void initAndStartGui(WsCoordinate size) {
    	new Thread() {
    		public void run() {
    			mapWindow = new MapWindow(size.getX(), size.getY());
    			mapWindow.showWindow();

    			scoreWindow = new ScoreWindow();
    			scoreWindow.showWindow();
    		}
    	}.start();
    }

    @Override
    public void refreshScore(WsScore score) {
    	new Thread() {
    	    public void run() {
    	        scoreWindow.refreshScore(score);
    	    }
    	}.start();
    }
}

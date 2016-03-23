package com.loxon.javachallenge.modules2016.gui.controller;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsScore;

/**
 * @author kalmarr
 */
public interface IGuiController {

    public void initAndStartGui(WsCoordinate size);

    public void updateElements(WsCoordinate... coordinates);

    public void refreshScore(WsScore score, int actionPointLeft, int explLeft);

}

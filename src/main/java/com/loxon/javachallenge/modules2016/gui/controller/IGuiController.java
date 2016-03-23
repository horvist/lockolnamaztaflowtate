package com.loxon.javachallenge.modules2016.gui.controller;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsScore;

import java.util.Collection;

/**
 * @author kalmarr
 */
public interface IGuiController {

    public void initAndStartGui(WsCoordinate size);

    public void updateElements(Collection<WsCoordinate> coordinates);

    public void refreshScore(WsScore score);

}

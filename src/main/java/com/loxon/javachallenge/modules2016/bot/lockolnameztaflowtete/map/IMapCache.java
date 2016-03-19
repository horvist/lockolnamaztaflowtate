package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map;

import com.loxon.javachallenge.modules2015.bot.core.Bot;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;

public interface IMapCache {
	
	public void initMap(int xSize, int ySize);
	
	public void placeShuttle(WsCoordinate coord);
	
	public void moveUnit(Bot bot, WsCoordinate coord);
	
	public boolean lockField(WsCoordinate coord);	//sync
	
	public boolean unLockField(WsCoordinate coord);
	
	public void structureField(WsCoordinate coord);

}

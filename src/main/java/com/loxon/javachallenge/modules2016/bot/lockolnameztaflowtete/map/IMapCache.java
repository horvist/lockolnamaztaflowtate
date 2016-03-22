package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map;

import java.util.Collection;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.Scouting;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsDirection;
import com.loxon.javachallenge.modules2016.bot.abslogic.AbstractLogicBot;

/**
 * IMapCache interface. (Such comment, much wow)
 * 
 * @author ihorvath
 *
 */
public interface IMapCache {

	// coord x = size x, coord y = size y
	public void initMap(WsCoordinate coord);
	
	public void placeShuttle(WsCoordinate coord);

	public void markShuttleExit(WsCoordinate coord);
	
	public void moveUnit(AbstractLogicBot bot, WsCoordinate coord) throws Exception;
	
	public void structureField(WsCoordinate coord);

	public WsDirection getDirection(WsCoordinate actual, WsCoordinate target) throws Exception;

	// this will handle watch and radar result
	public void handleScouts(Collection<Scouting> scoutings);

	public WsCoordinate getUnitPosition(final int unit);
	
	public boolean isInStartPos(final int unit);
}

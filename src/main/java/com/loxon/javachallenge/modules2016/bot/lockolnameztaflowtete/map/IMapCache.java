package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map;

import com.loxon.javachallenge.modules2015.bot.core.Bot;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.Scouting;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsDirection;

import java.util.Collection;

public interface IMapCache {

	// coord x = size x, coord y = size y
	public void initMap(WsCoordinate coord);
	
	public void placeShuttle(WsCoordinate coord);

	public void markShuttleExit(WsCoordinate coord);
	
	public void moveUnit(Bot bot, WsCoordinate coord);
	
	public void structureField(WsCoordinate coord);

	public WsDirection getDirection(WsCoordinate actual, WsCoordinate target);

	// this will handle watch and radar result
	public void handleScouts(Collection<Scouting> scoutings);

	public WsCoordinate getUnitPosition(final int i);
}

package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map;

import java.util.Collection;

import com.loxon.javachallenge.modules2015.bot.core.Bot;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectType;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.Scouting;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsDirection;

/**
 * Utility class for caching map fields and providing
 * extra functionality for map navigation.
 * 
 * @author ihorvath
 *
 */
public class MapCache implements IMapCache {
	
	private Field[][] map;

	@Override
	public void initMap(WsCoordinate coord) {
		this.map = new Field[coord.getX()][coord.getY()];
	}

	@Override
	public void placeShuttle(WsCoordinate coord) {
		getMappedFieldForCoords(coord)
			.setObjectType(ObjectType.SHUTTLE)
			.setTeam(FieldTeam.ALLY);
	}

	@Override
	public void markShuttleExit(WsCoordinate coord) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveUnit(Bot bot, WsCoordinate coord) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void structureField(WsCoordinate coord) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WsDirection getDirection(WsCoordinate actual, WsCoordinate target) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleScouts(Collection<Scouting> scoutings) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WsCoordinate getUnitPosition(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInStartPos(int unit) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private Field getMappedFieldForCoords(WsCoordinate coord) {
		// TODO csináljuk meg, ha egyértelmû hogy a térkép méretei mit is jelölnek.
		return null;
	}

}

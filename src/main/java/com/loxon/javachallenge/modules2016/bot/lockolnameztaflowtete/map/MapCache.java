package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map;

import java.util.Collection;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectType;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.Scouting;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsDirection;
import com.loxon.javachallenge.modules2016.bot.abslogic.AbstractLogicBot;
import com.loxon.javachallenge.modules2016.bot.enums.FieldTeam;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.time.ITimeHelper;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.time.TimeHelper;

/**
 * Utility class for caching map fields and providing
 * extra functionality for map navigation.
 * 
 * @author ihorvath
 *
 */
public class MapCache implements IMapCache {
	
	private static final int NUM_OF_UNITS = 4;
	
	private Field[][] map;
	
	private WsCoordinate[] unitCoords = new WsCoordinate[NUM_OF_UNITS];
	private WsCoordinate shuttleCoord;
	private WsCoordinate originalMapSize;
	private WsCoordinate shuttleExit;
	
    private static IMapCache instance = null;

	
    private MapCache(){
    }

    public static IMapCache getInstance() {
        if(instance == null) {
            instance = new MapCache();
        }
        return instance;
    }

	@Override
	public void initMap(WsCoordinate coord) {
		originalMapSize = coord;
		this.map = new Field[coord.getX() + 1][coord.getY() + 1];
	}

	@Override
	public void placeShuttle(WsCoordinate coord) {
		getMappedFieldForCoords(coord)
			.setObjectType(ObjectType.SHUTTLE)
			.setTeam(FieldTeam.ALLY);
		shuttleCoord = coord;
		for (int i = 0; i < NUM_OF_UNITS; i++) {
			// setting all units to starting point - the shuttles coordinates
			unitCoords[i] = coord;
		}
	}

	@Override
	public void markShuttleExit(WsCoordinate coord) {
		shuttleExit = coord;
	}

	@Override
	public void moveUnit(AbstractLogicBot bot, WsCoordinate coord) throws Exception {
		final int unit = bot.getUnitNumber();
		final WsCoordinate currentCoords = unitCoords[unit];
		final Field currentField = getMappedFieldForCoords(coord);
		if (currentField.getObjectType() != ObjectType.SHUTTLE) {
			// shuttle should not be set to other type
			
			getMappedFieldForCoords(currentCoords)
				.setObjectType(ObjectType.TUNNEL)
				.setTeam(FieldTeam.NO_MANS_LAND);	// may throw array out of bounds exception
		}
		
		getMappedFieldForCoords(coord)
		.setObjectType(ObjectType.BUILDER_UNIT)
		.setTeam(FieldTeam.ALLY);	// may throw array out of bounds exception
		
		unitCoords[unit] = coord;
		
		// TODO elég ennyi?
	}

	@Override
	public void structureField(WsCoordinate coord) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WsDirection getDirection(WsCoordinate actual, WsCoordinate target) throws Exception {
		final Field mappedTargetField = getMappedFieldForCoords(target);
		if (mappedTargetField.getTeam() != FieldTeam.NO_MANS_LAND) {
			throw new IllegalStateException(getCoordErrorMessage("The target field is not empty, somebody is standing on it!", target, null));
		}
		if (coordEquals(actual, shuttleCoord) && !coordEquals(target, shuttleExit)) {
			throw new IllegalStateException(getCoordErrorMessage("Unit is in starting point, target can only be the shuttle exit point!", target, actual));
		}
		if (isInMap(target)) {
			if (actual.getX() != target.getX()
					&& actual.getY() != target.getY()) {
				throw new IllegalStateException(getCoordErrorMessage("Unit can only go up/down/left/right", target, actual));
			} else {
				int xDiff = actual.getX() - target.getX();
				int yDiff = actual.getY() - target.getY();
				
				switch (xDiff) {
					case 1:
						return WsDirection.LEFT;
					case -1: 
						return WsDirection.RIGHT;
				}
				
				switch (yDiff) {
					case 1:
						return WsDirection.DOWN;
					case -1:
						return WsDirection.UP;
				}
				
				throw new IllegalStateException(getCoordErrorMessage("Unit can only move one square at a time ", target, actual));
			}
		} else {
			throw new IllegalStateException(getCoordErrorMessage("Target position is not in bounds of map ", target, actual));
		}
	}
	
	private String getCoordErrorMessage(String customMsg, WsCoordinate target, WsCoordinate actual) {
		String ret = customMsg;
		
		if (actual != null) {
			ret += ", actual coords: " + actual.getX() + ":" + actual.getY();
		}
		if (target != null) {
			ret += ", target coords: " + target.getX() + ":" + target.getY();
		}
		
		return ret;
	}

	@Override
	public void handleScouts(Collection<Scouting> scoutings) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WsCoordinate getUnitPosition(int i) {
		return unitCoords[i];
	}

	@Override
	public boolean isInStartPos(int unit) {
		return coordEquals(unitCoords[unit], shuttleCoord);
	}
	
	private boolean isInMap(WsCoordinate target) {
		return target.getX() > originalMapSize.getX()
				|| target.getX() < 0
				|| target.getY() > originalMapSize.getY()
				|| target.getY() < 0;
	}
	
	private Field getMappedFieldForCoords(WsCoordinate coord) {
		Field field = map[coord.getX()][coord.getY()];
		if (field == null) {
			field = new Field();
		}
		
		return field;
	}
	
	private boolean coordEquals(WsCoordinate coord1, WsCoordinate coord2) {
		return coord1 == coord2 || (coord1.getX() == coord2.getX() && coord1.getY() == coord2.getY());
	}

}

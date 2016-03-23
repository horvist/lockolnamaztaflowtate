package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectType;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.Scouting;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsDirection;
import com.loxon.javachallenge.modules2016.bot.abslogic.AbstractLogicBot;
import com.loxon.javachallenge.modules2016.bot.abslogic.Factory;
import com.loxon.javachallenge.modules2016.bot.enums.FieldTeam;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.InvalidDirectionException;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.InvalidMoveCommandException;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.StructureFieldException;

/**
 * Utility class for caching map fields and providing
 * extra functionality for map navigation.
 * 
 * @author ihorvath
 *
 */
public class MapCache implements IMapCache {
	
	private static final String TEAM_NAME = "lockolnameztaflowtete";
	
	private Field[][] map;
	
	private WsCoordinate[] unitCoords = new WsCoordinate[NUM_OF_UNITS];
	private WsCoordinate shuttleCoord;
	private WsCoordinate originalMapSize;
	private WsCoordinate shuttleExit;
	
    private static volatile IMapCache instance = null;
    
    private Map<LocalCoords, Field> uncommittedChanges = new HashMap<LocalCoords, Field>();

    private class LocalCoords {
    	final int x;
    	final int y;
    	final WsCoordinate wsCoord;
    	
    	LocalCoords(int x, int y, WsCoordinate wsCord) {
    		this.x = x;
    		this.y = y;
    		this.wsCoord = wsCord;
    	}
    	
		public int getX() {
			return x;
		}

		public WsCoordinate getWsCoord() {
			return wsCoord;
		}

		public int getY() {
			return y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LocalCoords other = (LocalCoords) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}
    }
	
    private MapCache(){
    }

    public static IMapCache getInstance() {
		if(instance == null){
			synchronized (MapCache.class) {
				if (instance == null) {
					instance = new MapCache();
				}
			}
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
		
		if (AbstractLogicBot.isTestMode()) {
			Factory.createGuiController().updateElements(coord);
		}
	}

	@Override
	public void markShuttleExit(WsCoordinate coord) {
		shuttleExit = coord;
		getMappedFieldForCoords(coord)
			.setObjectType(ObjectType.ROCK)
			.setTeam(FieldTeam.NO_MANS_LAND);
	}

	@Override
	public void moveUnit(int unit, WsCoordinate targetCoords) throws InvalidMoveCommandException {
		try {
			final WsCoordinate currentCoords = unitCoords[unit];
			final Field currentField = getMappedFieldForCoords(currentCoords);
			final Field targetField = getMappedFieldForCoords(targetCoords);
			
			addFieldToUncommittedChanges(currentCoords, currentField);
			addFieldToUncommittedChanges(targetCoords, targetField);
			
			if (currentField.getObjectType() != ObjectType.SHUTTLE) {
				// shuttle should not be set to other type
				
				currentField
					.setObjectType(ObjectType.TUNNEL)
					.setTeam(FieldTeam.ALLY);	// may throw array out of bounds exception
			}
			
			targetField
				.setObjectType(ObjectType.BUILDER_UNIT)
				.setTeam(FieldTeam.ALLY);	// may throw array out of bounds exception
			
			unitCoords[unit] = targetCoords;
		} catch (Exception e) {
			throw new InvalidMoveCommandException(e);
		}
	}

	@Override
	public void structureField(WsCoordinate coord) throws StructureFieldException {
		final Field mappedField = getMappedFieldForCoords(coord);
		
		addFieldToUncommittedChanges(coord, mappedField);
		
		if (mappedField.getObjectType() == ObjectType.GRANITE) {
			mappedField.setObjectType(ObjectType.ROCK);
		} else if (mappedField.getObjectType() == ObjectType.ROCK) {
			mappedField
				.setObjectType(ObjectType.TUNNEL)
				.setTeam(FieldTeam.ALLY);
		} else {
			throw new StructureFieldException(getCoordErrorMessage("Can not structure field: " + mappedField.getObjectType().name(), coord, null));
		}
	}

	@Override
	public WsDirection getDirection(WsCoordinate actual, WsCoordinate target) throws InvalidDirectionException {
		final Field mappedTargetField = getMappedFieldForCoords(target);
		if (mappedTargetField.getTeam() != FieldTeam.NO_MANS_LAND && mappedTargetField.getObjectType() != ObjectType.TUNNEL) {
			throw new InvalidDirectionException(getCoordErrorMessage("The target field is not empty, somebody is standing on it!", target, null));
		}
		if (coordEquals(actual, shuttleCoord) && !coordEquals(target, shuttleExit)) {
			throw new InvalidDirectionException(getCoordErrorMessage("Unit is in starting point, target can only be the shuttle exit point!", target, actual));
		}
		if (isInMap(target)) {
			if (actual.getX() != target.getX()
					&& actual.getY() != target.getY()) {
				throw new InvalidDirectionException(getCoordErrorMessage("Unit can only go up/down/left/right", target, actual));
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
				
				throw new InvalidDirectionException(getCoordErrorMessage("Unit can only move one square at a time ", target, actual));
			}
		} else {
			throw new InvalidDirectionException(getCoordErrorMessage("Target position is not in bounds of map ", target, actual));
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
		for (Scouting scouting : scoutings) {
			final Field field = getMappedFieldForCoords(scouting.getCord());
			field.setObjectType(scouting.getObject());
			if (field.getObjectType() == ObjectType.BUILDER_UNIT
					|| field.getObjectType() == ObjectType.SHUTTLE
					|| field.getObjectType() == ObjectType.TUNNEL) {
				field.setTeam(TEAM_NAME.equals(scouting.getTeam()) ? FieldTeam.ALLY : FieldTeam.ENEMY);
			} else {
				field.setTeam(FieldTeam.NO_MANS_LAND);
			}
		}
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
		return getMappedFieldForCoords(coord.getX(), coord.getY());
	}
	
	private Field getMappedFieldForCoords(int x, int y) {
		Field field = map[x][y];
		if (field == null) {
			field = new Field();
			map[x][y] = field;
		}
		
		return field;
	}
	
	private boolean coordEquals(WsCoordinate coord1, WsCoordinate coord2) {
		return coord1 == coord2 || (coord1.getX() == coord2.getX() && coord1.getY() == coord2.getY());
	}

	@Override
	public void revertChanges() {
		for (Entry<LocalCoords, Field> entry : uncommittedChanges.entrySet()) {
			if (AbstractLogicBot.isTestMode()) {
				Factory.createGuiController().updateElements(entry.getKey().getWsCoord());
			}
			resetFieldFromUncommittedChanges(entry.getKey(), entry.getValue());
		}
	}
	
	private void addFieldToUncommittedChanges(WsCoordinate coord, Field field) {
		uncommittedChanges.put(new LocalCoords(coord.getX(), coord.getY(), coord), field.clone());
	}
	
	private void resetFieldFromUncommittedChanges(LocalCoords coord, Field field) {
		map[coord.getX()][coord.getY()] = field;
	}

	@Override
	public void commitChanges() {
		if (AbstractLogicBot.isTestMode()) {
			for (LocalCoords localCoord : uncommittedChanges.keySet()) {
				Factory.createGuiController().updateElements(localCoord.getWsCoord());
			}
		}
		uncommittedChanges.clear();
	}

	public WsCoordinate getShuttleCoord() {
		return shuttleCoord;
	}

	public WsCoordinate getShuttleExit() {
		return shuttleExit;
	}
	
	public String toString() {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < originalMapSize.getX(); i++) {
			for (int j = 0; j < originalMapSize.getY(); j++) {
				ret.append(getMappedFieldForCoords(i, j).toString()).append("|");
			}
		}
		
		return ret.toString();
	}

	@Override
	public Field getField(WsCoordinate coord) {
		return getMappedFieldForCoords(coord);
	}
}

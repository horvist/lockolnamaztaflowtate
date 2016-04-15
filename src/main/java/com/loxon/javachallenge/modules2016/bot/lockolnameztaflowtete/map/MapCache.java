package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
 */
public class MapCache implements IMapCache {

    private static final String TEAM_NAME = "lockolnameztaflowtete";

    private Field[][] map;

    private final WsCoordinate[] unitCoords = new WsCoordinate[NUM_OF_UNITS];
    private WsCoordinate shuttleCoord;
    private WsCoordinate originalMapSize;
    private WsCoordinate shuttleExit;

    private static volatile IMapCache instance = null;

    private final Map<LocalCoords, Field> uncommittedChanges = new HashMap<LocalCoords, Field>();

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

    private MapCache() {
    }

    public static IMapCache getInstance() {
        if (instance == null) {
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
        if (AbstractLogicBot.isTestMode()) {
            Factory.createGuiController().updateElements(coord);
        }
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
                        .setTeam(FieldTeam.ALLY);    // may throw array out of bounds exception
            }

            targetField
                    .setObjectType(ObjectType.BUILDER_UNIT)
                    .setTeam(FieldTeam.ALLY);    // may throw array out of bounds exception

            unitCoords[unit] = targetCoords;
        } catch (Exception e) {
            throw new InvalidMoveCommandException(e);
        }
    }


    @Override
    public void checkFieldStructurable(WsCoordinate coord) throws StructureFieldException {
        final Field mappedField = getMappedFieldForCoords(coord);

        if (!(mappedField.getObjectType() == ObjectType.GRANITE
                || mappedField.getObjectType() == ObjectType.ROCK
                || (mappedField.getObjectType() == ObjectType.TUNNEL && mappedField.getTeam() == FieldTeam.ENEMY))) {
            throw new StructureFieldException(getCoordErrorMessage("Can not structure field: " + mappedField.getObjectType().name(), coord, null));
        }
    }

    @Override
    public void structureField(WsCoordinate coord) throws StructureFieldException {
        checkFieldStructurable(coord);
        final Field mappedField = getMappedFieldForCoords(coord);

        addFieldToUncommittedChanges(coord, mappedField);

        if (mappedField.getObjectType() == ObjectType.GRANITE) {
            mappedField.setObjectType(ObjectType.ROCK);
        } else if (mappedField.getObjectType() == ObjectType.ROCK) {
            mappedField
                    .setObjectType(ObjectType.TUNNEL)
                    .setTeam(FieldTeam.ALLY);
        } else if (mappedField.getObjectType() == ObjectType.TUNNEL && mappedField.getTeam() == FieldTeam.ENEMY) {
            mappedField
                    .setObjectType(ObjectType.ROCK)
                    .setTeam(FieldTeam.NO_MANS_LAND);
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
            if ((field.getObjectType() == ObjectType.BUILDER_UNIT || field.getObjectType() == ObjectType.SHUTTLE) && field.getTeam() == FieldTeam.ALLY) {
                continue;    // do not overwrite our team's unit with tunnel field
            }
            field.setObjectType(scouting.getObject());
            if (field.getObjectType() == ObjectType.BUILDER_UNIT
                    || field.getObjectType() == ObjectType.SHUTTLE
                    || field.getObjectType() == ObjectType.TUNNEL) {
                field.setTeam(TEAM_NAME.equals(scouting.getTeam().getValue()) ? FieldTeam.ALLY : FieldTeam.ENEMY);
            } else {
                field.setTeam(FieldTeam.NO_MANS_LAND);
            }

            if (AbstractLogicBot.isTestMode()) {
                Factory.createGuiController().updateElements(scouting.getCord());
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
        return isInMap(target.getX(), target.getY());
    }

    private boolean isInMap(int x, int y) {
        return !(x > originalMapSize.getX()
                || x < 0
                || y > originalMapSize.getY()
                || y < 0);
    }

    private Field getMappedFieldForCoords(WsCoordinate coord) {
        return getMappedFieldForCoords(coord.getX(), coord.getY());
    }

    private Field getMappedFieldForCoords(int x, int y) {
        Field field = map[x][y];
        if (field == null) {
            field = new Field(x, y);
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
            resetFieldFromUncommittedChanges(entry.getKey(), entry.getValue());
            if (AbstractLogicBot.isTestMode()) {
                Factory.createGuiController().updateElements(entry.getKey().getWsCoord());
            }
        }
        uncommittedChanges.clear();
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

    @Override
    public WsCoordinate getShuttleCoord() {
        return shuttleCoord;
    }

    @Override
    public WsCoordinate getShuttleExit() {
        return shuttleExit;
    }

    @Override
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

    @Override
    public Field getFieldForDirection(Field field, WsDirection direction) {
        int x = field.getX();
        int y = field.getY();

        if (direction == WsDirection.DOWN) {
            y--;
        } else if (direction == WsDirection.UP) {
            y++;
        } else if (direction == WsDirection.LEFT) {
            x--;
        } else if (direction == WsDirection.RIGHT) {
            x++;
        }

        if (isInMap(x, y)) {
            return getMappedFieldForCoords(x, y);
        }

        return null;
    }

    @Override
    public Field getUnitField(int unit) {
        return getMappedFieldForCoords(unitCoords[unit]);
    }

    @Override
    public Field getShuttleExitField() {
        return getMappedFieldForCoords(shuttleExit);
    }

    @Override
    public int getNumOfOurFieldsNextToField(Field field) {
        int ret = 0;
        for (WsDirection dir : WsDirection.values()) {
            Field fieldForDir = getFieldForDirection(field, dir);
            if (fieldForDir != null && field.getTeam() == FieldTeam.ALLY) {
                ret++;
            }
        }

        return ret;
    }

    @Override
    public Collection<WsCoordinate> getRadarableCoordinatesForUnit(int unit) {
        WsCoordinate unitCoord = getUnitPosition(unit);
        final int x = unitCoord.getX();
        final int y = unitCoord.getY();
        Set<WsCoordinate> radarableCoords = new HashSet<WsCoordinate>();

        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }

                try {
                    final Field field = getMappedFieldForCoords(x + i, y + j);
                    if (field != null) {
                        radarableCoords.add(field.getWsCoord());
                    }
                } catch (Exception e) {
                    // do nothing, probably index out of bounds or something, not too sophisticated exception handling :(
                }

            }
        }

        return radarableCoords;
    }

}

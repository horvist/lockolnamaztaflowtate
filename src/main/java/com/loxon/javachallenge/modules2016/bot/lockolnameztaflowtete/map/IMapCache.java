package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map;

import java.util.Collection;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.Scouting;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsDirection;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.InvalidDirectionException;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.InvalidMoveCommandException;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.StructureFieldException;

/**
 * IMapCache interface. (Such comment, much wow)
 *
 * @author ihorvath
 *
 */
public interface IMapCache {

	public static final int NUM_OF_UNITS = 4;

	// coord x = size x, coord y = size y
	public void initMap(WsCoordinate coord);

	public void placeShuttle(WsCoordinate coord);

	public void markShuttleExit(WsCoordinate coord);

	public void moveUnit(int unit, WsCoordinate coord) throws InvalidMoveCommandException;

	public void structureField(WsCoordinate coord) throws StructureFieldException;

	public void checkFieldStructurable(WsCoordinate coord) throws StructureFieldException;

	public WsDirection getDirection(WsCoordinate actual, WsCoordinate target) throws InvalidDirectionException;

	// this will handle watch and radar result
	public void handleScouts(Collection<Scouting> scoutings);

	public WsCoordinate getUnitPosition(final int unit);

    public Field getUnitField(final int unit);

	public boolean isInStartPos(final int unit);

	public void revertChanges();

	public void commitChanges();

	public WsCoordinate getShuttleCoord();

	public WsCoordinate getShuttleExit();

    public Field getShuttleExitField();

	public Field getField(WsCoordinate coord);

	public Field getFieldForDirection(Field field, WsDirection direction);

	public int getNumOfOurFieldsNextToField(Field field);

	public Collection<WsCoordinate> getRadarableCoordinatesForUnit(int unit);
}

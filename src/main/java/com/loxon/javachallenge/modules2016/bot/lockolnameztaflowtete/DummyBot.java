package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectType;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2016.bot.abslogic.AbstractLogicBot;
import com.loxon.javachallenge.modules2016.bot.abslogic.Factory;
import com.loxon.javachallenge.modules2016.bot.enums.FieldTeam;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.StructureFieldException;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.Field;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;
import com.loxon.javachallenge.modules2016.gui.controller.IGuiController;

public class DummyBot {

    private static IGuiController gui = Factory.createGuiController();
    private static IMapCache map = Factory.createMap();

    public static void main(String[] args) {
        initDummyMap();
    }

    private static WsCoordinate getCoord(int x, int y) {
        final WsCoordinate ret = new WsCoordinate();
        ret.setX(x);
        ret.setY(y);
        return ret;
    }

    private static void initDummyMap() {
        map.initMap(getCoord(4, 4));
        map.placeShuttle(getCoord(2, 1));
        map.markShuttleExit(getCoord(2, 2));
        AbstractLogicBot.setTestMode(true);
        gui.initAndStartGui(getCoord(4, 4));
        map.getField(getCoord(0, 0)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(0, 1)).setObjectType(ObjectType.GRANITE).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(0, 2)).setObjectType(ObjectType.GRANITE).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(0, 3)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(0, 4)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(1, 0)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(1, 1)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(1, 2)).setObjectType(ObjectType.GRANITE).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(1, 3)).setObjectType(ObjectType.GRANITE).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(1, 4)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(2, 0)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
//        map.getField(getCoord(2, 1)).setObjectType(ObjectType.SHUTTLE).setTeam(FieldTeam.ALLY);
        map.getField(getCoord(2, 2)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(2, 3)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(2, 4)).setObjectType(ObjectType.GRANITE).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(3, 0)).setObjectType(ObjectType.GRANITE).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(3, 1)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(3, 2)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(3, 3)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(3, 4)).setObjectType(ObjectType.GRANITE).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(4, 0)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(4, 1)).setObjectType(ObjectType.ROCK).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(4, 2)).setObjectType(ObjectType.GRANITE).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(4, 3)).setObjectType(ObjectType.GRANITE).setTeam(FieldTeam.NO_MANS_LAND);
        map.getField(getCoord(4, 4)).setObjectType(ObjectType.GRANITE).setTeam(FieldTeam.NO_MANS_LAND);

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                gui.updateElements(getCoord(i, j));
            }
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                // dummy movement
                try {
                    doMove(Factory.createAI().getNextStepForUnit(j, map, 75).getWsCoord(), j);
                    doMove(Factory.createAI().getNextStepForUnit(j, map, 75).getWsCoord(), j);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static void doMove(WsCoordinate targetCoord, int unit) throws Exception {
        Field targetField = map.getField(targetCoord);
        if (targetField.getObjectType() == ObjectType.GRANITE) {
            map.structureField(targetCoord);
            map.structureField(targetCoord);
        } else if (targetField.getObjectType() == ObjectType.ROCK) {
            map.structureField(targetCoord);

        }

        map.moveUnit(unit, targetCoord);
        map.commitChanges();

    }
}

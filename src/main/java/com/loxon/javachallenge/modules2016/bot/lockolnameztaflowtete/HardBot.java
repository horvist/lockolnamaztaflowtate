package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectType;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2016.bot.abslogic.AbstractLogicBot;
import com.loxon.javachallenge.modules2016.bot.abslogic.Factory;
import com.loxon.javachallenge.modules2016.bot.enums.Actions;
import com.loxon.javachallenge.modules2016.bot.enums.FieldTeam;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.EndOfTurnException;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.RunOutOfActionPointsException;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.RunOutOfTimeException;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.StructureFieldException;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.UnSuccessfulRequestException;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.Field;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.prop.PropertyHolder;

/**
 * @author kalmarr
 */
public class HardBot extends AbstractLogicBot {

    protected boolean connectedToGame = false;

    protected static final long TIME_INTERVAL = PropertyHolder.getTimeBetweenIsMyTurn();

    public HardBot(String name, String password, String endpointAddress) {
        super(name, password, endpointAddress);
    }

    @Override
    public void finished() {
        super.finished();
        System.out.println("finished");
    }

    @Override
    protected void process() throws InterruptedException {
        if (!connectedToGame) {
            this.login();
            connectedToGame = true;
        }

        while (true) {
            try {
                if (isMyTurn()) {
                    this.timeHelper.setStartTime(unitNumber);
                    if (timeHelper.isInTime()) {
                        this.doSomething();
                    }
                }
            } catch (StructureFieldException fasz) {
//                fasz.printStackTrace();
            } catch (Exception e) {
//                 e.printStackTrace();
            }
            Thread.sleep(TIME_INTERVAL);
        }
    }

    private void doSomething() throws Exception {
        while (true) {
            try {
                doExplore();
                drillRemainingFields();

                final Field targetField = Factory.createAI().getNextStepForUnit(unitNumber, mapCache, turnsLeft, this);
                final WsCoordinate targetCoord = targetField.getWsCoord();

                if (hasUndrilledMovement.get(unitNumber)) {
                    hasUndrilledMovement.put(unitNumber, false);
                    drillNearbyFields(targetCoord);
                }

                doFieldHaHa(targetField, targetCoord);

                Factory.createAI().lastMovementWasExecutedSuccessfully(unitNumber);  // if no exception happened until this point, movement is considered completed

            } catch (RunOutOfTimeException e){
                throw e;
            } catch (RunOutOfActionPointsException e) {
                doUseRemainingActionPoints();
                throw e;
            } catch (UnSuccessfulRequestException e){
                Factory.createAI().clearBotPath(unitNumber);
                throw e;
            }
        }
    }

    private static Map<Integer, Stack<WsCoordinate>> cachedDrills = new HashMap<>();

    static {
        for (int i = 0; i < 4; i++) {
            cachedDrills.put(i, new Stack<WsCoordinate>());
        }
    }

    private void drillRemainingFields() throws Exception {
        Stack<WsCoordinate> coords = cachedDrills.get(unitNumber);
        while (!coords.isEmpty()) {
            WsCoordinate targetCoord = coords.peek();
            if (ObjectType.ROCK.equals(mapCache.getField(targetCoord).getObjectType())) {
                try {
                    System.out.println("körbedrill22222");
                    doAction(Actions.DRILL, targetCoord);
                    coords.pop();
                } catch (RunOutOfActionPointsException | RunOutOfTimeException e) {
                    throw e;
                } catch (Exception e) {
                    coords.pop();
//                    e.printStackTrace();
                    // do nothing if other exception occured, continue work
                }
            } else {
                coords.pop();
            }

        }
    }

    private void drillNearbyFields(final WsCoordinate targetCoords) throws EndOfTurnException {
        if (turnsLeft < 75) {
            Collection<WsCoordinate> nearbyCoords = mapCache.getNearbyFields(unitNumber, ObjectType.ROCK);
            for (WsCoordinate coord : nearbyCoords) {
                if (coord.getX() != targetCoords.getX() && coord.getY() != targetCoords.getY()) {
                    try {
                        System.out.println("körbedrill");
                        doAction(Actions.DRILL, coord);
                    } catch (RunOutOfActionPointsException | RunOutOfTimeException e) {
                        cachedDrills.get(unitNumber).push(targetCoords);
                        throw e;
                    } catch (Exception e) {
//                        e.printStackTrace();
                        // do nothing if other exception occured, continue work
                    }
                }
            }
        }
    }

    private final static Map<Integer, Boolean> hasUndrilledMovement = new HashMap<Integer, Boolean>();

    static {
        for (int i = 0; i < 4; i++) {
            hasUndrilledMovement.put(i, false);
        }
    }

    private void doFieldHaHa(Field targetField, WsCoordinate targetCoord) throws Exception {
        if (targetField.getObjectType() == ObjectType.GRANITE || (targetField.getObjectType() == ObjectType.TUNNEL && targetField.getTeam() == FieldTeam.ENEMY)) {
            System.out.println("exploooooood");
            doAction(Actions.EXPLODE, targetCoord);
        }

        if (targetField.getObjectType() == ObjectType.ROCK) {
            System.out.println("drilllllll");
            doAction(Actions.DRILL, targetCoord);
        }

        if (targetField.getObjectType() == ObjectType.TUNNEL && targetField.getTeam() == FieldTeam.ALLY){
            System.out.println("múúúúúv");
            doAction(Actions.MOVE, targetCoord);
            hasUndrilledMovement.put(unitNumber, true);
        }
    }


    public static void main(String[] args) {
        HardBot hardBot = new HardBot(args[1], args[2], args[0]);
        if (args.length > 3) {
            hardBot.setTestMode(true);
            hardBot.setGuiController(Factory.createGuiController());
        }

    }

}

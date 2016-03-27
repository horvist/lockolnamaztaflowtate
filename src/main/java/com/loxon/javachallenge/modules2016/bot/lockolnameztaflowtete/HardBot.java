package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectType;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2016.bot.abslogic.AbstractLogicBot;
import com.loxon.javachallenge.modules2016.bot.abslogic.Factory;
import com.loxon.javachallenge.modules2016.bot.enums.Actions;
import com.loxon.javachallenge.modules2016.bot.enums.FieldTeam;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.Field;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;

/**
 * @author kalmarr
 */
public class HardBot extends AbstractLogicBot {

    protected boolean connectedToGame = false;
    protected static final long TIME_INTERVAL = 151L;

    private final boolean[] escaped = new boolean[IMapCache.NUM_OF_UNITS];

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

                    this.doSomething();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(TIME_INTERVAL);
        }
    }

    private void doSomething() throws Exception {
        doWatch();

        final Field targetField = Factory.createAI().getNextStepForUnit(unitNumber, mapCache);
        final WsCoordinate targetCoord = targetField.getWsCoord();

        if (targetField.getObjectType() == ObjectType.GRANITE || (targetField.getObjectType() == ObjectType.TUNNEL && targetField.getTeam() == FieldTeam.ENEMY)) {
            doAction(Actions.EXPLODE, targetCoord);
            doAction(Actions.DRILL, targetCoord);
        } else if (targetField.getObjectType() == ObjectType.ROCK) {
            doAction(Actions.DRILL, targetCoord);
        }

        doAction(Actions.MOVE, targetCoord);
        Factory.createAI().lastMovementWasExecutedSuccessfully(unitNumber);  // if no exception happened until this point, movement is considered completed

//        if (mapCache.isInStartPos(getUnitNumber())) {
//            if (ObjectType.ROCK.equals(this.mapCache.getField(this.mapCache.getShuttleExit()).getObjectType())) {
//                doAction(Actions.DRILL, mapCache.getShuttleExit());
//            }
//            if (ObjectType.TUNNEL.equals(this.mapCache.getField(mapCache.getShuttleExit()).getObjectType())
//                    && FieldTeam.ALLY.equals(this.mapCache.getField(mapCache.getShuttleExit()).getTeam())) {
//                doAction(Actions.MOVE, mapCache.getShuttleExit());
//            }
//            escaped[this.getUnitNumber()] = true;
//        } else {
//        }
//        doWatch();
    }

    public static void main(String[] args) {
        HardBot hardBot = new HardBot(args[1], args[2], args[0]);
        if (args.length > 3) {
            hardBot.setTestMode(true);
            hardBot.setGuiController(Factory.createGuiController());
        }

    }

}

package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectType;
import com.loxon.javachallenge.modules2016.bot.abslogic.AbstractLogicBot;
import com.loxon.javachallenge.modules2016.bot.abslogic.Factory;
import com.loxon.javachallenge.modules2016.bot.enums.Actions;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.EndOfTurnException;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;

/**
 * @author kalmarr
 */
public class HardBot extends AbstractLogicBot {

    protected Boolean connectedToGame = false;
    protected static final long TIME_INTERVAL = 145L;

    private boolean[] escaped = new boolean[IMapCache.NUM_OF_UNITS];

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
                    this.timeHelper.setStartTime();

                    this.doSomething();

                } else {
                    Thread.sleep(TIME_INTERVAL);
                }
            } catch (EndOfTurnException e){
                continue;
            } catch (Exception e){
//                Thread.sleep(TIME_INTERVAL);
            }
        }
    }

    private void doSomething() throws Exception {
        while (true) {
            if(!escaped[this.getUnitNumber()]){
                if(ObjectType.ROCK.equals(this.mapCache.getField(this.mapCache.getShuttleExit()).getObjectType())){
                    doAction(Actions.DRILL, mapCache.getShuttleExit());
                }
                doAction(Actions.MOVE, mapCache.getShuttleExit());
                escaped[this.getUnitNumber()] = true;
            }

            doWatch();

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

package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete;

import com.loxon.javachallenge.modules2016.bot.abslogic.AbstractLogicBot;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.RunOutOfTimeException;

/**
 * @author kalmarr
 */
public class HardBot extends AbstractLogicBot {

    protected Boolean connectedToGame = false;
    protected static final long TIME_INTERVAL = 145L;
    private int counter = 0;


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
            } catch (RunOutOfTimeException e){
                continue;
            } catch (Exception e){
                Thread.sleep(TIME_INTERVAL);
            }
        }
    }

    private void doSomething() throws Exception {
        while (true) {

            doWatch();

        }
    }

    public static void main(String[] args) {
        HardBot hardBot = new HardBot(args[1], args[2], args[0]);
        if (args.length > 3) {
            hardBot.setTestMode(true);
        }

    }

}

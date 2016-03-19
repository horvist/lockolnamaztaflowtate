package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
import com.loxon.javachallenge.modules2016.bot.xmlwrite.AbstractLogicBot;

/**
 * @author kalmarr
 */
public class HardBot extends AbstractLogicBot {

    protected Boolean         connectedToGame = false;
    protected static final long TIME_INTERVAL = 1000L;
    private int 					  counter = 0;
    
    private WsCoordinate coords;

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
        if(!connectedToGame) {
            this.login();
            connectedToGame = true;
        }
        this.doSomething();
        Thread.sleep(TIME_INTERVAL);
    }


    private void doSomething() {
        System.out.println("do something "+counter++);
    }

    public static void main(String[] args) {
        HardBot hardBot = new HardBot(args[1], args[2], args[0]);
        if(args.length > 3){
            hardBot.setTestMode(true);
        }

    }

}

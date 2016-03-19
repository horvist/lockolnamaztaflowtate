package com.loxon.javachallenge.modules2015.bot.example;

import com.loxon.javachallenge.modules2015.bot.core.Bot;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.CommonResp;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.StartGameRequest;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.StartGameResponse;

public class ExampleBot extends Bot {

    protected Boolean         connectedToGame = false;
    protected static final long TIME_INTERVAL = 1000L;
    private int 					  counter = 0;
    
    public ExampleBot(String name, String password, String endpointAddress) {
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
    
    /**
     * example login method()
     */
    protected void login() {
        StartGameResponse response = service.startGame(new StartGameRequest());
        CommonResp commonResponse = response.getResult();
        System.out.println(commonResponse.getCode()+" - "+commonResponse.getMessage());
        //... etc.
    }
    
    private void doSomething() {
    	System.out.println("do something "+counter++);
    }
    
    public static void main(String[] args) {
    	new ExampleBot("jcteam1", "jcteam1", "http://localhost:8080/engine/CentralControl?wsdl");    	
    }
    
}

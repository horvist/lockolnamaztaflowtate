package com.loxon.javachallenge.modules2016.bot.xmlwrite;

import com.loxon.javachallenge.modules2015.bot.core.Bot;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.*;
import com.loxon.javachallenge.modules2016.bot.enums.Actions;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.PrintStream;

/**
 * @author kalmarr
 */
public abstract class AbstractLogicBot extends Bot {

    // change this to redirect output if desired
    private static PrintStream out = System.out;

    private static final ObjectFactory FACTORY = new ObjectFactory();

    private static boolean TEST_MODE = false;

    private boolean costsAreAvailable = false;

    private static int actualUnit = -1;

    protected int expLeft = -1;

    public AbstractLogicBot(String name, String password, String endpointAddress) {
        super(name, password, endpointAddress);
}

    @Override
    protected abstract void process() throws InterruptedException;

    protected StartGameResponse startGame(){
        StartGameResponse response = service.startGame(FACTORY.createStartGameRequest());
        logToSystemOut(response, response.getClass());
        return response;
    }

    protected void initActionCosts(){
        if(!costsAreAvailable) {
            ActionCostResponse response = service.getActionCost(FACTORY.createActionCostRequest());
            logToSystemOut(response, response.getClass());
            costsAreAvailable = true;
            this.costDrill = response.getDrill();
            this.costMove = response.getMove();
            this.costRadar = response.getRadar();
            this.costExplode = response.getExplode();
            this.costWatch = response.getWatch();
            this.numberOfActionPoints = response.getAvailableActionPoints();
            this.numberOfExplosives = response.getAvailableExplosives();
        }
    }

    protected int getActionCost(Actions actionType){
        switch (actionType){
            case DRILL:
                return this.costDrill;
            case MOVE:
                return this.costMove;
            case RADAR:
                return this.costRadar;
            case EXPLODE:
                return this.costExplode;
            case WATCH:
                return this.costWatch;
            default:
                return 1000000000;
        }
    }

    /**
     * example login method()
     */
    protected void login() {
        StartGameResponse response = startGame();
//        CommonResp commonResponse = response.getResult();
        initActionCosts();
    }

    protected int getAvailableActionPoints(){
        return this.numberOfActionPoints;
    }

    protected int getAvailableExplosives(){
        return this.numberOfExplosives;
    }

    protected ExplodeCellResponse getExplodeCellRequest(){
        ExplodeCellResponse response = service.explodeCell (FACTORY.createExplodeCellRequest());
        logToSystemOut(response, response.getClass());
        return response;
    }

    protected boolean isMyTurn(){
        IsMyTurnResponse response = service.isMyTurn(FACTORY.createIsMyTurnRequest());
        logToSystemOut(response, response.getClass());
        handleCommonResponse(response.getResult());

        return response.isIsYourTurn();
    }

    public static int getActualUnit() {
        return actualUnit;
    }

    public static void setTestMode(boolean testMode) {
        TEST_MODE = testMode;
    }

    private void handleCommonResponse(final CommonResp commonResp){
        actualUnit = commonResp.getBuilderUnit();
        this.apLeft = commonResp.getActionPointsLeft();
        this.expLeft = commonResp.getExplosivesLeft();

        // TODO amikor a mi korunk van, letárolni az a korhoz az informaciokat
    }

    private void logToSystemOut(Object message, Class clz) {
        if(TEST_MODE) {
            try {
                JAXBContext context = JAXBContext.newInstance(StartGameResponse.class);
                Marshaller m = context.createMarshaller();
                //for pretty-print XML in JAXB
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

                // Write to File
                m.marshal(message, out);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
    }

}

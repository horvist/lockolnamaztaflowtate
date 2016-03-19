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
public abstract class AbstractResponseWriterBot extends Bot {

    // change this to redirect output if desired
    private static PrintStream out = System.out;

    private static final ObjectFactory FACTORY = new ObjectFactory();

    private static boolean TEST_MODE = false;

    private ActionCostResponse actions = null;

    public AbstractResponseWriterBot(String name, String password, String endpointAddress) {
        super(name, password, endpointAddress);
}

    @Override
    protected abstract void process() throws InterruptedException;

    protected StartGameResponse startGame(){
        StartGameResponse response = service.startGame(FACTORY.createStartGameRequest());
        logToSystemOut(response, response.getClass());
        return response;
    }

    protected int getActionCost(Actions actionType){
        if(actions == null) {
            ActionCostResponse response = service.getActionCost(FACTORY.createActionCostRequest());
            logToSystemOut(response, response.getClass());
        }
        switch (actionType){
            case DRILL:
                return actions.getDrill();
            case MOVE:
                return actions.getMove();
            case RADAR:
                return actions.getRadar();
            case EXPLODE:
                return actions.getRadar();
            case WATCH:
                return actions.getWatch();
            case AVAILABLEACTIONPOINTS:
                return actions.getAvailableActionPoints();
            case AVAILABLEEXPLOSIVES:
                return actions.getAvailableExplosives();
            default:
                return 1000000000;
        }
    }

    protected ExplodeCellResponse getExplodeCellRequest(){
        ExplodeCellResponse response = service.explodeCell (FACTORY.createExplodeCellRequest());
        logToSystemOut(response, response.getClass());
        return response;
    }

    public static void setTestMode(boolean testMode) {
        TEST_MODE = testMode;
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

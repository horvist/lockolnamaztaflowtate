package com.loxon.javachallenge.modules2016.bot.abslogic;

import com.loxon.javachallenge.modules2015.bot.core.Bot;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.*;
import com.loxon.javachallenge.modules2016.bot.enums.Actions;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.time.ITimeHelper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author kalmarr
 */
public abstract class AbstractLogicBot extends Bot {

    // change this to redirect output if desired
    private static PrintStream out = System.out;

    private static final ObjectFactory FACTORY = new ObjectFactory();

    private ITimeHelper timeHelper = Factory.createTimeHelper();

    protected WsCoordinate coords;

    private static volatile boolean INIT_SHUTTLE = false;

    private static volatile boolean INIT_MAP = false;

    private static boolean TEST_MODE = false;

    protected int expLeft;

    protected int turnsLeft;

    private int unitNumber;

    public AbstractLogicBot(String name, String password, String endpointAddress) {
        super(name, password, endpointAddress);
    }

    @Override
    protected abstract void process() throws InterruptedException;

    protected StartGameResponse startGame() {
        StartGameResponse response = service.startGame(FACTORY.createStartGameRequest());
        CommonResp commonResponse = response.getResult();
        if (success(commonResponse)) {
            handleCommonResponse(commonResponse);
        }
        logToSystemOut(response, response.getClass());
        return response;
    }

    private void initActionCosts() {
        boolean success = false;
        while (!success) {
            ActionCostResponse response = service.getActionCost(FACTORY.createActionCostRequest());
            CommonResp commonResp = response.getResult();
            if (!success(commonResp)) {
                continue;
            }
            this.costDrill = response.getDrill();
            this.costMove = response.getMove();
            this.costRadar = response.getRadar();
            this.costExplode = response.getExplode();
            this.costWatch = response.getWatch();
            this.numberOfActionPoints = response.getAvailableActionPoints();
            this.numberOfExplosives = response.getAvailableExplosives();

            success = true;
            logToSystemOut(response, response.getClass());
        }
    }

    private synchronized void initMap(final WsCoordinate size) {
        if (!INIT_MAP) {
            IMapCache map = Factory.getMap();
            map.initMap(size);
            INIT_MAP = true;
        }
    }

    private synchronized void initShuttleAndExitPos() {
        if (!INIT_SHUTTLE) {
            IMapCache map = Factory.getMap();
            boolean success = false;
            while (!success) {
                GetSpaceShuttlePosResponse response = service.getSpaceShuttlePos(FACTORY.createGetSpaceShuttlePosRequest());
                CommonResp commonResp = response.getResult();
                if (success(commonResp)) {
                    map.placeShuttle(response.getCord());
                    handleCommonResponse(commonResp);
                    success = true;
                }
                logToSystemOut(response, response.getClass());
            }


            success = false;
            while (!success) {
                GetSpaceShuttleExitPosResponse response = service.getSpaceShuttleExitPos(FACTORY.createGetSpaceShuttleExitPosRequest());
                CommonResp commonResp = response.getResult();
                if (success(commonResp)) {
                    map.markShuttleExit(response.getCord());
                    handleCommonResponse(commonResp);
                    success = true;
                }
                logToSystemOut(response, response.getClass());
            }
            // initilaze for shuttle informations has finished
            INIT_SHUTTLE = true;
        }
    }

    protected int getActionCost(Actions actionType) {
        switch (actionType) {
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

    protected boolean doAction(final Actions actionType, final WsCoordinate targetCoordinate) {
        if (this.apLeft < getActionCost(actionType)) {
            return false;
        }

        final IMapCache map = Factory.getMap();
        boolean success = true;
        try {
            if (timeHelper.isInTime()) {
                CommonResp commonResp;
                boolean isStructuring = false;
                switch (actionType) {
                    case DRILL:
                        isStructuring = true;
                        commonResp = doDrill(targetCoordinate);
                        break;
                    case EXPLODE:
                        isStructuring = true;
                        commonResp = doExplode(targetCoordinate);
                        break;
                    case MOVE:
                        commonResp = doMove(targetCoordinate);
                        break;
                    default:
                        return false;
                }
                if (commonResp != null) {
                    if (!success(commonResp)) {
                        throw new Exception();
                    }

                    handleCommonResponse(commonResp);

                    if (isStructuring) {
                        map.structureField(targetCoordinate);
                    }
                }
            } else {
                success = false;
            }
        } catch (Exception e) {
            success = false;
        }

        return success;
    }

    protected void login() {
        StartGameResponse response = startGame();
        CommonResp commonResp = response.getResult();
        if (success(commonResp)) {
            initMap(response.getSize());
            initShuttleAndExitPos(); // init shuttle positions
            initActionCosts(); // init cost informations
        }
    }

    protected Collection<Scouting> doRadar(final Collection<WsCoordinate> coordinates) {
        if (this.apLeft < getActionCost(Actions.RADAR) * coordinates.size()) {
            return Collections.EMPTY_LIST;
        }

        RadarRequest radarRequest = FACTORY.createRadarRequest();
        radarRequest.getCord().addAll(coordinates);
        radarRequest.setUnit(this.unitNumber);
        RadarResponse radarResponse = service.radar(radarRequest);

        CommonResp commonResp = radarResponse.getResult();
        if (success(commonResp)) {
            handleCommonResponse(commonResp);
            return radarResponse.getScout();
        }

        logToSystemOut(radarResponse, radarResponse.getClass());
        return Collections.EMPTY_LIST;
    }

    protected Collection<Scouting> doWatch() {
        if (this.apLeft < getActionCost(Actions.WATCH)) {
            return Collections.EMPTY_LIST;
        }

        WatchRequest watchRequest = FACTORY.createWatchRequest();
        watchRequest.setUnit(this.unitNumber);
        WatchResponse watchResponse = service.watch(watchRequest);

        CommonResp commonResp = watchResponse.getResult();
        if (success(commonResp)) {
            handleCommonResponse(commonResp);
            return watchResponse.getScout();
        }

        logToSystemOut(watchRequest, watchRequest.getClass());
        return Collections.EMPTY_LIST;
    }

    private CommonResp doExplode(WsCoordinate targetCoordinate) throws Exception {
        if (this.expLeft == 0) {
            throw new Exception("Run out of exp.");
        }

        IMapCache map = Factory.getMap();

        ExplodeCellRequest explodeCellRequest = FACTORY.createExplodeCellRequest();
        explodeCellRequest.setDirection(map.getDirection(this.coords, targetCoordinate));
        explodeCellRequest.setUnit(this.unitNumber);
        ExplodeCellResponse response = service.explodeCell(explodeCellRequest);

        logToSystemOut(response, response.getClass());

        return response.getResult();
    }

    private CommonResp doMove(WsCoordinate targetCoordinate) {
        IMapCache map = Factory.getMap();

        MoveBuilderUnitRequest request = FACTORY.createMoveBuilderUnitRequest();
        request.setDirection(map.getDirection(this.coords, targetCoordinate));
        request.setUnit(this.unitNumber);
        MoveBuilderUnitResponse response = service.moveBuilderUnit(request);

        logToSystemOut(response, response.getClass());

        return response.getResult();
    }

    private CommonResp doDrill(WsCoordinate targetCoordinate) {
        IMapCache map = Factory.getMap();

        StructureTunnelRequest request = FACTORY.createStructureTunnelRequest();
        request.setDirection(map.getDirection(this.coords, targetCoordinate));
        request.setUnit(this.unitNumber);
        StructureTunnelResponse response = service.structureTunnel(request);

        logToSystemOut(response, response.getClass());

        return response.getResult();
    }

    protected boolean isMyTurn() {
        IsMyTurnResponse response = service.isMyTurn(FACTORY.createIsMyTurnRequest());
        CommonResp commonResp = response.getResult();
        if (success(commonResp)) {
            handleCommonResponse(response.getResult());
            return response.isIsYourTurn();
        }
        logToSystemOut(response, response.getClass());
        return false;
    }

    public static void setTestMode(boolean testMode) {
        TEST_MODE = testMode;
    }

    private void handleCommonResponse(final CommonResp commonResp) {
        if (success(commonResp)) {
            this.apLeft = commonResp.getActionPointsLeft();
            this.expLeft = commonResp.getExplosivesLeft();
            this.turnsLeft = commonResp.getTurnsLeft();
            this.unitNumber = commonResp.getBuilderUnit();
        }
    }

    private boolean success(final CommonResp commonResp) {
        return ResultType.DONE.equals(commonResp.getType());
    }

    private void logToSystemOut(Object message, Class clz) {
        if (TEST_MODE) {
            try {
                JAXBContext context = JAXBContext.newInstance(clz);
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

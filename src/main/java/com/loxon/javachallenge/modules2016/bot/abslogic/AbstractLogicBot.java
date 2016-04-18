package com.loxon.javachallenge.modules2016.bot.abslogic;

import com.loxon.javachallenge.modules2015.bot.core.Bot;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.*;
import com.loxon.javachallenge.modules2016.bot.enums.Actions;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.IActionCostProvider;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions.*;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.time.ITimeHelper;
import com.loxon.javachallenge.modules2016.gui.controller.IGuiController;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import java.io.PrintStream;
import java.util.Collection;

/**
 * @author kalmarr
 */
public abstract class AbstractLogicBot extends Bot implements IActionCostProvider {

    private IGuiController guiController = null;

    // change this to redirect output if desired
    private static PrintStream out = System.out;

    private static final ObjectFactory FACTORY = new ObjectFactory();

    protected ITimeHelper timeHelper = Factory.createTimeHelper();

    protected IMapCache mapCache = Factory.createMap();

    protected WsCoordinate coords;

    private static boolean TEST_MODE = false;

    protected int expLeft;

    protected int turnsLeft;

    protected int unitNumber;

    public AbstractLogicBot(String name, String password, String endpointAddress) {
        super(name, password, endpointAddress);
    }

    @Override
    protected abstract void process() throws InterruptedException;

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

    private synchronized void initShuttleAndExitPos() {
        boolean success = false;
        while (!success) {
            GetSpaceShuttleExitPosResponse response = service.getSpaceShuttleExitPos(FACTORY.createGetSpaceShuttleExitPosRequest());
            CommonResp commonResp = response.getResult();
            if (success(commonResp)) {
                this.mapCache.markShuttleExit(response.getCord());
                handleCommonResponse(commonResp);
                success = true;
                logToSystemOut(response, response.getClass());
            }
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

    protected void doAction(final Actions actionType, final WsCoordinate targetCoordinate) throws Exception {
        if (this.apLeft < getActionCost(actionType)) {
            throw new RunOutOfActionPointsException(actionType.name());
        }

        try {
            CommonResp commonResp = null;
            switch (actionType) {
                case DRILL:
                    commonResp = doDrill(targetCoordinate);
                    break;
                case EXPLODE:
                    commonResp = doExplode(targetCoordinate);
                    break;
                case MOVE:
                    commonResp = doMove(targetCoordinate);
                    break;
                default:
                    throw new Exception("Invalid action.");
            }
            if (commonResp != null) {
                if (!success(commonResp)) {
                    throw new UnSuccessfulRequestException(actionType.name());
                }
                this.mapCache.commitChanges();

                handleCommonResponse(commonResp);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    protected void login() {
        StartGameResponse response = service.startGame(FACTORY.createStartGameRequest());
        CommonResp commonResponse = response.getResult();
        if (TEST_MODE) {
            this.guiController.initAndStartGui(response.getSize());
        }
        this.mapCache.initMap(response.getSize());
        initShuttleAndExitPos(); // init shuttle positions
        initActionCosts(); // init cost informations
        this.mapCache.placeShuttle(response.getUnits().get(0).getCord());
        handleCommonResponse(commonResponse);
        logToSystemOut(response, response.getClass());

        for (WsBuilderunit unit : response.getUnits()) {
            this.mapCache.placeUnit(unit.getUnitid(), unit.getCord());
        }
    }

    protected void doRadar(final Collection<WsCoordinate> coordinates) throws Exception {
        if (this.apLeft < getActionCost(Actions.RADAR) * coordinates.size()) {
            throw new RunOutOfActionPointsException("DoRadar");
        }
        if (!timeHelper.isInTime()) {
            throw new RunOutOfTimeException("DoRadar");
        }

        RadarRequest radarRequest = FACTORY.createRadarRequest();
        radarRequest.getCord().addAll(coordinates);
        radarRequest.setUnit(this.unitNumber);
        RadarResponse radarResponse = service.radar(radarRequest);

        CommonResp commonResp = radarResponse.getResult();
        if (success(commonResp)) {
            handleCommonResponse(commonResp);
            this.mapCache.handleScouts(radarResponse.getScout());
        } else {
            logToSystemOut(radarResponse, radarResponse.getClass());
        }
    }

    /**
     * Uses radar or watch function, depending on their cost (which one is free) to discover the fields around the current unit.
     *
     * @throws Exception
     */
    protected void doExplore() throws Exception {
        if (getActionCost(Actions.RADAR) == 0) {
            doRadar(this.mapCache.getRadarableCoordinatesForUnit(unitNumber));
        } else {
            doWatch();
        }
    }

    /**
     * Does nothing special, only uses all the remaining action points if the round should be ended.
     * Uses radar or watch, depending on their cost (which one is NOT free)
     *
     * @throws Exception
     */
    protected void doUseRemainingActionPoints() throws Exception {
        if (getActionCost(Actions.DRILL) >= this.apLeft) {
            for (WsCoordinate coordinate : mapCache.getNearbyFields(unitNumber, ObjectType.ROCK)) {
                try {
                    doAction(Actions.DRILL, coordinate);
                } catch (Exception e) {
                    continue;
                }
            }
        } else if (getActionCost(Actions.EXPLODE) >= this.apLeft) {
            for (WsCoordinate coordinate : mapCache.getNearbyFields(unitNumber, ObjectType.GRANITE)) {
                try {
                    doAction(Actions.EXPLODE, coordinate);
                } catch (Exception e) {
                    continue;
                }
            }
        }

        while (this.apLeft > 0) {
            if (getActionCost(Actions.RADAR) > 0) {
                doAction(Actions.RADAR, this.mapCache.getShuttleExit()); // we should check the field near our shuttle here.
            } else {
                doWatch();
            }
        }
    }

    protected void doWatch() throws Exception {
        if (this.apLeft < getActionCost(Actions.WATCH)) {
            throw new RunOutOfActionPointsException("DoWatch");
        }
        if (!timeHelper.isInTime()) {
            throw new RunOutOfTimeException("DoWatch");
        }

        WatchRequest watchRequest = FACTORY.createWatchRequest();
        watchRequest.setUnit(this.unitNumber);
        WatchResponse watchResponse = service.watch(watchRequest);

        CommonResp commonResp = watchResponse.getResult();
        if (success(commonResp)) {
            handleCommonResponse(commonResp);
            this.mapCache.handleScouts(watchResponse.getScout());
        } else {
            logToSystemOut(watchResponse, watchResponse.getClass());
        }

    }

    private CommonResp doExplode(WsCoordinate targetCoordinate) throws Exception {
        this.mapCache.checkFieldStructurable(targetCoordinate);

        if (this.expLeft == 0) {
            throw new RunOutOfExplosionsException("DoExplode");
        }

        if (!timeHelper.isInTime()) {
            throw new RunOutOfTimeException("DoDrill");
        }

        ExplodeCellRequest explodeCellRequest = FACTORY.createExplodeCellRequest();
        explodeCellRequest.setDirection(this.mapCache.getDirection(this.coords, targetCoordinate));
        explodeCellRequest.setUnit(this.unitNumber);
        ExplodeCellResponse response = service.explodeCell(explodeCellRequest);

        if (success(response.getResult())) {
            this.mapCache.structureField(targetCoordinate);
        } else {
            logToSystemOut(response, response.getClass());
        }


        return response.getResult();
    }

    private CommonResp doMove(WsCoordinate targetCoordinate) throws Exception {
        WsDirection wsDirection = this.mapCache.getDirection(this.coords, targetCoordinate);
        if (!timeHelper.isInTime()) {
            throw new RunOutOfTimeException("DoDrill");
        }
        MoveBuilderUnitRequest request = FACTORY.createMoveBuilderUnitRequest();
        request.setDirection(wsDirection);
        request.setUnit(this.unitNumber);
        MoveBuilderUnitResponse response = service.moveBuilderUnit(request);
        if (success(response.getResult())) {
            this.mapCache.moveUnit(unitNumber, targetCoordinate);
        } else {
            logToSystemOut(response, response.getClass());
        }

        return response.getResult();
    }

    private CommonResp doDrill(WsCoordinate targetCoordinate) throws Exception {
        if (!timeHelper.isInTime()) {
            throw new RunOutOfTimeException("DoDrill");
        }

        this.mapCache.checkFieldStructurable(targetCoordinate);
        StructureTunnelRequest request = FACTORY.createStructureTunnelRequest();
        request.setDirection(this.mapCache.getDirection(this.coords, targetCoordinate));
        request.setUnit(this.unitNumber);
        StructureTunnelResponse response = service.structureTunnel(request);
        if (success(response.getResult())) {
            this.mapCache.structureField(targetCoordinate);
        } else {
            logToSystemOut(response, response.getClass());
        }

        return response.getResult();
    }


    protected boolean isMyTurn() throws UnSuccessfulRequestException {
//        long diff = System.currentTimeMillis() - lastIsMyTurnRequest;
//        System.out.println("********************* diff: " + diff);
//        lastIsMyTurnRequest = System.currentTimeMillis();
        IsMyTurnResponse response = service.isMyTurn(FACTORY.createIsMyTurnRequest());
        CommonResp commonResp = response.getResult();
//        logToSystemOut(response, response.getClass());
        if (success(commonResp)) {
            if (response.isIsYourTurn()) {
                handleCommonResponse(response.getResult());
                this.coords = this.mapCache.getUnitPosition(this.unitNumber);
                return true;
            } else {
                return false;
            }
        } else {
            try {
                JAXBContext context = JAXBContext.newInstance(IsMyTurnResponse.class);
                Marshaller m = context.createMarshaller();
                //for pretty-print XML in JAXB
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

                // Write to File
                m.marshal(response, out);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            throw new UnSuccessfulRequestException("isMyTurn, milisecs: " + System.currentTimeMillis());
        }
    }

//    private static long lastIsMyTurnRequest = System.currentTimeMillis();

//    protected boolean isMyTurn() throws UnSuccessfulRequestException {
//        long diff = System.currentTimeMillis() - lastIsMyTurnRequest;
//        System.out.println("********************* diff: " + diff);
//        lastIsMyTurnRequest = System.currentTimeMillis();
//        IsMyTurnResponse response = service.isMyTurn(FACTORY.createIsMyTurnRequest());
//        CommonResp commonResp = response.getResult();
//        if (success(commonResp)) {
//            return true;
//        } else {
//            try {
//                JAXBContext context = JAXBContext.newInstance(IsMyTurnResponse.class);
//                Marshaller m = context.createMarshaller();
//                //for pretty-print XML in JAXB
//                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//
//                // Write to File
//                m.marshal(response, System.err);
//            } catch (JAXBException e) {
//                e.printStackTrace();
//            }
//            throw new UnSuccessfulRequestException("isMyTurn, milisecs: " + System.currentTimeMillis());
//        }
//    }

    public static void setTestMode(boolean testMode) {
        TEST_MODE = testMode;
    }

    public static boolean isTestMode() {
        return TEST_MODE;
    }

    private void handleCommonResponse(final CommonResp commonResp) {
        this.apLeft = commonResp.getActionPointsLeft();
        this.expLeft = commonResp.getExplosivesLeft();
        this.turnsLeft = commonResp.getTurnsLeft();
        this.unitNumber = commonResp.getBuilderUnit();
        if (TEST_MODE) {
            guiController.refreshScore(commonResp.getScore(), this.apLeft, this.expLeft, commonResp.getTurnsLeft());
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

    public int getUnitNumber() {
        return unitNumber;
    }

    public void setGuiController(IGuiController guiController) {
        this.guiController = guiController;
    }

    public CommonResp isMyTurnTest() {
        IsMyTurnResponse response = service.isMyTurn(FACTORY.createIsMyTurnRequest());
        CommonResp commonResp = response.getResult();
        return commonResp;
    }

    @Override
    public int getNumberOfExplosives() {
        return this.numberOfExplosives;
    }

    @Override
    public int getNumberOfActionPoints() {
        return this.numberOfActionPoints;
    }

    @Override
    public int getCostDrill() {
        return this.costDrill;
    }

    @Override
    public int getCostMove() {
        return this.costMove;
    }

    @Override
    public int getCostRadar() {
        return this.costRadar;
    }

    @Override
    public int getCostExplode() {
        return this.costExplode;
    }

    @Override
    public int getCostWatch() {
        return this.costWatch;
    }

}

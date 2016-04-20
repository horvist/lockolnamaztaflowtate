package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.ai;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectType;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsDirection;
import com.loxon.javachallenge.modules2016.bot.enums.FieldTeam;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.IActionCostProvider;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.Field;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.prop.PropertyHolder;


public class AI_1 implements IAI {

    private static final int MAX_GRAPH_DEPTH = PropertyHolder.getGraphDept();

    private static final boolean CHECK_FOUND_SMALLEST_COST = PropertyHolder.isCheckFoundSmallestCost();

    private static final double COST_REDUCE_RATIO = PropertyHolder.getReduceRatio();    // if a field is considered more valuable than the other fields with the same type, it's cost should be multiplied with this reducing ratio
    private static final double COST_INCREASE_RATIO = PropertyHolder.getIncRatio();    // increasing the cost of a field by this ratio if needed
    private double COST_GRANITE;
    private double COST_ROCK;
    private final double COST_OWN_TUNNEL = PropertyHolder.getCostOwnTunnel();    // hard coded value, bots are forced to drill/explode new fields, only walk on existing tunnel if necessary
    private double COST_ENEMY_TUNNEL;
    private double COST_DEFAULT;


    private static final int NUM_OF_UNITS = 4;

    private static AI_1 instance;

    private AI_1() {
        for (int i = 0; i < NUM_OF_UNITS; i++) {
            smallestCostFields.put(i, new Stack<>());  // stacks for unit movements should not be null, initialized in private constructor
        }
    }

    public static IAI getInstance() {
        if (instance == null) {
            synchronized (AI_1.class) {
                if (instance == null) {
                    instance = new AI_1();

                }
            }
        }
        return instance;
    }

    private int currentUnit = -1;
    private final Map<Double, Node> leafNodes = new HashMap<Double, Node>();      // NOTE: this is only used locally when a new graph is built, FIXME may use TreeSet?
    private final Map<Integer, Stack<Field>> smallestCostFields = new HashMap<Integer, Stack<Field>>(); // storing all the found smallest paths for bots, key is the bot's id
    //    private final Stack<Field> smallestCostFields = new Stack<Field>();
    private double foundSmallestCost = 999999;  // NOTE: this too is only used locally when a new graph is built, no thread concurrency is assumed!!!

    private void clearBotPaths() {
        for (Stack<Field> path : smallestCostFields.values()) {
            path.clear();
        }
    }

    @Override
    public void lastMovementWasExecutedSuccessfully(int unit) {
        final Stack<Field> fieldsForUnit = smallestCostFields.get(unit);
        fieldsForUnit.pop();
        if (isOnlyUncoveredFieldsAreInBotsPath(unit)) {
            fieldsForUnit.clear();
        }
//        smallestCostFields.pop();
    }

    private boolean isOnlyUncoveredFieldsAreInBotsPath(int unit) {
        for (Field field : smallestCostFields.get(unit)) {
            if (field.getObjectType() != null) {
                return false;
            }
        }

        return true;
    }

    private boolean isFieldInAnotherUnitsSteps(int unit, Field field) {
        for (Entry<Integer, Stack<Field>> entry : smallestCostFields.entrySet()) {
            if (entry.getKey() == unit) {
                continue;
            }

            for (Field otherField : entry.getValue()) {
                if (otherField.equals(field)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void fillActionCosts(IActionCostProvider actionCostProvider) {
        // inverse cost: cost of explode + drill + move
        COST_GRANITE = actionCostProvider.getCostDrill() + actionCostProvider.getCostExplode() + actionCostProvider.getCostMove();

        // cost of rock should be the smallest cost (it only needs to be drilled) - so bot will be forced to go for rocks when possible
        COST_ROCK = actionCostProvider.getCostDrill() < actionCostProvider.getCostExplode() ? actionCostProvider.getCostDrill() : actionCostProvider.getCostExplode();

        COST_ENEMY_TUNNEL = COST_ROCK * COST_INCREASE_RATIO;    // an enemy tunnel is considered to be slightly higher cost than drilling a rock

        COST_DEFAULT = COST_GRANITE * COST_REDUCE_RATIO;    // the default cost will be between the granite and rock
    }

    private boolean hasDirtyFieldInPath(int unit) {
        boolean ret = false;
        for (Field field : smallestCostFields.get(unit)) {
            if (field.isDirty()) {
                ret = true;
                field.clearDirtyFlag(); // should continue iteration to clear dirty flag from all fields in path
            }
        }

        return ret;
    }

    @Override
    public Field getNextStepForUnit(int unit, IMapCache map, int round, IActionCostProvider actionCostProvider) {
        fillActionCosts(actionCostProvider);
//        final long time = System.currentTimeMillis();

        final Stack<Field> movementsForUnit = smallestCostFields.get(unit);
//        final Stack<Field> movementsForUnit = smallestCostFields;

        if (round > 73) {
            clearBotPaths();
        }

        if (currentUnit != unit || movementsForUnit.isEmpty() || round > 70 || hasDirtyFieldInPath(unit)) {
            currentUnit = unit;
            foundSmallestCost = 999999;
            leafNodes.clear();
            movementsForUnit.clear();
        }

        if (map.isInStartPos(unit)) {
            movementsForUnit.push(map.getShuttleExitField());
            return map.getShuttleExitField();
        }

        if (movementsForUnit.isEmpty()) {
            getPotentialStepsGraph(map.getUnitField(unit), null, MAX_GRAPH_DEPTH, map, 0, 0, true);
            if (leafNodes.isEmpty()) {
                // this means that the current bot could not find a path because all the other bots blocked it's way...
                // clearing other bot's stored paths
                clearBotPaths();
                getPotentialStepsGraph(map.getUnitField(unit), null, MAX_GRAPH_DEPTH, map, 0, 0, true);
            }

            Node node = leafNodes.get(Collections.min(leafNodes.keySet()));

            while (node != null) {
                movementsForUnit.push(node.getField());
                node = node.getParent();
            }

            movementsForUnit.pop();  // NOTE: actual position of unit is in stack, popping it out
        }

//        System.out.println(smallestCostFields);
//        System.out.println("AI time: " + (System.currentTimeMillis() - time));


        return movementsForUnit.peek();   // peek only the next movement step, it will be removed when the successfull method is called
    }

    @SuppressWarnings("unused")
    private Node getPotentialStepsGraph(Field current, Node parent, int maxDepth, IMapCache map, int currentDepth, double currentCost, boolean firstStep) {
        if (CHECK_FOUND_SMALLEST_COST && currentCost >= foundSmallestCost) {
            return null;
        }
        if (current == null || (parent != null && isInCurrentSteps(current, parent)) || isFieldInAnotherUnitsSteps(currentUnit, current)) {
            return null;
        }

        if (!firstStep) {
            final ObjectType type = current.getObjectType();
            if ((!firstStep && type == ObjectType.BUILDER_UNIT) || type == ObjectType.OBSIDIAN || type == ObjectType.SHUTTLE) {
                return null;
            }

            currentCost += getCost(current, map);
        }

        Node currentNode = new Node(current, parent);


        if (currentDepth + 1 > maxDepth) {
            if (foundSmallestCost > currentCost) {
                foundSmallestCost = currentCost;
            }
            leafNodes.put(currentCost, currentNode);
            return currentNode;
        }

        List<WsDirection> randomDirections = Arrays.asList(WsDirection.values());
        Collections.shuffle(randomDirections); // shuffle directions to have more random movement for bots

        boolean foundMorePotentialLeafNodes = false;

        for (WsDirection direction : randomDirections) {
            Node newNode = getPotentialStepsGraph(map.getFieldForDirection(current, direction), currentNode, maxDepth, map, currentDepth + 1, currentCost, false);
            if (newNode != null) {
                currentNode.addChild(newNode);
                foundMorePotentialLeafNodes = true;
            }
        }

        if (!foundMorePotentialLeafNodes) {
            // if depth is smaller than the maximum depth, path should not be preferred more than a max_depth path
            // adding additional cost
            currentCost += (MAX_GRAPH_DEPTH - currentDepth) * COST_DEFAULT;

            if (foundSmallestCost > currentCost) {
                foundSmallestCost = currentCost;
            }
            leafNodes.put(currentCost, currentNode);
        }


        return currentNode;
    }

    private boolean isInCurrentSteps(Field field, Node parent) {
        while (parent != null) {
            if (parent.getField().equals(field)) {
                return true;
            }
            parent = parent.getParent();
        }

        return false;
    }

    private double getCost(Field field, IMapCache map) {
//      action costs:

//      <drill>8</drill>   <drill>6</drill>
//      <move>2</move>     <move>2</move>
//      <radar>1</radar>   <radar>0</radar>
//      <explode>6</explode>  <explode>3</explode>
//      <watch>0</watch>   <watch>1</watch>
//      <availableActionPoints>20</availableActionPoints>
//      <availableExplosives>80</availableExplosives>

        final ObjectType type = field.getObjectType();
        final FieldTeam team = field.getTeam();
        double cost = COST_DEFAULT;   // default value for uncovered fields

        if (type == ObjectType.TUNNEL) {
            if (team == FieldTeam.ALLY) {
                cost = COST_OWN_TUNNEL;
            } else if (field.isWasOurs()) {
                cost = COST_ENEMY_TUNNEL * COST_REDUCE_RATIO;
            } else {
                cost = COST_ENEMY_TUNNEL;
            }
        } else if (type == ObjectType.ROCK) {
            cost = COST_ROCK;
        } else if (type == ObjectType.GRANITE) {
            cost = COST_GRANITE;
        }

        if (!(type == ObjectType.TUNNEL && team == FieldTeam.ALLY)) {
            final int numOfOurFieldsNextToField = map.getNumOfOurFieldsNextToField(field);
            if (numOfOurFieldsNextToField > 0) {
                // if a field is next to our field, regardless of it's type it is considered to be more valuable
                cost *= (1 / (numOfOurFieldsNextToField * numOfOurFieldsNextToField));  // nééééégyzetesen
            }
        }

        // fields closer to the shuttle are move valuable?
        if (PropertyHolder.isFieldsWeighting() && PropertyHolder.getUnitsUseFieldWeight().contains(currentUnit)) {
            cost *= Math.sqrt(map.getFieldDistanceFromShuttle(field));
        }

        return cost;
    }

}

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
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.Field;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;


public class AI_1 implements IAI {

    private static final int MAX_GRAPH_DEPTH = 8;

    private static final boolean CHECK_FOUND_SMALLEST_COST = false;

    private static final int DEFAULT_COST = 10;

    private static final int NUM_OF_UNITS = 4;

    private static AI_1 instance;

    private AI_1(){
        for (int i = 0; i < NUM_OF_UNITS; i++) {
            smallestCostFields.put(i, new Stack<Field>());  // stacks for unit movements should not be null, initialized in private constructor
        }
    }

    public static IAI getInstance() {
        if(instance == null){
            synchronized (AI_1.class) {
                if (instance == null) {
                    instance = new AI_1();

                }
            }
        }
        return instance;
    }

    private int currentUnit = -1;
    private final Map<Integer, Node> leafNodes = new HashMap<Integer, Node>();      // NOTE: this is only used locally when a new graph is built, FIXME may use TreeSet?
    private final Map<Integer, Stack<Field>> smallestCostFields = new HashMap<Integer, Stack<Field>>();
//    private final Stack<Field> smallestCostFields = new Stack<Field>();
    private int foundSmallestCost = Integer.MAX_VALUE;  // NOTE: this too is only used locally when a new graph is built, no thread concurrency is assumed!!!

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

    @Override
    public Field getNextStepForUnit(int unit, IMapCache map, int round) {
//        final long time = System.currentTimeMillis();

        final Stack<Field> movementsForUnit = smallestCostFields.get(unit);
//        final Stack<Field> movementsForUnit = smallestCostFields;

        if (currentUnit != unit || movementsForUnit.isEmpty() || round > 70) {
            currentUnit = unit;
            foundSmallestCost = Integer.MAX_VALUE;
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
    private Node getPotentialStepsGraph(Field current, Node parent, int maxDepth, IMapCache map, int currentDepth, int currentCost, boolean firstStep) {
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

            currentCost += getCost(current);
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
            currentCost += (MAX_GRAPH_DEPTH - currentDepth) * DEFAULT_COST;

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

    private int getCost(Field field) {
//      action costs:

//      <drill>8</drill>
//      <move>2</move>
//      <radar>1</radar>
//      <explode>6</explode>
//      <watch>0</watch>
//      <availableActionPoints>20</availableActionPoints>
//      <availableExplosives>80</availableExplosives>

        final ObjectType type = field.getObjectType();
        final FieldTeam team = field.getTeam();
        int cost = DEFAULT_COST;   // default value for uncovered fields - granite's cost

        if (type == ObjectType.TUNNEL) {
            if (team == FieldTeam.ALLY) {
                cost = 30;  // inverse cost: cost of explode + drill + move + 2
            } else {
                cost = 4;  // cost for exploding an enemy tunnel is the same as drilling a rock - because of exploration of possibilities to destroy enemy tunnels
            }
        }

        if (type == ObjectType.ROCK) {
            cost = 2; // inverse cost: cost of move
        }

        if (type == ObjectType.GRANITE) {
            cost = 10;    // inverse cost: cost of move + drill
        }

        // field is not yet visible, default cost is applied

        return cost;
    }
}

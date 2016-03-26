package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.ai;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectType;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsDirection;
import com.loxon.javachallenge.modules2016.bot.enums.FieldTeam;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.Field;
import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.IMapCache;


public class AI_1 implements IAI {

    private static final int MAX_GRAPH_DEPTH = 12;

    private static final boolean CHECK_FOUND_SMALLEST_COST = false;

    private static final int DEFAULT_COST = 8;

    private static AI_1 instance;

    private AI_1(){
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
    private final Map<Integer, Node> leafNodes = new HashMap<Integer, Node>();      // FIXME may use TreeSet?
    private final Stack<Field> smallestCostFields = new Stack<Field>();
    private int foundSmallestCost = Integer.MAX_VALUE;

    @Override
    public Field getNextStepForUnit(int unit, IMapCache map) {
//        final long time = System.currentTimeMillis();

        if (currentUnit != unit || smallestCostFields.isEmpty()) {
            currentUnit = unit;
            foundSmallestCost = Integer.MAX_VALUE;
            leafNodes.clear();
            smallestCostFields.clear();
        }

        if (map.isInStartPos(unit)) {
            return map.getShuttleExitField();
        }

        if (smallestCostFields.isEmpty()) {
            getPotentialStepsGraph(map.getUnitField(unit), null, MAX_GRAPH_DEPTH, map, 0, 0, true);

            Node node = leafNodes.get(Collections.min(leafNodes.keySet()));

            while (node != null) {
                smallestCostFields.push(node.getField());
                node = node.getParent();
            }

            smallestCostFields.pop();  // NOTE: actual position of unit is in stack, popping it out
        }

//        System.out.println(smallestCostFields);
//        System.out.println("AI time: " + (System.currentTimeMillis() - time));


        return smallestCostFields.pop();
    }

    @SuppressWarnings("unused")
    private Node getPotentialStepsGraph(Field current, Node parent, int maxDepth, IMapCache map, int currentDepth, int currentCost, boolean firstStep) {
        if (CHECK_FOUND_SMALLEST_COST && currentCost >= foundSmallestCost) {
            return null;
        }
        if (current == null || (parent != null && isInCurrentSteps(current, parent))) {
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
//                optimalFieldsForCurrentUnit = fieldsInCurrentRoot;
//                Field field = parent.getField()
                foundSmallestCost = currentCost;
            }
            leafNodes.put(currentCost, currentNode);
            return currentNode;
        }


        for (WsDirection direction : WsDirection.values()) {
            Node newNode = getPotentialStepsGraph(map.getFieldForDirection(current, direction), currentNode, maxDepth, map, currentDepth + 1, currentCost, false);
            if (newNode != null) {
                currentNode.addChild(newNode);
            } else {
                // if depth is smaller than the maximum depth, path should not be preferred more than a max_depth path
                // adding additional cost
                currentCost += (MAX_GRAPH_DEPTH - currentDepth) * DEFAULT_COST;
            }
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
                cost = 2;  // cost for exploding an enemy tunnel is the same as drilling a rock - because of exploration of possibilities to destroy enemy tunnels
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

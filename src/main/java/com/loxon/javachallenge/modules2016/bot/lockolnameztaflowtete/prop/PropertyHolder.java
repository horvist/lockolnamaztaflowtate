package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.prop;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author kalmarr
 */
public final class PropertyHolder {

    private static final String PROPERTY_FILE_LOC = "properties/base.properties";

    private static final String ROUND_TIME_INTERV_PROP = "time.oneRound";

    private static long oneRoundTime;

    private static final String TIME_BETWEEN_ISMYTURN = "time.between.ismyturn";

    private static long timeBetweenIsMyTurn;

    private static final String GRAPH_DEPT = "ai.graph.depth";

    private static int graphDept;

    private static final String CHECK_FOUND_SMALLEST_COST = "ai.checkFoundSmallestCost";

    private static boolean checkFoundSmallestCost;

    private static final String REDUCE_RATIO = "ai.cost.reduceRatio";

    private static double reduceRatio;

    private static final String INC_RATIO = "ai.cost.increaseRatio";

    private static double incRatio;

    private static final String COST_OWN_TUNNEL = "ai.cost.ownTunnel";

    private static double costOwnTunnel;

    private static final String FIELDS_WEIGHTING = "ai.fieldsWeighting";

    private static boolean fieldsWeighting;

    private static final String UNITS_USE_FIELD_WEIGHTING = "ai.unitsForWeight";

    private static Set<Integer> unitsUseFieldWeight;

    private static final String COLLECT_ISLANDS = "ai.collectIslands";

    private static boolean collectIslands;

    private static final String UNITS_COLLECT_ISLANDS = "ai.unitsForCollectIslands";

    private static Set<Integer> unitsCollectIslands;

    private static final String FIELDS_INVERS_WEIGHTING = "ai.inversFieldWieghting";

    private static boolean inversFieldsWeighting;

    private static final String UNITS_USE_FIELD_INVERS_WEIGHTING = "ai.unitsForInversWeighting";

    private static Set<Integer> unitsUseInversFieldWeight;

    private static final String USE_DIRTY = "ai.useDirty";

    private static boolean useDirty;

    private static final String COUNT_SURROUNDING_ROCKS= "ai.countSurroundingRocks";

    private static boolean countSurroundingRocks;

    private PropertyHolder() {
    }

    static {
        try {
            Properties pro = new Properties();
            pro.load(
             new FileInputStream(new File(new PropertyHolder().getClass().getClassLoader().getResource(PROPERTY_FILE_LOC).getFile()))
            );

            oneRoundTime = Long.valueOf(pro.getProperty(ROUND_TIME_INTERV_PROP));

            timeBetweenIsMyTurn = Long.valueOf(pro.getProperty(TIME_BETWEEN_ISMYTURN));

            graphDept = Integer.valueOf(pro.getProperty(GRAPH_DEPT));

            checkFoundSmallestCost = Boolean.TRUE.toString().equalsIgnoreCase(pro.getProperty(CHECK_FOUND_SMALLEST_COST));

            reduceRatio = Double.valueOf(pro.getProperty(REDUCE_RATIO));

            incRatio = Double.valueOf(pro.getProperty(INC_RATIO));

            costOwnTunnel = Double.valueOf(pro.getProperty(COST_OWN_TUNNEL));

            fieldsWeighting = Boolean.TRUE.toString().equalsIgnoreCase(pro.getProperty(FIELDS_WEIGHTING));

            unitsUseFieldWeight = getUnitsListFromProp(pro.getProperty(UNITS_USE_FIELD_WEIGHTING));

            collectIslands = Boolean.TRUE.toString().equalsIgnoreCase(pro.getProperty(COLLECT_ISLANDS));

            unitsCollectIslands = getUnitsListFromProp(pro.getProperty(UNITS_COLLECT_ISLANDS));

            inversFieldsWeighting = Boolean.TRUE.toString().equalsIgnoreCase(pro.getProperty(FIELDS_INVERS_WEIGHTING));

            unitsUseInversFieldWeight = getUnitsListFromProp(pro.getProperty(UNITS_USE_FIELD_INVERS_WEIGHTING));

            useDirty = Boolean.TRUE.toString().equalsIgnoreCase(pro.getProperty(USE_DIRTY));

            countSurroundingRocks = Boolean.TRUE.toString().equalsIgnoreCase(pro.getProperty(COUNT_SURROUNDING_ROCKS));

        } catch (Exception e) {
            // in the case, when exception occured by anything, application must not start
            System.exit(-1);
        }
    }

    private static Set<Integer> getUnitsListFromProp(String list){
        final Set<Integer> result = new HashSet<>();
        StringTokenizer tokenizer = new StringTokenizer(list, ",");
        while(tokenizer.hasMoreElements()){
            try {
                result.add(Integer.valueOf(tokenizer.nextToken()) - 1);
            } catch (NumberFormatException e){
                continue;
            }
        }
        return result;
    }

    public static long getTimeBetweenIsMyTurn() {
        return timeBetweenIsMyTurn;
    }

    public static long getOneRoundTime() {
        return oneRoundTime;
    }

    public static int getGraphDept() {
        return graphDept;
    }

    public static boolean isCheckFoundSmallestCost() {
        return checkFoundSmallestCost;
    }

    public static double getReduceRatio() {
        return reduceRatio;
    }

    public static double getIncRatio() {
        return incRatio;
    }

    public static double getCostOwnTunnel() {
        return costOwnTunnel;
    }

    public static boolean isFieldsWeighting() {
        return fieldsWeighting;
    }

    public static Set<Integer> getUnitsUseFieldWeight() {
        return unitsUseFieldWeight;
    }

    public static boolean isCollectIslands() {
        return collectIslands;
    }

    public static boolean isInversFieldsWeighting() {
        return inversFieldsWeighting;
    }

    public static Set<Integer> getUnitsUseInversFieldWeight() {
        return unitsUseInversFieldWeight;
    }

    public static Set<Integer> getUnitsCollectIslands() {
        return unitsCollectIslands;
    }

    public static boolean isUseDirty() {
        return useDirty;
    }

    public static boolean isCountSurroundingRocks() {
        return countSurroundingRocks;
    }
}


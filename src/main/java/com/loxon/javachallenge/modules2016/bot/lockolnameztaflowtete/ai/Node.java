package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.ai;

import java.util.HashSet;
import java.util.Set;

import com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map.Field;

public class Node {
    private final Field field;
    private final Node parent;
    private final Set<Node> childNodes = new HashSet<Node>();
    private int cost;

    public Node(Field field, Node parent) {
        this.field = field;
        this.parent = parent;
    }

    public int getCost() {
        return cost;
    }

    public void setReward(int cost) {
        this.cost = cost;
    }

    public Field getField() {
        return field;
    }

    public Set<Node> getChildNodes() {
        return childNodes;
    }

    public void addChild(Node node) {
        if (node != null) {
            this.childNodes.add(node);
        }
    }

    public Node getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return this.field.toString();
    }
}

package it.polimi.ingsw.game;

import java.util.ArrayList;

public class NextAction {
    private String action_name;
    private ArrayList<Vector2> available_position;

    public NextAction(Worker w, Map m, GameConstraints constraints,BehaviourNode node) {
        this.action_name = node.getAction().displayName();
        this.available_position = new ArrayList<>();
        this.available_position.addAll(node.getAction().possibleCells(w,m,constraints,node));
    }

    public String getAction_name() {
        return this.action_name;
    }
}
package it.polimi.ingsw.game;

import java.util.ArrayList;

/**
 * Class the holds a list of valid positions for an action
 */
public class NextAction {
    private int worker;
    private String actionName;
    private ArrayList<Vector2> available_position;

    public NextAction(Worker w, Map m, GameConstraints constraints,BehaviourNode node) {
        this.worker = w.getId();
        this.actionName = node.getAction().displayName();
        this.available_position = new ArrayList<>();
        this.available_position.addAll(node.getAction().possibleCells(w,m,constraints));
    }

    /**
     * Return the action name
     * @return the action name
     */
    public String getActionName() {
        return this.actionName;
    }

    /**
     * Return a valid list of position where this move can be applied
     * @return valid list of cells
     */
    public ArrayList<Vector2> getAvailable_position(){ return this.available_position; }

    /**
     * Return the worker id that must be used to run this move
     * @return worker id
     */
    public int getWorker() { return worker; }
}
package it.polimi.ingsw.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Class the holds a list of valid positions for an action
 */
public class NextAction {
    private int worker;
    private String actionName;
    private ArrayList<Vector2> availablePositions;

    public NextAction(Worker w, Map m, GameConstraints constraints,BehaviourNode node) {
        this.worker = w.getId();
        this.actionName = node.getAction().displayName();
        this.availablePositions = new ArrayList<>();
        this.availablePositions.addAll(node.getAction().possibleCells(w,m,constraints));
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
    public List<Vector2> getAvailablePositions(){ return this.availablePositions; }

    /**
     * Return the worker id that must be used to run this move
     * @return worker id
     */
    public int getWorkerID() { return worker; }
}
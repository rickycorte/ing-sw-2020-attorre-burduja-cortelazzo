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
    private boolean isUndoAction;


    /**
     * Create a new next action to send to the play
     * @param name action name displayed to the player
     * @param w worker allowed to run this action
     * @param position valid position where this action can be applied
     * @param isUndo set to true if this action should be used as undo
     */
    public NextAction(String name, Worker w, Vector2 position, boolean isUndo)
    {
        this.worker = w.getId();
        availablePositions = new ArrayList<>();
        availablePositions.add(position);
        actionName = name;
        isUndoAction = isUndo;
    }

    /**
     * Generate a new next action to send to the play
     * @param w worker allowed to run this action
     * @param m map where this action should be calculated
     * @param constraints extra constraints that should be applied to the generation of valid moves
     * @param node bahaviour graph used to generate valid moves for this action
     */
    public NextAction(Worker w, Map m, GameConstraints constraints,BehaviourNode node) {
        this.worker = w.getId();
        this.actionName = node.getAction().displayName();
        this.availablePositions = new ArrayList<>();
        this.availablePositions.addAll(node.getAction().possibleCells(w,m,constraints));
        isUndoAction = false;
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

    /**
     * Check if this actions is an undo or not
     * @return true if undo
     */
    public boolean isUndo()
    {
        return isUndoAction;
    }

    /**
     * Check if this action is an end turn
     * @return true if this action is an end turn for the player
     */
    public boolean isEndTurnAction() { return this.actionName.equals(EndTurnAction.class.toString(  )); }
}
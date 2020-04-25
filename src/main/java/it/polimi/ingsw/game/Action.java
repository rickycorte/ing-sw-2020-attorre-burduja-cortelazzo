package it.polimi.ingsw.game;

import java.util.ArrayList;

/**
 * Abstract class that represents an action executed by a card
 * Actions should be stateless and know nothing about the current game
 * Every parameter needed to execute the action is passed with run function
 *
 * Action should only store data about behaviour customizations passed only as Constructor parameters
 * Each Action is identified by a net_id 10-19 -- moveTypeAction
 *                                       20-29 -- buildTypeAction
 *                                       30-39 -- endTurnTypeAction
 * Every Action subclass must also set its display name and return it with display_name()
 */
public abstract class Action
{

    protected int netId;
    protected  String displayName;

    /**
     * This function executes the action behaviour on the parameters passed
     * You should pass a reference to the original object actually executing actions on your game
     * @param w target worker used in this action
     * @param target target position where the action should take place
     * @param m map where the action is executed
     * @param globalConstrains global game constrains that should be applied before action execution
     * @return 1 = I won, 0 = continue, -1 = I lost
     * @throws NotAllowedMoveException if action is not possible with the current parameters
     */
    public abstract int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException;

    /**
     * Return the display name for an action
     * This name should include applied constrains
     * Example: Move - Horizontal Only
     * @return action display name
     */
    public String displayName() { return displayName; }

    /**
     * @param w worker doing the job
     * @param m current map
     * @param gc list of constraints
     * @return an ArrayList of Vector2 objects, representing all the possible cells i can run the action from the w.getpos() cell
     */
    public abstract ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc);

    /**
     * Merge constrains and return a copy leaving the original ones untouched
     * @param localConstraints local constraints
     * @param globalConstraints global constraints
     * @return merged constraints
     */
    protected GameConstraints mergeConstraints(final GameConstraints localConstraints, final GameConstraints globalConstraints)
    {
        GameConstraints gc = new GameConstraints(localConstraints);
        gc.add(globalConstraints);
        return gc;
    }
}

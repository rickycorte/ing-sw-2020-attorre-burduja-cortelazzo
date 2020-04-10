package it.polimi.ingsw.game;

import java.util.ArrayList;

/**
 * Abstract class that represents an action executed by a card
 * Actions should be stateless and know nothing about the current game
 * Every parameter need to execute the action is passed with run function
 *
 * Action should only store data about behaviour customizations passed only as Constructor parameters
 * Every Action subclass must also set its display name and return it with display_name()
 */
public abstract class Action
{

    protected static int net_id;
    protected static String display_name;

    /**
     * This function execute the action behaviour on the parameters passed
     * You should pass a reference to the original object actually execute actions on your game
     * @param w target worker used in this action
     * @param target target position where the action should take place
     * @param m map where the action is executed
     * @param globalConstrains global game constrains that should be applied before action execution
     * @return action status report [TBD]
     * @throws NotAllowedMoveException if action is not possible with the current parameters
     */
    public abstract int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains, BehaviourNode node) throws NotAllowedMoveException;

    /**
     * Return the display name for an action
     * This name should include applied constrains
     * Example: Move - Horizontal Only
     * @return action display name
     */
    public String displayName() { return display_name; }

    public abstract ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc, BehaviourNode node) throws OutOfMapException;
}

package it.polimi.ingsw.controller.compact;

import it.polimi.ingsw.game.Vector2;

/**
 * This is a data only class used to be serialized over the network
 * and contains data about the action that a player wants to run
 */
public class CompactSelectedAction
{
    private int actionID;
    private int workerID;
    private Vector2 position;

    public CompactSelectedAction(int actionID, int workerID, Vector2 position)
    {
        this.actionID = actionID;
        this.workerID = workerID;
        this.position = position;
    }

    /**
     * Return the action id (index of the action) chosen
     * @return action id
     */
    public int getActionID()
    {
        return actionID;
    }

    /**
     * Return the selected worker id relative to the owner
     * @return worker id
     */
    public int getWorkerID()
    {
        return workerID;
    }

    /**
     * Return the target position where this action should take place
     * @return target position of the action
     */
    public Vector2 getPosition()
    {
        return position;
    }
}

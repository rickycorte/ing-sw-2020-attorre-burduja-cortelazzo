package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.compact.CompactSelectedAction;
import it.polimi.ingsw.game.NextAction;
import it.polimi.ingsw.game.Vector2;

import java.util.List;

/**
 * This command is used to request/run action during a turn and contains all possible actions that a client can do
 * (Server)
 * Request a client to run an action from the provided ones
 * (Client)
 * Send the server what action the player wants to run
 */
public class ActionCommand extends BaseCommand {
    private NextAction[] actions;
    private CompactSelectedAction selectedAction;

    /**
     * (Server) create a new action command to ask a client to chose an action
     * @param sender sender of the command
     * @param target target that should perform an action
     * @param nextActions available actions
     */
    public ActionCommand(int sender, int target, List<NextAction> nextActions) {
        super(sender, target);
        selectedAction = null;
        actions = nextActions.toArray(new NextAction[0]);

    }


    /**
     * (Client) Chose a action and inform the server on what to do
     * @param sender sender of the command (client id)
     * @param target target that should receive this command
     * @param actionID id of the action to run
     * @param workerID id of the worker used to run the action
     * @param targetPosition target position where the action should run
     */
    public ActionCommand(int sender, int target, int actionID, int workerID, Vector2 targetPosition)
    {
        super(sender, target);
        actions = null;
        selectedAction = new CompactSelectedAction(actionID, workerID, targetPosition);
    }


    /**
     * Get available actions contained in the command
     * @return available action list
     */
    public NextAction[] getAvailableActions()
    {
        return actions;
    }

    /**
     * Return the selected action contained in the command
     * @return selected action
     */
    public CompactSelectedAction getSelectedAction()
    {
        return selectedAction;
    }


    /**
     * (Server) create a new action command to ask a client to chose an action
     * @param sender sender of the command
     * @param target target that should perform an action
     * @param nextActions available actions
     * @return wrapped command ready to be sent over the network
     */
    public static CommandWrapper makeRequest(int sender, int target, List<NextAction> nextActions)
    {
        if(nextActions == null)
            return null;
        return new CommandWrapper(CommandType.ACTION_TIME, new ActionCommand(sender,target,nextActions));
    }

    /**
     * (Client) Chose a action and inform the server on what to do
     * @param sender sender of the command (client id)
     * @param target target that should receive this command
     * @param actionID id of the action to run
     * @param workerID id of the worker used to run the action
     * @param targetPosition target position where the action should run
     * @return wrapped command ready to be sent over the network
     */
    public static CommandWrapper makeReply(int sender, int target, int actionID, int workerID, Vector2 targetPosition)
    {
        return new CommandWrapper(CommandType.ACTION_TIME, new ActionCommand(sender,target,actionID, workerID,targetPosition));
    }

}

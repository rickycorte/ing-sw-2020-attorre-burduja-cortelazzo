package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.compact.CompactSelectedAction;
import it.polimi.ingsw.game.Action;
import it.polimi.ingsw.game.NextAction;
import it.polimi.ingsw.game.Vector2;

import java.util.ArrayList;
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

    //-----------------------------------------------------------------------------------------------------------------------------


    /**
     * @deprecated use the new constructor
     */
    @Deprecated
    public ActionCommand(int sender, int target, int[] workerAndAction, Vector2 selectedPos){
        //REQUEST FROM CLIENT
        super(sender, target);
        actions = null;
        selectedAction = new CompactSelectedAction(workerAndAction[1], workerAndAction[0], selectedPos);
    }

    /**
     * @deprecated use {@link #getAvailableActions()}
     */
    @Deprecated
    public int[] getIdWorkerNMove() {
        return idWorkerNMoveToArray(actions);
    }

    /**
     * @deprecated use {@link #getAvailableActions()}
     */
    @Deprecated
    public Vector2[] getAvailablePos() {
        return availablePosToArray(actions);
    }

    /**
     * @deprecated use {@link #getAvailableActions()}
     */
    @Deprecated
    public String[] getActionName() {
        return actionNameToArray(actions);
    }

    /**
     * utility method
     * string array of every name of possible actions
     * @param list NextAction list
     * @return String array of possible next action's name
     */
    @Deprecated
    private String[] actionNameToArray(NextAction[] list){
        String[] array = new String[list.length];

        for(int i=0 ;i<list.length;i++){
            array[i] = list[i].getActionName();
        }

        return array;
    }

    /**
     * utility method
     * pair of int set with (id worker, number of possible position available with that worker)
     * @param list NextAction list
     * @return int array of pair (worker, n. possible position)
     */
    @Deprecated
    private int[] idWorkerNMoveToArray(NextAction[] list){
        int[] array = new int[list.length*2];

        for(int i = 0,j = 0 ; i<list.length ; i++ ){
            array[j] = list[i].getWorkerID();
            array[j+1] = list[i].getAvailablePositions().size();
            j = j+2;
        }
        return array;
    }


    /**
     * utility method
     * every possible position available for next actions of a player are set in vector2 array
     * @param list NextAction list
     * @return Vector2 array of every available position
     */
    @Deprecated
    private Vector2[] availablePosToArray(NextAction[] list){
        List<Vector2> newList = new ArrayList<>();

        for (NextAction nextAction : list) {
            newList.addAll(nextAction.getAvailablePositions());
        }

        return newList.toArray(new Vector2[0]);
    }

    /**
     *
     * @return worker id that can be selected for this turn
     * @deprecated use {@link #getAvailableActions()}
     */
    @Deprecated
    public List<Integer> getAvailableWorker(){
        var idWorkerNMove = getIdWorkerNMove();
        List<Integer> availableWorkers = new ArrayList<>();
        for(int i = 0; i<idWorkerNMove.length; i=i+2){
            Integer x = idWorkerNMove[i];
            if(!availableWorkers.contains(x)){
                availableWorkers.add(x);
            }
        }
        return availableWorkers;
    }

    /**
     *
     * @param workerId id of selected worker
     * @return all possible action that can be executed with workerId
     * @deprecated use {@link #getAvailableActions()}
     */
    public List<String> getActionForWorker(int workerId){
        var idWorkerNMove = getIdWorkerNMove();
        var actionName = getActionName();
        List<String> actionForWorker = new ArrayList<>();
        for(int workerCounter = 0,actionCounter=0; workerCounter<idWorkerNMove.length && actionCounter<actionName.length; workerCounter=workerCounter+2, actionCounter++){
            if(idWorkerNMove[workerCounter] == workerId){
                actionForWorker.add(actionName[actionCounter]);
            }
        }
        return actionForWorker;
    }


    /**
     *
     * @param workerId (baseIndex + selectedId) selected worker from client
     * @return index of first element can be selected (e.g. if selected 0, then baseIndex + 0 get action from list correspondent to first element selected)
     * @deprecated use {@link #getAvailableActions()}
     */
    @Deprecated
    public int getBaseIndexForAction(int workerId){
        var idWorkerNMove = getIdWorkerNMove();
        for(int workerCounter = 0,actionCounter=0; workerCounter<idWorkerNMove.length; workerCounter=workerCounter+2, actionCounter++){
            if(idWorkerNMove[workerCounter] == workerId){
                return actionCounter;
            }
        }
        return -1;
    }

    /**
     *
     * @param actionID (baseIndex + selectedId) selected action (internal rep)
     * @return available cells to perform the selected action
     * @deprecated use {@link #getAvailableActions()}
     */
    @Deprecated
    public List<Vector2> getPositionsForAction(int actionID){
        var idWorkerNMove = getIdWorkerNMove();
        var availablePos = getAvailablePos();
        List<Vector2> availablePositions = new ArrayList<>();
        int index;
        if(actionID == 0) {
            index = actionID + 1;
            for(int i = 0 ; i<idWorkerNMove[index] ; i++){
                availablePositions.add(availablePos[i]);
            }
        }
        else {
            index = (actionID * 2)+1;
            int i,j;
            for(i=1,j=0; i<index; i=i+2){
                j=j+idWorkerNMove[i];
            }
            for(int k=0; k<idWorkerNMove[i] && j<availablePos.length;j++,k++){
                availablePositions.add(availablePos[j]);
            }
        }
        return availablePositions;
    }

    /**
     *
     * @param actionID selected action id
     * @return action identifier already summed to the selected one (it is the positions of internal array representation)
     * @deprecated use {@link #getAvailableActions()}
     */
    @Deprecated
    public int getBaseIndexForPositions(int actionID){
        var idWorkerNMove = getIdWorkerNMove();
        if(actionID == 0) {
            return 0;
        }
        else {
            int index = (actionID * 2)+1;
            int i,j;
            for(i=1,j=0; i<index; i=i+2){
                j=j+idWorkerNMove[i];
            }
            return j;
        }
    }

    /**
     *
     * @param positionIndex (baseIndex + selectedId) selected target (internal rep)
     * @return Vector2 position where action want to be executed
     * @deprecated use {@link #getAvailableActions()}
     */
    @Deprecated
    public Vector2 getTargetPosition(int positionIndex){

        return getAvailablePos()[positionIndex];
    }

}

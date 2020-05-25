package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.NextAction;
import it.polimi.ingsw.game.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This command is used to request/run action during a turn and contains all possible actions that a client can do
 */
public class ActionCommand extends BaseCommand {
    private  int[] idWorkerNMove;
    private Vector2[] availablePos;
    private String[] actionName;

    public ActionCommand(int sender, int target, List<NextAction> nextActions) {
        //UPDATE FROM SERVER
        super(sender, target);
        this.idWorkerNMove = idWorkerNMoveToArray(nextActions);
        this.availablePos = availablePosToArray(nextActions);
        this.actionName = actionNameToArray(nextActions);
    }

    //TODO: change parameter int[]
    public ActionCommand(int sender, int target, int[] workerAndAction, Vector2 selectedPos){
        //REQUEST FROM CLIENT
        super(sender, target);
        this.idWorkerNMove = workerAndAction;
        this.availablePos = new Vector2[]{selectedPos};
        this.actionName = null;
    }

    public int[] getIdWorkerNMove() {
        return idWorkerNMove;
    }

    public Vector2[] getAvailablePos() {
        return availablePos;
    }

    public String[] getActionName() {
        return actionName;
    }

    /**
     * utility method
     * string array of every name of possible actions
     * @param list NextAction list
     * @return String array of possible next action's name
     */
    private String[] actionNameToArray(List<NextAction> list){
        String[] array = new String[list.size()];

        for(int i=0 ;i<list.size();i++){
            array[i] = list.get(i).getActionName();
        }

        return array;
    }

    /**
     * utility method
     * pair of int set with (id worker, number of possible position available with that worker)
     * @param list NextAction list
     * @return int array of pair (worker, n. possible position)
     */
    private int[] idWorkerNMoveToArray(List<NextAction> list){
        int[] array = new int[list.size()*2];

        for(int i = 0,j = 0 ; i<list.size() ; i++ ){
            array[j] = list.get(i).getWorker();
            array[j+1] = list.get(i).getAvailable_position().size();
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
    private Vector2[] availablePosToArray(List<NextAction> list){
        List<Vector2> newList = new ArrayList<>();

        for (NextAction nextAction : list) {
            newList.addAll(nextAction.getAvailable_position());
        }

        return newList.toArray(new Vector2[0]);
    }

    /**
     *
     * @return worker id that can be selected for this turn
     */
    public List<Integer> getAvailableWorker(){
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
     */
    public List<String> getActionForWorker(int workerId){
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
     */
    public int getBaseIndexForAction(int workerId){
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
     */
    public List<Vector2> getPositionsForAction(int actionID){
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
     */
    public int getBaseIndexForPositions(int actionID){

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
     */
    public Vector2 getTargetPosition(int positionIndex){
        return availablePos[positionIndex];
    }

}

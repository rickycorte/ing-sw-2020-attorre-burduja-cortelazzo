package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.NextAction;
import it.polimi.ingsw.game.Vector2;

import java.util.ArrayList;
import java.util.List;

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


}

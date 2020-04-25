package it.polimi.ingsw.game;

import java.util.ArrayList;

public class Turn
{
    private Player player;
    private BehaviourGraph graph;
    private Worker worker;
    private int possible_move;

    /**
     * make a turn and reset the graph status for player's graph
     * @param p player owner of the turn
     */
    public Turn(Player p) {
        this.player = p;
        this.graph = p.getGod().getGraph();
        this.worker = null;

        graph.resetExecutionStatus();
    }

    /**
     * Select worker for the selected turn
     * @param target index of player's Worker
     */
    public void selectWorker(int target) { this.worker = player.getWorkers().get(target);}

    /**
     * Remove selected worker
     */
    public void resetSelectedWorker() { this.worker = null; }

    /**
     * Getter of turn's worker
     * @return worker, null if a worker is not been selected for turn
     */
    public Worker getWorker(){
        return worker;
    }

    /**
     * Execute an action and get a result
     * @param id next action index
     * @param target position selected for Action
     * @param m game's map
     * @param globalConstrains global constraints in turn
     * @return int value : 0 if player can continue, greater 0 if player met a win condition, lower 0 if player met a lose condition
     * @throws NotAllowedMoveException if the action cannot be run due to wrong parameters
     * @throws OutOfGraphException if the actions id is wrong and no action exist for that id
     */
    public int runAction(int id, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException, OutOfGraphException {
        graph.selectAction(id);
        return graph.runSelectedAction(worker,target,m,globalConstrains);
    }

    /**
     * Get next actions from the last executed
     * get action's names and list of available cell for that action
     * @param m game's map
     * @param constraints game's constraints
     * @return ArrayList of NextAction from the graph's current node
     */
    public ArrayList<NextAction> getNextAction (Map m, GameConstraints constraints){
        return graph.getNextActions(worker,m,constraints);
    }

    /**
     * Get next actions from the last executed
     * get action's names and list of available cell for that action
     * @param w selected worker
     * @param m games'map
     * @param constraints game's constraints
     * @return ArrayList of NextAction from the graph's current node
     */
    public ArrayList<NextAction> getNextAction (Worker w, Map m, GameConstraints constraints){
        return graph.getNextActions(w,m,constraints);
    }

    /**
     * Check if there are possible move option for the current turn, starting from root
     * An option is valid if the worker can complete the turn without getting stuck
     * @param map game's map
     * @param gc game constraint
     * @return true if there are possible movement option, else false
     */
    public boolean canStillMove(Map map,GameConstraints gc) {
        possible_move = 0;
        for (Worker w : player.getWorkers()){
            canStillMoveNextAction(graph.getBehaviourNode(),w,map,gc);
        }
        return possible_move>0;
    }

    /**
     * Check from a node if it is a possible route (it has only possible movement for its actions until end of route)
     * @param node node to check
     * @param w selected worker for turn
     * @param map seleted map of game
     * @param gc selected game constraints
     */
    private void canStillMoveNextAction(BehaviourNode node, Worker w, Map map,GameConstraints gc) {

        if(node.getChildNodes().isEmpty()) possible_move++;

        for(BehaviourNode next : node.getChildNodes()){
            if(next.getAction().possibleCells(w,map,gc).size() > 0) canStillMoveNextAction(next,w,map,gc);
        }
    }

    /**
     * Check if a turn is ended
     * @return true if ended
     */
    public boolean isEnded() { return graph.isExecutionEnded(); }

}

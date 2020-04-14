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

        graph.resetExecutionStatus();
    }

    /**
     * Select worker for the selected turn
     * @param target index of player's Worker
     */
    public void selectWorker(int target) { this.worker = player.getWorkers().get(target);}


    /**
     * Execute an action and get a result
     * @param id next action index
     * @param target position selected for Action
     * @param m game's map
     * @param globalConstrains global constraints in turn
     * @return int value : 0 if player can continue, >0 if player met a win condition, <0 if player met a lose condition
     */
    public int runAction(int id, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException, OutOfGraphException {
        graph.selectAction(id);
        return graph.runSelectedAction(worker,target,m,globalConstrains);
    }

    /**
     * Get next actions from the last executed
     * information are action's names and list of available cell for that action
     * @param m game's map
     * @param constraints game's constraints
     * @return ArrayList of NextAction from the graph's current node
     */
    public ArrayList<NextAction> getNextAction (Map m, GameConstraints constraints){
        return graph.getNextActions(worker,m,constraints);
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
            canStillMoveNextAction(graph.getCurrent_node(),w,map,gc);
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

        if(node.getChild_nodes().isEmpty()) possible_move++;

        for(BehaviourNode next : node.getChild_nodes()){
            if(next.getAction().possibleCells(w,map,gc,next).size() > 0) canStillMoveNextAction(next,w,map,gc);
        }
    }

    /**
     * Check if a turn is ended
     * @return true if ended
     */
    public boolean isEnded() { return graph.isExecutionEnded(); }

}

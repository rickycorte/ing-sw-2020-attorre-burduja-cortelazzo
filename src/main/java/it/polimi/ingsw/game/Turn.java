package it.polimi.ingsw.game;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represent a turn in the match and holds his current state of progression
 */
public class Turn
{

    public static final int MAX_UNDO_MILLI = 5000;

    private Player player;
    private BehaviourGraph graph;
    private Worker worker;
    private int possible_move;

    private boolean allowUndo;
    private Vector2 lastTargetPosition;
    private Map oldMap;
    private List<Vector2> oldWorkerPositions;
    private java.util.Map<BehaviourNode, Instant> usedUndos;


    /**
     * Make a turn and reset the graph status for player's graph (undo is disabled)
     * @param p player owner of the turn
     */
    public Turn(Player p)
    {
        this(p, false);
    }


    /**
     * Make a turn and reset the graph status for player's graph and select if undo is enabled or not
     * @param p player owner of the turn
     * @param allowUndo pass true to enable undo of moves
     */
    public Turn(Player p, boolean allowUndo)
    {
        this.player = p;
        this.graph = p.getGod().getGraph();
        this.worker = null;
        this.allowUndo = false;
        lastTargetPosition = null;
        oldMap = null;
        this.allowUndo = allowUndo;
        oldWorkerPositions = new ArrayList<>();
        usedUndos = new java.util.HashMap<>();

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
    public void resetSelectedWorker()
    {
        this.worker = null;
    }

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
    public int runAction(int id, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException, OutOfGraphException
    {
        int res = 0;

        // undo enabled, it's appended at the end so its id is the same as next actions size
        if(allowUndo && worker != null && id == graph.getCurrentNode().getChildNodes().size())
        {

            if(isUndoTimerExpired(graph.getCurrentNode()))
            {
                throw new NotAllowedMoveException();
            }

            m.importMap(oldMap);      // rollback map to remove builds
            restoreWorkerPositions(m); // rollback positions

            //add current node to prevent an undo on older (or already undone) actions with MAX_UNDO_SECONDS + 1
            usedUndos.put(graph.getCurrentNode(), Instant.now().minusMillis(MAX_UNDO_MILLI +1));

            graph.rollback();
            // reset worker if we went back to root
            if(graph.isAtRoot())
                worker = null;

            //add old node to prevent an undo on older (or already undone) actions with MAX_UNDO_SECONDS + 1
            usedUndos.put(graph.getCurrentNode(), Instant.now().minusMillis(MAX_UNDO_MILLI +1));
        }
        else
        {
            if(allowUndo)
            {
                oldMap = new Map(m);  // save map backup for undo
                lastTargetPosition = target; // save pos for undo
                saveWorkerPositions(m); // save worker positions
            }

            graph.selectAction(id);
            res = graph.runSelectedAction(worker,target,m,globalConstrains);
        }

        return res;
    }


    /**
     * Get next actions from the last executed
     * get action's names and list of available cell for that action
     * @param w selected worker
     * @param m games'map
     * @param constraints game's constraints
     * @return ArrayList of NextAction from the graph's current node
     */
    public ArrayList<NextAction> getNextAction (Worker w, Map m, GameConstraints constraints)
    {
        var nextActions = graph.getNextActions(w,m,constraints);
        // append undo only if a worker is selected. Also check if already used for this action
        if(allowUndo && worker != null && !isUndoTimerExpired(graph.getCurrentNode()))
        {
            nextActions.add(new NextAction("Undo", worker, lastTargetPosition));
            // add only once
            if(!usedUndos.containsKey(graph.getCurrentNode()))
                usedUndos.put(graph.getCurrentNode(), Instant.now());
        }

        return nextActions;
    }

    /**
     * Get next actions using an already selected worker
     * get action's names and list of available cell for that action
     * @param m game's map
     * @param constraints game's constraints
     * @return ArrayList of NextAction from the graph's current node
     */
    public ArrayList<NextAction> getNextAction (Map m, GameConstraints constraints){
        return getNextAction(worker, m, constraints);
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
            canStillMoveNextAction(graph.getCurrentNode(),w,map,gc);
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


    /**
     * Save worker positions in a list
     * @param m map used to make a backup of workers
     */
    private void saveWorkerPositions(Map m)
    {
        oldWorkerPositions.clear();
        for (Worker w: m.getWorkers())
        {
            oldWorkerPositions.add(w.getPosition().copy());
        }
    }

    /**
     * Restore worker positions
     * @param m map where worker backup should be applied
     */
    private void restoreWorkerPositions(Map m)
    {
        for(int i=0; i < m.getWorkers().size(); i++)
        {
            m.getWorkers().get(i).setPosition(oldWorkerPositions.get(i));
        }
        oldWorkerPositions.clear();
    }


    /**
     * Return true if timer for undo is expired and undo should be rejected
     * @param node node to check
     * @return true is timer is expired
     */
    private boolean isUndoTimerExpired(BehaviourNode node)
    {
        // check if timer is expired
        if(usedUndos.containsKey(graph.getCurrentNode()))
        {
            var interval = Instant.now().minusMillis(usedUndos.get(graph.getCurrentNode()).toEpochMilli()).toEpochMilli();
            System.out.println("[UNDO] Time delta: "+ interval);
            if(interval > MAX_UNDO_MILLI)
                return true;
        }

        return false;
    }

}

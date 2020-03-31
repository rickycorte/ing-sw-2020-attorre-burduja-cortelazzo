package it.polimi.ingsw.game;

/**
 * Set of actions that describes a god action in the game.
 * You should not reuse a graph for multiple cards because this class stores the current execution progress of the actions.
 * No isolation for reuse is provided, share a graph at your own risk.
 */
public class BehaviourGraph
{
    private BehaviourNode root_node;
    private BehaviourNode current_node;
    private boolean alreadyRun;
    private boolean endReached;

    public BehaviourGraph()
    {
        root_node = BehaviourNode.makeRootNode(null);
        resetExecutionStatus();
    }

    /**
     * Reset the current use status of this ActionSequence to the root element
     * Should be called every turn start
     */
    public void resetExecutionStatus() {
        current_node = root_node;
        alreadyRun = true; // root node has no action
        endReached = false;
    }

    /**
     * Select one of the actions returned by getNextActions using array index
     * @param pos action index used to select the next operation (index obtained from action list)
     * @throws OutOfGraphException if there is no node with the specified index
     */
    public void selectAction(int pos) throws OutOfGraphException
    {
        current_node = current_node.getNextNode(pos);
        alreadyRun = false;
    }

    /**
     * Run the action selected with SelecAction, calling two times the same selected action has no effect
     * @param w target worker used in this action
     * @param target target position where the action should take place
     * @param m map where the action is executed
     * @param globalConstrains global game constrains that should be applied before action execution
     * @throws NotAllowedMoveException if and illegal move is detected
     */
    public void runSelectedAction(Worker w, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException
    {
        if(!isExecutionEnded() && !alreadyRun)
        {
            current_node.getAction().run(w,target, m, globalConstrains);
            alreadyRun = true;
        }

    }

    /**
     * Returns if the current execution of the graph is ended
     * @return true if execution is ended
     */
    public boolean isExecutionEnded() {
        return  current_node == null || current_node.getNextActionCount() <= 0;
    }

    /**
     * Returns next actions names in an array
     * This array index is used in SelectAction to select next move
     * @return and array of action names
     */
    public String[] getNextActionNames() {
        return current_node.getNextActionNames();
    }


    /**
     * Add a sub-graph to the current one starting from root
     * This function is used to create multiple choice at the beginning of a turn
     * @param node node to add to the graph
     * @return current graph
     */
    public BehaviourGraph appendSubGraph(BehaviourNode node) {
        if(node != null){
            root_node.addBranch(node);
        }

        return this;
    }

    /**
     * Generate a empty graph
     * Short version of new creation that can be used to concatenate graph creation
     * @return empty graph
     */
    public static BehaviourGraph makeEmptyGraph() {
        return new BehaviourGraph();
    }

}

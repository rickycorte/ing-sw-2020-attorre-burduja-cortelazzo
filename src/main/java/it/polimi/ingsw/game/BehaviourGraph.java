package it.polimi.ingsw.game;

import java.util.ArrayList;

/**
 * Set of actions that describes a god action in the game.
 * You should not reuse a graph for multiple cards because this class stores the current execution progress of the actions.
 * No isolation for reuse is provided, share a graph at your own risk.
 */
public class BehaviourGraph
{
    private BehaviourNode rootNode;
    private BehaviourNode currentNode;
    private BehaviourNode previousNode;
    private BehaviourNode undoNode;
    private boolean alreadyRun;

    public BehaviourGraph()
    {
        rootNode = BehaviourNode.makeRootNode(null);
        resetExecutionStatus();
    }

    /**
     * Reset the current use status of this ActionSequence to the root element
     * Should be called every turn start
     */
    public void resetExecutionStatus() {
        currentNode = rootNode;
        previousNode = rootNode;
        alreadyRun = true; // root node has no action
        undoNode = rootNode;
    }

    /**
     * Return the current node
     * @return return the current node
     */
    public BehaviourNode getCurrentNode() {
        return currentNode;
    }


    /**
     * Select one of the actions returned by getNextActions using array index
     * @param pos action index used to select the next operation (index obtained from action list)
     * @throws OutOfGraphException if there is no node with the specified index
     */
    public void selectAction(int pos) throws OutOfGraphException
    {
        currentNode = currentNode.getNextNode(pos);
        alreadyRun = false;
    }

    /**
     * Run the action selected with SelectAction, calling two times the same selected action has no effect
     * @param w target worker used in this action
     * @param target target position where the action should take place
     * @param m map where the action is executed
     * @param globalConstrains global game constrains that should be applied before action execution
     * @return int value : 0 if player can continue, greater 0 if player met a win condition, lower 0 if player met a lose condition
     * @throws NotAllowedMoveException if and illegal move is detected
     */
    public int runSelectedAction(Worker w, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException
    {
        try
        {
            if (!alreadyRun && currentNode.getAction() != null)
            {
                int res = currentNode.getAction().run(w, target, m, globalConstrains);
                alreadyRun = true;
                undoNode = previousNode; // save undo node before moving forward
                previousNode = currentNode;
                return res;
            }
            return -1; //break the game if errors happens here

        }catch (NotAllowedMoveException e){
            currentNode = previousNode; // move back in case of a wrong move
            throw e; // notify caller of the error
        }
    }

    /**
     * Returns if the current execution of the graph is ended
     * @return true if execution is ended
     */
    public boolean isExecutionEnded() {
        return currentNode.getNextActionCount() <= 0;
    }

    /**
     * Returns next actions(name and available cells)
     * @param w target worker to use to calculate next valid action
     * @param m current game map
     * @param constraints constraints to apply
     * @return ArrayList of NextAction from the current node
     */
    public ArrayList<NextAction> getNextActions(Worker w, Map m, GameConstraints constraints) {
        return currentNode.getNextActions(w,m,constraints);
    }

    /**
     * Returns next actions names in an array
     * This array index is used in SelectAction to select next move
     * @return and array of action names
     */
    public String[] getNextActionNames() {
        return currentNode.getNextActionNames();
    }


    /**
     * Add a sub-graph to the current one starting from root
     * This function is used to create multiple choice at the beginning of a turn
     * @param node node to add to the graph
     * @return current graph
     */
    public BehaviourGraph appendSubGraph(BehaviourNode node) {
        if(node != null){
            rootNode.addBranch(node);
        }

        return this;
    }

    /**
     * Move back to previous executed node
     * You can move back only once, multiple call of this function will always move to the same node
     */
    public void rollback()
    {
        currentNode = undoNode;
        alreadyRun = false;
    }

    /**
     * Return true if current node is the root of the graph
     * @return true if current node is root of the graph
     */
    public boolean isAtRoot()
    {
        return currentNode == rootNode;
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

package it.polimi.ingsw.game;


import java.util.ArrayList;
import java.util.List;

/**
 * This class holds and action an the next ones
 * Node of the behaviour graph.
 * The node action should not be changed after creation
 * It's also recommend to no use use graph build helpers to change the graph after it's creation
 */
public class BehaviourNode
{
    private Action action;

    private BehaviourNode parent;

    private List<BehaviourNode> childNodes;


    private BehaviourNode(Action action)
    {
        this(action, null);
    }

    private BehaviourNode(Action action, BehaviourNode parent)
    {
        this.action = action;
        childNodes = new ArrayList<>();
        this.parent = parent;
    }

    /**
     * Return current node children
     * @return children list
     */
    public List<BehaviourNode> getChildNodes() {
        return childNodes;
    }

    /**
     * Get the parent node
     * @return parent node
     */
    public BehaviourNode getParent()
    {
        return parent;
    }

    /**
     * Set the parent of this node
     * @param parent new parent node
     */
    public void setParent(BehaviourNode parent)
    {
        this.parent = parent;
    }

    /**
     * Return node action
     * @return node action
     */
    Action getAction(){
        return action;
    }

    /**
     * Return a list of next actions
     * @return list o next action names
     */
    ArrayList<NextAction> getNextActions(Worker w, Map m, GameConstraints constraints){
        ArrayList<NextAction> nodes = new ArrayList<>();

        for(BehaviourNode n : childNodes)
        {
            NextAction nextAction = new NextAction(w,m,constraints,n);

            //add only valid nodes with at least one possible position
            if(nextAction.getAvailable_position().size() > 0)
                nodes.add(nextAction);
        }

        return nodes;
    }

    /**
     * Return a list of next actions
     * @return list o next action names
     */
    String[] getNextActionNames(){
        ArrayList<String> nodes = new ArrayList<>();
        for(BehaviourNode n : childNodes)
        {
            nodes.add(n.getAction().displayName()); //use display name
        }

        return nodes.toArray(new String[0]);
    }

    /**
     * Select next action to execute
     * @param pos index of the next action
     * @return next action node
     * @throws OutOfGraphException if there is no node for the requested position
     */
    BehaviourNode getNextNode(int pos) throws OutOfGraphException
    {
        if(pos < 0 || pos >= childNodes.size() ) throw new OutOfGraphException();

        return  childNodes.get(pos);
    }

    /**
     * Get the number of next actions
     * @return number of next nodes
     */
    public int getNextActionCount(){
        return childNodes.size();
    }


    // **********************************************************************************************
    // factory helpers

    /**
     * Return a new node with the specified action and no parent
     * Short hand to new
     * @param act action to set to the node
     * @return new node
     */
    public static BehaviourNode makeRootNode(Action act){
        return new BehaviourNode(act);
    }

    /**
     * Create a new branch from the current node with an already existing node (== or)
     * @param node first node of the new branch
     * @return branch root node
     */
    public BehaviourNode addBranch(BehaviourNode node)
    {
        childNodes.add(node);
        node.setParent(this);
        return this;
    }

    /**
     * Create a new branch from an action
     * This functions create a new node for the action used
     * @param act action to use
     * @return branch root node
     */
    public BehaviourNode addBranch(Action act){
        return  addBranch(new BehaviourNode(act));
    }

    /**
     * Merge all branches that starts in the current node into the node passed as parameter
     * This function can be used to concatenate single branch items
     * @param node branch destination node
     * @return branch destination node
     */
    public BehaviourNode mergeBranches(BehaviourNode node){
        if(childNodes.size() > 0 )
        {
            for (int i = 0; i < childNodes.size(); i++)
            {
                childNodes.get(i).mergeBranches(node);
            }
        }
        else
        {
            setNext(node);
        }

        return  node;
    }

    /**
     * Merge all branches that starts in the current node into a new node with the specified action
     * @param act action to assign to destination node
     * @return branch destination node
     */
    public BehaviourNode mergeBranches(Action act){
        return  mergeBranches(new BehaviourNode(act));
    }

    /**
     * Set "forced" next node. This function deletes all current next nodes and sets only the parameter as next node
     * @param node next node to set
     * @return next node
     */
    public BehaviourNode setNext(BehaviourNode node)
    {
        node.setParent(this);
        childNodes.clear();
        childNodes.add(node);
        return node;
    }

    /**
     * Set "forced" next node. This function deletes all current next nodes and sets only the parameter as next node (generated with action)
     * @param act action to use in the new node
     * @return generated node
     */
    public BehaviourNode setNext(Action act){
        return setNext(new BehaviourNode(act));
    }

    /**
     * Return the current graph root (first element without parent)
     * @return root node
     */
    public BehaviourNode getRoot() {
        BehaviourNode itr = this;
        while(itr.getParent() != null)
            itr = itr.getParent();

        return itr;
    }

}

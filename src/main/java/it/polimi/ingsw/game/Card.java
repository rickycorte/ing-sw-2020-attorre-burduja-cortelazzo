package it.polimi.ingsw.game;

/**
 * This class represent a card an holds a name, a id and a graph.
 * The id of the card is used to identify it in the network messages therefore should be unique for every card
 * The behaviour graph represents the card actions on the game
 * it's guarantee that every card has its onw isolated graph (good for multi threading)
 */
public class Card {
    final private int id;
    final private String name;
    final private BehaviourGraph graph;
    private GameConstraints.Constraint constraintToClear;


    /**
     * Create a new god card
     * @param id god id used by this card, this is required to be unique to ensure correct god picks and power usages during gameplay
     * @param name name of the god
     * @param graph behaviour graph used to execute the god turn
     */
    public Card(int id, String name, BehaviourGraph graph) {
        this.id = id;
        this.name = name;
        this.graph = graph;
        constraintToClear = GameConstraints.Constraint.NONE;
    }

    /**
     * Return the card ID
     * @return card ID
     */
    public int getId()
    {
        return id;
    }

    /**
     * Return the card name
     * @return card name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Return card behaviour graph
     * @return behaviour graph
     */
    public BehaviourGraph getGraph()
    {
        return graph;
    }

    /**
     * Get the constraint that should be cleared when this card is removed from the game
     * @return constraint to remove
     */
    public GameConstraints.Constraint getConstraintToClear()
    {
        return constraintToClear;
    }

    /**
     * Set the constraint that should be cleared when this card is removed form the game
     * @param constraintToClear constraint to clear
     */
    public void setConstraintToClear(GameConstraints.Constraint constraintToClear)
    {
        this.constraintToClear = constraintToClear;
    }

}

package it.polimi.ingsw.game;

/**
 * This class represent a card an holds a name, a id and a graph.
 * The id of the card is used to identify it in the network messages therefore should be unique for every card
 * The behaviour graph represents the card actions on the game
 * it's guarantee that every card has its onw isolated graph (good for multi threading)
 */
public class Card {
    private int id;
    private String name;
    private transient BehaviourGraph graph;


    public Card(int id, String name, BehaviourGraph graph) {
        this.id = id;
        this.name = name;
        this.graph = graph;
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
     * Set the card behaviour graph
     * @param graph new behaviour graph
     */
    public void setGraph(BehaviourGraph graph) { this.graph = graph; }
}

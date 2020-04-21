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

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public BehaviourGraph getGraph()
    {
        return graph;
    }

    public void setGraph(BehaviourGraph graph) { this.graph = graph; }
}

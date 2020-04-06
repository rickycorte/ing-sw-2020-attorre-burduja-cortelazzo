package it.polimi.ingsw.game;

public class Card {
    private int id;
    private String name;
    private BehaviourGraph graph;


    public Card(int id, String name, BehaviourGraph graph) {
        this.id = id;
        this.name = name;
        this.graph = graph;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public int runSelectedAction(int id, Worker worker, Vector2 vector2, Map map, GameConstraints constraint){
        //TODO
        return 0;
    }

    public boolean isTurnEnded(){
        //
        return false;
    }

    public String[] getNextActionArray(){
        return null;
    }
}

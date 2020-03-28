package it.polimi.ingsw.game;
import java.util.ArrayList;

public class Player
{
    private int id;
    private String username;
    private Card god;
    private ArrayList<Worker> workers;

    public Player (int id, String username){
        this.id = id;
        this.username = username;
    }

    public int getId() { return id; }

    public void setGod(Card god){
        this.god = god;
    }

    public Card getGod(){
        return this.god;
    }

    public String getUsername(){
        return this.username;
    }

    public ArrayList<Worker> getWorkers(){
        return this.workers;
    }
}

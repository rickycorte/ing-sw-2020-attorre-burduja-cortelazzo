package it.polimi.ingsw.game;
import java.util.ArrayList;
import java.util.Objects;

public class Player
{
    private int id;
    private String username;
    private Card god;
    private transient ArrayList<Worker> workers;

    public Player (int id, String username){
        this.id = id;
        this.username = username;
        workers = new ArrayList<>();
        god = null;
    }

    public void playerAfterSave (){
        this.workers = new ArrayList<>();
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

    public void addWorker(Worker worker) {
        this.workers.add(worker);
    }

    /**
     * Remove player god and workers, should be called when player joins a game
     */
    public void clear()
    {
        god = null;
        workers = new ArrayList<>();
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return username.equals(player.username);
    }
}

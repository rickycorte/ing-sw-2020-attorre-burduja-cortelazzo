package it.polimi.ingsw.game;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This class represents a player logged into a game and holds all the data needed to run game actions
 * and network operations
 */
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

    /**
     * Return current player id
     * @return player id
     */
    public int getId() { return id; }

    /**
     * Set player god that will be used in the game
     * @param god got do use
     */
    public void setGod(Card god){
        this.god = god;
    }

    /**
     * Return the current player god
     * @return current selected god
     */
    public Card getGod(){
        return this.god;
    }

    /**
     * Return player username
     * @return player username
     */
    public String getUsername(){
        return this.username;
    }

    /**
     * Return player workers
     * @return list of player workers
     */
    public ArrayList<Worker> getWorkers(){
        return this.workers;
    }

    /**
     * Add a worker to this player
     * @param worker worker to add
     */
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

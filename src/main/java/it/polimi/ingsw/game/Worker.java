package it.polimi.ingsw.game;

/**
 * This class represent a worker of the game that can be placed in the map and used to execute turns
 */
public class Worker
{
    private int id;
    private Player owner;
    private Vector2 position;

    private transient Vector2 lastLocation;
    private transient Vector2 lastBuildLocation;

    public Worker(Player owner){
        this(-1, owner, new Vector2(0,0));
    }

    public Worker(int id, Player owner, Vector2 position){
        this.id = id;
        this.owner = owner;
        this.position = position;
        this.lastBuildLocation = null;
        this.lastLocation = null;
    }


    /**
     * @return current worker position
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * @return player that owns this worker
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * @param pos move a worker into a new position
     */
    public void setPosition(Vector2 pos){
        this.position = pos;
    }

    /**
     * @return worker id relative to
     */
    public int getId() {
        return id;
    }

    /**
     * Set the worker owner
     * @param owner new player that owns this worker
     */
    public void setOwner(Player owner) { this.owner = owner; }

    /**
     * @return return last worker position, null if no move has been done
     */
    public Vector2 getLastLocation()
    {
        return lastLocation;
    }

    /**
     * Update the last position of the worker after a move action
     * @param lastLocation new location of the worker
     */
    public void setLastLocation(Vector2 lastLocation)
    {
        this.lastLocation = lastLocation;
    }

    /**
     * @return get last position where a worker build something, null if no build has been done
     */
    public Vector2 getLastBuildLocation()
    {
        return lastBuildLocation;
    }

    /**
     * Update the last position where the worker build something
     * @param lastBuildLocation last build position
     */
    public void setLastBuildLocation(Vector2 lastBuildLocation)
    {
        this.lastBuildLocation = lastBuildLocation;
    }

    /**
     * Compare two workers, they are the same if id, position and owner are the same
     * @param o worker to compare
     * @return true if equals
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Worker worker = (Worker) o;
        return id == worker.id &&
                owner.equals(worker.owner) &&
               position.equals(worker.position);
    }


    /**
     * Clear last locations for this worker
     * Locations should be cleared at the beginning of the turn
     */
    public void clearLastLocations()
    {
        lastBuildLocation = null;
        lastLocation = null;
    }

}

package it.polimi.ingsw.game;
import java.util.ArrayList;

/**
 * Game map representation
 */
public class Map
{
    /**
     * Map height
     */
    public static final int HEIGHT = 5;
    /**
     * Map width
     */
    public static final int LENGTH = 5;
    /**
     * Dome representation value, useful for client's
     */
    public static final int DOME_VALUE = 128;

    private int[][] map = new int[HEIGHT][LENGTH];

    private ArrayList<Worker> workers;

    /*
     *          00000001 = 1 liv.0
     *          00000010 = 2 liv.1
     *          00000100 = 4 liv.2
     *          00001000 = 8 liv.3
     *          00010000 = 16 liv.4 ( when reached becomes a dome)
     *
     *          10000001 = 128 + 1 liv.0(dome)
     *          10000010 = 128 + 2 liv.1(dome)
     *          10000100 = 128 + 4 liv.2(dome)
     *          10001000 = 128 + 8 liv.3(dome)
     *          10010000 = 128 + 16 liv.4(dome) completed cell
     */

    /**
     * Initialize the map to level zero
     */
    public Map(){
        workers = new ArrayList<>();
        for (int i = 0; i<LENGTH ; i++)
            for (int j = 0; j<HEIGHT; j++)
                map[i][j] = 1;
    }

    /**
     * Copy constructor
     * @param other copied map
     */
    public Map(Map other){
        workers = new ArrayList<>();
        workers.addAll(other.workers);
        for (int i = 0; i<LENGTH ; i++)
            for (int j = 0; j<HEIGHT; j++)
                map[i][j] = other.map[i][j];
    }

    public int[][] getMap() { return map; }

    /**
     * Increases level of selected cell, if level = 4 becomes a dome
     * @param pos selected cell
     * @return if build is successful
     */
    public boolean build(Vector2 pos)
    {

        if(isInsideMap(pos) && !isCellDome(pos))
        {
            map[pos.getX()][pos.getY()] = map[pos.getX()][pos.getY()] << 1;

            if (getLevel(pos) == 4)
                map[pos.getX()][pos.getY()] = map[pos.getX()][pos.getY()] + DOME_VALUE;

            return true;
        }

        return false;
    }

    /**
     * Builds a dome in the selected cell, any level is valid
     * @param pos selected cell
     * @return true if build is successful
     */
    public boolean buildDome(Vector2 pos)
    {
        if(isInsideMap(pos) && !isCellDome(pos))
        {
            map[pos.getX()][pos.getY()] = map[pos.getX()][pos.getY()] << 1;
            map[pos.getX()][pos.getY()] = map[pos.getX()][pos.getY()] + DOME_VALUE;
            return true;
        }

        return false;
    }

    /**
     *
     * @param pos selected cell
     * @return selected cell height, -1 if it is not in map
     */
    public int getLevel(Vector2 pos) {
        if(isInsideMap(pos))
            return Integer.numberOfTrailingZeros(map[pos.getX()][pos.getY()]);
        return -1;
    }

    /**
     * Check if selected cell has a dome, any level is valid
     * @param pos selected cell
     * @return true if dome, false if is not in map or is not dome
     */
    public boolean isCellDome (Vector2 pos){
        if(isInsideMap(pos)) return map[pos.getX()][pos.getY()] > DOME_VALUE;
        return false;
    }

    /**
     * Check if selected cell is a valid position in the map
     * @param pos selected cell
     * @return true if is valid
     */
    public boolean isInsideMap(Vector2 pos){
        return pos.getX() < HEIGHT && pos.getX() >= 0 && pos.getY() >= 0 && pos.getY() < LENGTH;
    }

    /**
     * Add in map's workers list all the element of the player's workers list
     * @param player selected player
     */
    public void setWorkers(Player player){
        workers.addAll(player.getWorkers());
    }

    public ArrayList<Worker> getWorkers(){
        return workers;
    }

    /**
     * Return a worker in a specified position, null if none is found
     * No check on position is performed, a wrong position will always return null
     * @param position position to search
     * @return worker found or null if not found
     */
    public Worker getWorker(Vector2 position)
    {
        for (Worker worker : workers) {
            if (worker.getPosition().equals(position))
                return worker;
        }
        return null;
    }

    /**
     * Remove from map's workers list all the element of the player's workers list
     * Remove also from player its workers
     * @param player selected player
     */
    public void removeWorkers (Player player){
        workers.removeAll(player.getWorkers());
        player.getWorkers().removeAll(player.getWorkers());
    }
    /**
     * Check if a worker is in the selected cell
     * @param pos selected cell
     * @return true if a worker is in the selected cell, false if pos is not in map
     */
    public boolean isCellEmpty (Vector2 pos){
        if(isInsideMap(pos)) {
            for (Worker worker : workers) {
                if (worker.getPosition().getX() == pos.getX() && worker.getPosition().getY() == pos.getY()) return false;
            }
            return true;
        }
        return true;
    }

    /**
     * check every position of the map and return the cell without worker
     * @return list of all Vector2 element with no Worker on it
     */
    public ArrayList<Vector2> cellWithoutWorkers(){
        ArrayList<Vector2> free =  new ArrayList<>();
        Vector2 pos;
        for (int i = 0; i<LENGTH ; i++)
            for (int j = 0; j<HEIGHT; j++){
                pos = new Vector2(i,j);
                if(isCellEmpty(pos)) free.add(pos);
            }

        return free;
    }

}







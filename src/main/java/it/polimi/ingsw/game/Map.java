package it.polimi.ingsw.game;
import java.util.ArrayList;

/**
 * Game map representation
 */
public class Map
{
    private static final int HEIGHT = 7;
    private static final int LENGTH = 7;

    private int[][] map = new int[HEIGHT][LENGTH];

    private ArrayList<Worker> workers = new ArrayList<>() ;

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
        for (int i = 0; i<LENGTH ; i++)
            for (int j = 0; j<HEIGHT; j++)
                map[i][j] = 1;
    }

    /**
     * Increases level of selected cell, if level = 4 becomes a dome
     * @param pos selected cell
     * @throws CellCompletedException selected cell has already reached dome level
     * @throws OutOfMapException selected cell is not in map
     */
    public void build(Vector2 pos) throws CellCompletedException, OutOfMapException {

        if(isInsideMap(pos)) {
            if (isCellDome(pos)) throw new CellCompletedException();

            map[pos.getX()][pos.getY()] = map[pos.getX()][pos.getY()] << 1;

            if (getLevel(pos) == 4) map[pos.getX()][pos.getY()] = map[pos.getX()][pos.getY()] + 128;
        }
    }

    /**
     * Builds a dome in the selected cell, any level is valid
     * @param pos selected cell
     * @throws OutOfMapException selected cell is not in map
     * @throws CellCompletedException selected cell was already a dome
     */
    public void buildDome(Vector2 pos) throws OutOfMapException, CellCompletedException{
        if(isInsideMap(pos)){
            if(!isCellDome(pos)){
                map[pos.getX()][pos.getY()] = map[pos.getX()][pos.getY()] << 1;
                map[pos.getX()][pos.getY()] = map[pos.getX()][pos.getY()] + 128;
            }else
                throw new CellCompletedException();
        }
    }

    /**
     *
     * @param pos selected cell
     * @return selected cell height
     * @throws OutOfMapException selected cell is not in map
     */
    public int getLevel(Vector2 pos) throws OutOfMapException {
        if(isInsideMap(pos))
            return Integer.numberOfTrailingZeros(map[pos.getX()][pos.getY()]);
        return -1; //return value in case of exception OutOfMap
    }

    /**
     * Check if selected cell has a dome, any level is valid
     * @param pos selected cell
     * @return true if dome
     * @throws OutOfMapException selected cell is not in map
     */
    public boolean isCellDome (Vector2 pos) throws OutOfMapException{
        if(isInsideMap(pos)) return map[pos.getX()][pos.getY()] > 128;
        return false; //return value in case of exception OutOfMap
    }

    /**
     * Check if selected cell is a valid position in the map
     * @param pos selected cell
     * @return true if is valid
     * @throws OutOfMapException selected cell is not in map
     */
    private boolean isInsideMap(Vector2 pos) throws OutOfMapException{

        if (pos.getX() >= HEIGHT || pos.getX() < 0 || pos.getY() < 0 || pos.getY() >= LENGTH)  throw new OutOfMapException();

        return true;
    }

    /**
     * Add in map's workers list all the element of the player's workers list
     * @param player selected player
     */
    public void setWorkers(Player player){
        workers.addAll(player.getWorkers());
    }

    /**
     * Remove from map's workers list all the element of the player's workers list
     * @param player selected player
     */
    public void removeWorkers (Player player){
        workers.removeAll(player.getWorkers());
    }

    /**
     * Check if a worker is in the selected cell
     * @param pos selected cell
     * @return true if a worker is in the selected cell
     * @throws OutOfMapException selected cell is not in map
     */
    public boolean isCellEmpty (Vector2 pos) throws OutOfMapException{
        if(isInsideMap(pos)) {
            for (Worker worker : workers) {
                if (worker.getPos().getX() == pos.getX() && worker.getPos().getY() == pos.getY()) return false;
            }
            return true;
        }
        return false; //return value in case of exception OutOfMap
    }

}







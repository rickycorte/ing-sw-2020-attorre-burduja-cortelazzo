package it.polimi.ingsw.controller.compact;

import it.polimi.ingsw.game.Map;
import it.polimi.ingsw.game.Worker;

/**
 * This is a data only class used to send the game map
 * over the network
 */
public class CompactMap
{
    int[][] map;
    CompactWorker[] workers;


    public CompactMap(Map gmap)
    {
        map = gmap.getMap().clone();

        workers = new CompactWorker[gmap.getWorkers().size()];
        for(int i=0; i < workers.length; i++)
        {
            workers[i] = new CompactWorker(gmap.getWorkers().get(i));
        }

    }

    /**
     * Get level of a cell in the map
     * @param x cell x
     * @param y cell y
     * @return level of the selected cell
     */
    public int getLevel(int x, int y)
    {
        return Integer.numberOfTrailingZeros(map[x][y]);
    }

    /**
     * Check if a cell is a dome or not
     * @param x cell x
     * @param y cell y
     * @return true if cell is dome
     */
    public boolean isDome(int x, int y)
    {
        return map[x][y] > Map.DOME_VALUE;
    }

    /**
     * Get workers placed in the map
     * @return
     */
    public CompactWorker[] getWorkers()
    {
        return workers;
    }

    /**
     * Get map height
     * @return map height
     */
    public int getHeight()
    {
        return Map.HEIGHT;
    }

    /**
     * Get map length
     * @return map length
     */
    public int getLength()
    {
        return Map.LENGTH;
    }
}

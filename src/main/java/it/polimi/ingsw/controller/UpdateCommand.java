package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.compact.CompactMap;
import it.polimi.ingsw.game.Map;
import it.polimi.ingsw.game.Vector2;

/**
 * This command is used only as an information method
 * that updates the clients with a new state of the game map is calculated
 * (Server)
 * Send a new map state to the clients
 * (Client)
 * A new map state is received and should be processed
 */
public class UpdateCommand extends BaseCommand {
    private CompactMap map;

    /**
     * (Server) Create a new map update to send to the clients
     * @param sender sender id
     * @param target receiver if of the command (should be broadcast)
     * @param map new map state
     */
    public UpdateCommand(int sender, int target, Map map){
        super(sender,target);
        this.map = new CompactMap(map);
    }


    /**
     * Get the new map sent by th server
     * @return new map state
     */
    public CompactMap getUpdatedMap()
    {
        return map;
    }

    /**
     * (Server) Create a new map update to send to the clients
     * @param sender sender id
     * @param target receiver if of the command (should be broadcast)
     * @param map new map state
     * @return wrapped command ready to be sent over the network
     */
    public static CommandWrapper makeWrapped(int sender, int target, Map map)
    {
        return new CommandWrapper(CommandType.UPDATE, new UpdateCommand(sender, target, map));
    }



    //----------------------------------------------------------------------------------------------------------------------------------------

    /**
     * @deprecated use {@link #getUpdatedMap()}
     */
    @Deprecated
    public int[] getIntData() {
        return mapToArray(map);
    }

    /**
     * @deprecated use {@link #getUpdatedMap()}
     */
    @Deprecated
    public Vector2[] getV2Data() {
        return workerToArray(map);
    }

    /**
     * @deprecated use {@link #getUpdatedMap()}
     */
    @Deprecated
    public int[] getWorkersInfo(){ return workersInfo(map); }

    /**
     * utility method
     * @param map map to convert
     * @return first [HEIGHT*LENGTH] are map int value, then pairs (worker owner id, worker id)
     */
    @Deprecated
    private int[] mapToArray(CompactMap map){
        int[] intMap = new int[(Map.LENGTH * Map.LENGTH)+(map.getWorkers().length*2)];
        for(int x = 0, i = 0; i < Map.LENGTH; i++){
            for(int j = 0; j < Map.HEIGHT ; j++){
                intMap[x] = 1 << map.getLevel(i,j);
                intMap[x] += map.isDome(i,j) ? Map.DOME_VALUE : 0;
                x++;
            }
        }

        for(int i = Map.HEIGHT * Map.LENGTH, j = 0; j<map.getWorkers().length;i=i+2,j++){
            intMap[i] = map.getWorkers()[j].getOwnerID();
            intMap[i+1] = map.getWorkers()[j].getWorkerID();
        }

        return intMap;
    }

    /**
     * utility method
     * @param map map to convert
     * @return array composed of Vector2 representing worker's positions
     */
    @Deprecated
    private Vector2[] workerToArray(CompactMap map){
        Vector2[] vector = new Vector2[map.getWorkers().length];
        for(int i=0; i<map.getWorkers().length;i++)
            vector[i] = map.getWorkers()[i].getPosition();

        return vector;
    }

    /**
     * Utility method, gets all the information about the workers of a given map and puts it in an array
     * @param map map to get the worker's info from
     * @return an array of ints. interpret as blocks of 4 [worker_owner_id, worker_id, worker_x_pos, worker_y_pos]
     */
    @Deprecated
    private int[] workersInfo(CompactMap map){
        int[] workers_info = new int[map.getWorkers().length * 4];
        int nHandledWorker = 0;
        for(int i = 0; i < (map.getWorkers().length * 4) ; i = i + 4){
            workers_info[i] = map.getWorkers()[nHandledWorker].getOwnerID();
            workers_info[i + 1] = map.getWorkers()[nHandledWorker].getWorkerID();
            workers_info[i + 2] = map.getWorkers()[nHandledWorker].getPosition().getX();
            workers_info[i + 3] = map.getWorkers()[nHandledWorker].getPosition().getY();
            nHandledWorker++;
        }
        return workers_info;
    }
}

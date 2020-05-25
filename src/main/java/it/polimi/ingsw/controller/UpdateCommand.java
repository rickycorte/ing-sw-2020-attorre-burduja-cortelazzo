package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Map;
import it.polimi.ingsw.game.Vector2;

/**
 * This command is used only as an information method
 * that updates the clients with a new state of the game map is calculated
 */
public class UpdateCommand extends BaseCommand {
    private int[] mapWorkerPair;
    private Vector2[] workerPos;
    private int[] workersInfo;

    //to client
    public UpdateCommand(int sender, int target, Map map){
        super(sender,target);
        this.mapWorkerPair = mapToArray(map);
        this.workerPos = workerToArray(map);
        this.workersInfo = workersInfo(map);
    }

    public int[] getIntData() {
        return mapWorkerPair;
    }

    public Vector2[] getV2Data() {
        return workerPos;
    }

    public int[] getWorkersInfo(){return workersInfo;}

    /**
     * utility method
     * @param map map to convert
     * @return first [HEIGHT*LENGTH] are map int value, then pairs (worker owner id, worker id)
     */
    private int[] mapToArray(Map map){
        int[] intMap = new int[(Map.LENGTH * Map.LENGTH)+(map.getWorkers().size()*2)];
        for(int x = 0, i = 0; i < Map.LENGTH; i++){
            for(int j = 0; j < Map.HEIGHT ; j++){
                intMap[x] = map.getMap()[i][j];
                x++;
            }
        }

        for(int i = Map.HEIGHT * Map.LENGTH, j = 0; j<map.getWorkers().size();i=i+2,j++){
            intMap[i] = map.getWorkers().get(j).getOwner().getId();
            intMap[i+1] = map.getWorkers().get(j).getId();
        }

        return intMap;
    }

    /**
     * utility method
     * @param map map to convert
     * @return array composed of Vector2 representing worker's positions
     */
    private Vector2[] workerToArray(Map map){
        Vector2[] vector = new Vector2[map.getWorkers().size()];
        for(int i=0; i<map.getWorkers().size();i++)
            vector[i] = map.getWorkers().get(i).getPosition();

        return vector;
    }

    /**
     * Utility method, gets all the information about the workers of a given map and puts it in an array
     * @param map map to get the worker's info from
     * @return an array of ints. interpret as blocks of 4 [worker_owner_id, worker_id, worker_x_pos, worker_y_pos]
     */
    private int[] workersInfo(Map map){
        int[] workers_info = new int[map.getWorkers().size() * 4];
        int nHandledWorker = 0;
        for(int i = 0; i < (map.getWorkers().size() * 4) ; i = i + 4){
            workers_info[i] = map.getWorkers().get(nHandledWorker).getOwner().getId();
            workers_info[i + 1] = map.getWorkers().get(nHandledWorker).getId();
            workers_info[i + 2] = map.getWorkers().get(nHandledWorker).getPosition().getX();
            workers_info[i + 3] = map.getWorkers().get(nHandledWorker).getPosition().getY();
            nHandledWorker++;
        }
        return workers_info;
    }
}

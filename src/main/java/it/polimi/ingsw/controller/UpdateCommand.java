package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Map;
import it.polimi.ingsw.game.Vector2;

public class UpdateCommand extends BaseCommand {
    private int[] mapWorkerPair;
    private Vector2[] workerPos;

    //to client
    public UpdateCommand(int sender, int target, Map map){
        super(sender,target);
        this.mapWorkerPair = mapToArray(map);
        this.workerPos = workerToArray(map);
    }

    public int[] getIntData() {
        return mapWorkerPair;
    }

    public Vector2[] getV2Data() {
        return workerPos;
    }

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
}

package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Vector2;

import java.util.ArrayList;
import java.util.List;

public class WorkerPlaceCommand extends BaseCommand {
    private Vector2[] positions;

    //to client
    public WorkerPlaceCommand(int sender, int target, List<Vector2> cellWithoutWorker) {
        super( sender, target);
        this.positions = cellWithoutWorker.toArray(new Vector2[0]);
    }

    //to server
    public WorkerPlaceCommand(int sender, int target, Vector2[] positions) {
        super(sender, target);
        this.positions = positions;
    }

    public Vector2[] getPositions() {
        return positions;
    }

    public List<Integer> getPositionsIndexes(){
        List<Integer> availableIndexes = new ArrayList<>();
        for(int i = 0; i<positions.length; i++){
            availableIndexes.add(i);
        }
        return availableIndexes;
    }
}

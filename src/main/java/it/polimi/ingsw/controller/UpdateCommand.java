package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Vector2;

public class UpdateCommand extends BaseCommand {
    private int[] mapWorkerPair;
    private Vector2[] workerPos;

    //to client
    public UpdateCommand(int type, boolean request, int sender, int target, int[] mapWorkerPair, Vector2[] workerPos){
        super(type,request,sender,target);
        this.mapWorkerPair = mapWorkerPair;
        this.workerPos = workerPos;
    }

    public int[] getIntData() {
        return mapWorkerPair;
    }

    public Vector2[] getV2Data() {
        return workerPos;
    }
}

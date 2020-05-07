package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Vector2;

public class WorkerPlaceCommand extends BaseCommand {
    Vector2[] positions;

    //to client
    public WorkerPlaceCommand(int type, boolean request, int sender, int target, Vector2[] positions) {
        super(type, request, sender, target);
        this.positions = positions;
    }

    //to server
    public WorkerPlaceCommand(int type, boolean request, int sender, int target, Vector2 positions) {
        super(type, request, sender, target);
        this.positions = new Vector2[]{positions};
    }

    public Vector2[] getPositions() {
        return positions;
    }
}

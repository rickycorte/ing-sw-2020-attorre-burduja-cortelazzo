package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Vector2;

public class ActionCommand extends BaseCommand {
    private  int[] idWorkerNMove;
    private Vector2[] availablePos;
    private String[] actionName;

    //to client
    public ActionCommand(int type, boolean request, int sender, int target,int[] idWorkerNMove,Vector2[] availablePos,String[] actionName) {
        super(type, request, sender, target);
        this.idWorkerNMove = idWorkerNMove;
        this.availablePos = availablePos;
        this.actionName = actionName;
    }

    //to server
    public ActionCommand(int type, boolean request, int sender, int target, int[] workerAndAction, Vector2 selectedPos){
        super(type, request, sender, target);
        this.idWorkerNMove = workerAndAction;
        this.availablePos = new Vector2[]{selectedPos};
        this.actionName = null;
    }

    public int[] getIdWorkerNMove() {
        return idWorkerNMove;
    }

    public Vector2[] getAvaialablePos() {
        return availablePos;
    }

    public String[] getActionName() {
        return actionName;
    }
}

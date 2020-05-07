package it.polimi.ingsw.controller;

public class PickGodCommand extends BaseCommand {
    private int[] godsID;

    //to client
    public PickGodCommand(int type, boolean request, int sender, int target,int[] godsID) {
        super(type, request, sender, target);
        this.godsID = godsID;
    }

    //to server
    public PickGodCommand(int type, boolean request, int sender, int target,int godID) {
        super(type, request, sender, target);
        this.godsID = new int[]{godID};
    }

    public int[] getGodID() {
        return godsID;
    }

}

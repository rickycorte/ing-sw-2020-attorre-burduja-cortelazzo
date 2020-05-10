package it.polimi.ingsw.controller;

public class PickGodCommand extends BaseCommand {
    private int[] godsID;

    //to client
    public PickGodCommand(int sender, int target,int[] godsID) {
        super(sender, target);
        this.godsID = godsID;
    }

    //to server
    public PickGodCommand(int sender, int target,int godID) {
        super(sender, target);
        this.godsID = new int[]{godID};
    }

    public int[] getGodID() {
        return godsID;
    }

}

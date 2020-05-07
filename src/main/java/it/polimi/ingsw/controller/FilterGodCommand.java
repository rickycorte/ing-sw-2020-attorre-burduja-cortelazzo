package it.polimi.ingsw.controller;

public class FilterGodCommand extends BaseCommand {
    private int[] godsID;

    //to client
    public FilterGodCommand(int type, boolean request, int sender, int target) {
        super(type, request, sender, target);
        this.godsID = null;
    }

    //to server
    public FilterGodCommand(int type, boolean request, int sender, int target,int[] godsID) {
        super(type, request, sender, target);
        this.godsID = godsID;
    }

    public int[] getGodID() {
        return godsID;
    }

}

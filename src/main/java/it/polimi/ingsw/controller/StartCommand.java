package it.polimi.ingsw.controller;

public class StartCommand extends BaseCommand {
    private int[] playersID;


    //to client
    public StartCommand(int type, boolean request, int sender, int target,int[] playersID) {
        super(type, request, sender, target);
        this.playersID = playersID;
    }

    //to server
    public StartCommand(int type, boolean request, int sender, int target) {
        super(type, request, sender, target);
        this.playersID = null;
    }


    public int[] getPlayersID() {
        return playersID;
    }

}

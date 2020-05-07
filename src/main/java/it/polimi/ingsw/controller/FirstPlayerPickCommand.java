package it.polimi.ingsw.controller;

public class FirstPlayerPickCommand extends BaseCommand {
    private int[] playersID;
    private String[] usernames;

    //to client
    public FirstPlayerPickCommand(int type, boolean request, int sender, int target, int[] playersID, String[] usernames) {
        super(type, request, sender, target);
        this.playersID = playersID;
        this.usernames = usernames;
    }

    //to server
    public FirstPlayerPickCommand(int type, boolean request, int sender, int target, int playerID) {
        super(type, request, sender, target);
        this.playersID = new int[]{playerID};
        this.usernames = null;
    }

    public int[] getPlayersID() {
        return playersID;
    }

    public String[] getUsernames() {
        return usernames;
    }

}

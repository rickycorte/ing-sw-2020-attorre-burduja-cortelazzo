package it.polimi.ingsw.controller;

public class JoinCommand extends BaseCommand {
    private String username;
    private boolean isJoin;

    public JoinCommand(int sender, int target, boolean isSuccess){
        //UPDATE FROM SERVER
        super(sender,target);
        this.username = null;
        this.isJoin = isSuccess;
    }

    public JoinCommand(int sender, int target, String username,boolean isJoin) {
        //REQUEST FROM CLIENT
        super(sender, target);
        this.username = username;
        this.isJoin = isJoin;
    }

    public String getUsername() {
        return username;
    }

    public boolean isJoin() {
        return isJoin;
    }
}

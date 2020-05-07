package it.polimi.ingsw.controller;

public class JoinCommand extends BaseCommand {
    private String username;
    private boolean isJoin;

    //to client
    public JoinCommand(int type,boolean request, int sender, int target, boolean isSuccess){
        super(type,request,sender,target);
        this.username = null;
        this.isJoin = isSuccess;
    }

    //to server
    public JoinCommand(int type, boolean request, int sender, int target, String username,boolean isJoin) {
        super(type, request, sender, target);
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

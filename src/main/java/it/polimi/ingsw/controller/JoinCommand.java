package it.polimi.ingsw.controller;

public class JoinCommand extends BaseCommand {
    private String username;
    private boolean isJoin;
    private boolean validUsername;         //Flag in case username is not valid

    public JoinCommand(int sender, int target, boolean isSuccess, boolean validUsername){
        //UPDATE FROM SERVER
        super(sender,target);
        this.username = null;
        this.isJoin = isSuccess;
        this.validUsername = validUsername;
    }

    public JoinCommand(int sender, int target, String username,boolean isJoin) {
        //REQUEST FROM CLIENT
        super(sender, target);
        this.username = username;
        this.isJoin = isJoin;
        this.validUsername = true;
    }

    public String getUsername() {
        return username;
    }

    public boolean isJoin() {
        return isJoin;
    }

    public boolean isValidUsername(){ return validUsername;
    }
}

package it.polimi.ingsw.controller;

public class EndGameCommand extends BaseCommand {
    boolean isWinner;

    //to client
    public EndGameCommand(int type, boolean request, int sender, int target, boolean isWinner) {
        super(type, request, sender, target);
        this.isWinner = isWinner;
    }

    public boolean isWinner() {
        return isWinner;
    }
}

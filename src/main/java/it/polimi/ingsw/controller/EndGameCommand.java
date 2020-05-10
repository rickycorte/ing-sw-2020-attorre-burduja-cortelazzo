package it.polimi.ingsw.controller;

public class EndGameCommand extends BaseCommand {
    private boolean isWinner;

    public EndGameCommand(int sender, int target, boolean isWinner) {
        //FROM SERVER
        super(sender, target);
        this.isWinner = isWinner;
    }

    public boolean isWinner() {
        return isWinner;
    }
}

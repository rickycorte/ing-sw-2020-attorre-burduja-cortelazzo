package it.polimi.ingsw.controller;

/**
 * This command is send when a match ends
 * If the match is won winnerID is set to the id of the winner
 * If the game was interrupted -1 is used as winner
 */
public class EndGameCommand extends BaseCommand {
    private int winnerID;

    public static final int INTERRUPTED_GAME = -1;
    public static final int TARGET_LOST = -666;

    public EndGameCommand(int sender, int target, int winnerID) {
        //FROM SERVER
        super(sender, target);
        this.winnerID = winnerID;
    }

    public int getWinnerID() { return winnerID; }
}

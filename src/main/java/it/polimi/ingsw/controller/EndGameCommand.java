package it.polimi.ingsw.controller;

/**
 * This command is send when a match ends
 * If the match is won winnerID is set to the id of the winner
 * If the game was interrupted INTERRUPTED_GAME is used as winner
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

    /**
     * Return winner id, can also be INTERRUPTED_GAME if the game ended because someone left
     * or TARGET_LOST if the target client lost but the match is still running
     * @return winner id
     */
    public int getWinnerID() { return winnerID; }
}

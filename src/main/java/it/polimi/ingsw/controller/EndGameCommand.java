package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Player;

/**
 * This command is send when a match ends
 * If the match is won winnerID is set to the id of the winner
 * If the game was interrupted INTERRUPTED_GAME is used as winner
 * (Server)
 * Inform a client that the match is ended (winner/interruped/loser)
 * (Client)
 * Not send
 *
 * When issued this command always has a winnerID that holds different meanings.
 * winnerID is:
 * 1) greater than 0: the game has a real winner and this variable is the client id of the winner
 * 2) INTERRUPTED_GAME: this value mean the the game was interrupted because a client left a running match. This also means that the match is now closed
 * and the client should try to join a new game.
 * 3) TARGET_LOST: this values means that the client with the id located in Command Target has lost the game
 */
public class EndGameCommand extends BaseCommand {
    private int winnerID;

    public static final int INTERRUPTED_GAME = -1;
    public static final int TARGET_LOST = -666;

    public EndGameCommand(int sender, int target, int winnerID) {
        super(sender, target);
        this.winnerID = winnerID;
    }

    /**
     * Return winner id, can also be INTERRUPTED_GAME if the game ended because someone left
     * or TARGET_LOST if the target client lost but the match is still running
     * @return winner id
     */
    public int getWinnerID() { return winnerID; }


    /**
     * Check if this endgame notification was sent due to a match interrupt
     * @return true if match was interrupted
     */
    public boolean isMatchInterrupted()
    {
        return winnerID == INTERRUPTED_GAME;
    }


    /**
     * Return true is command target lose the match
     * @return true if target is not winner
     */
    public boolean isTargetLoser()
    {
        return winnerID == TARGET_LOST || winnerID != getTarget();
    }

    /**
     * Check if the match is still running or not
     * @return return true if match is still running and only the target lost
     */
    public boolean isMatchStillRunning()
    {
        return winnerID == TARGET_LOST;
    }


    /**
     * Create a end game notification when the match ends for everyone
     * If winner is not null its id will be send otherwise if winner is null
     * an interrupted game notification will be created
     * @param sender sender id
     * @param target target id that should receive this message
     * @param winner winner player (or null if interrupted game)
     * @return wrapped command ready to be sent over the network
     */
    public static CommandWrapper makeWrapped(int sender, int target, Player winner)
    {
        return new CommandWrapper(CommandType.END_GAME, new EndGameCommand(sender, target,  winner == null ? INTERRUPTED_GAME : winner.getId()));
    }

    /**
     * Create a lose notification for a player that lost during a match but the game is still running
     * @param sender sender id
     * @param target target id of the player that lost the game
     * @return wrapped command ready to be sent over the network
     */
    public static CommandWrapper makeLoseSingle(int sender, int target)
    {
        return new CommandWrapper(CommandType.END_GAME, new EndGameCommand(sender, target, TARGET_LOST));
    }

}

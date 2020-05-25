package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Player;

import java.util.List;

/**
 * Command used to request/notify that a match started
 */
public class StartCommand extends BaseCommand {
    private int[] playersID;


    /**
     * (Server) Used to inform all the clients that the match they have joined
     * is started
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param connectedPlayers list of connected players (ids)
     */
    public StartCommand(int sender, int target, List<Player> connectedPlayers) {
        //UPDATE FROM SERVER
        super(sender, target);
        this.playersID = idsToArray(connectedPlayers);
    }

    /**
     * (Client) Used to request the server to start the game before the lobby is full
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     */
    public StartCommand(int sender, int target) {
        //REQUEST FROM CLIENT
        super(sender, target);
        this.playersID = null;
    }

    /**
     * Return stored array of player ids
     * @return array of player ids
     */
    public int[] getPlayersID() {
        return playersID;
    }

    /**
     * Convert a list of player ids into an array
     * @return array id of every player
     */
    private int[] idsToArray(List<Player> connectedPlayers){
        int[] ids = new int[connectedPlayers.size()];

        for(int i = 0; i < connectedPlayers.size(); i++ )
            ids[i] = connectedPlayers.get(i).getId();

        return ids;
    }
}

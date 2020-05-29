package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.compact.CompactPlayer;
import it.polimi.ingsw.game.Player;

import java.util.List;

/**
 * Command used to select the first player that should start placing the workers
 * (Server)
 * Request a client to chose what player from the provided list should start to play first
 * (Client)
 * Reply to the server with the selected player that should start the match
 */
public class FirstPlayerPickCommand extends BaseCommand {

    private CompactPlayer[] players;
    private int picked;

    /**
     * (Server) Request the host to pick a player that will start the game
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param connectedPlayers list of players that can be selected
     */
    public FirstPlayerPickCommand(int sender, int target, List<Player> connectedPlayers) {
        super(sender, target);
        players = toCompatPlayerArray(connectedPlayers);
        picked =  -1;
    }


    /**
     * (Client) Send to the server the selected player id
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param playerID first player that will start placing workers
     */
    public FirstPlayerPickCommand(int sender, int target, int playerID) {
        super(sender, target);
        players = null;
        picked = playerID;
    }


    /**
     * Convert player list to compact player array
     * @param playerList player list
     * @return player list converted
     */
    private CompactPlayer[] toCompatPlayerArray(List<Player> playerList)
    {
        CompactPlayer[] arr = new CompactPlayer[playerList.size()];
        for(int i =0; i< playerList.size(); i++)
        {
            arr[i] = new CompactPlayer(playerList.get(i));
        }
        return arr;
    }


    /**
     * @deprecated use {@link #getPlayers()}
     */
    @Deprecated
    public int[] getPlayersID()
    {
        int[] ids = new int[players.length];

        for(int i =0 ;i < players.length; i++)
            ids[i] = players[i].getId();

        return ids;
    }

    /**
     * @deprecated use {@link #getPlayers()}
     */
    @Deprecated
    public String[] getUsernames()
    {
        String[] ids = new String[players.length];

        for(int i =0 ;i < players.length; i++)
            ids[i] = players[i].getUsername();

        return ids;
    }

    /**
     * @deprecated use {@link #getPlayers()}
     */
    @Deprecated
    public int[] getGodID()
    {
        int[] ids = new int[players.length];

        for(int i =0 ;i < players.length; i++)
            ids[i] = players[i].getGodID();

        return ids;
    }

    /**
     * Return array of selectable players with their information
     * @return player list
     */
    public CompactPlayer[] getPlayers()
    {
        return players;
    }

    /**
     * Return picked player id (-1 if error)
     * @return picked player id
     */
    public int getPickedPlayerID()
    {
        return picked;
    }


    /**
     * (Server) Request the host to pick a player that will start the game
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param connectedPlayers list of players that can be selected
     * @return wrapped command ready to be sent over the network
     */
    public static CommandWrapper makeRequest(int sender, int target, List<Player> connectedPlayers)
    {
        return new CommandWrapper(CommandType.SELECT_FIRST_PLAYER, new FirstPlayerPickCommand(sender,target,connectedPlayers));
    }


    /**
     * (Client) Send to the server the selected player id
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param playerID first player that will start placing workers
     * @return wrapped command ready to be sent over the network
     */
    public static CommandWrapper makeReply(int sender, int target, int playerID)
    {
        return new CommandWrapper(CommandType.SELECT_FIRST_PLAYER, new FirstPlayerPickCommand(sender, target, playerID));
    }

}

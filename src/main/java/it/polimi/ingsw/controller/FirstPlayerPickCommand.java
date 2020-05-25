package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Player;

import java.util.List;

/**
 * Command used to select the first player that should start placing the workers
 */
public class FirstPlayerPickCommand extends BaseCommand {
    private int[] playersID;
    private String[] usernames;
    private int[] godID;

    //to client

    /**
     * (Server) Request the host to pick a player that will start the game
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param connectedPlayers list of players that can be selected
     */
    public FirstPlayerPickCommand(int sender, int target, List<Player> connectedPlayers) {
        super(sender, target);
        this.playersID = idsToArray(connectedPlayers);
        this.usernames = usernamesToArray(connectedPlayers);
        this.godID = godIdsToArray(connectedPlayers);
    }


    /**
     * (Client) Send to the server the selected player id
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param playerID first player that will start placing workers
     */
    public FirstPlayerPickCommand(int sender, int target, int playerID) {
        super(sender, target);
        this.playersID = new int[]{playerID};
        this.usernames = null;
        this.godID = null;
    }


    /**
     * utility method
     * every id of players
     * @return array id of every player
     */
    private int[] idsToArray(List<Player> connectedPlayers){
        int[] ids = new int[connectedPlayers.size()];

        for(int i = 0; i < connectedPlayers.size(); i++ )
            ids[i] = connectedPlayers.get(i).getId();

        return ids;
    }

    /**
     * every username of players
     * @return array username of every player
     */
    private String[] usernamesToArray(List<Player> connectedPlayers){
        String[] usernames = new String[connectedPlayers.size()];

        for(int i = 0; i < connectedPlayers.size(); i++ )
            usernames[i] = connectedPlayers.get(i).getUsername();

        return usernames;
    }

    private int[] godIdsToArray(List<Player> connectedPlayers){
        int[] gods = new int[connectedPlayers.size()];

        for(int i = 0 ; i < connectedPlayers.size(); i++){
            gods[i] = connectedPlayers.get(i).getGod().getId();
        }

        return gods;
    }

    public int[] getPlayersID() {
        return playersID;
    }

    public String[] getUsernames() {
        return usernames;
    }

    public int[] getGodID(){return godID;}

}

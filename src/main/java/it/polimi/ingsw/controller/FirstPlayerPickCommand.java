package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Player;

import java.util.List;

public class FirstPlayerPickCommand extends BaseCommand {
    private int[] playersID;
    private String[] usernames;

    //to client
    public FirstPlayerPickCommand(int sender, int target, List<Player> connectedPlayers) {
        super(sender, target);
        this.playersID = idsToArray(connectedPlayers);
        this.usernames = usernamesToArray(connectedPlayers);
    }

    //to server
    public FirstPlayerPickCommand(int sender, int target, int playerID) {
        super(sender, target);
        this.playersID = new int[]{playerID};
        this.usernames = null;
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


    public int[] getPlayersID() {
        return playersID;
    }

    public String[] getUsernames() {
        return usernames;
    }

}

package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Player;

import java.util.List;

public class StartCommand extends BaseCommand {
    private int[] playersID;


    public StartCommand(int sender, int target, List<Player> connectedPlayers) {
        //UPDATE FROM SERVER
        super(sender, target);
        this.playersID = idsToArray(connectedPlayers);
    }

    public StartCommand(int sender, int target) {
        //REQUEST FROM CLIENT
        super(sender, target);
        this.playersID = null;
    }

    public int[] getPlayersID() {
        return playersID;
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
}

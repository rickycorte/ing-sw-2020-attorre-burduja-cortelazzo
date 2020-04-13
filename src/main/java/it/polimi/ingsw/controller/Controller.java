package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.NotAllowedOperationException;
import it.polimi.ingsw.game.Player;

import java.util.List;

public class Controller
{

    List<Player> connected_players;
    Game match;

    /**
     * Callback function called by the network layer when a new player joins the game
     * @param id user id
     * @param username username
     */
    public void onConnect(int id, String username)
    {
        if(match == null || match.isEnded())
            match = new Game();

        Player p = new Player(id, username);
        connected_players.add(p);
        //TODO: check username

        boolean res = match.join(p);
    }

    /**
     * Callback function called by the network layer when a command is received
     * @param cmd command issued by a client
     */
    void onCommand(Command cmd)
    {
        Game.GameState prevGameState = match.getCurrentState();
        Player prevPlayer = match.getCurrentPlayer();
        try
        {

            runCommand(match, cmd);
        }catch (NotAllowedOperationException e)
        {
            //TODO: inform that a broken command was issued
        }


    }

    /**
     * Callback function called by the network layer when a player leave the game
     * @param id player id
     */
    public void onDisconnect(int id)
    {
        if(match == null || match.isEnded())
            return;

        match.left(getPlayer(id));
    }

    // **********************************************************************************************
    // Private
    // **********************************************************************************************

    /**
     * Translate a numeric id into a real connected player
     * @param id player id
     * @return Player instance when passed id
     */
    private Player getPlayer(int id)
    {
        for (Player p : connected_players)
        {
            if(p.getId() == id)
                return p;
        }
        return null;
    }


    /**
     * Run command on a selected game
     * @param gm match instance where command should run
     * @param cmd command to run
     * @return false if a command execution failed
     * @throws NotAllowedOperationException if a not allowed command is run
     */
    private boolean runCommand(Game gm, Command cmd) throws NotAllowedOperationException
    {
        Player p = getPlayer(cmd.getSender());
        switch (cmd.getType()){
            case START:
                return gm.start(p);
            case FILER_GODS:
                return gm.applyGodFilter(p, cmd.getIntData());
            case PICK_GOD:
                break;
            case SELECT_FIRST_PLAYER:
                break;
            case PLACE_WORKERS:
                break;
            case ACTION_TIME:
                break;
        }

        //TODO: error here
        return false;
    }

}

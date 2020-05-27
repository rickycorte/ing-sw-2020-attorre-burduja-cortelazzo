package it.polimi.ingsw.network.matchmaking;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkForwarder;
import it.polimi.ingsw.network.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class represent a virtual match that is running on the server
 * The main purpose of this class is to track events in a mach and "record" where the came from
 * this is used have more control where data and events should be delivered
 * Several helper functions are provided to the user to better understand that is inside this class
 * This class also thread safety over multiple concurrent access on the same resource
 */
public class VirtualMatch implements INetworkForwarder
{

    private VirtualMatchmaker matchmaker;
    private  final ICommandReceiver controller;
    private List<Integer> players;

    private AtomicBoolean isStarted, isEnded;


    /**
     * Create a virtual match bind to a matchmaking
     * @param matchmaker matchmaker to bind to
     */
    public VirtualMatch(VirtualMatchmaker matchmaker)
    {
        this.matchmaker = matchmaker;
        controller = new Controller(this);
        isEnded = new AtomicBoolean(false);
        isStarted = new AtomicBoolean( false);
        players = new ArrayList<>();
    }

    //---------------------------------------------------------------------------------------------
    // Interface for Controller class
    // it wont notice that it's running in a virtual environment and not directly on the network layer

    /**
     * Receiver for controller commands
     * @param packet packet to send
     */
    @Override
    public void send(CommandWrapper packet)
    {
        //peek command to understand state
        if(packet.getType() == CommandType.START)
        {
            System.out.printf("[MATCH %s] Detected start\n",this.hashCode());
            isStarted.set(true);
        }

        // game ends when a END_GAME command and the winner is one of the player in the match
        // the get works because of lazy evaluation, when not END_GAME the getCommand wont even run
        // an no error will show up
        boolean isGameEnded = packet.getType() == CommandType.END_GAME && players.contains(packet.getCommand(EndGameCommand.class).getWinnerID());
        matchmaker.send(this, packet, isGameEnded);
    }

    @Override
    public int getServerID()
    {
        return Server.SERVER_ID;
    }

    @Override
    public int getBroadCastID()
    {
        return Server.BROADCAST_ID;
    }

    @Override
    public int getDefaultPort()
    {
        return Server.DEFAULT_SERVER_PORT;
    }

    //---------------------------------------------------------------------------------------------
    // Interface for matchmaker

    /**
     * Add a player to the game
     * This can be seen as a way to join the virtual game
     * @param id id of the player to add
     * @param cmd command cache to forward
     */
    public synchronized void addPlayer(int id, CommandWrapper cmd)
    {
        synchronized (controller)
        {
            players.add(id);
            controller.onConnect(cmd);
        }
    }

    /**
     * Request a command execution on this game
     * @param cmd command to forward
     */
    public void execAction(CommandWrapper cmd)
    {
        synchronized (controller)
        {
            controller.onCommand(cmd);
        }
    }

    /**
     * Remove a player from this game
     * This function can be seen as a "disconnect" from the virtual game
     * @param id id of the player to remove
     * @param cmd command cache to forward
     */
    public void removePlayer(int id, CommandWrapper cmd)
    {
        synchronized (controller)
        {
            //use indexof because removing with a int is used as "index" and not as a search item
            players.remove(players.indexOf(id));
            controller.onDisconnect(cmd);
        }
    }


    /**
     * Return an array of connected player ids
     * useful to filter command forwarding
     * @return array of player ids
     */
    public int[] getPlayerIDs()
    {
        synchronized (controller)
        {
            int[] arr = new int[players.size()];
            for (int i = 0; i < players.size(); i++) arr[i] = players.get(i);

            return arr;
        }
    }


    /**
     * Return true if the match is full
     * @return true is match is full
     */
    public boolean isFull()
    {
        synchronized (controller)
        {
            return players.size() > Game.MAX_PLAYERS;
        }
    }

    /**
     * Return if the match is in wait state or not
     * @return true is match is a waiting lobby
     */
    public boolean isWaiting() { return !isEnded.get() && !isStarted.get(); }

    /**
     * Return the current player count of this match
     * @return player count
     */
    public int playerCount() {
        synchronized (controller)
        {
            return players.size();
        }
    }

}

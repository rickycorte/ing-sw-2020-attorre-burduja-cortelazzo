package it.polimi.ingsw.network.matchmaking;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.server.Server;

import java.util.*;

/**
 * This class implements a virtual matchmaking for multiple games.
 * Virtual means this class virtualize the execution of multiple games transparently
 * Only this class knows about multiple games, everything else is designed to work with a single game
 */
public class VirtualMatchmaker implements ICommandReceiver
{

    private Server server;

    private final Map<Integer, String> loggedPlayers; // keep join command to send to the controller with the original
    private final List<VirtualMatch> matches;

    private final Map<Integer, VirtualMatch> matchCache;


    //
    // Lost of function has a CommandWrapper as parameter to pass a cached reference
    // creating commands is ugly and booring
    //


    public VirtualMatchmaker()
    {
        server = null;
        loggedPlayers = Collections.synchronizedMap(new HashMap<>());
        matches = Collections.synchronizedList(new ArrayList<>());
        matchCache = Collections.synchronizedMap(new HashMap<>());

    }



    //-----------------------------------------------------------------------
    // Network interface


    /**
     * Start the matchmaking
     */
    public void start()
    {
        start(Server.DEFAULT_SERVER_PORT);
    }

    /**
     * Start the matchmaking
     * @param port
     */
    public void start(int port)
    {
        if(server == null)
        {
            server = new Server(port);
            server.setReceiver(this);
            server.startInBackground();
        }
    }

    /**
     * Stop the matchmaking
     */
    public void stop()
    {
        if(server != null)
        {
            server.stop();
            server = null;
        }
    }


    /**
     * Send data to all the clients connected to a virtual match
     * @param vm
     * @param cmd
     * @param gameEnded
     */
    public void send(VirtualMatch vm, CommandWrapper cmd, boolean gameEnded)
    {
        // send data to clients
        var clients = vm.getPlayerIDs();

        for (Integer client : clients)
            server.send(client, cmd);

        // remove game if ended and keep client sessions
        if(gameEnded)
            removeEndedMatch(vm);
    }



    //-----------------------------------------------------------------------
    // Command Receiver interface

    @Override
    public void onConnect(CommandWrapper cmd)
    {
        System.out.println("[MATCHMAKER] Connect got " + cmd.toString());
        JoinCommand jcm = cmd.getCommand(JoinCommand.class);

        if(!login(jcm))
        {
            // duplicate username notify client
            server.send(jcm.getSender(),
                    new CommandWrapper(CommandType.JOIN,
                            new JoinCommand(Server.SERVER_ID, jcm.getSender(), false, false)
                    ));
            System.out.printf("[MATCHMAKER] Rejected login from user: [%d] %s\n", jcm.getSender(), jcm.getUsername());
        }
        else
        {
            if(matchCache.get(jcm.getSender()) != null)
                return; // already in a match skip

            // join match
            var vm = joinMatch(jcm, cmd);
            matchCache.put(jcm.getSender(), vm);
            System.out.printf("[MATCHMAKER] User: [%d] %s joined match %s, match cache size: %d\n", jcm.getSender(), jcm.getUsername(), vm.hashCode(), matchCache.size());
        }

    }

    @Override
    public void onDisconnect(CommandWrapper cmd)
    {
        System.out.println("[MATCHMAKER] Disconnect got " + cmd.toString());
        LeaveCommand lcm = cmd.getCommand(LeaveCommand.class);

        var vm =  matchCache.get(lcm.getSender());
        if(vm != null)
        {
            vm.removePlayer(lcm.getSender(), cmd);
            matchCache.remove(lcm.getSender());

            //clear empty lobby
            if(vm.playerCount() == 0 && vm.isWaiting())
                removeEndedMatch(vm);
        }

        System.out.printf("[MATCHMAKER] User: %d disconnected, match cache size: %d\n", lcm.getSender(), matchCache.size());
        logout(lcm);
    }


    @Override
    public void onCommand(CommandWrapper cmd)
    {
        System.out.println("[MATCHMAKER] Execute got " + cmd.toString());
        var vm = matchCache.get(cmd.getCommand(BaseCommand.class).getSender());
        if(vm != null)
        {
            vm.execAction(cmd);
        }
    }



    //-----------------------------------------------------------------------
    // Internal functions

    /**
     * Join a waiting match or create a new one
     * @param jcm
     * @param cmd
     * @return
     */
    private VirtualMatch joinMatch(JoinCommand jcm, CommandWrapper cmd)
    {
        VirtualMatch vm;
        synchronized (matches)
        {
            // check for open games
            for(int i = 0; i < matches.size(); i++)
            {
                vm =  matches.get(i);
                System.out.printf("[MATCH %s] Full: %b, Waiting: %b\n", vm.hashCode(), vm.isFull(), vm.isWaiting());
                if(!vm.isFull() && vm.isWaiting())
                {
                    vm.addPlayer(jcm.getSender(), cmd);
                    return vm;
                }
            }

            // no open game found if we reach here so we create a new one
            vm = new VirtualMatch(this);
            vm.addPlayer(jcm.getSender(), cmd);
            //add match to available ones
            matches.add(vm);
            System.out.printf("[MATCHMAKER] Created new match %s, current games: %d\n", vm.hashCode(), matches.size());
        }

        return vm;
    }


    /**
     * Check if a user can login into the game
     * @param cmd
     * @return
     */
    boolean login(JoinCommand cmd)
    {
        String usr = cmd.getUsername();

        if(loggedPlayers.containsValue(usr))
        {
            System.out.println("[MATCHMAKER] Duplicate username detected: "+ usr);
            // same username and same id -> login ok
            if (usr.equals(loggedPlayers.get(cmd.getSender())))
                return true;
            else
                return false;
        }
        else
        {
            // new user make session
            System.out.println("[MATCHMAKER] Created session for: "+ usr);
            loggedPlayers.put(cmd.getSender(), cmd.getUsername());
            return true;
        }
    }


    /**
     * Remove a client session
     * @param lcm
     */
    void logout(LeaveCommand lcm)
    {
        matchCache.remove(lcm.getSender());
        loggedPlayers.remove(lcm.getSender());
    }

    /**
     * Remove a match that is ended
     * @param vm
     */
    void removeEndedMatch(VirtualMatch vm)
    {
        var clients = vm.getPlayerIDs();

        for (Integer client : clients)
            matchCache.remove(client);

        matches.remove(vm);
        System.out.printf("[MATCHMAKER] Match %s ended, current games: %d, match cache size: %d\n", vm.hashCode(), matches.size(), matchCache.size());
    }

}

package it.polimi.ingsw.network.matchmaking;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
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
    // creating commands is ugly and boring
    //


    /**
     * Create a new instance of the matchmaker that could be used to replace {@link it.polimi.ingsw.network.TPCNetwork} on server side
     * to enable multiple matches
     * This function does not start the matchmaking
     */
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
     * Start the matchmaking and listen in background
     * @param port port where server should start
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
     * Start the matchmaking in background on default port
     */
    public void start()
    {
        start(Server.DEFAULT_SERVER_PORT);
    }


    /**
     *  Start matchmaking on the caller thread
     * @param port port where server should start
     */
    public void startSync(int port)
    {
        if(server == null)
        {
            server = new Server(port);
            server.setReceiver(this);
            server.start();
        }
    }

    /**
     *  Start matchmaking on the caller thread and use default port
     */
    public void startSync()
    {
        startSync(Server.DEFAULT_SERVER_PORT);
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
     * Send data to all the clients connected to a virtual match,
     * broadcast sent is emulated only for clients connected to the virtual match
     * no other client connected will receive a message of a different match.
     * This function also detects when a match ends and clear up its resources
     * @param vm virtual match that whats to send commands
     * @param cmd command to send
     * @param gameEnded true if virtual match ended
     */
    public void send(VirtualMatch vm, CommandWrapper cmd, boolean gameEnded)
    {
        // send data to clients
        var clients = vm.getPlayerIDs();


        for (Integer client : clients)
            server.send(client, cmd);

        // someone lost in the game and we should remove him from the match
        // to avoid sending messages to a "idle" client
        if(!gameEnded && cmd.getType() == CommandType.END_GAME)
        {
            int id = cmd.getCommand(BaseCommand.class).getTarget();
            matchCache.remove(id);
            vm.removePlayerNoDisconnect(id);
        }

        // remove game if ended and keep client sessions
        if(gameEnded)
            removeEndedMatch(vm);
    }



    //-----------------------------------------------------------------------
    // Command Receiver interface

    /**
     * Handle onConnect events from network
     * This function also generates and fill lobby
     * @param cmd join command
     */
    @Override
    public void onConnect(CommandWrapper cmd)
    {
        System.out.println("[MATCHMAKER] Connect got " + cmd.toString());
        JoinCommand jcm = cmd.getCommand(JoinCommand.class);
        if(jcm == null) return;

        if(!login(jcm))
        {
            // duplicate username notify client
            server.send(jcm.getSender(), JoinCommand.makeReplyFail(Server.SERVER_ID, jcm.getSender(), false));
            System.out.printf("[MATCHMAKER] Rejected login of user: [%d] %s\n", jcm.getSender(), jcm.getUsername());
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

    /**
     * Handle a disconnect event
     * This function also clear unused lobbies
     * @param cmd disconnect command
     */
    @Override
    public void onDisconnect(CommandWrapper cmd)
    {
        System.out.println("[MATCHMAKER] Disconnect got " + cmd.toString());
        LeaveCommand lcm = cmd.getCommand(LeaveCommand.class);
        if(lcm == null)
        {
            System.out.println("[MATCHMAKER] Invalid leave command received");
            return;
        }

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


    /**
     * Handle command events and redirect them to the correct match
     * @param cmd command to process
     */
    @Override
    public void onCommand(CommandWrapper cmd)
    {
        System.out.println("[MATCHMAKER] Execute got " + cmd.toString());
        var baseCmd = cmd.getCommand(BaseCommand.class);
        if(baseCmd == null) return;

        var vm = matchCache.get(baseCmd.getSender());
        if(vm != null)
        {
            vm.execAction(cmd);
        }
    }



    //-----------------------------------------------------------------------
    // Internal functions

    /**
     * Join a waiting match or create a new one
     * @param jcm join command
     * @param cmd command wrapper of jcm used as cache
     * @return match joined by the player or null if there was an error (example unable to login)
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
     * Return if a username is valid and can be used to login
     * Valid username: size: [3, 32] and chars: [a-z A-Z 0-9]
     * @param usr username to check
     * @return true if username is valid
     */
    boolean isUsernameValid(String usr)
    {
        return usr != null && usr.matches("^[a-zA-Z0-9]{3,32}$");
    }

    /**
     * Check if a user can login into the game
     * @param cmd join command
     * @return true if player logged in correctly
     */
    boolean login(JoinCommand cmd)
    {
        String usr = cmd.getUsername();

        if(!isUsernameValid(usr))
            return false;

        if(loggedPlayers.containsValue(usr))
        {
            // same username and same id -> login ok
            boolean isOk = usr.equals(loggedPlayers.get(cmd.getSender()));
            System.out.println("[MATCHMAKER] Duplicate username detected: "+ usr+". Accepted: "+ isOk);
            return isOk;
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
     * @param lcm leave command
     */
    void logout(LeaveCommand lcm)
    {
        matchCache.remove(lcm.getSender());
        loggedPlayers.remove(lcm.getSender());
    }

    /**
     * Remove a match that is ended
     * @param vm virtual match that has to be removed
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

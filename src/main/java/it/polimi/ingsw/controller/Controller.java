package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.*;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkForwarder;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.game.Game.GameState.END;

/**
 * Controller class that handles incoming messages from layer network
 * executes "queries" on an instance of game (a match) and generates new commands
 * to send to the clients
 */
public class Controller implements ICommandReceiver {


    private List<Player> connectedPlayers;
    private Game match;
    private INetworkForwarder network;
    private CommandWrapper lastSent;


    public Controller (INetworkForwarder adapter){
        connectedPlayers = new ArrayList<>();
        network = adapter;
        lastSent = null;
    }

    // **********************************************************************************************
    // Getter (for tests only)
    // **********************************************************************************************

    public Game getMatch() { return match; }

    public CommandWrapper getLastSent() { return lastSent; }

    public List<Player> getConnectedPlayers() { return connectedPlayers; }

    // **********************************************************************************************
    // Operation
    // **********************************************************************************************

    /**
     * Callback function called by the network layer when a new player joins the game
     * Create a new game if necessary
     * Send an acknowledgment with identifier if player joined correctly (negative identifier if can't join)
     * wait for Start command, or start a game if number of players reached MAX_PLAYERS
     * @param cmdWrapper wrapped join command
     */
    public void onConnect(CommandWrapper cmdWrapper)
    {
        JoinCommand joinCommand = cmdWrapper.getCommand(JoinCommand.class);

        if (match == null || match.isEnded()) {
            match = new Game();
        }

        if(!joinCommand.isJoin() || joinCommand.getUsername() == null || !isUsernameAvailable(joinCommand.getUsername()))
        {
            sendCommand(JoinCommand.makeReplyFail(network.getServerID(), joinCommand.getSender(), false));
            return;
        }

        System.out.println("[CONTROLLER] Player " + joinCommand.getUsername() + " wants to join");
        Player p = new Player(joinCommand.getSender(), joinCommand.getUsername());

        if(match.join(p))
        {
            connectedPlayers.add(p);
            // send join notification to player
            sendCommand(JoinCommand.makeReplyOk(network.getServerID(), joinCommand.getSender(), match.getHost().getId()));
            // auto start match with three players
            if(match.playerCount() == Game.MAX_PLAYERS)
            {
                // start game
                match.start(getPlayer(match.getHost().getId()));
                sendCommand(makeNextCommand(Game.GameState.WAIT, match.getCurrentState())); // WAIT because was in lobby
            }
        }
        else
        {
            // no more slots sorry :L
            sendCommand(JoinCommand.makeReplyFail(network.getServerID(), joinCommand.getSender(), true));
        }
    }

    /**
     * Callback function called by the network layer when a command is received
     * Discard the command if not allowed for the sender
     * Send a new command to client
     * @param cmdWrapper wrapped command issued by a client
     */
    public void onCommand(CommandWrapper cmdWrapper)
    {
        CommandWrapper nextCmd;
        Game.GameState prevGameState = match.getCurrentState();

        if(!filterCommandType(cmdWrapper)) return; //command not expected

        try
        {
            if (runCommand(cmdWrapper))
            {
                nextCmd = makeNextCommand(prevGameState, match.getCurrentState());
                sendMapUpdate(cmdWrapper);
            }
            else //command failed
            {
                if(cmdWrapper.getType() == CommandType.START) nextCmd = null;
                else nextCmd = lastSent;
            }

            sendCommand(nextCmd);
        }
        catch (NotAllowedOperationException e)
        {
            //it's not your turn to action
            // ignore command
        }

    }

    /**
     * Callback function called by the network layer when a player leave the game
     *
     * @param leaveWrapper CommandWrapper sent by the player want to leave
     */
    public void onDisconnect(CommandWrapper leaveWrapper)
    {
        if (match == null || match.isEnded())
            return;

        LeaveCommand cmd = leaveWrapper.getCommand(LeaveCommand.class);

        System.out.println("[CONTROLLER] Disconnect "+ cmd.getSender());
        match.left(getPlayer(cmd.getSender()));

        if(match.getCurrentState() == END)
        {
            sendCommand(EndGameCommand.makeWrapped(network.getServerID(), network.getBroadCastID(), match.getWinner()));
        }
    }

    // **********************************************************************************************
    // Private
    // **********************************************************************************************

    /**
     * Translate a numeric id into a real connected player
     *
     * @param id player id
     * @return Player instance when passed id
     */
    private Player getPlayer(int id)
    {
        for (Player p : connectedPlayers) {
            if (p.getId() == id)
                return p;
        }
        return null;
    }

    /**
     * Check if cmd type is the required one
     * include first command case
     * @param cmd Command to be checked
     * @return true if cmd type is valid
     */
    private boolean filterCommandType(CommandWrapper cmd)
    {
        //start is not a request
        if(match.getCurrentState() != Game.GameState.WAIT && cmd.getType() == CommandType.START) return false;

        //join are not onCommand type
        if(lastSent.getType() == CommandType.JOIN && cmd.getType() == CommandType.START) return true;

        //command is not the requested one
        return lastSent == null || lastSent.getType() == cmd.getType();
    }

    /**
     * Run a receiver and command and return if the execution was successful of not
     * @param cmd command to run
     * @return false if a command execution failed
     * @throws NotAllowedOperationException if a not allowed command is run, it's not sender turn
     */
    private boolean runCommand(CommandWrapper cmd) throws NotAllowedOperationException
    {
        BaseCommand baseCommand = cmd.getCommand(BaseCommand.class);
        Player p = getPlayer(baseCommand.getSender());

        switch (cmd.getType()) {
            case START:
                return match.start(p);

            case FILTER_GODS:
                FilterGodCommand filterGodCommand = cmd.getCommand(FilterGodCommand.class);
                return match.applyGodFilter(p, filterGodCommand.getGodFilter());

            case PICK_GOD:
                PickGodCommand pickGodCommand = cmd.getCommand(PickGodCommand.class);
                return match.selectGod(p, pickGodCommand.getPickedGodID());

            case SELECT_FIRST_PLAYER:
                FirstPlayerPickCommand firstPlayerPickCommand = cmd.getCommand(FirstPlayerPickCommand.class);
                return match.selectFirstPlayer(p, getPlayer(firstPlayerPickCommand.getPickedPlayerID()));

            case PLACE_WORKERS:
                WorkerPlaceCommand workerPlaceCommand = cmd.getCommand(WorkerPlaceCommand.class);
                return match.placeWorkers(p, workerPlaceCommand.getPositions());

            case ACTION_TIME:{
                ActionCommand actionCommand = cmd.getCommand(ActionCommand.class);
                var selectedAction = actionCommand.getSelectedAction();

                int res = match.executeAction(p, selectedAction.getWorkerID(), selectedAction.getActionID(), selectedAction.getPosition());

                if(res > 0) // exec ok
                {
                    return true;
                }
                else if(res == 0) // current player lost
                {
                    network.send(EndGameCommand.makeLoseSingle(network.getServerID(), actionCommand.getSender()));
                    return true;
                }
                else // exec fail
                {
                    return false;
                }
            }
        }
        return false;
    }


    /**
     * Create a new command for the new state reached by the game,
     * this function also generates correct values if the game is still in the same state
     * but some data values change
     * @param oldState old game state (before command run)
     * @param currentState current game state (after command run)
     * @return new command that should be sent to the clients
     */
    private CommandWrapper makeNextCommand(Game.GameState oldState, Game.GameState currentState)
    {
        CommandWrapper nextCmd;
        //check for state changes
        if (oldState != currentState)
        {
            // check if should notify a game start
            if(oldState == Game.GameState.WAIT)
                sendCommand(StartCommand.makeReply(network.getServerID(), network.getBroadCastID(), connectedPlayers));

            //different state
            nextCmd = makeCommandForState(currentState);
        }
        else
        {
            //different player same state OR same player same state
            nextCmd = makeCommandForState(currentState);

            if(nextCmd.getType() == CommandType.END_GAME)
            {
                //notify player lose in his turn
                sendCommand(nextCmd);
                nextCmd = makeCommandForState(currentState); // calculate new moves for a player
            }
        }

        return nextCmd;
    }

    /**
     * Create a new command to send to the client based on a game state
     * @param state state used to generate the command
     * @return wrapped command fot the supplied state
     */
    private CommandWrapper makeCommandForState(Game.GameState state)
    {
        var currentPlayer = match.getCurrentPlayer();
        var host = match.getHost();
        switch (state)
        {
            case GOD_FILTER:
                return FilterGodCommand.makeRequest(network.getServerID(), host.getId());

            case GOD_PICK:
                return PickGodCommand.makeRequest(network.getServerID(), currentPlayer.getId(), match.getAllowedCardIDs());

            case FIRST_PLAYER_PICK:
                return FirstPlayerPickCommand.makeRequest(network.getServerID(),host.getId(), connectedPlayers);

            case WORKER_PLACE:
                return WorkerPlaceCommand.makeWrapped(network.getServerID(), currentPlayer.getId(), match.getCurrentMap().cellWithoutWorkers());

            case GAME:
                var actions = match.getNextActions(currentPlayer);
                if(actions != null)
                    return ActionCommand.makeRequest(network.getServerID(), currentPlayer.getId(), actions);
                else
                    return EndGameCommand.makeLoseSingle(network.getServerID(), currentPlayer.getId());

            case END:
                return EndGameCommand.makeWrapped(network.getServerID(), network.getBroadCastID(), match.getWinner());
        }
        return null;
    }


    /**
     * check if a username is already used for a player in the game
     * @param username new player's username
     * @return true if username is available
     */
    private boolean isUsernameAvailable(String username)
    {
        for(Player player : match.getPlayers()){
            if(player.getUsername().equals(username)) return false;
        }
        return true;
    }

    /**
     * Send update of game status
     * Data is stored in an int array like :
     *  LENGTH * HEIGHT element represent map values
     *  the rest are pairs of (id owner, id worker)
     * Every Vector2 is corrispondent to a pai of (id owner, id worker)
     * @param lastCommand last command received
     */
    private void sendMapUpdate(CommandWrapper lastCommand)
    {
        if(lastCommand.getType() == CommandType.PLACE_WORKERS || lastCommand.getType() == CommandType.ACTION_TIME) {

            System.out.println("Sending map update to everyone");
            network.send(UpdateCommand.makeWrapped(network.getServerID(), network.getBroadCastID(), match.getCurrentMap()));
        }
    }


    /**
     * send a command
     * @param cmdWrap next command to send
     */
    private void sendCommand(CommandWrapper cmdWrap)
    {
        if(cmdWrap != null)
        {
            lastSent = cmdWrap;
            network.send(cmdWrap);
        }
    }
}

package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.*;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.INetworkForwarder;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.game.Game.GameState.END;

public class Controller implements ICommandReceiver {
    private static final int SERVER_ID = -1111;
    private static final int BROADCAST_ID = -2222;
    private static final int MAX_PLAYERS = 3;
    private static final int MIN_PLAYERS = 2;


    private List<Player> connectedPlayers;
    private Game match;
    private INetworkForwarder virtualProxy;
    private CommandWrapper lastSent;
    private int host;
    private  int nextPlayer;

    /*
    outgoing command description
    BaseCommand = - (ping command ?)
    JoinCommand = notification for successful(or not) joining (ack)
    StartCommand = notification of started game, int players' ID
    FilterGodCommand =  no payload
    PickGodCommand =  available card's id
    FirstPlayerPickCommand =  id and username of connected players in this game
    WorkerPlaceCommand =  send available Vector2
    ActionTimeCommand = send pair(id worker,n. available cell), all Vector2 available cell, all action's names
    EndGameCommand = notification , boolean lose/win notification
    UpdateCommand = update, first 49 int element are map, from 49 to end are pairs(id owner,id worker), Vector2 element for workers position on map
    LeaveCommand = -
     */

    /*
    incoming command description
    BaseCommand = - (ping command ?)
    JoinCommand = username want to play, boolean if i want to join/leave
    StartCommand = notification (from host) to start a match
    FilterGodCommand = gods' id want to be allowed(from host)
    PickGodCommand = chosen god id
    FirstPlayerPickCommand = id of first player (from host)
    WorkerPlaceCommand = two Vector2 for client's 2 workers
    ActionTimeCommand = selected worker,action id, target position Vector2
    EndGameCommand = -
    UpdateCommand = -
    LeaveCommand = no payload , sender want to leave the game (voluntary and involuntary)
     */

    public Controller (INetworkForwarder adapter){
        connectedPlayers = new ArrayList<>();
        virtualProxy = adapter;
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
    public void onConnect(CommandWrapper cmdWrapper) {
        //System.out.println("Controller got "+ cmdWrapper.getType().name() + "command in onConnect");

        JoinCommand joinCommand;
        if(cmdWrapper == null) return;
        else joinCommand = cmdWrapper.getCommand(JoinCommand.class);

        if (match == null || match.isEnded()) {
            match = new Game();
            host = joinCommand.getSender();
        }

        if(joinCommand.getUsername() == null || !joinCommand.isJoin()){
            ackJoin(joinCommand,false, false); // reason 2, username not valid
            return;
        }

        String username = joinCommand.getUsername();

        System.out.println("Player "+username+" wants to join");
        if(usernameAvailability(username)) {
            Player p = new Player(joinCommand.getSender(),username);

            if(match.join(p)){
                connectedPlayers.add(p);
                ackJoin(joinCommand,true,true); //player connected to current game
                if(match.getPlayers().size() == MAX_PLAYERS ){
                    match.start(getPlayer(host));
                    virtualProxy.send(new CommandWrapper(CommandType.START,new StartCommand(SERVER_ID,BROADCAST_ID,connectedPlayers)));
                    sendNextCommand(new CommandWrapper(CommandType.FILTER_GODS,new FilterGodCommand(SERVER_ID,host))); //first command
                }
            }else
                ackJoin(joinCommand,false, true); //reason 1, connection problem

        }else
            ackJoin(joinCommand,false, false); //can be fixed with return a usernameError value, reason 2, username not valid
    }

    /**
     * Callback function called by the network layer when a command is received
     * Discard the command if not allowed for the sender
     * Send a new command to client
     * @param cmdWrapper wrapped command issued by a client
     */
    public void onCommand(CommandWrapper cmdWrapper) {
        CommandWrapper nextCmd;
        Game.GameState prevGameState = match.getCurrentState();

        //System.out.println("Controller got "+ cmdWrapper.getType().name() + "command in onCommand");

        if(!filterCommandType(cmdWrapper)) return; //command not expected

        //System.out.println("entering try");
        try {

            if (runCommand(cmdWrapper)) {
                //System.out.println("command executed");
                //command executed
                if (prevGameState != match.getCurrentState()) {
                    //different state
                    nextCmd = changedState(cmdWrapper, prevGameState);
                } else {
                    //different player same state OR same player same state
                    nextCmd = repeatCommand();
                }
                sendUpdate(cmdWrapper);
            } else {
                //command failed
                if(cmdWrapper.getType() == CommandType.START) nextCmd = null;
                else nextCmd = lastSent;

            }

            sendNextCommand(nextCmd);
        } catch (NotAllowedOperationException e) {
            //it's not your turn to action
        }

    }

    /**
     * Callback function called by the network layer when a player leave the game
     *
     * @param leaveWrapper CommandWrapper sent by the player want to leave
     */
    public void onDisconnect(CommandWrapper leaveWrapper) {
        if (match == null || match.isEnded())
            return;

        LeaveCommand cmd = leaveWrapper.getCommand(LeaveCommand.class);

        System.out.println("[CONTROLLER] Disconnect "+ cmd.getSender());
        match.left(getPlayer(cmd.getSender()));

        if(match.getCurrentState() == END)
        {
            var winner = match.getWinner();
            sendNextCommand(new CommandWrapper(CommandType.END_GAME, new EndGameCommand(SERVER_ID, virtualProxy.getBroadCastID(), winner == null ? EndGameCommand.INTERRUPTED_GAME : winner.getId())));
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
    private Player getPlayer(int id) {
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
    private boolean filterCommandType(CommandWrapper cmd){
        //start is not a request
        if(match.getCurrentState() != Game.GameState.WAIT && cmd.getType() == CommandType.START) return false;

        //join are not onCommand type
        if(lastSent.getType() == CommandType.JOIN && cmd.getType() == CommandType.START) return true;

        //command is not the requested one
        return lastSent == null || lastSent.getType() == cmd.getType();
    }

    /**
     * Run command on a selected game
     *
     * @param cmd command to run
     * @return false if a command execution failed
     * @throws NotAllowedOperationException if a not allowed command is run, it's not sender turn
     */
    private boolean runCommand(CommandWrapper cmd) throws NotAllowedOperationException {
        //System.out.println("about to get the base command");
        BaseCommand baseCommand = cmd.getCommand(BaseCommand.class);
        //System.out.println("got base command");
        Player p = getPlayer(baseCommand.getSender());
        nextPlayer = baseCommand.getSender();

        switch (cmd.getType()) {
            case START:
                //System.out.println("RUNNING START COMMAND");
                return match.start(p);
            case FILTER_GODS:
                //System.out.println("RUNNING FILTER GOD COMMAND");
                FilterGodCommand filterGodCommand = cmd.getCommand(FilterGodCommand.class);
                //System.out.println("DONE RUNNING FILTER GOD");
                return match.applyGodFilter(p, filterGodCommand.getGodID());
            case PICK_GOD:
                PickGodCommand pickGodCommand = cmd.getCommand(PickGodCommand.class);
                return match.selectGod(p, pickGodCommand.getGodID()[0]);
            case SELECT_FIRST_PLAYER:
                FirstPlayerPickCommand firstPlayerPickCommand = cmd.getCommand(FirstPlayerPickCommand.class);
                return match.selectFirstPlayer(p, getPlayer(firstPlayerPickCommand.getPlayersID()[0]));
            case PLACE_WORKERS:
                WorkerPlaceCommand workerPlaceCommand = cmd.getCommand(WorkerPlaceCommand.class);
                return match.placeWorkers(p, workerPlaceCommand.getPositions());
            case ACTION_TIME:{
                ActionCommand actionCommand = cmd.getCommand(ActionCommand.class);
                int res = match.executeAction(p, actionCommand.getIdWorkerNMove()[0], actionCommand.getIdWorkerNMove()[1], actionCommand.getAvailablePos()[0]);
                if(res>0)
                    return true;
                else if(res == 0) {
                    metLoseCondition(actionCommand.getSender());
                    return true;
                }else
                    return false;
            }
        }

        //TODO: error here
        return false;
    }

    /**
     * Method called when Game.State changed after a run command
     * create the next command based on game sequencing
     * @param cmdWrapper command already executed
     * @param previousGameState Game.State before the command execution
     * @return new command to be Sent
     */
    private CommandWrapper changedState(CommandWrapper cmdWrapper, Game.GameState previousGameState) {

        switch (previousGameState) {
            case WAIT:
                virtualProxy.send(new CommandWrapper(CommandType.START,new StartCommand(virtualProxy.getServerID(),BROADCAST_ID,connectedPlayers))) ;
                return new CommandWrapper(CommandType.FILTER_GODS, new FilterGodCommand(SERVER_ID, match.getCurrentPlayer().getId()));
            case GOD_FILTER:
                return new CommandWrapper(CommandType.PICK_GOD,new PickGodCommand(SERVER_ID, match.getCurrentPlayer().getId(), match.getAllowedCardIDs()));
            case GOD_PICK:
                return new CommandWrapper(CommandType.SELECT_FIRST_PLAYER,new FirstPlayerPickCommand(SERVER_ID, match.getCurrentPlayer().getId(),connectedPlayers));
            case FIRST_PLAYER_PICK:
                return new CommandWrapper(CommandType.PLACE_WORKERS,new WorkerPlaceCommand(SERVER_ID, match.getCurrentPlayer().getId(), match.getCurrentMap().cellWithoutWorkers()));
            case WORKER_PLACE:
                return new CommandWrapper(CommandType.ACTION_TIME, new ActionCommand(SERVER_ID, match.getCurrentPlayer().getId(),match.getNextActions(match.getCurrentPlayer())));
            case GAME:
                var winner = match.getWinner();
                return new CommandWrapper(CommandType.END_GAME, new EndGameCommand(SERVER_ID, virtualProxy.getBroadCastID(),  winner == null ? EndGameCommand.INTERRUPTED_GAME : winner.getId() ));
        }

        return null; //never here
    }

    /**
     * Method called if state or player are not changed after a run command
     * create the next command based on Current State of game and Current Player of game
     * @return new command to be Sent
     */
    private CommandWrapper repeatCommand() {
        switch (match.getCurrentState()) {
            case WAIT:
                return null; //start failed then don't send a command back
            case GOD_FILTER:
                return new CommandWrapper(CommandType.FILTER_GODS, new FilterGodCommand(SERVER_ID,match.getCurrentPlayer().getId()));
            case GOD_PICK:
                return new CommandWrapper(CommandType.PICK_GOD, new PickGodCommand(SERVER_ID, match.getCurrentPlayer().getId(), match.getAllowedCardIDs()));
            case FIRST_PLAYER_PICK:
                return new CommandWrapper(CommandType.SELECT_FIRST_PLAYER, new FirstPlayerPickCommand(SERVER_ID, match.getCurrentPlayer().getId(), connectedPlayers));
            case WORKER_PLACE:
                return new CommandWrapper(CommandType.PLACE_WORKERS, new WorkerPlaceCommand(SERVER_ID, match.getCurrentPlayer().getId(),match.getCurrentMap().cellWithoutWorkers()));
            case GAME:
                return new CommandWrapper(CommandType.ACTION_TIME, new ActionCommand(SERVER_ID, match.getCurrentPlayer().getId(),match.getNextActions(match.getCurrentPlayer())));
        }
        return null;
    }

    /**
     * Create and send a command with correct information for acknowledgment
     * @param cmd JOIN command received
     * @param successfulJoining true if player joined the game, false if player could't join
     * @param reason 0 - if join is successful, 1 - if can't join match, 2 - if username is unavailable
     */
    private void ackJoin(JoinCommand cmd, Boolean successfulJoining, boolean reason){
        CommandWrapper next = new CommandWrapper(CommandType.JOIN,new JoinCommand(cmd.getTarget(), cmd.getSender(),successfulJoining, reason,host));
        lastSent = next;
        System.out.println("Player joined:" + successfulJoining);
        virtualProxy.send(next);
    }

    /**
     * check if a username is already used for a player in the game
     * @param username new player's username
     * @return true if username is available
     */
    private boolean usernameAvailability(String username){
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
    private void sendUpdate(CommandWrapper lastCommand){
        if(lastCommand.getType() == CommandType.PLACE_WORKERS || lastCommand.getType() == CommandType.ACTION_TIME) {
            CommandWrapper next = new CommandWrapper(CommandType.UPDATE, new UpdateCommand(SERVER_ID, BROADCAST_ID,match.getCurrentMap()));

            System.out.println("Sending map update to everyone");
            virtualProxy.send(next);
        }
    }

    /**
     * invoke this if a player met a lose condition
     * send a command for defeat update
     * @param player loser player
     */
    private void metLoseCondition(int player){
        CommandWrapper cmd = new CommandWrapper(CommandType.END_GAME,new EndGameCommand(SERVER_ID, player, EndGameCommand.TARGET_LOST));
        System.out.println("Sending lose notification for player: "+ player);
        virtualProxy.send(cmd);
    }

    /**
     * send a command
     * @param cmdWrap next command to send
     */
    private void sendNextCommand(CommandWrapper cmdWrap){
        if(cmdWrap != null){
            lastSent = cmdWrap;
            virtualProxy.send(cmdWrap);
        }
    }
}

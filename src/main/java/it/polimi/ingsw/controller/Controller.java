package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.*;
import it.polimi.ingsw.network.INetworkAdapter;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.game.Game.GameState.END;

public class Controller {
    private static final int SERVER_ID = -1111;
    private static final int BROADCAST_ID = -2222;
    private static final int MAX_PLAYERS = 3;
    private static final int MIN_PLAYERS = 2;
    private static final int HEIGHT = 7;
    private static final int LENGTH = 7;



    private List<Player> connected_players;
    private Game match;
    private INetworkAdapter virtualProxy;
    private CommandWrapper lastSent;
    private int host;
    private  int nextPlayer;

    /*
    outgoing command description
    BaseCommand = -
    JoinCommand = notification for successful(or not) joining TODO:notifcation also for leave ? (needed by network ??)
    StartCommand = notification of started game, int player's ID
    FilterGodCommand = request, no payload
    PickGodCommand = request, available card's id
    FirstPlayerPickCommand = request, id and username of connected players in this game
    WorkerPlaceCommand = request, send available Vector2
    ActionTimeCommand = request, send pair(id worker,n. available cell), all Vector2 available cell, all action's names
    EndGameCommand = update, boolean lose/win notification
    UpdateCommand = update, first 49 int element are map, from 49 to end are pairs(id owner,id worker), Vector2 element for workers position on map
     */

    /*
    incoming command description
    BaseCommand = -
    JoinCommand = username want to play, boolean if i want to join/leave
    StartCommand = notification (from host) to start a match
    FilterGodCommand = god's id want to be allowed(from host)
    PickGodCommand = chosen god id
    FirstPlayerPickCommand = id of first player (from host)
    WorkerPlaceCommand = two Vector2 for client's two worker
    ActionTimeCommand = selected worker,action id, target position Vector2
    EndGameCommand = -
    UpdateCommand = -
     */

    public Controller (INetworkAdapter adapter){
        connected_players = new ArrayList<>();
        virtualProxy = adapter;
        lastSent = null;
    }

    @Deprecated
    public Controller(){
        connected_players = new ArrayList<>();
        lastSent = null;
    }

    // **********************************************************************************************
    // Getter (for tests only)
    // **********************************************************************************************

    public Game getMatch() { return match; }

    public CommandWrapper getLastSent() { return lastSent; }

    public List<Player> getConnected_players() { return connected_players; }

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
        JoinCommand joinCommand;
        if(cmdWrapper == null) return;
        else   joinCommand = cmdWrapper.getCommand(JoinCommand.class);

        if (match == null || match.isEnded()) {
            match = new Game();
            host = joinCommand.getSender();
        }

        if(joinCommand.getUsername() == null){
            ackJoin(joinCommand,false);
            return;
        }

        String username = joinCommand.getUsername();
        if(usernameAvailability(username)) {
            Player p = new Player(joinCommand.getSender(),username);

            if(match.join(p)){
                connected_players.add(p);
                ackJoin(joinCommand,true); //player connected to current game
                if(match.getPlayers().size() == MAX_PLAYERS ){
                    match.start(getPlayer(host));
                    virtualProxy.SendBroadcast(new CommandWrapper(CommandType.START,new StartCommand(CommandType.START.toInt(),false,BROADCAST_ID,SERVER_ID,idsToArray())));
                    sendNextCommand(new CommandWrapper(CommandType.FILTER_GODS,new FilterGodCommand(CommandType.FILTER_GODS.toInt(),true,host,SERVER_ID))); //first command
                }
            }else
                ackJoin(joinCommand,false); //connection failed

        }else
            ackJoin(joinCommand,false); //can be fixed with return a usernameError value
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


        if(!filterCommandType(cmdWrapper)) return; //command not expected

        try {

            if (runCommand(cmdWrapper)) {
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
                nextCmd = lastSent;
            }

            sendNextCommand(nextCmd);
        } catch (NotAllowedOperationException e) {
            //it's not your turn to action
        }

    }

    /**
     * Callback function called by the network layer when a player leave the game
     *
     * @param id player id
     */
    public void onDisconnect(int id) {
        if (match == null || match.isEnded())
            return;

        match.left(getPlayer(id));

        //TODO: send notification for leave ?

        if(match.getCurrentState() == END)
            sendNextCommand(new CommandWrapper(CommandType.END_GAME,new EndGameCommand(CommandType.END_GAME.toInt(),false,SERVER_ID,match.getCurrentPlayer().getId(),true)));

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
        for (Player p : connected_players) {
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
        if(match.getCurrentState() != Game.GameState.WAIT && cmd.getType() == CommandType.START) return false;

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
        BaseCommand baseCommand = cmd.getCommand(BaseCommand.class);
        Player p = getPlayer(baseCommand.getSender());
        nextPlayer = baseCommand.getSender();

        switch (cmd.getType()) {
            case START:
                return match.start(p);
            case FILTER_GODS:
                FilterGodCommand filterGodCommand = cmd.getCommand(FilterGodCommand.class);
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
                int res = match.executeAction(p, actionCommand.getIdWorkerNMove()[0], actionCommand.getIdWorkerNMove()[1], actionCommand.getAvaialablePos()[0]);
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
                //virtualProxy.SendBroadcast(new CommandWrapper(CommandType.START,new StartCommand(CommandType.START.toInt(),false,BROADCAST_ID,SERVER_ID,idsToArray()))) ;
                return new CommandWrapper(CommandType.FILTER_GODS, new FilterGodCommand(CommandType.FILTER_GODS.toInt(), true,SERVER_ID, match.getCurrentPlayer().getId()));
            case GOD_FILTER:
                return new CommandWrapper(CommandType.PICK_GOD,new PickGodCommand(CommandType.PICK_GOD.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(), match.getAllowedCardIDs()));
            case GOD_PICK:
                int[] ids = idsToArray();
                String[] usernames = usernamesToArray();
                return new CommandWrapper(CommandType.SELECT_FIRST_PLAYER,new FirstPlayerPickCommand(CommandType.SELECT_FIRST_PLAYER.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(), ids, usernames));
            case FIRST_PLAYER_PICK:
                Vector2[] pos = match.getCurrentMap().cellWithoutWorkers().toArray(new Vector2[0]);
                return new CommandWrapper(CommandType.PLACE_WORKERS,new WorkerPlaceCommand(CommandType.PLACE_WORKERS.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(), pos));
            case WORKER_PLACE:
                String[] actionName = actionNameToArray(match.getNextActions(match.getCurrentPlayer()));
                int[] idWorkerNMove = idWorkerNMoveToArray(match.getNextActions(match.getCurrentPlayer()));
                Vector2[] availablePosPerAction = availablePosToArray(match.getNextActions(match.getCurrentPlayer()));
                return new CommandWrapper(CommandType.ACTION_TIME, new ActionCommand(CommandType.ACTION_TIME.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(),idWorkerNMove,availablePosPerAction,actionName));
            case GAME:
                return new CommandWrapper(CommandType.END_GAME, new EndGameCommand(CommandType.END_GAME.toInt(),false,SERVER_ID,match.getCurrentPlayer().getId(),true));
        }

        return null; //never here
    }

    /**
     * Method called if state or player are not changed after a run command
     * create the next command baseod on Current State of game and Current Player of game
     * @return new command to be Sent
     */
    private CommandWrapper repeatCommand() {
        switch (match.getCurrentState()) {
            case WAIT:
                return null; //start failed then don't send a command back
            case GOD_FILTER:
                return new CommandWrapper(CommandType.FILTER_GODS, new FilterGodCommand(CommandType.FILTER_GODS.toInt(),true,SERVER_ID,match.getCurrentPlayer().getId()));
            case GOD_PICK:
                return new CommandWrapper(CommandType.PICK_GOD, new PickGodCommand(CommandType.PICK_GOD.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(), match.getAllowedCardIDs()));
            case FIRST_PLAYER_PICK:
                int[] ids = idsToArray();
                String[] usernames = usernamesToArray();
                return new CommandWrapper(CommandType.SELECT_FIRST_PLAYER, new FirstPlayerPickCommand(CommandType.SELECT_FIRST_PLAYER.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(), ids, usernames));
            case WORKER_PLACE:
                Vector2[] pos = match.getCurrentMap().cellWithoutWorkers().toArray(new Vector2[0]);
                return new CommandWrapper(CommandType.PLACE_WORKERS, new WorkerPlaceCommand(CommandType.PLACE_WORKERS.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(),pos));
            case GAME:
                String[] actionName = actionNameToArray(match.getNextActions(match.getCurrentPlayer()));
                int[] idWorkerNMove = idWorkerNMoveToArray(match.getNextActions(match.getCurrentPlayer()));
                Vector2[] availablePosPerAction = availablePosToArray(match.getNextActions(match.getCurrentPlayer()));
                return new CommandWrapper(CommandType.ACTION_TIME, new ActionCommand(CommandType.ACTION_TIME.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(),idWorkerNMove,availablePosPerAction,actionName));
        }
        return null;
    }

    /**
     * Create and send a command with correct information for acknowledgment
     * @param cmd JOIN command received
     * @param successfulJoining true if player joined the game, false if player could't join
     */
    private void ackJoin(JoinCommand cmd, Boolean successfulJoining){
        int id = cmd.getSender();
        CommandWrapper next = new CommandWrapper(CommandType.JOIN,new JoinCommand(CommandType.JOIN.toInt(),false, cmd.getTarget(), cmd.getSender(),successfulJoining));
        //virtualProxy.Send(id,next);
    }

    /**
     * utility method
     * every possible position available for next actions of a player are set in vector2 array
     * @param list NextAction list
     * @return Vector2 array of every available position
     */
    private Vector2[] availablePosToArray(List<NextAction> list){
        ArrayList<Vector2> newList = new ArrayList<>();

        for (NextAction nextAction : list) {
            newList.addAll(nextAction.getAvailable_position());
        }

        return newList.toArray(new Vector2[0]);
    }

    /**
     * utility method
     * pair of int set with (id worker, number of possible position available with that worker)
     * @param list NextAction list
     * @return int array of pair (worker, n. possible position)
     */
    private int[] idWorkerNMoveToArray(List<NextAction> list){
        int[] array = new int[list.size()*2];

        for(int i = 0,j = 0 ; i<list.size() ; i++ ){
            array[j] = list.get(i).getWorker();
            array[j+1] = list.get(i).getAvailable_position().size();
            j = j+2;
        }
        return array;
    }

    /**
     * utility method
     * string array of every name of possible actions
     * @param list NextAction list
     * @return String array of possible next action's name
     */
    private String[] actionNameToArray(List<NextAction> list){
        String[] array = new String[list.size()];

        for(int i=0 ;i<list.size();i++){
            array[i] = list.get(i).getActionName();
        }

        return array;
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
     * utility method
     * every id of players
     * @return array id of every player
     */
    private int[] idsToArray(){
        int[] ids = new int[connected_players.size()];

        for(int i = 0; i < connected_players.size(); i++ )
            ids[i] = connected_players.get(i).getId();

        return ids;
    }

    /**
     * every username of players
     * @return array username of every player
     */
    private String[] usernamesToArray(){
        String[] usernames = new String[connected_players.size()];

        for(int i = 0; i < connected_players.size(); i++ )
            usernames[i] = connected_players.get(i).getUsername();

        return usernames;
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
            Map map = match.getCurrentMap();

            int[] intMap = new int[(HEIGHT * LENGTH)+(map.getWorkers().size()*2)];
            for(int x = 0, i = 0; i < LENGTH; i++){
                for(int j = 0; j < HEIGHT ; j++){
                    intMap[x] = map.getMap()[i][j];
                    x++;
                }
            }

            for(int i = HEIGHT * LENGTH,j = 0; j<map.getWorkers().size();i=i+2,j++){
                intMap[i] = map.getWorkers().get(j).getOwner().getId();
                intMap[i+1] = map.getWorkers().get(j).getId();
            }

            Vector2[] vector = new Vector2[map.getWorkers().size()];
            for(int i=0; i<map.getWorkers().size();i++)
                vector[i] = map.getWorkers().get(i).getPosition();


            CommandWrapper next = new CommandWrapper(CommandType.UPDATE, new UpdateCommand(CommandType.UPDATE.toInt(), false, SERVER_ID, BROADCAST_ID, intMap,vector));
            //virtualProxy.SendBroadcast(next);
        }
    }

    /**
     * invoke this if a player met a lose condition
     * send a command for defeat update
     * @param player loser player
     */
    private void metLoseCondition(int player){

        CommandWrapper cmd = new CommandWrapper(CommandType.END_GAME,new EndGameCommand(CommandType.END_GAME.toInt(),false,SERVER_ID,player,false));
        //virtualProxy.Send(player,cmd);
    }

    /**
     * send a command
     * @param cmdWrap next command to send
     */
    private void sendNextCommand(CommandWrapper cmdWrap){
        if(cmdWrap != null){
            lastSent = cmdWrap;
            //virtualProxy.Send(nextPlayer,cmd);
        }
    }
}

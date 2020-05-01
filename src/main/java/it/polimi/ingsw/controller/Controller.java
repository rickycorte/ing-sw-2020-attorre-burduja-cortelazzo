package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.*;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;

import java.lang.reflect.Array;
import java.nio.channels.NetworkChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static it.polimi.ingsw.game.Game.GameState.END;
import static it.polimi.ingsw.game.Game.GameState.GAME;

public class Controller {
    private static final int SERVER_ID = -1111;
    private static final int BROADCAST_ID = -2222;
    private static final int MAX_PLAYERS = 3;
    private static final int HEIGHT = 7;
    private static final int LENGTH = 7;



    private List<Player> connected_players;
    private Game match;
    private INetworkAdapter virtualProxy;
    private Command lastSent;
    private int host;

    /*
    outgoing command description
    JOIN : -
    LEAVE : -
    START : -
    FILTER_GOD : request
    PICK_GOD : request, this cmd is Sent with available card's ids
    FIRST_PLAYER_PICK : request, send ids and usernames of connected players in this game
    WORKER_PLACE : request, send available Vector2 in map
    ACTION_TIME : request, send int[0] id worker, int [1] n. of avaialble cell, Vector2[0 to int[1]] available cell,String[0] action name
    LOSER : update, lose notification
    WINNER : update, win notification
    ACK_JOIN : update, join successful/not
    UPDATE : update, first 49 [0-48] are map int, from 49 to end are pairs id owner - id worker
     */

    /*
    incoming command description
    JOIN : -
    LEAVE : - (network send only id to onDisconnect)
    START : -
    FILTER_GOD : (host only)send ids of card want to be allowed
    PICK_GOD : send selected god id
    FIRST_PLAYER_PICK : (host only) send id of first player
    WORKER_PLACE : send Vector2 with 2 positions for workers
    ACTION_TIME : send worker selected(forced if turn started already),action id for graph(selectAction),Vector2 target
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

    public Command getLastSent() { return lastSent; }

    public List<Player> getConnected_players() { return connected_players; }

    // **********************************************************************************************
    // Operation
    // **********************************************************************************************

    /**
     * Callback function called by the network layer when a new player joins the game
     * Create a new game if necessary
     * Send an acknowledgment with identifier if player joined correctly (negative identifier if can't join)
     * wait for Start command, or start a game if number of players reached MAX_PLAYERS
     * @param cmd join command
     */
    public void onConnect(Command cmd) {
        if (match == null || match.isEnded()) {
            match = new Game();
            host = cmd.getSender();
        }

        if(cmd.getStringData() == null){
            ackJoin(cmd,false);
            return;
        }

        String username = cmd.getStringData()[0];
        if(usernameAvailability(username)) {
            Player p = new Player(cmd.getSender(),username);

            if(match.join(p)){
                connected_players.add(p);
                ackJoin(cmd,true); //player connected to current game
                if(match.getPlayers().size() == MAX_PLAYERS ){
                    match.start(getPlayer(host));
                    sendNextCommand(new Command(Command.CType.FILTER_GODS.toInt(),true,host,SERVER_ID)); //first command
                }
            }else
                ackJoin(cmd,false); //connection failed

        }else
            ackJoin(cmd,false); //can be fixed with return a usernameError value
    }

    /**
     * Callback function called by the network layer when a command is received
     * Discard the command if not allowed for the sender
     * Send a new command to client
     * @param cmd command issued by a client
     */
    public void onCommand(Command cmd) {
        Game.GameState prevGameState = match.getCurrentState();
        Player prevPlayer = match.getCurrentPlayer();

        Command nextCmd;

        if(!filterCommandType(cmd)) return; //se comando diverso da atteso

        try {

            if (runCommand(cmd)) {
                //command executed
                if (prevGameState != match.getCurrentState()) {
                    //different state
                    nextCmd = changedState(cmd, prevGameState);
                } else {
                    //different player same state OR same player same state
                    nextCmd = repeatCommand(cmd);
                }
                sendUpdate(cmd);
            } else {
                //command failed
                nextCmd = repeatCommand(cmd);
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

        if(match.getCurrentState() == END)
            sendNextCommand(new Command(Command.CType.WINNER.toInt(),false,SERVER_ID,match.getCurrentPlayer().getId()));
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
    private boolean filterCommandType(Command cmd){
        if(match.getCurrentState() != Game.GameState.WAIT && cmd.getType() == Command.CType.START) return false;

        return lastSent == null || lastSent.getType() == cmd.getType();
    }

    /**
     * Run command on a selected game
     *
     * @param cmd command to run
     * @return false if a command execution failed
     * @throws NotAllowedOperationException if a not allowed command is run, it's not sender turn
     */
    private boolean runCommand(Command cmd) throws NotAllowedOperationException {
        Player p = getPlayer(cmd.getSender());

        switch (cmd.getType()) {
            case START:
                return match.start(p);
            case FILTER_GODS:
                return match.applyGodFilter(p, cmd.getIntData());
            case PICK_GOD:
                return match.selectGod(p, cmd.getIntData()[0]);
            case SELECT_FIRST_PLAYER:
                return match.selectFirstPlayer(p, getPlayer(cmd.getIntData()[0]));
            case PLACE_WORKERS:
                return match.placeWorkers(p, cmd.getV2Data());
            case ACTION_TIME:{
                int res = match.executeAction(p, cmd.getIntData()[0], cmd.getIntData()[1], cmd.getV2Data()[0]);
                if(res>0)
                    return true;
                else if(res == 0) {
                    metLoseCondition(cmd.getSender());
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
     * create the next command
     * @param cmd command already executed
     * @param previousGameState Game.State before the command execution
     * @return new command to be Sent
     */
    private Command changedState(Command cmd, Game.GameState previousGameState) {

        switch (previousGameState) {
            case WAIT:
                return new Command(Command.CType.FILTER_GODS.toInt(), true,SERVER_ID, match.getCurrentPlayer().getId());
            case GOD_FILTER:
                return new Command(Command.CType.PICK_GOD.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(), match.getAllowedCardIDs());
            case GOD_PICK:
                int[] ids = idsToArray();
                String[] usernames = usernamesToArray();
                return new Command(Command.CType.SELECT_FIRST_PLAYER.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(), ids, usernames);
            case FIRST_PLAYER_PICK:
                Vector2[] pos = match.getCurrentMap().cellWithoutWorkers().toArray(new Vector2[0]);
                return new Command(Command.CType.PLACE_WORKERS.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(), pos);
            case WORKER_PLACE:
                String[] actionName = actionNameToArray(match.getNextActions(match.getCurrentPlayer()));
                int[] idWorkerNMove = idWorkerNMoveToArray(match.getNextActions(match.getCurrentPlayer()));
                Vector2[] availablePosPerAction = availablePosToArray(match.getNextActions(match.getCurrentPlayer()));
                return new Command(Command.CType.ACTION_TIME.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(),idWorkerNMove,availablePosPerAction,actionName);
            case GAME:
                return new Command(Command.CType.WINNER.toInt(),false,SERVER_ID,match.getCurrentPlayer().getId());
        }

        return null; //never here
    }

    /**
     * Method called if state or player are not changed after a run command
     * create the next command
     * @param cmd command just runned
     * @return new command to be Sent
     */
    private Command repeatCommand(Command cmd) {
        //if it's GAME then send next possible actions (should send with starting worker chose)
        switch (match.getCurrentState()) {
            case WAIT:
                return null; //start failed then don't send a command back
            case GOD_FILTER:
                return new Command(cmd.getType().toInt(),true,SERVER_ID,cmd.getSender());
            case GOD_PICK:
                return new Command(cmd.getType().toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(), match.getAllowedCardIDs());
            case FIRST_PLAYER_PICK:
                int[] ids = idsToArray();
                String[] usernames = usernamesToArray();
                return new Command(Command.CType.SELECT_FIRST_PLAYER.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(), ids, usernames);
            case WORKER_PLACE:
                Vector2[] pos = match.getCurrentMap().cellWithoutWorkers().toArray(new Vector2[0]);
                return new Command(Command.CType.PLACE_WORKERS.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(),pos);
            case GAME:
                String[] actionName = actionNameToArray(match.getNextActions(match.getCurrentPlayer()));
                int[] idWorkerNMove = idWorkerNMoveToArray(match.getNextActions(match.getCurrentPlayer()));
                Vector2[] availablePosPerAction = availablePosToArray(match.getNextActions(match.getCurrentPlayer()));
                return new Command(Command.CType.ACTION_TIME.toInt(), true, SERVER_ID, match.getCurrentPlayer().getId(),idWorkerNMove,availablePosPerAction,actionName);
        }
        return null;
    }

    /**
     * Create and send a command with correct information for acknowledgment
     * @param cmd JOIN command received
     * @param correctJoin true if player joined the game
     */
    private void ackJoin(Command cmd, Boolean correctJoin){
        int id = cmd.getSender();
        if(correctJoin){
            Command next = new Command(Command.CType.ACK_JOIN.toInt(),false, cmd.getTarget(), cmd.getSender(),id);
            //virtualProxy.Send(id,next);
        }else{
            Command next = new Command(Command.CType.ACK_JOIN.toInt(),false,cmd.getTarget(),cmd.getSender(),-id);
            //virtualProxy.Send(id,next);
        }
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
     * @param lastCommand
     */
    private void sendUpdate(Command lastCommand){
        if(lastCommand.getType() == Command.CType.PLACE_WORKERS || lastCommand.getType() == Command.CType.ACTION_TIME) {
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

            Command next = new Command(Command.CType.UPDATE.toInt(), false, SERVER_ID, BROADCAST_ID, intMap,vector);
            //virtualProxy.SendBroadcast(next);
        }
    }

    /**
     * invoke this if a player met a lose condition
     * send a loser update command
     * @param player loser player
     */
    private void metLoseCondition(int player){
        Command cmd = new Command(Command.CType.LOSER.toInt(),false,lastSent.getSender(),player);
        //virtualProxy.Send(player,cmd);
    }

    /**
     * send a command
     * @param cmd next command to send
     */
    private void sendNextCommand(Command cmd){
        if(cmd != null){
            lastSent = cmd;
            //virtualProxy.Send(cmd.getTarget(),cmd);
        }
    }
}

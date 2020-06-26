package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.*;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkForwarder;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


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

    private Timer undoCheckTimer;
    private Player[] prevPlayers;


    private CommandType allowedCommandType;
    private boolean alreadySentEndGame;


    public Controller (INetworkForwarder adapter){
        connectedPlayers = new ArrayList<>();
        network = adapter;
        lastSent = null;
        allowedCommandType = CommandType.START;
        alreadySentEndGame = false;
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
            match.enableUndo();
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
            sendCommand(JoinCommand.makeReplyOk(network.getServerID(), joinCommand.getSender(), joinCommand.getUsername() , match.getHost().getId()));
            // auto start match with three players
            if(match.playerCount() == Game.MAX_PLAYERS)
            {
                // start game
                runStartCommand(new StartCommand(match.getHost().getId(), network.getServerID())); // start game by emulating start command from host
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
        if(match.isEnded()) return; // cant accept commands if match is ended

        try
        {
            executeCommand(cmdWrapper);
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

        connectedPlayers.remove(getPlayer(cmd.getSender()));

        if(match.getCurrentState() == Game.GameState.END)
        {
            sendCommand(EndGameCommand.makeWrapped(network.getServerID(), network.getBroadCastID(), match.getWinner()));
        }
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
        for (Player p : connectedPlayers) {
            if (p.getId() == id)
                return p;
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
     * Check if a command is allowed to run or not
     * @param cmd command to check
     * @return true is command is allowed
     */
    private boolean allowedCommand(CommandWrapper cmd)
    {
        return !match.isEnded() || cmd.getType() == allowedCommandType;
    }


    /**
     * Execute an incoming command
     * @param cmd command to execute
     * @throws NotAllowedOperationException if sender of the command can't run the command
     */
    private void executeCommand(CommandWrapper cmd) throws NotAllowedOperationException
    {
        prevPlayers = match.getPlayers().toArray(Player[]::new);

        if(cmd == null || !allowedCommand(cmd))
        {
            System.out.println("[CONTROLLER] Got a not allowed command "+ cmd);
        }

        switch (cmd.getType())
        {
            case START:
                runStartCommand(cmd.getCommand(StartCommand.class));
                break;
            case FILTER_GODS:
                runFilterGodCommand(cmd.getCommand(FilterGodCommand.class));
                break;
            case PICK_GOD:
                runPickGodCommand(cmd.getCommand(PickGodCommand.class));
                break;
            case SELECT_FIRST_PLAYER:
                runSelectFirstPlayerCommand(cmd.getCommand(FirstPlayerPickCommand.class));
                break;
            case PLACE_WORKERS:
                runWorkerPlaceCommand(cmd.getCommand(WorkerPlaceCommand.class));
                break;
            case ACTION_TIME:
                runActionCommand(cmd.getCommand(ActionCommand.class));
                break;
        }

        runEndGameDetection(prevPlayers);

    }


    /**
     * Send a command to connected clients
     * @param cmdWrap next command to send
     */
    private void sendCommand(CommandWrapper cmdWrap)
    {
        if(cmdWrap != null)
        {
            if(cmdWrap.getType() != CommandType.UPDATE)
                lastSent = cmdWrap;

            network.send(cmdWrap);
        }
    }


    //****************************************************************************************************************
    // Command handlers

    /**
     * Execute a start command
     * If command is correct the match is started and filter god request is sent
     * If data is not correct nothing is performed
     * @param cmd start command to execute
     */
    private void runStartCommand(StartCommand cmd)
    {
        if(cmd == null || match.getCurrentState() != Game.GameState.WAIT)
        {
            System.out.println("[CONTROLLER] Unexpected "+ cmd+" when game is in "+ match.getCurrentState());
            return;
        }

        if(match.start(getPlayer(cmd.getSender())))
        {
            // send start notification to everyone
            sendCommand(StartCommand.makeReply(network.getServerID(), network.getBroadCastID(), connectedPlayers));
            // start filter god phase
            sendCommand(FilterGodCommand.makeRequest(network.getServerID(), match.getHost().getId()));
            allowedCommandType = CommandType.FILTER_GODS;
        }
    }

    /**
     * Execute a Filter God Command
     * If command is correct god filter is applied to the match and the first God pick command is issued
     * If command is not correct god filter request is repeated
     * @param cmd god filter command to run
     * @throws NotAllowedOperationException if sender of the command can't run the command
     */
    private void runFilterGodCommand(FilterGodCommand cmd) throws NotAllowedOperationException
    {
        if(cmd == null || match.getCurrentState() != Game.GameState.GOD_FILTER)
        {
            System.out.println("[CONTROLLER] Unexpected "+ cmd+" when game is in "+ match.getCurrentState());
            return;
        }

        if(match.applyGodFilter(getPlayer(cmd.getSender()), cmd.getGodFilter()))
        {
            //start god pick phase
            sendCommand(PickGodCommand.makeRequest(network.getServerID(), match.getCurrentPlayer().getId(), match.getAllowedCardIDs()));
            allowedCommandType = CommandType.PICK_GOD;
        }
        else
        {
            // send again required action
            sendCommand(FilterGodCommand.makeRequest(network.getServerID(), match.getHost().getId()));
        }
    }


    /**
     * Execute god pick command
     * If command is correct a new god pick command is issued if there are some players that still need to chose a god otherwise
     * Select First Player Command is issued
     * If command is not correct god pick command is repeated for the player that sent a broken request
     * @param cmd god pick command to execute
     * @throws NotAllowedOperationException if sender of the command can't run the command
     */
    private void runPickGodCommand(PickGodCommand cmd) throws NotAllowedOperationException
    {
        if(cmd == null || match.getCurrentState() != Game.GameState.GOD_PICK)
        {
            System.out.println("[CONTROLLER] Unexpected "+ cmd +" when game is in "+ match.getCurrentState());
            return;
        }

        // if pick is applied and we moved to the next stage of game
        if(match.selectGod(getPlayer(cmd.getSender()), cmd.getPickedGodID()) && match.getCurrentState() != Game.GameState.GOD_PICK)
        {
            // start select first player phase
           sendCommand(FirstPlayerPickCommand.makeRequest(network.getServerID(), match.getHost().getId(), connectedPlayers));
           allowedCommandType = CommandType.SELECT_FIRST_PLAYER;
        }
        else
        {
            // send again required action
            sendCommand(PickGodCommand.makeRequest(network.getServerID(), match.getCurrentPlayer().getId(), match.getAllowedCardIDs()));
        }

    }


    /**
     * Execute Select First Player Command
     * If command is correct the first Worker Place command is issued to the selected first player
     * If command is not correct Select First Player Command is issued again
     * @param cmd first player command to run
     * @throws NotAllowedOperationException if sender of the command can't run the command
     */
    private void runSelectFirstPlayerCommand(FirstPlayerPickCommand cmd) throws NotAllowedOperationException
    {
        if(cmd == null || match.getCurrentState() != Game.GameState.FIRST_PLAYER_PICK)
        {
            System.out.println("[CONTROLLER] Unexpected "+ cmd +" when game is in "+ match.getCurrentState());
            return;
        }

        if(match.selectFirstPlayer(getPlayer(cmd.getSender()), getPlayer(cmd.getPickedPlayerID())))
        {
            // move to the next phase, worker place
            sendCommand(WorkerPlaceCommand.makeWrapped(network.getServerID(), match.getCurrentPlayer().getId(), match.getCurrentMap().cellWithoutWorkers()));
            allowedCommandType = CommandType.PLACE_WORKERS;
        }
        else
        {
            // send again required action
            sendCommand(FirstPlayerPickCommand.makeRequest(network.getServerID(), match.getHost().getId(), connectedPlayers));
        }
    }


    /**
     * Execute Worker Place Command
     * If command is correct issue a new Worker Place Command if there are players that still require to place workers
     * otherwise start the match and send the first ActionCommand
     * If command is not correct issue again the Worker Place Command to the player who sent a broken command
     * @param cmd worker place command to run
     * @throws NotAllowedOperationException if sender of the command can't run the command
     */
    private void runWorkerPlaceCommand(WorkerPlaceCommand cmd) throws NotAllowedOperationException
    {
        if(cmd == null || match.getCurrentState() != Game.GameState.WORKER_PLACE)
        {
            System.out.println("[CONTROLLER] Unexpected "+ cmd +" when game is in "+ match.getCurrentState());
            return;
        }

        var res = match.placeWorkers(getPlayer(cmd.getSender()), cmd.getPositions());

        // send map update
        if(res)
            sendCommand(UpdateCommand.makeWrapped(network.getServerID(), network.getBroadCastID(), match.getCurrentMap()));

        if(res && match.getCurrentState() != Game.GameState.WORKER_PLACE)
        {
            //move to game phase if noone has to place workers
            sendActionsToCurrentPlayer();
            allowedCommandType = CommandType.ACTION_TIME;
        }
        else
        {
            // request again if action failed, or make a request to another player
            sendCommand(WorkerPlaceCommand.makeWrapped(network.getServerID(), match.getCurrentPlayer().getId(), match.getCurrentMap().cellWithoutWorkers()));
        }


    }

    /**
     * Return true if one of the actions passed as parameter is an undo action
     * @param nextActions list of actions to check
     * @return true is there is an undo action
     */
    private boolean hasUndoAction(List<NextAction> nextActions)
    {
        for (NextAction a: nextActions)
        {
            if(a.isUndo()) return true;
        }

        return false;
    }

    /**
     * Generate and send next actions for the current game state
     * This function takes care to send actions in any kind of situation.
     * For example it sends actions for every phase of the current player turn but is also capable to understand
     * that a new turn has been started and act accordingly.
     *
     * End game checks are also performed if the game ends during an action
     * this function sends END GAME notifications to players
     * Lastly during action generations undo checks are performed to understand if its required to start an active timer
     * to detect undo expire on possible game ends
     */
    private void sendActionsToCurrentPlayer()
    {
        var nextActions = match.getNextActions();

        if(match.isEnded()) // no more actions to do for a player that caused the game to end
        {
            sendCommand(EndGameCommand.makeWrapped(network.getServerID(), network.getBroadCastID(), match.getWinner()));
            alreadySentEndGame = true;
            return;
        }
        else
        {
            if(nextActions == null) // no action for a player; player lost in 3p match (will be detected by end game checks)
                nextActions = match.getNextActions();

            // start active timer to check undo action expire only if its used as last possible action
            if(nextActions.size() == 1 && hasUndoAction(nextActions))
                startUndoLoseCheckTimer();
        }

        sendCommand(ActionCommand.makeRequest(network.getServerID(), match.getCurrentPlayer().getId(), nextActions));
    }


    /**
     * Execute an Action Command
     * If command is correct an action is run then the new map is updated. After action run a new Action command is issued for the current player or for the next
     * one if the turn changed after the execution
     * This is the last and final game state that is ended only when a end game check detects that the match is ended
     * If command is not correct the previous action command is issued
     *
     * Notice that if undo is enabled in the game (by default it is) next actions sent to clients expire due to undo "timer"
     * If timer ends for an undo action it wont be returned as next action, but no active notification is sent to clients when their undo move
     * is not available anymore. If an undo command fails because of the timer and the player tries to run that action it will fail
     * as other action with wrong data and a new action command is issued to the same client.
     * @param cmd action command to execute
     * @throws NotAllowedOperationException if sender of the command can't run the command
     */
    private void runActionCommand(ActionCommand cmd) throws NotAllowedOperationException
    {
        if(cmd == null || match.getCurrentState() != Game.GameState.GAME)
        {
            System.out.println("[CONTROLLER] Unexpected "+ cmd +" when game is in "+ match.getCurrentState());
            return;
        }

        var selectedAction = cmd.getSelectedAction();
        int res = match.executeAction(getPlayer(cmd.getSender()), selectedAction.getWorkerID(), selectedAction.getActionID(), selectedAction.getPosition());
        stopUndoLoseCheckTimer();

        // send map update only if action run successfully
        if(res > 0)
        {
            sendCommand(UpdateCommand.makeWrapped(network.getServerID(), network.getBroadCastID(), match.getCurrentMap()));
        }
        else if(res == 0 && match.isEnded()) // player who run the actions lost (single player lose is handled by endGameCheck after turn execution)
        {
            sendCommand(EndGameCommand.makeWrapped(network.getServerID(), network.getBroadCastID(), match.getWinner()));
            alreadySentEndGame = true;
            return;
        }

        // create next actions (error, different turn phase, new player)
        sendActionsToCurrentPlayer();
    }

    /**
     * Run checks to understand if game is ended and send END_GAME notifications if end is found
     * @param prevPlayers player list before command execution
     */
    private void runEndGameDetection(Player[] prevPlayers)
    {
        if(alreadySentEndGame) return;

        if(match.playerCount() < prevPlayers.length && match.getCurrentState() != Game.GameState.WAIT)
        {
            System.out.println("[CONTROLLER] Player count decreased, from "+ prevPlayers.length +" to "+ match.playerCount());
            if(match.getCurrentState() == Game.GameState.END)
            {
                // match ended
                sendCommand(EndGameCommand.makeWrapped(network.getServerID(), network.getBroadCastID(), match.getWinner()));
                return;
            }

            // lose inside match
            for(int i = 0; i < prevPlayers.length; i++)
            {
                if (!match.getPlayers().contains(prevPlayers[i]))
                {
                    System.out.println("[CONTROLLER] Detected player "+ prevPlayers[i]+ " lost");
                    sendCommand(EndGameCommand.makeLoseSingle(network.getServerID(), prevPlayers[i].getId()));
                    // update map with removed player
                    sendCommand(UpdateCommand.makeWrapped(network.getServerID(), network.getBroadCastID(), match.getCurrentMap()));
                    connectedPlayers.remove(prevPlayers[i]);
                }
            }
        }
    }


    //****************************************************************************************************************
    // Undo

    /**
     * Create a timer that checks if the undo timer is ended,
     * if true run endGameChecks to check if a player lost because he has only undo as last action
     * and didn't run it in the allowed time interval
     */
    private void startUndoLoseCheckTimer()
    {
        if(undoCheckTimer != null)
            undoCheckTimer.cancel();
        System.out.println("[CONTROLLER] Starting undo check");
        undoCheckTimer = new Timer();
        undoCheckTimer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                match.getNextActions(); // force game to update
                runEndGameDetection(prevPlayers);
                System.out.println("[CONTROLLER] Undo check done");
            }
        }, Turn.MAX_UNDO_MILLI + 1);
    }


    /**
     * Cancel the undo lose check timer if its currently running
     * otherwise this function does nothing
     */
    private void stopUndoLoseCheckTimer()
    {
        if(undoCheckTimer != null)
            undoCheckTimer.cancel();
    }
}

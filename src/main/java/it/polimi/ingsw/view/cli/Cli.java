package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.controller.compact.CompactMap;
import it.polimi.ingsw.controller.compact.CompactWorker;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.Map;
import it.polimi.ingsw.game.NextAction;
import it.polimi.ingsw.game.Vector2;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.view.CardCollection;
import it.polimi.ingsw.view.IHumanInterface;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Old cli adapted to use some network changes
 */
public class Cli implements IHumanInterface, ICommandReceiver {
    private static final int HEIGHT = it.polimi.ingsw.game.Map.HEIGHT;
    private static final int LENGTH = it.polimi.ingsw.game.Map.LENGTH;
    private static final int N_WORKER = Game.WORKERS_PER_PLAYER;
    private static final int MIN_PLAYER = Game.MIN_PLAYERS;
    private static final int MAX_PLAYER = Game.MAX_PLAYERS;

    private INetworkAdapter virtualServer;
    private int idPlayer;
    private int idHost;
    private PrintStream stream;
    private Scanner scanner;
    private boolean logged;
    private CardCollection cardCollection;

    private List<Player> players;
    private List<Color> availableColors;

    private NextAction[] nextActions;
    private AtomicBoolean cmdNotify;
    private Vector2[] availablePositions;
    private CompactMap lastCompactMap;
    private int color;

    private enum MatchState {WAIT, WAIT_NEXT, GOD_SELECT, PICK_GOD, FIRST_PLAYER_SELECT, WORKER_PLACE,ACTION_TIME, END, QUIT}
    private MatchState currentState;

    private boolean shouldStop;
    private enum ExecutionResponse {SUCCESS, FAIL, QUIT, ENDED_GAME, BACK}



    public Cli(INetworkAdapter adapter) {
        virtualServer = adapter;
        stream = new PrintStream(System.out, true);
        scanner = new Scanner(System.in);
        logged = false;
        cmdNotify = new AtomicBoolean(false);
        currentState = MatchState.WAIT;
        idPlayer = -1;
        cardCollection = new CardCollection();
        players = new ArrayList<>();
        availableColors = new ArrayList<>();
        availableColors.addAll(Arrays.asList(Color.values()).subList(0,7));
    }

    @Override
    public void onConnect(CommandWrapper cmdWrapper) {
        int targetID = cmdWrapper.getCommand(BaseCommand.class).getTarget();

        if (cmdWrapper.getType() == CommandType.JOIN && idPlayer == -1) {
            JoinCommand cmd = cmdWrapper.getCommand(JoinCommand.class);

            if (cmd.isJoin())
                successfulJoin(cmd);
            else {
                if (cmd.isValidUsername()) stream.println("Can't join a game right now, try another time.");
                else retryJoin(cmd);
            }

        } else if (cmdWrapper.getType() == CommandType.JOIN && targetID != idPlayer) {
            JoinCommand cmd = cmdWrapper.getCommand(JoinCommand.class);
            if (cmd.isJoin()){
                players.add(new Player(cmd.getTarget(),cmd.getUsername(),availableColors.get(0)));
                availableColors.remove(availableColors.get(0));
            }
        }
    }

    @Override
    public void start() {
        cmdNotify.lazySet(false);
        stream.println("Type 'quit' to leave the game");

        stream.println("Insert server ip: (press ENTER for local setup)");
        String ip;
        String ipInput = scanner.nextLine();
        if(!ipInput.equals("")) ip = ipInput;
        else ip = "127.0.0.1";

        stream.println("Insert your username: ");
        String user = scanner.nextLine();

        color = getColor();
        
        if (virtualServer.connect(ip, virtualServer.getDefaultPort(), user)) {

            do{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) { }
            }while(!logged);

            inputLoop();
        } else {
            stream.println("Connection refused");
        }

    }

    @Override
    public void onDisconnect(CommandWrapper cmdWrapper) {
        if(cmdWrapper == null) {
            stream.println("lost server connection.");
            setCurrentState(MatchState.QUIT);
        }else{
            LeaveCommand leaveCommand = cmdWrapper.getCommand(LeaveCommand.class);
            players.remove(getPlayer(leaveCommand.getLeftPlayerID()));
            if(idHost == leaveCommand.getLeftPlayerID()){
                idHost = leaveCommand.getNewHostPlayerID();
                if(idHost == idPlayer){
                    stream.println("You are new host for this game, type 'start' or wait 3rd player...");
                    setCurrentState(MatchState.WAIT);
                }
            }


            //*
            if(leaveCommand.getLeftPlayerID() == idPlayer){
                setCurrentState(MatchState.QUIT);
            }
        }

    }

    @Override
    public void onCommand(CommandWrapper cmdWrapper) {
        BaseCommand baseCommand = cmdWrapper.getCommand(BaseCommand.class);
        int targetId = baseCommand.getTarget();
        if (targetId != idPlayer && targetId != virtualServer.getBroadCastID()) {
            if(cmdWrapper.getType() != CommandType.END_GAME)
                stream.println("Turn of " + getPlayer(targetId).escapePlayerColor() + getPlayer(targetId).getUsername() + Color.RESET + " wait until " + cmdWrapper.getType().toString());
            setCurrentState(MatchState.WAIT_NEXT);
            return;
        }
        switch (cmdWrapper.getType()) {
            case START:
                StartCommand startCommand = cmdWrapper.getCommand(StartCommand.class);
                startSetup(startCommand);
                break;
            case FILTER_GODS:
                filterGodSetup();
                break;
            case PICK_GOD:
                PickGodCommand pickGodCommand = cmdWrapper.getCommand(PickGodCommand.class);
                pickGodSetup(pickGodCommand);
                break;
            case SELECT_FIRST_PLAYER:
                selectFirstPlayerSetup();
                break;
            case PLACE_WORKERS:
                WorkerPlaceCommand workerPlaceCommand = cmdWrapper.getCommand(WorkerPlaceCommand.class);
                placeWorkerSetup(workerPlaceCommand);
                break;
            case ACTION_TIME:
                ActionCommand actionCommand = cmdWrapper.getCommand(ActionCommand.class);
                actionTimeSetup(actionCommand);
                break;
            case END_GAME:
                EndGameCommand endGameCommand = cmdWrapper.getCommand(EndGameCommand.class);
                endGameSetup(endGameCommand);
                break;
            case UPDATE:
                UpdateCommand updateCommand = cmdWrapper.getCommand(UpdateCommand.class);
                setLastCompactMap(updateCommand.getUpdatedMap());
                showMap(updateCommand.getUpdatedMap(),null);
                break;

        }

    }

    // SETUP STATE METHODS

    private void retryJoin(JoinCommand cmd) {
        stream.println("Your username is not valid, choose another one (at least 3 char,only numbers and letters)");
        virtualServer.send(JoinCommand.makeRequest(cmd.getTarget(),virtualServer.getServerID(),scanner.nextLine()));
    }

    private void successfulJoin(JoinCommand cmd) {
        stream.println("Join successful");

        players.add(new Player(cmd.getTarget(),cmd.getUsername(),availableColors.get(color)));
        availableColors.remove(availableColors.get(color));

        idPlayer = cmd.getTarget();
        idHost = cmd.getHostPlayerID();

        if (idPlayer == idHost) stream.println("Type 'start' to start a game with 2 player or wait 3rd player...");
        else stream.println("Connected to game, wait to start...");

        if(idPlayer == idHost)
            setCurrentState(MatchState.WAIT);
        else
            setCurrentState(MatchState.WAIT_NEXT);
        logged = true;
    }

    private int getColor() {
        String[] inputColor;
        int[] selectedColor = new int[1];
        do{
            for(int i = 0; i<availableColors.size(); i++){
                stream.print(i + "." + availableColors.get(i).escape() + availableColors.get(i) + Color.RESET + " ");
            }
            stream.println("\nWhich color do you prefer?");

            inputColor = scanner.nextLine().split("\\s+");
            if(canConvertInputToInt(inputColor) && inputColor.length == 1)
                selectedColor = inputToInt(inputColor);
            else
                selectedColor[0] = -1;

        }while(selectedColor[0] < 0 || selectedColor[0] >= availableColors.size());
        return selectedColor[0];
    }

    private void endGameSetup(EndGameCommand endGameCommand) {

        if (endGameCommand.isMatchStillRunning()) {
            int playerLost = endGameCommand.getTarget();
            //TODO: end game adn continue game fix --- don't print player lost and jump do update without waiting
            if(playerLost == idPlayer) {
                stream.println(getPlayer(playerLost).escapePlayerColor() + "You LOST ..." + Color.RESET);
                stream.println("\n\nWould you like to play another game ? y/n");
                setCurrentState(MatchState.END);
            }else {
                stream.println("Player " + getPlayer(playerLost).escapePlayerColor() + playerLost + Color.RESET + " lost");
                players.remove(playerLost);
                if (players.size() >= MIN_PLAYER) {
                    //match continue
                    setCurrentState(MatchState.WAIT_NEXT);
                } else {
                    if (endGameCommand.getWinnerID() > 0)
                        if (endGameCommand.getWinnerID() == idPlayer)
                            stream.println("MATCH ENDED !!! \n" + getPlayer(endGameCommand.getWinnerID()).escapePlayerColor() + "YOU have conquered Santorini ☀ ⛴" + Color.RESET);
                        else
                            stream.println("MATCH ENDED !!! \n" + getPlayer(endGameCommand.getWinnerID()).escapePlayerColor() + "PLAYER n." + endGameCommand.getWinnerID() + " has conquered Santorini ☀ ⛴" + Color.RESET);
                    stream.println("\n\nWould you like to play another game ? y/n");
                    setCurrentState(MatchState.END);
                }
            }
        } else{
            //match interrupted
            if(endGameCommand.getWinnerID() > 0){
                if(endGameCommand.getWinnerID() == idPlayer)
                    stream.println("MATCH ENDED !!! \n" +
                            getPlayer(endGameCommand.getWinnerID()).escapePlayerColor() +
                            "YOU have conquered Santorini ☀ ⛴" + Color.RESET);
                else
                    stream.println("MATCH ENDED !!! \n" +
                            getPlayer(endGameCommand.getWinnerID()).escapePlayerColor() +
                            "PLAYER n." + endGameCommand.getWinnerID() + " has conquered Santorini ☀ ⛴" + Color.RESET);
            }else
                stream.println("MATCH ENDED !!! interruption occurred.");

            stream.println("\n\nWould you like to play another game ? y/n");
            setCurrentState(MatchState.END);
        }
    }

    private void placeWorkerSetup(WorkerPlaceCommand workerPlaceCommand) {
        setAvailablePositions(workerPlaceCommand);
        showMap(getLastCompactMap(),getAvailablePositions());
        stream.println(getPlayer(idPlayer).escapePlayerColor() + "----------IT'S YOUR TURN----------\n" + Color.RESET);

        stream.println("Select 2 available positions by its identifiers: \n<int1> <int2>");

        cmdNotify.lazySet(true);
        setCurrentState(MatchState.WORKER_PLACE);
    }

    private void selectFirstPlayerSetup() {
        clearScreen();
        stream.println(getPlayer(idPlayer).escapePlayerColor() + "----------IT'S YOUR TURN----------\n" + Color.RESET);

        for(int i = 1; i<=players.size(); i++){
            stream.print(i + "." + players.get(i-1).escapePlayerColor() + players.get(i-1).getUsername() + Color.RESET + " ");
        }
        stream.println();

        stream.println("Select first player for this game: \n<int1>");

        cmdNotify.lazySet(true);
        setCurrentState(MatchState.FIRST_PLAYER_SELECT);
    }

    private void pickGodSetup(PickGodCommand pickGodCommand) {
        clearScreen();
        stream.println(getPlayer(idPlayer).escapePlayerColor() + "----------IT'S YOUR TURN----------\n" + Color.RESET);

        printGods(pickGodCommand.getAllowedGodsIDS());
        stream.println("Pick a god by selecting id : \n<int1>");

        cmdNotify.lazySet(true);
        setCurrentState(MatchState.PICK_GOD);
    }

    private void filterGodSetup() {
        clearScreen();
        stream.println(getPlayer(idPlayer).escapePlayerColor() + "----------IT'S YOUR TURN----------\n" + Color.RESET);

        printGods();
        stream.println("Select " + players.size() + " IDs you want to be allowed for this game :");
        for (int i = 1; i <= players.size(); i++) stream.print("<int" + i + "> ");
        stream.println();

        cmdNotify.lazySet(true);
        setCurrentState(MatchState.GOD_SELECT);
    }


    private void printGods() {
        List<Integer> availableIds = cardCollection.getAvailableIds();

        for(int id : availableIds){
            stream.print(id + "." + " " +
                    cardCollection.getCard(id).getName() + " - " +
                    cardCollection.getCard(id).getDescription() +
                    "\n"
            );
        }
    }

    private void printGods(int[] availableGods){
        for(int id : availableGods){
            stream.print(id + "." + " " +
                    cardCollection.getCard(id).getName() + " - " +
                    cardCollection.getCard(id).getDescription() + "\n\t" +
                    cardCollection.getCard(id).getPower() + "\n"
            );
        }
    }

    private Player getPlayer(int playerID){
        for(Player player : players){
            if(player.getId() == playerID)
                return player;
        }
        return null;
    }

    private void startSetup(StartCommand startCommand) {
        String[] username = startCommand.getUsername();
        int[] ids = startCommand.getPlayersID();

        int userIndex= 0;
        for (int id : ids) {
            if (getPlayer(id) == null) {
                players.add(new Player(id, username[userIndex], availableColors.get(0)));
                availableColors.remove(0);
            }
            userIndex++;
        }

        stream.println("\nGAME IS NOW STARTED !!!\n");
    }

    private void actionTimeSetup(ActionCommand actionCommand) {
        stream.println(getPlayer(idPlayer).escapePlayerColor() + "----------IT'S YOUR TURN----------\n" + Color.RESET);

        setNextActions(actionCommand);

        NextAction[] nextActions = actionCommand.getAvailableActions();

        List<Integer> availableWorker = getAvailableWorker(nextActions);

        //if can auto-select worker
        if (canAutoSelectWorker(nextActions)) {
            int idWorker = availableWorker.get(0);

            List<NextAction> actionForWorker = clearNextActions(nextActions,idWorker);

            //if can auto-select action also
            if (canAutoSelectAction(actionForWorker)) {
                int actionId = 0;

                List<Vector2> positionForAction = actionForWorker.get(actionId).getAvailablePositions();

                //if can auto-select position also
                if (canAutoSelectPosition(positionForAction)) {
                    stream.println("Auto-selection worker n." + getPlayer(idPlayer).escapePlayerColor() + availableWorker.get(0) + Color.RESET);
                    stream.print("Time to " + actionForWorker.get(actionId).getActionName() + "...");
                    stream.println(" to (" + positionForAction.get(0).getX() + "," + positionForAction.get(0).getY() + ")");
                    Vector2 target = positionForAction.get(0);
                    setCurrentState(MatchState.WAIT_NEXT);

                    virtualServer.send(ActionCommand.makeReply(idPlayer, virtualServer.getServerID(), 0, idWorker, target));
                } else {
                    List<NextAction> actions = clearNextActions(nextActions,idWorker);
                    NextAction nextAction = getSelectedNextAction(actions,actionId);
                    List<Vector2> possiblePositions = getAvailablePosition(nextAction);
                    stream.print("\n");
                    showMap(getLastCompactMap(),possiblePositions.toArray(Vector2[]::new));

                    stream.println("Auto-selection worker n." + getPlayer(idPlayer).escapePlayerColor() + availableWorker.get(0) + Color.RESET);
                    stream.print("Time to " + actionForWorker.get(actionId).getActionName() + "...");


                    stream.println("\nSelect a position n. where you want to execute action: ");
                    for(int i = 0; i < possiblePositions.size(); i++){
                        stream.print(i + ".(" + possiblePositions.get(i).getX() + "," + possiblePositions.get(i).getY() + ")   ");
                    }
                    stream.println("\n<int1>");

                    cmdNotify.lazySet(true);
                    setCurrentState(MatchState.ACTION_TIME);
                }

            } else {
                List<NextAction> actions = clearNextActions(nextActions,idWorker);
                stream.println("Select action n. you want to carry out: ");
                for(int i = 0; i<actions.size(); i++){
                    stream.print(i + "." + actions.get(i).getActionName() + "   ");
                }
                stream.println("\n<int1>");
                cmdNotify.lazySet(true);
                setCurrentState(MatchState.ACTION_TIME);
            }

        } else {
            for (Integer integer : availableWorker) {
                stream.print(getPlayer(idPlayer).escapePlayerColor() + "W" + integer + "   ");
            }
            stream.println(Color.RESET);

            stream.println("Select id of worker you want to move :\n<int1>");

            cmdNotify.lazySet(true);
            setCurrentState(MatchState.ACTION_TIME);
        }
    }

    private boolean canAutoSelectWorker(NextAction[] nextActions) {
        return getAvailableWorker(nextActions).size() <= 1;
    }

    private boolean canAutoSelectAction(List<NextAction> actions){
        if(actions == null) return false;
        return actions.size() == 1;
    }

    private boolean canAutoSelectEndTurn(List<NextAction> actions){
        if(actions == null) return false;
        return actions.size() == 1 && actions.get(0).isEndTurnAction();
    }

    private boolean canAutoSelectPosition(List<Vector2> possiblePositions){
        if(possiblePositions == null) return false;
        return possiblePositions.size() == 1;
    }

    //new for actionCommand

    //return list of possible worker for this turn and its execution
    private List<Integer> getAvailableWorker(NextAction[] nextActions){
        List<Integer> availableWorkers = new ArrayList<>();

        for (NextAction nextAction : nextActions) {
            if (!availableWorkers.contains(nextAction.getWorkerID())) {
                availableWorkers.add(nextAction.getWorkerID());
            }
        }

        return availableWorkers;
    }

    //remove actions made by the unselected worker(if available worker = 1 should remain the same)
    private List<NextAction> clearNextActions(NextAction[] nextActions, int selectedWorker){
        List<NextAction> actions = new ArrayList<>();

        for(NextAction nextAction : nextActions){
            if(nextAction.getWorkerID() == selectedWorker)
                actions.add(nextAction);
        }

        return actions;
    }


    private NextAction getSelectedNextAction(List<NextAction> nextActions, int selectedAction){
        return nextActions.get(selectedAction);
    }

    private List<Vector2> getAvailablePosition(NextAction nextAction){
        return nextAction.getAvailablePositions();
    }


    //safe information passing between the two threads

    private synchronized void setLastCompactMap(CompactMap compactMap){
        lastCompactMap = compactMap;
    }

    private synchronized CompactMap getLastCompactMap(){
        return Objects.requireNonNullElseGet(lastCompactMap, () -> new CompactMap(new it.polimi.ingsw.game.Map()));
    }

    private synchronized void setAvailablePositions(WorkerPlaceCommand workerPlaceCommand){
        availablePositions = workerPlaceCommand.getPositions();
    }

    private synchronized Vector2[] getAvailablePositions(){
        if(availablePositions != null)
            return availablePositions.clone();
        else return null;
    }

    private synchronized void setNextActions(ActionCommand actionCommand) {
        nextActions = actionCommand.getAvailableActions();
    }

    private synchronized NextAction[] getNextActions() {
        if(nextActions!=null)
            return nextActions;
        else
            return null;
    }

    private synchronized void setCurrentState(MatchState newState) {
        currentState = newState;
    }

    private synchronized MatchState getCurrentState() {
        return currentState;
    }

    ////////     MAIN THREAD METHODS

    private void inputLoop() {
        String input;
        boolean endGame;

        do {
            input = scanner.nextLine();
            String[] inputArray = input.split("\\s+");

            MatchState state = getCurrentState();

            shouldStop = checkStatelessInput(inputArray,state);
            endGame = endGameCheck(inputArray);

            if (cmdNotify.compareAndSet(true, false)) {
                NextAction[] nextActions = getNextActions();
                Vector2[] availableVectors = getAvailablePositions();

                boolean retry = false;
                ExecutionResponse response;
                boolean canGoBack = true;
                do {
                    if (retry) {
                        input = scanner.nextLine();
                        inputArray = input.split("\\s+");
                        shouldStop = quitCheck(inputArray);
                        endGame = endGameCheck(inputArray);
                    }
                    if(!shouldStop && !endGame)
                        response = matchState(inputArray, state, nextActions, availableVectors, canGoBack);
                    else if(endGame)
                        response = ExecutionResponse.ENDED_GAME;
                    else
                        response = ExecutionResponse.SUCCESS;

                    if(response == ExecutionResponse.BACK) canGoBack = false;
                    retry = true;
                } while (response == ExecutionResponse.FAIL || response == ExecutionResponse.BACK);

                if(response == ExecutionResponse.QUIT) shouldStop = true;
            }

            if(currentState == MatchState.WAIT_NEXT && !quitCheck(inputArray))
                printFail(inputArray);

        } while (!shouldStop);

    }

    private void printFail(String[] inputArray){
        if(inputArray.length > 0 && !inputArray[0].equals(""))
            stream.println("Oak's words echoed... There's a time and a place for everything, but not now.");
    }

    private boolean checkStatelessInput(String[] inputArray,MatchState currentState) {
        if (currentState == MatchState.WAIT) {
            if (canStartGame(inputArray))
                virtualServer.send(StartCommand.makeRequest(idPlayer,virtualServer.getServerID()));
            else if(inputArray.length == 1 && inputArray[0].equals("start"))
                stream.println("can't start this game now...");
            else if(quitCheck(inputArray))
                return true;
            else
                printFail(inputArray);
        }else
                return quitCheck(inputArray);

        return false;
    }

    private boolean quitCheck(String[] inputArray) {
        if (inputArray.length == 1 && inputArray[0].equals("quit")) {
            virtualServer.send(LeaveCommand.makeRequest(idPlayer, virtualServer.getServerID()));
            return true;
        }else
            return getCurrentState() == MatchState.QUIT;
    }

    private ExecutionResponse matchState(String[] inputArray, MatchState state, NextAction[] nextActions, Vector2[] availablePositions, boolean canGoBack) {
        switch (state) {

            case GOD_SELECT:
                if (canConvertInputToInt(inputArray)) {
                    int[] selectedGods = inputToInt(inputArray);
                    virtualServer.send(FilterGodCommand.makeReply(idPlayer, virtualServer.getServerID(), selectedGods));
                    return ExecutionResponse.SUCCESS;
                }
                stream.println("Select Gods allowed for this game, please try again: ");
                for (int i = 1; i <= players.size(); i++) stream.print("<int" + i + "> ");
                stream.println();
                return ExecutionResponse.FAIL;


            case PICK_GOD:
                if (canConvertInputToInt(inputArray) && inputArray.length == 1) {
                    int[] pickedGod = inputToInt(inputArray);
                    virtualServer.send(PickGodCommand.makeReply(idPlayer, virtualServer.getServerID(), pickedGod[0]));
                    return ExecutionResponse.SUCCESS;
                }
                stream.println("Select you God, please try again: \n<int1>");
                return ExecutionResponse.FAIL;


            case FIRST_PLAYER_SELECT:
                if (canConvertInputToInt(inputArray) && inputArray.length == 1) {
                    int[] firstPlayer = inputToInt(inputArray);
                    if(firstPlayer[0]>0 && firstPlayer[0] <=players.size()){

                        virtualServer.send(FirstPlayerPickCommand.makeReply(idPlayer, virtualServer.getServerID(), players.get(firstPlayer[0]-1).getId()));
                        return ExecutionResponse.SUCCESS;
                    }
                }
                stream.println("Select first player for this game, please try again: \n<int1>");
                return ExecutionResponse.FAIL;


            case WORKER_PLACE:
                if (canConvertInputToInt(inputArray) && inputArray.length == N_WORKER) {
                    int[] selectedPos = inputToInt(inputArray);
                    Vector2[] selectedPosition = new Vector2[N_WORKER];
                    for (int i = 0; i < selectedPos.length; i++) {
                        if (selectedPos[i] < 0 || selectedPos[i] >= availablePositions.length) {
                            stream.println("Position not available, please try again: ");
                            for (int j = 0; j < N_WORKER; j++) {
                                stream.print("<int" + j + "> ");
                            }
                            stream.println();
                            return ExecutionResponse.FAIL;
                        }
                        selectedPosition[i] = availablePositions[selectedPos[i]];
                    }
                    virtualServer.send(WorkerPlaceCommand.makeWrapped(idPlayer, virtualServer.getServerID(), selectedPosition));
                    return ExecutionResponse.SUCCESS;
                }
                stream.println("Select " + N_WORKER + " identifier positions where you want to place your workers: ");
                for (int i = 0; i < N_WORKER; i++) {
                    stream.print("<int" + i + "> ");
                }
                stream.println();
                return ExecutionResponse.FAIL;


            case ACTION_TIME:
                if (canAutoSelectWorker(nextActions)) {
                    int workerID = nextActions[0].getWorkerID(); //cause of auto select
                    List<NextAction> actions = clearNextActions(nextActions, workerID);
                    if (canAutoSelectAction(actions)) {
                        int actionID = 0; // cause of auto select
                        //input is positionID
                        if (canConvertInputToInt(inputArray) && inputArray.length == 1) {
                            int[] input = inputToInt(inputArray);
                            int targetID = input[0];
                            NextAction nextAction = getSelectedNextAction(actions, actionID);
                            List<Vector2> positions = getAvailablePosition(nextAction);
                            if (targetID >= 0 && targetID < positions.size()) {
                                Vector2 target = positions.get(targetID);
                                virtualServer.send(ActionCommand.makeReply(idPlayer, virtualServer.getServerID(), actionID, workerID, target));
                                return ExecutionResponse.SUCCESS;
                            }
                            stream.println("Position not available, please try again: \n<int1>");
                            return ExecutionResponse.FAIL;
                        }
                        stream.println("Select target n. you want to execute move to, please try again: \n<int1>");
                        return ExecutionResponse.FAIL;
                    } else {
                        //input is actionID
                        if (canConvertInputToInt(inputArray) && inputArray.length == 1) {
                            int[] input = inputToInt(inputArray);
                            int actionID = input[0];
                            if (actionID >= 0 && actionID < nextActions.length) {
                                NextAction nextAction = getSelectedNextAction(actions, actionID);
                                List<Vector2> possiblePositions = getAvailablePosition(nextAction);
                                return positionSelection(actions, possiblePositions, actionID);
                            }
                            stream.println("Action not available, please try again: \n<int1>");
                            return ExecutionResponse.FAIL;
                        }
                        stream.println("Select action n. you want to perform, please try again: \n<int1>");
                        return ExecutionResponse.FAIL;
                    }
                } else {
                    //input is workerID
                    if (canConvertInputToInt(inputArray) && inputArray.length == 1) {
                        int[] input = inputToInt(inputArray);
                        int workerID = input[0];
                        if (workerID >= 0 && workerID < N_WORKER) {
                            List<NextAction> actions = clearNextActions(nextActions,workerID);
                            return actionSelection(nextActions, actions,canGoBack);
                        }
                        stream.println("Worker not available, please try again: \n<int1>");
                        return ExecutionResponse.FAIL;
                    }
                    stream.println("Select worker you want to use, please try again: \n<int1>");
                    return ExecutionResponse.FAIL;
                }


            case END:
                if (inputArray[0].equals("y") && inputArray.length == 1){
                    int currentID = idPlayer;
                    String currentUsername = getPlayer(idPlayer).getUsername();
                    players.clear();
                    availableColors.clear();
                    availableColors.addAll(Arrays.asList(Color.values()));
                    idPlayer = -1;
                    virtualServer.send(JoinCommand.makeRequest(currentID, virtualServer.getServerID(), currentUsername));
                    return  ExecutionResponse.SUCCESS;
                }else {
                    stream.println("another game? y/n");
                    return ExecutionResponse.FAIL;
                }


            default:
                return ExecutionResponse.SUCCESS;
        }
    }

    /**
     *
     * @param actions already cleared NextActions List, avery element has the same (selected) worker
     */
    private ExecutionResponse actionSelection(NextAction[] nextActions, List<NextAction> actions, boolean canGoBack) {
        if((canAutoSelectEndTurn(actions)) || (!canGoBack && canAutoSelectAction(actions))){
            stream.println("Time to " + actions.get(0).getActionName() + "...");
            int actionID = 0;
            NextAction nextAction = getSelectedNextAction(actions, actionID);
            List<Vector2> availablePositions = getAvailablePosition(nextAction);
            return positionSelection(actions, availablePositions, actionID);
        }else{
            stream.println("Select the identifier of action you want to carry out: ");
            int i = 0;
            for(; i<actions.size(); i++){
                stream.print(i + "." + actions.get(i).getActionName() + "   ");
            }
            stream.print(i + ".Back");
            stream.println("\n<int1>");

            boolean shouldStop;
            boolean endGame;
            String[] inputArray;
            int actionID = -1;
            boolean retry = false;
            do {
                if(retry) {
                    stream.println("Select the identifier of action you want to carry out, please try again: \n<int1>");
                }
                inputArray = scanner.nextLine().split("\\s+");
                shouldStop = quitCheck(inputArray);
                endGame = endGameCheck(inputArray);
                if(canConvertInputToInt(inputArray) && inputArray.length == 1){
                    actionID = inputToInt(inputArray)[0];
                }
                retry = true;
            } while (!shouldStop && (actionID < 0 || actionID > actions.size()) );

            if(!shouldStop && !endGame) {
                if(actionID == i){
                    Integer[] availableWorker = getAvailableWorker(nextActions).toArray(new Integer[0]);
                    for (Integer integer : availableWorker) {
                        stream.print(getPlayer(idPlayer).escapePlayerColor() + "W" + integer + "   ");
                    }
                    stream.println(Color.RESET);

                    stream.println("Select id of worker you want to move :\n<int1>");
                    return ExecutionResponse.BACK;
                }
                else{
                    NextAction nextAction = getSelectedNextAction(actions, actionID);
                    List<Vector2> possiblePositions = getAvailablePosition(nextAction);
                    return positionSelection(actions, possiblePositions, actionID);
                }
            }else if(endGame)
                return ExecutionResponse.ENDED_GAME;
            else
                return ExecutionResponse.QUIT;
        }
    }

    private ExecutionResponse positionSelection(List<NextAction> actions, List<Vector2> possiblePositions, int actionID) {
        if(canAutoSelectPosition(possiblePositions)){
            int workerID = actions.get(0).getWorkerID();
            int targetID = 0;
            Vector2 target = possiblePositions.get(targetID);
            stream.println("Execution to (" + target.getX() + "," + target.getY() + ")");
            virtualServer.send(ActionCommand.makeReply(idPlayer,virtualServer.getServerID(),actionID,workerID,target));
            return ExecutionResponse.SUCCESS;
        }else{
            showMap(getLastCompactMap(),actions.get(actionID).getAvailablePositions().toArray(Vector2[]::new));
            stream.println("Select the identifier n. of position you want to chose: ");
            for(int i = 0; i<possiblePositions.size(); i++){ stream.print(i + ".(" + possiblePositions.get(i).getX() + "," + possiblePositions.get(i).getY() + ")   "); }
            stream.println("\n<int1>");

            boolean shouldStop;
            boolean endGame;
            String[] inputArray;
            int targetID = -1;
            boolean retry = false;
            do {
                if(retry){
                    stream.println("Select the identifier n. of position you want to execute on, please try again: \n<int1>");
                }
                inputArray = scanner.nextLine().split("\\s+");
                shouldStop = quitCheck(inputArray);
                endGame = endGameCheck(inputArray);

                if(canConvertInputToInt(inputArray) && inputArray.length == 1){
                    targetID = inputToInt(inputArray)[0];
                }

                retry = true;
            } while (!shouldStop && !endGame && (targetID < 0 || targetID >= possiblePositions.size()) );

            if(!shouldStop && !endGame) {
                int workerID = actions.get(0).getWorkerID();
                Vector2 target = possiblePositions.get(targetID);
                virtualServer.send(ActionCommand.makeReply(idPlayer,virtualServer.getServerID(),actionID,workerID,target));
                return ExecutionResponse.SUCCESS;
            }else if(endGame){
                return ExecutionResponse.ENDED_GAME;
            } else
                return ExecutionResponse.QUIT;
        }
    }

    private boolean endGameCheck(String[] inputArray) {
        if(getCurrentState() == MatchState.END){
            boolean retry = false;
            do{
                if(retry){
                    inputArray = scanner.nextLine().split("\\s+");
                }


                if(inputArray[0].equals("y") && inputArray.length == 1){
                    int currentID = idPlayer;
                    String currentUsername = getPlayer(idPlayer).getUsername();
                    players.clear();
                    availableColors.clear();
                    availableColors.addAll(Arrays.asList(Color.values()));
                    idPlayer = -1;
                    lastCompactMap = new CompactMap(new Map());
                    virtualServer.send(JoinCommand.makeRequest(currentID,virtualServer.getServerID(),currentUsername));
                    break;
                }else if(inputArray[0].equals("n") && inputArray.length == 1){
                    virtualServer.send(LeaveCommand.makeRequest(idPlayer,virtualServer.getServerID()));
                    shouldStop = true;
                    break;
                }else{
                    stream.println("Would you like to play another game? y/n");
                }

                retry = true;

            }while(true);

            return true;
        }else
            return false;
    }

    private boolean canConvertInputToInt(String[] inputArray) {
        try {
            for (String s : inputArray) {
                Integer.parseInt(s);
            }
            return true;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    private int[] inputToInt(String[] inputArray) {
        int[] intInput = new int[inputArray.length];
        for (int i = 0; i < inputArray.length; i++) {
            intInput[i] = Integer.parseInt(inputArray[i]);
        }
        return intInput;
    }

    private boolean canStartGame(String[] inputArray) {
        return inputArray.length == 1 && inputArray[0].equals("start") && idHost == idPlayer && players.size()>=MIN_PLAYER && players.size()<MAX_PLAYER;
    }


    private void showMap(CompactMap compactMap, Vector2[] availablePositions){
        CompactWorker[] compactWorkers = compactMap.getWorkers();

        clearScreen();
        StringBuilder string = new StringBuilder();

        string.append(Color.SOFT_WHITE.escape() + "\n");
        for(int i = 0; i<HEIGHT; i++){

            //FIRST LEVEL
            string.append("                ");
            for(int j = 0; j<LENGTH ; j++) {
                if (availablePositions != null && isAvailablePosition(availablePositions,i,j)) string.append(Color.RESET).append(Color.CLICKABLE.escape());
                else string.append(Color.RESET).append(Color.SOFT_WHITE.escape());
                string.append(" █");
                string.append("▀".repeat(8));
                string.append("█");
            }
            string.append("\n");

            //SECOND LEVEL
            string.append("                ");
            for(int j = 0; j<LENGTH ; j++) {

                if (availablePositions != null && isAvailablePosition(availablePositions,i,j)) string.append(Color.RESET).append(Color.CLICKABLE.escape());
                else string.append(Color.RESET).append(Color.SOFT_WHITE.escape());
                string.append(" █");
                string.append(Color.RESET);

                string.append(" ");
                if(compactMap.isDome(i,j)){
                    string.append(Color.BLUE.escape()).append(Color.BOLD_ON.escape()).append(" ████").append(Color.RESET);
                }else{
                    int level = compactMap.getLevel(i,j);
                    switch (level){
                        case 1 :
                            string.append(level);
                            string.append(Color.BOLD_ON.escape()).append("░ ").append(Color.RESET);
                            break;
                        case 2 :
                            string.append(level);
                            string.append(Color.BOLD_ON.escape()).append("▒ ").append(Color.RESET);
                            break;
                        case 3 :
                            string.append(level);
                            string.append(Color.BOLD_ON.escape()).append("▓ ").append(Color.RESET);
                            break;
                        default:
                            string.append(" ".repeat(3));
                            break;
                    }
                    string.append(" ".repeat(1));
                }


                if(canPrintWorker(i,j,compactWorkers)){
                    int ownerID = getOwnerForWorker(compactWorkers, i, j);
                    int workerID = getWorkerID(compactWorkers,i,j);
                    string.append(getPlayer(ownerID).escapePlayerColor()).append(Color.BOLD_ON.escape()).append("₩").append(Color.RESET);
                    string.append(getPlayer(ownerID).escapePlayerColor()).append(Color.BOLD_ON.escape()).append(workerID).append(Color.RESET);
                    string.append(" ".repeat(1));
                }else if(compactMap.isDome(i,j)){
                    string.append(" ".repeat(2));
                }else
                    string.append(" ".repeat(3));

                if (availablePositions != null && isAvailablePosition(availablePositions,i,j)) string.append(Color.RESET).append(Color.CLICKABLE.escape());
                else string.append(Color.RESET).append(Color.SOFT_WHITE.escape());
                string.append("█");
            }
            string.append("\n");

            //THIRD LEVEL
            string.append("                ");
            for(int j = 0; j<LENGTH ; j++) {
                if (availablePositions != null && isAvailablePosition(availablePositions,i,j)) string.append(Color.RESET).append(Color.CLICKABLE.escape());
                else string.append(Color.RESET).append(Color.SOFT_WHITE.escape());
                string.append(" █");
                string.append(Color.RESET);


                if(availablePositions != null && isAvailablePosition(availablePositions,i,j)){
                    string.append(" ".repeat(3));

                    string.append(Color.CLICKABLE.escape()).append(Color.BOLD_ON.escape()).append("»").append(getPositionIdentifier(availablePositions,i,j)).append(Color.RESET);

                    if(getPositionIdentifier(availablePositions,i,j) > 9)
                        string.append(" ".repeat(2));
                    else
                        string.append(" ".repeat(3));
                }else if(compactMap.isDome(i,j)) {
                    string.append(Color.BLUE.escape()).append(Color.BOLD_ON.escape()).append("  ████  ");
                }else{
                    string.append(" ".repeat(8));
                }

                if (availablePositions != null && isAvailablePosition(availablePositions,i,j)) string.append(Color.RESET).append(Color.CLICKABLE.escape());
                else string.append(Color.RESET).append(Color.SOFT_WHITE.escape());
                string.append("█");
            }

            string.append("\n");

            //FOURTH LEVEL
            string.append("                ");
            for(int j = 0; j<LENGTH ; j++) {
                if (availablePositions != null && isAvailablePosition(availablePositions,i,j)) string.append(Color.RESET).append(Color.CLICKABLE.escape());
                else string.append(Color.RESET).append(Color.SOFT_WHITE.escape());
                string.append(" █");
                string.append("▄".repeat(8));
                string.append("█");
            }
            string.append("\n");
        }
        string.append(Color.RESET).append("\n");
        stream.print(string);

    }

    private int getPositionIdentifier(Vector2[] availablePositions, int i, int j) {
        for(int x = 0; x < availablePositions.length; x++){
            if(availablePositions[x].getX() == i && availablePositions[x].getY() == j){
                return x;
            }
        }

        return -1; //method called without checking if position have a correspondent in available ones
    }

    private boolean isAvailablePosition(Vector2[] availablePositions, int i, int j) {
        for(Vector2 pos : availablePositions){
            if(pos.getX() == i && pos.getY() == j){
                return true;
            }
        }
        return false;
    }

    private int getWorkerID(CompactWorker[] compactWorkers, int i, int j) {
        for(CompactWorker worker : compactWorkers){
            if(worker.getPosition().getX() == i && worker.getPosition().getY() == j){
                return worker.getWorkerID();
            }
        }

        return -1; //method called without checking if a worker is inside the current pos
    }

    private int getOwnerForWorker(CompactWorker[] compactWorkers, int i, int j) {
        for(CompactWorker worker : compactWorkers){
            if(worker.getPosition().getX() == i && worker.getPosition().getY() == j){
                return worker.getOwnerID();
            }
        }

        return -1; //method called without checking if a worker is inside the current pos
    }

    private boolean canPrintWorker(int i, int j,CompactWorker[] compactWorker) {
        for(CompactWorker worker : compactWorker){
            if(worker.getPosition().getX() == i && worker.getPosition().getY() == j){
                return true;
            }
        }

        return false;
    }

    private void clearScreen() {
        stream.println("TIME TO CLEAR SCREEN --------------------------------------------");
        System.out.print("\033[2J");
        System.out.flush();
    }

}
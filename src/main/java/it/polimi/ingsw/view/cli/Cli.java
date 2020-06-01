package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.controller.compact.CompactMap;
import it.polimi.ingsw.controller.compact.CompactWorker;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.NextAction;
import it.polimi.ingsw.game.Vector2;
import it.polimi.ingsw.game.Worker;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.view.IHumanInterface;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Old cli adapted to use some network changes
 */
public class Cli implements IHumanInterface {
    private static final int HEIGHT = it.polimi.ingsw.game.Map.HEIGHT;
    private static final int LENGTH = it.polimi.ingsw.game.Map.LENGTH;
    private static final int MAX_PLAYER = Game.MAX_PLAYERS;
    private static final int MIN_PLAYER = Game.MIN_PLAYERS;
    private static final int DOME_VALUE = it.polimi.ingsw.game.Map.DOME_VALUE;
    private static final int N_WORKER = Game.WORKERS_PER_PLAYER;
    private static final int TARGET_LOST = EndGameCommand.TARGET_LOST;
    private static final int INTERRUPTED_GAME = EndGameCommand.INTERRUPTED_GAME;

    private INetworkAdapter virtualServer;
    private int playersNumber;
    private int idPlayer = -1;
    private String username;
    private int idHost;
    private Map<Integer, Color> colors = new HashMap<>();
    private PrintStream stream = new PrintStream(System.out, true);
    private Scanner scanner = new Scanner(System.in);
    private boolean logged = false;

    private CommandWrapper lastCommandWrap;
    private WorkerPlaceCommand lastWorkerCommand;

    private NextAction[] nextActions;
    private AtomicBoolean cmdNotify = new AtomicBoolean(false);
    private Vector2[] availablePositions;
    private CompactMap lastCompactMap;

    private enum MatchState {WAIT, WAIT_NEXT, GOD_SELECT, PICK_GOD, FIRST_PLAYER_SELECT, WORKER_PLACE,ACTION_TIME, END, QUIT}
    private MatchState currentState = MatchState.WAIT;

    private enum ExecutionResponse {SUCCESS, FAIL, QUIT}

    public Cli(INetworkAdapter adapter) {
        virtualServer = adapter;
    }

    public void onConnect(CommandWrapper cmdWrapper) {
        int targetID = cmdWrapper.getCommand(BaseCommand.class).getTarget();

        if (cmdWrapper.getType() == CommandType.JOIN && idPlayer == -1) {
            JoinCommand cmd = cmdWrapper.getCommand(JoinCommand.class);

            if (cmd.isJoin())
                successfulJoin(cmd);
            else {
                if (!cmd.isValidUsername()) stream.println("Can't join a game right now, try another time.");
                else retryJoin(cmd);
            }

        } else if (cmdWrapper.getType() == CommandType.JOIN && targetID != idPlayer) {
            JoinCommand cmd = cmdWrapper.getCommand(JoinCommand.class);
            if (cmd.isJoin())
                playersNumber++;
        }
    }

    @Override
    public void start() {
        cmdNotify.lazySet(false);
        stream.println("Type 'quit' to leave the game");

        //TODO : handle ip insert error
        stream.println("Insert server ip: ");
        String ip;
        String ipInput = scanner.nextLine();
        if(!ipInput.equals("")){
            ip = ipInput;
        } else
            ip = "127.0.0.1";

        stream.println("Insert your username: ");
        String user = scanner.nextLine();

        if (virtualServer.connect(ip, virtualServer.getDefaultPort(), user)) {

            //TODO: change sleep with wait condition
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

    public void onDisconnect(CommandWrapper cmdWrapper) {
        //TODO : here when lose server connection
    }

    public void onCommand(CommandWrapper cmdWrapper) {
        lastCommandWrap = cmdWrapper;
        BaseCommand baseCommand = cmdWrapper.getCommand(BaseCommand.class);

        if (baseCommand.getTarget() != idPlayer && baseCommand.getTarget() != virtualServer.getBroadCastID()) {
            stream.println("Turn of player n. " + baseCommand.getTarget() + " wait until " + colors.get(baseCommand.getTarget()).escape() + cmdWrapper.getType().toString() + Color.RESET);
            currentState = MatchState.WAIT_NEXT;
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
                showMap(updateCommand.getUpdatedMap());
                break;
        }

    }

    // SETUP STATE METHODS

    private void retryJoin(JoinCommand cmd) {
        stream.println("Your username is not valid, choose another one");
        String newUsername;
        newUsername = scanner.nextLine();
        virtualServer.send(JoinCommand.makeRequest(cmd.getTarget(),virtualServer.getServerID(),newUsername));
    }

    private void successfulJoin(JoinCommand cmd) {
        idPlayer = cmd.getTarget();
        stream.println("Join successful");
        idHost = cmd.getHostPlayerID();
        playersNumber++;

        if (idPlayer == idHost) stream.println("Type 'start' to start a game with 2 player or wait 3rd player...");
        else stream.println("Connected to game, wait to start...");

        username = cmd.getUsername();
        setCurrentState(MatchState.WAIT);
        logged = true;
    }

    private void endGameSetup(EndGameCommand endGameCommand) {
        if (endGameCommand.getWinnerID() == TARGET_LOST) {
            int playerLost = endGameCommand.getTarget();
            stream.println("Player " + colors.get(playerLost).escape() + playerLost + Color.RESET + " left game");

            setCurrentState(MatchState.WAIT_NEXT);
        } else if (endGameCommand.getWinnerID() == INTERRUPTED_GAME) {
            virtualServer.send(JoinCommand.makeRequest(idPlayer, virtualServer.getServerID(), username));
            currentState = MatchState.WAIT_NEXT;
        }
    }

    private void placeWorkerSetup(WorkerPlaceCommand workerPlaceCommand) {
        setAvailablePositions(workerPlaceCommand);
        lastWorkerCommand = workerPlaceCommand;
        showMap(getLastCompactMap(),getAvailablePositions());
        for (int i = 0; i < workerPlaceCommand.getPositions().length; i++)
            stream.print(i + ".(" + workerPlaceCommand.getPositions()[i].getX() + "," + workerPlaceCommand.getPositions()[i].getY() + ") ");

        stream.println();

        stream.println("Select 2 available positions by its identifiers: \n<int1> <int2>");

        cmdNotify.lazySet(true);
        setCurrentState(MatchState.WORKER_PLACE);
    }

    private void selectFirstPlayerSetup() {
        for (Integer i : colors.keySet()) stream.print(i + " ");
        stream.println();

        stream.println("Select first player for this game: \n<int1>");

        cmdNotify.lazySet(true);
        setCurrentState(MatchState.FIRST_PLAYER_SELECT);
    }

    private void pickGodSetup(PickGodCommand pickGodCommand) {
        stream.print("Available god ID for this game : ");
        for (int i : pickGodCommand.getAllowedGodsIDS()) {
            stream.print(i + ", ");
        }
        stream.println();

        stream.println("Pick a god by selecting id : \n<int1>");

        cmdNotify.lazySet(true);
        setCurrentState(MatchState.PICK_GOD);
    }

    private void filterGodSetup() {
        printGodsID();
        stream.println("Select " + playersNumber + " IDs you want to be allowed for this game :");
        for (int i = 1; i <= playersNumber; i++) stream.print("<int" + i + "> ");
        stream.println();

        cmdNotify.lazySet(true);
        setCurrentState(MatchState.GOD_SELECT);
    }

    private void printGodsID() {
        //TODO : print CardCollection (player name + ID)
    }

    private void startSetup(StartCommand startCommand) {
        playersNumber = startCommand.getPlayersID().length;
        initPlayers(startCommand);
        stream.println("Game is now started !!!");
    }

    private void actionTimeSetup(ActionCommand actionCommand) {

        setNextActions(actionCommand);

        NextAction[] nextActions = actionCommand.getAvailableActions();

        List<Integer> availableWorker = getAvailableWorker(nextActions);

        //if can auto-select worker
        if (canAutoSelectWorker(nextActions)) {
            stream.println("Auto-selection worker n." + colors.get(idPlayer).escape() + availableWorker.get(0) + Color.RESET);
            int idWorker = availableWorker.get(0);

            List<NextAction> actionForWorker = clearNextActions(nextActions,idWorker);

            //if can auto-select action also
            if (canAutoSelectAction(actionForWorker)) {
                int actionId = 0;
                stream.print("Time to " + actionForWorker.get(actionId).getActionName() + "...");

                List<Vector2> positionForAction = actionForWorker.get(actionId).getAvailablePositions();

                //if can auto-select position also
                if (canAutoSelectPosition(positionForAction)) {
                    stream.println(" to (" + positionForAction.get(0).getX() + "," + positionForAction.get(0).getY() + ")");
                    Vector2 target = positionForAction.get(0);
                    setCurrentState(MatchState.WAIT_NEXT);

                    virtualServer.send(ActionCommand.makeReply(idPlayer, virtualServer.getServerID(), 0, idWorker, target));
                } else {
                    List<NextAction> actions = clearNextActions(nextActions,idWorker);
                    NextAction nextAction = getSelectedNextAction(actions,actionId);
                    List<Vector2> possiblePositions = getAvailablePosition(nextAction);
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
                stream.print(integer + "   ");
            }
            stream.println();

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
        boolean shouldStop;

        do {
            input = scanner.nextLine();
            String[] inputArray = input.split("\\s+");

            shouldStop = checkStatelessInput(inputArray);

            //i can set the value to false in this thread because :
            //  if true i have to reply to the other thread
            //  if false i have to wait a command
            //  it should not set true inside this block but only at the end because it come after a send call or onCommand auto recall
            if (cmdNotify.compareAndSet(true, false)) {
                MatchState state = getCurrentState();
                NextAction[] nextActions = getNextActions();
                Vector2[] availableVectors = getAvailablePositions();

                boolean retry = false;
                ExecutionResponse response;
                do {
                    if (retry) {
                        input = scanner.nextLine();
                        inputArray = input.split("\\s+");
                        shouldStop = quitCheck(inputArray);
                    }
                    if(!shouldStop)
                        response = matchState(inputArray, state, nextActions, availableVectors);
                    else
                        response = ExecutionResponse.SUCCESS;

                    retry = true;
                } while (response != ExecutionResponse.QUIT && response != ExecutionResponse.SUCCESS);

                if(response == ExecutionResponse.QUIT) shouldStop = true;
            }

        } while (!shouldStop);

    }

    private boolean checkStatelessInput(String[] inputArray) {
        if (currentState == MatchState.WAIT) {
            //TODO : can't start if player != min player fix
            if (canStartGame(inputArray))
                virtualServer.send(StartCommand.makeRequest(idPlayer,virtualServer.getServerID()));
        }else
                return quitCheck(inputArray);

        return false;
    }

    private boolean quitCheck(String[] inputArray) {
        if (inputArray.length <= 1 && inputArray[0].equals("quit")) {
            virtualServer.send(LeaveCommand.makeRequest(idPlayer, virtualServer.getServerID()));
            return true;
        }
        return false;
    }

    private ExecutionResponse matchState(String[] inputArray, MatchState state, NextAction[] nextActions, Vector2[] availablePositions) {
        switch (state) {
            case GOD_SELECT:
                if (canConvertInputToInt(inputArray)) {
                    int[] selectedGods = inputToInt(inputArray);
                    virtualServer.send(FilterGodCommand.makeReply(idPlayer,virtualServer.getServerID(),selectedGods));
                    return ExecutionResponse.SUCCESS;
                }
                stream.println("Select Gods allowed for this game, please try again: ");
                for (int i = 1; i <= playersNumber; i++) stream.print("<int" + i + "> ");
                stream.println();
                return ExecutionResponse.FAIL;
            case PICK_GOD:
                if (canConvertInputToInt(inputArray) && inputArray.length == 1) {
                    int[] pickedGod = inputToInt(inputArray);
                    virtualServer.send(PickGodCommand.makeReply(idPlayer,virtualServer.getServerID(),pickedGod[0]));
                    return ExecutionResponse.SUCCESS;
                }
                stream.println("Select you God, please try again: \n<int1>");
                return ExecutionResponse.FAIL;
            case FIRST_PLAYER_SELECT:
                if (canConvertInputToInt(inputArray) && inputArray.length == 1) {
                    int[] firstPlayer = inputToInt(inputArray);
                    virtualServer.send(FirstPlayerPickCommand.makeReply(idPlayer,virtualServer.getServerID(),firstPlayer[0]));
                    return ExecutionResponse.SUCCESS;
                }
                stream.println("Select first player for this game, please try again: \n<int1>");
                return ExecutionResponse.FAIL;
            case WORKER_PLACE:
                if(canConvertInputToInt(inputArray) && inputArray.length == N_WORKER){
                    int[] selectedPos = inputToInt(inputArray);
                    Vector2[] selectedPosition = new Vector2[N_WORKER];
                    for(int i = 0 ; i<selectedPos.length; i++){
                        if(selectedPos[i] < 0 || selectedPos[i] >= availablePositions.length) {
                            stream.println("Position not available, please try again: ");
                            for(int j=0; j < N_WORKER; j++){
                                stream.print("<int" + j + "> ");
                            }
                            stream.println();
                            return ExecutionResponse.FAIL;
                        }
                        selectedPosition[i] = availablePositions[selectedPos[i]];
                    }
                    virtualServer.send(WorkerPlaceCommand.makeWrapped(idPlayer,virtualServer.getServerID(),selectedPosition));
                    return ExecutionResponse.SUCCESS;
                }
                stream.println("Select " + N_WORKER + " identifier positions where you want to place your workers: ");
                for(int i=0; i < N_WORKER; i++){
                    stream.print("<int" + i + "> ");
                }
                stream.println();
                return ExecutionResponse.FAIL;
            case ACTION_TIME:
                if(canAutoSelectWorker(nextActions)){
                    int workerID = nextActions[0].getWorkerID(); //cause of auto select
                    List<NextAction> actions = clearNextActions(nextActions,workerID);
                    if(canAutoSelectAction(actions)){
                        int actionID = 0; // cause of auto select
                        //input is positionID
                        if(canConvertInputToInt(inputArray) && inputArray.length == 1){
                            int[] input = inputToInt(inputArray);
                            int targetID = input[0];
                            NextAction nextAction = getSelectedNextAction(actions,actionID);
                            List<Vector2> positions = getAvailablePosition(nextAction);
                            if(targetID >= 0 && targetID < positions.size()){
                                Vector2 target = positions.get(targetID);
                                virtualServer.send(ActionCommand.makeReply(idPlayer,virtualServer.getServerID(),actionID,workerID,target));
                                return ExecutionResponse.SUCCESS;
                            }
                            stream.println("Position not available, please try again: \n<int1>");
                            return ExecutionResponse.FAIL;
                        }
                        stream.println("Select target n. you want to execute move to, please try again: \n<int1>");
                        return ExecutionResponse.FAIL;
                    }else{
                        //input is actionID
                        if(canConvertInputToInt(inputArray) && inputArray.length == 1){
                            int[] input = inputToInt(inputArray);
                            int actionID = input[0];
                            if(actionID >= 0 && actionID < nextActions.length){
                                NextAction nextAction = getSelectedNextAction(actions,actionID);
                                List<Vector2> possiblePositions = getAvailablePosition(nextAction);
                                return positionSelection(actions, possiblePositions,actionID);
                            }
                            stream.println("Action not available, please try again: \n<int1>");
                            return ExecutionResponse.FAIL;
                        }
                        stream.println("Select action n. you want to perform, please try again: \n<int1>");
                        return ExecutionResponse.FAIL;
                    }
                }else{
                    //input is workerID
                    if(canConvertInputToInt(inputArray) && inputArray.length == 1){
                        int[] input = inputToInt(inputArray);
                        int workerID = input[0];
                        if(workerID>=0 && workerID<N_WORKER) {
                            List<NextAction> actions =  clearNextActions(nextActions,workerID);
                            return actionSelection(actions);
                        }
                        stream.println("Worker not available, please try again: \n<int1>");
                        return ExecutionResponse.FAIL;
                    }
                    stream.println("Select worker you want to use, please try again: \n<int1>");
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
    private ExecutionResponse actionSelection(List<NextAction> actions) {
        if(canAutoSelectAction(actions)){
            stream.println("Time to " + actions.get(0).getActionName() + "...");
            int actionID = 0;
            NextAction  nextAction = getSelectedNextAction(actions,actionID);
            List<Vector2> availablePositions = getAvailablePosition(nextAction);
            return positionSelection(actions, availablePositions , actionID);
        }else{
            stream.println("Select the identifier of action you want to carry out: ");
            for(int i = 0; i<actions.size(); i++){ stream.println(i + "." + actions.get(i).getActionName() + "   "); }
            stream.println("\n<int1>");

            boolean shouldStop;
            String[] inputArray;
            int actionID = -1;
            do {
                inputArray = scanner.nextLine().split("\\s+");
                shouldStop = quitCheck(inputArray);

                if(canConvertInputToInt(inputArray) && inputArray.length == 1){
                    actionID = inputToInt(inputArray)[0];
                }else if(!shouldStop){
                    stream.println("Select the identifier of action you want to carry out, please try again: \n<int1>");
                }

            } while (!shouldStop && actionID >= 0 && actionID < actions.size());

            if(!shouldStop) {
                NextAction nextAction = getSelectedNextAction(actions,actionID);
                List<Vector2> possiblePositions = getAvailablePosition(nextAction);
                return positionSelection(actions, possiblePositions, actionID);
            }else
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
            String[] inputArray;
            int targetID = -1;
            do {
                inputArray = scanner.nextLine().split("\\s+");
                shouldStop = quitCheck(inputArray);

                if(canConvertInputToInt(inputArray) && inputArray.length == 1){
                    targetID = inputToInt(inputArray)[0];
                }else{
                    if(!shouldStop){
                        stream.println("Select the identifier n. of position you want to execute on, please try again: \n<int1>");
                    }
                }

            } while (!shouldStop && (targetID < 0 || targetID >= possiblePositions.size()) );

            if(!shouldStop) {
                int workerID = actions.get(0).getWorkerID();
                Vector2 target = possiblePositions.get(targetID);
                virtualServer.send(ActionCommand.makeReply(idPlayer,virtualServer.getServerID(),actionID,workerID,target));
                return ExecutionResponse.SUCCESS;
            }else
                return ExecutionResponse.QUIT;
        }
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
        return inputArray.length == 1 && inputArray[0].equals("start") && idHost == idPlayer;
    }


    //TODO : split in functions
    private void showMap(CompactMap compactMap) {
        CompactWorker[] compactWorkers = compactMap.getWorkers();

        clearScreen();
        System.out.flush();

        StringBuilder string = new StringBuilder();
        string.append("\n");
        string.append(" y |");
        for (int i = 0; i < LENGTH; i++) string.append(" ").append(i).append(" |");
        string.append("\nx   ");
        string.append("___ ".repeat(LENGTH));
        string.append("\n");

        for (int x = 0; x < HEIGHT; x++) {
            string.append(x).append("  |");
            for (int y = 0; y < LENGTH; y++) {
                boolean isWorkerOnCurrentCell = false;
                for (CompactWorker compactWorker : compactWorkers) {
                    if (compactWorker.getPosition().getX() == x && compactWorker.getPosition().getY() == y) {
                        string.append(colors.get(compactWorker.getOwnerID()).escape()).append(compactWorker.getWorkerID()).append(Color.RESET).append(" ");
                        isWorkerOnCurrentCell = true;
                    }
                }
                if (compactMap.isDome(x, y)) {
                    string.append("# #");
                } else {
                    if (!isWorkerOnCurrentCell) string.append("  ");
                    string.append(compactMap.getLevel(x, y));
                }
                string.append("|");
            }
            string.append("\n");
            string.append("__ |");
            string.append("___|".repeat(LENGTH));
            string.append("\n");
        }
        string.append("\n");
        stream.print(string);
    }

    private void showMap(CompactMap compactMap, Vector2[] availablePositions){
        CompactWorker[] compactWorkers = compactMap.getWorkers();

        clearScreen();
        System.out.flush();

        StringBuilder string = new StringBuilder();
        string.append("\n");
        string.append(" y |");
        for (int i = 0; i < LENGTH; i++) string.append(" ").append(i).append(" |");
        string.append("\nx   ");
        string.append("___ ".repeat(LENGTH));
        string.append("\n");

        for (int x = 0; x < HEIGHT; x++) {
            string.append(x).append("  |");
            for (int y = 0; y < LENGTH; y++) {
                boolean isWorkerOnCurrentCell = false;
                for (CompactWorker compactWorker : compactWorkers) {
                    if (compactWorker.getPosition().getX() == x && compactWorker.getPosition().getY() == y) {
                        string.append(colors.get(compactWorker.getOwnerID()).escape()).append(compactWorker.getWorkerID()).append(Color.RESET).append(" ");
                        isWorkerOnCurrentCell = true;
                    }
                }
                if (compactMap.isDome(x, y)) {
                    string.append("# #");
                } else {
                    if (!isWorkerOnCurrentCell) string.append("  ");
                    string.append(compactMap.getLevel(x, y));
                }
                string.append("|");
            }
            string.append("\n");
            string.append("__ |");
            if(availablePositions.length < 10){
                boolean available;
                for(int i = 0; i<LENGTH; i++){
                    available = false;
                    for(int j = 0; j<availablePositions.length ; j++){
                        if(availablePositions[j].getX() == x && availablePositions[j].getY() == i){
                            string.append("_").append(Color.GREEN.escape()).append(j).append(Color.RESET).append("_|");
                            available = true;
                        }
                    }
                    if(!available) string.append("___|");
                }
            }else{
                boolean available;
                for(int i = 0; i<LENGTH; i++){
                    available = false;
                    for (Vector2 availablePosition : availablePositions) {
                        if (availablePosition.getX() == x && availablePosition.getY() == i) {
                            string.append("_").append(Color.GREEN.escape()).append("+").append(Color.RESET).append("_|");
                            available = true;
                        }
                    }
                    if(!available) string.append("___|");
                }
            }

            string.append("\n");
        }
        string.append("\n");
        stream.print(string);
    }

    private void clearScreen() {
        System.out.print("\033[2J");
        System.out.flush();
    }

    private void initPlayers(StartCommand cmd) {
        if (idPlayer == cmd.getPlayersID()[0]) {
            colors.put(idPlayer, Color.BLUE);
            colors.put(cmd.getPlayersID()[1], Color.RED);
            if (playersNumber > 2)
                colors.put(cmd.getPlayersID()[2], Color.YELLOW);

        } else if (idPlayer == cmd.getPlayersID()[1]) {
            colors.put(idPlayer, Color.BLUE);
            colors.put(cmd.getPlayersID()[0], Color.RED);
            if (playersNumber > 2)
                colors.put(cmd.getPlayersID()[2], Color.YELLOW);

        } else {
            colors.put(idPlayer, Color.BLUE);
            colors.put(cmd.getPlayersID()[0], Color.RED);
            if (playersNumber > 2)
                colors.put(cmd.getPlayersID()[1], Color.YELLOW);
        }
    }
}
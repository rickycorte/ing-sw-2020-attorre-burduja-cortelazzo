package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.game.Vector2;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.view.IHumanInterface;

import java.io.PrintStream;
import java.util.*;

/**
 * Old cli adapted to use some network changes
 */
public class Cli implements IHumanInterface
{
    private static final int HEIGHT = it.polimi.ingsw.game.Map.HEIGHT;
    private static final int LENGTH = it.polimi.ingsw.game.Map.LENGTH;
    private static final int DOME_VALUE = 128;
    private static final int N_WORKER = 2;
    private static final int SERVER_ID = -1111;

    private INetworkAdapter virtualServer;
    private int playersNumber;
    private int idPlayer = -1;
    private int idHost;
    private Map<Integer,Color> colors = new HashMap<>();
    private PrintStream stream = new PrintStream(System.out,true);
    private Scanner scanner = new Scanner(System.in);
    private boolean logged = false;

    private Vector2[] availablePosition;
    private CommandWrapper lastCommandWrap;
    private ActionCommand lastActionCommand;
    private WorkerPlaceCommand lastWorkerCommand;

    private MatchState currentState = MatchState.WAIT;

    private enum MatchState{WAIT,GOD_SELECT,PICK_GOD,FIRST_PLAYER_SELECT,WORKER_PLACE,ACTION_TIME,END};


    public Cli(INetworkAdapter adapter){
        virtualServer = adapter;
    }

    public void onConnect(CommandWrapper cmdWrapper) {
        int targetID = cmdWrapper.getCommand(BaseCommand.class).getTarget();

        stream.println("[CLI] Received " + cmdWrapper.getType().name() + " command on onConnect");
        if (cmdWrapper.getType() == CommandType.JOIN && idPlayer == -1) {
            JoinCommand cmd = cmdWrapper.getCommand(JoinCommand.class);


            if (cmd.isJoin()) {
                idPlayer = cmd.getTarget();
                stream.println("[CLI] Join successful");
                stream.println("Connected to game, wait to start...");
                currentState = MatchState.WAIT;
                idHost = cmd.getHostPlayerID();
                logged = true;
            } else {
                if (cmd.isValidUsername()) {
                    stream.println("Can't join a game right now, try another time.");
                } else {
                    stream.println("Your username is not valid, choose another one");
                    String newUsername;
                    newUsername = scanner.nextLine();
                    CommandWrapper newJoinCommand = new CommandWrapper(CommandType.JOIN, new JoinCommand(cmd.getTarget(),virtualServer.getServerID(), newUsername, true));
                    virtualServer.send(newJoinCommand);
                }
                System.out.println("[Cli] Received "+ cmdWrapper.getType());
                //stream.println("Can't join a game.");
            }
        }else if (cmdWrapper.getType() == CommandType.JOIN && idPlayer == idHost){
            JoinCommand joinCommand = cmdWrapper.getCommand(JoinCommand.class);
            if(joinCommand.isJoin())
                stream.println("Type 'start' to start a 2 player game or wait 3rd player");
        }
    }

    @Override
    public void start()
    {
        stream.println("Type 'quit' to leave the game");
        System.out.println("Insert your username: ");


        if(virtualServer.connect("127.0.0.1", virtualServer.getDefaultPort(), scanner.nextLine()))
        {
            while(!logged) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored){}
            }
            inputLoop();
        }
        else
        {
            System.out.println("Connection refused");
        }

    }

    public void onDisconnect(CommandWrapper cmdWrapper){
        //never here ?
    }

    public void onCommand(CommandWrapper cmdWrapper){
        lastCommandWrap = cmdWrapper;
        BaseCommand baseCommand = cmdWrapper.getCommand(BaseCommand.class);

        if(baseCommand.getTarget() != idPlayer && baseCommand.getTarget() != virtualServer.getBroadCastID()) {
            stream.println("Turn of player n. "+ baseCommand.getTarget() + " wait until " + colors.get(baseCommand.getTarget()).escape() + cmdWrapper.getType().toString() + Color.RESET);
            return;
        }

        switch (cmdWrapper.getType()){
            case START:
                StartCommand startCommand = cmdWrapper.getCommand(StartCommand.class);
                playersNumber = startCommand.getPlayersID().length;
                initPlayers(startCommand);
                stream.println("GAME IS STARTED!");
                break;
            case FILTER_GODS:
                printGodsID();
                stream.println("Select "+playersNumber+" IDs you want to be allowed for this game :");
                for(int i=1; i<=playersNumber; i++){
                    stream.print("<int" + i + "> ");
                }
                stream.println();
                currentState = MatchState.GOD_SELECT;
                break;
            case PICK_GOD:
                PickGodCommand pickGodCommand = cmdWrapper.getCommand(PickGodCommand.class);
                stream.print("Available god ID for this game : ");
                for (int i : pickGodCommand.getGodID()){
                    stream.print(i + ", ");
                }
                stream.println();
                stream.println("Pick a god by selecting id : ");
                stream.println("<int1>");
                currentState = MatchState.PICK_GOD;
                break;
            case SELECT_FIRST_PLAYER:
                for (Integer i : colors.keySet())
                    stream.print(i + " ");
                stream.println();
                stream.println("Select first player for this game: ");
                stream.println("<int1>");
                currentState = MatchState.FIRST_PLAYER_SELECT;
                break;
            case PLACE_WORKERS:
                WorkerPlaceCommand cmd = cmdWrapper.getCommand(WorkerPlaceCommand.class);
                for(int i = 0; i<cmd.getPositions().length;i++){
                    stream.print(i + ".(" + cmd.getPositions()[i].getX() + "," + cmd.getPositions()[i].getY() + ") ");
                }
                stream.println();
                stream.println("Select 2 available positions by its identifiers: ");
                stream.println("<int1> <int2>");
                lastWorkerCommand = cmd;
                availablePosition = cmd.getPositions();
                currentState = MatchState.WORKER_PLACE;
                break;
            case ACTION_TIME:
                ActionCommand actionCommand = cmdWrapper.getCommand(ActionCommand.class);
                stream.print(actionCommand.getIdWorkerNMove()[0] + ", ");
                for(int i = 0,last = actionCommand.getIdWorkerNMove()[0];i<actionCommand.getIdWorkerNMove().length;i=i+2){
                    if(actionCommand.getIdWorkerNMove()[i] != last){
                        stream.print(actionCommand.getIdWorkerNMove()[i]);
                        last = actionCommand.getIdWorkerNMove()[i];
                    }
                }
                stream.println();
                stream.println("Select id of worker you want to move");
                stream.println("<int1>");

                currentState = MatchState.ACTION_TIME;
                lastActionCommand = actionCommand;
                break;
            case END_GAME:
                EndGameCommand endGameCommand = cmdWrapper.getCommand(EndGameCommand.class);
                if(endGameCommand.getWinnerID() == EndGameCommand.TARGET_LOST)
                    stream.print("Game ended.");
                else
                    stream.println("Game ended: " + endGameCommand.getWinnerID() + " win! ");
                break;
            case UPDATE:
                UpdateCommand updateCommand = cmdWrapper.getCommand(UpdateCommand.class);
                show(updateCommand);
                break;
        }

    }

    private void inputLoop(){
        String input;
        boolean shouldStop = false;

        do{
            input = scanner.nextLine();
            String[] inputArray = input.split("\\s+");

            if(inputArray[0].equals("start") && currentState == MatchState.WAIT && idPlayer == idHost){
                virtualServer.send(new CommandWrapper(CommandType.START,new StartCommand(idPlayer,virtualServer.getServerID())));
            }else if(inputArray[0].equals("quit")){
                shouldStop = true;
                virtualServer.send(new CommandWrapper(CommandType.LEAVE,new LeaveCommand(idPlayer,virtualServer.getServerID())));
            }else{
                try{
                    int[] intInput = new int[inputArray.length];
                    for(int i = 0; i<inputArray.length ; i++){
                        intInput[i] = Integer.parseInt(inputArray[i]);
                    }
                    receivedIntFromLine(intInput);
                }catch(NumberFormatException | NullPointerException e){
                    if(currentState != MatchState.WAIT) this.onCommand(lastCommandWrap);
                }
            }

        }while(!shouldStop);

    }

    private synchronized void receivedIntFromLine(int[] intInput){
        switch (currentState){
            case GOD_SELECT:
                virtualServer.send(new CommandWrapper(CommandType.FILTER_GODS, new FilterGodCommand(idPlayer,virtualServer.getServerID(),intInput)));
                break;
            case PICK_GOD:
                virtualServer.send(new CommandWrapper(CommandType.PICK_GOD, new PickGodCommand(idPlayer,virtualServer.getServerID(),intInput)));
                break;
            case FIRST_PLAYER_SELECT:
                if(intInput.length == 1) virtualServer.send(new CommandWrapper(CommandType.SELECT_FIRST_PLAYER, new FirstPlayerPickCommand(idPlayer,virtualServer.getServerID(),intInput[0])));
                else virtualServer.send(new CommandWrapper(CommandType.SELECT_FIRST_PLAYER, new FirstPlayerPickCommand(idPlayer,virtualServer.getServerID(),-1)));
                break;
            case WORKER_PLACE:
                if(intInput.length == N_WORKER && lastWorkerCommand.getPositionsIndexes().contains(intInput[0]) && lastWorkerCommand.getPositionsIndexes().contains(intInput[1])) {
                    Vector2[] selectedPositions = new Vector2[N_WORKER];
                    selectedPositions[0] = availablePosition[intInput[0]];
                    selectedPositions[1] = availablePosition[intInput[1]];
                    virtualServer.send(new CommandWrapper(CommandType.PLACE_WORKERS, new WorkerPlaceCommand(idPlayer, virtualServer.getServerID(), selectedPositions)));
                }else{
                    this.onCommand(lastCommandWrap);
                }
                break;
            case ACTION_TIME:
                if(0<=intInput[0] && intInput[0]<N_WORKER)
                    executeAction(intInput[0]);
                else
                    this.onCommand(lastCommandWrap);
        }
    }

    private void executeAction(int workerId){
        List<Integer> availableInteger = new ArrayList<>();
        if(!lastActionCommand.getAvailableWorker().contains(workerId)){
            this.onCommand(lastCommandWrap);
            return;
        }

        List<String> actions = lastActionCommand.getActionForWorker(workerId);

        for(int i = 0; i<actions.size(); i++){
            stream.print(i + "." + actions.get(i) + "  ");
            availableInteger.add(i);
        }

        stream.println();
        stream.println("Select action's number you want execute: ");
        stream.println("<int1>");

        int actionId = scanner.nextInt();
        scanner.nextLine();
        if(!availableInteger.contains(actionId)){
            this.onCommand(lastCommandWrap);
            return;
        }


        int baseIndex = lastActionCommand.getBaseIndexForAction(workerId);
        List<Vector2> positions = lastActionCommand.getPositionsForAction(baseIndex + actionId);

        availableInteger.clear();
        for(int i = 0; i<positions.size(); i++){
            stream.print(i + ".(" + positions.get(i).getX() + "," + positions.get(i).getY() + ")  ");
            availableInteger.add(i);
        }


        stream.println();
        stream.println("Select a cell from available : ");
        stream.println("<int1>");

        int targetId = scanner.nextInt();
        scanner.nextLine();
        if(!availableInteger.contains(targetId)){
            this.onCommand(lastCommandWrap);
            return;
        }

        baseIndex = lastActionCommand.getBaseIndexForPositions(baseIndex + actionId);
        Vector2 target = lastActionCommand.getTargetPosition(baseIndex + targetId);

        virtualServer.send(new CommandWrapper(CommandType.ACTION_TIME,new ActionCommand(idPlayer,SERVER_ID,new int[]{workerId,actionId},target)));
    }

    private void show(UpdateCommand updateCommand){
        Vector2[] workerPos = updateCommand.getV2Data().clone();
        int[] pairOwnerWorker = getPairOwnerWorker(updateCommand.getIntData());

        /*
        int[][] map = new int[LENGTH][HEIGHT];
        for(int x = 0; x < LENGTH; x++) {
            for (int y = 0; y < HEIGHT; y++)
                map[x][y] = updateCommand.getIntData()[x + y];
        }
         */
        clearScreen();

        System.out.flush();

        StringBuilder string = new StringBuilder();
        string.append(" y | 0 | 1 | 2 | 3 | 4 |\n");
        string.append("x   ___ ___ ___ ___ ___ \n");
        int mapInt = 0;
        for(int x = 0; x < HEIGHT; x++){
            string.append(x).append("  |");
            for(int y = 0; y<LENGTH; y++){
                boolean flag=false;
                for(int i=0,j=0; i<workerPos.length;i++,j= j+2){
                    if(workerPos[i].getX() == x && workerPos[i].getY() == y){
                        string.append(colors.get(pairOwnerWorker[j]).escape()).append(pairOwnerWorker[j+1]).append(Color.RESET).append(" ");
                        flag = true;
                    }
                }
                if(!flag) string.append("  ");
                if(updateCommand.getIntData()[x+y] >= DOME_VALUE)
                    string.append("# #");
                else
                    string.append(Integer.numberOfTrailingZeros(updateCommand.getIntData()[mapInt]));
                mapInt++;
                string.append("|");
            }
            string.append("\n");
            string.append("__ |___|___|___|___|___|\n");
        }
        stream.print(string);
        //System.out.print(string);
    }

    private int[] getPairOwnerWorker(int[] updateIntData){
        int pairLength = updateIntData.length - (LENGTH*HEIGHT);
        int[] pairOwnerWorker = new int[pairLength];

        System.arraycopy(updateIntData,(LENGTH*HEIGHT),pairOwnerWorker,0,pairLength);

        return pairOwnerWorker;
    }

    private void printGodsID(){
        //TODO : print CardCollection (player name + ID)
    }

    private void clearScreen(){
        System.out.print("\033[2J");
        //System.out.print("\r");
        //System.out.print("\b\b\b\b\b");
        System.out.flush();
    }

    private void initPlayers(StartCommand cmd){
        //TODO : random ? and select my color from available?
        if(idPlayer == cmd.getPlayersID()[0]){
            colors.put(idPlayer,Color.BLUE);
            colors.put(cmd.getPlayersID()[1],Color.RED);
            if(playersNumber>2)
                colors.put(cmd.getPlayersID()[2],Color.YELLOW);

        }else if(idPlayer == cmd.getPlayersID()[1]){
            colors.put(idPlayer,Color.BLUE);
            colors.put(cmd.getPlayersID()[0],Color.RED);
            if(playersNumber>2)
                colors.put(cmd.getPlayersID()[2],Color.YELLOW);

        }else{
            colors.put(idPlayer,Color.BLUE);
            colors.put(cmd.getPlayersID()[0],Color.RED);
            if(playersNumber>2)
                colors.put(cmd.getPlayersID()[1],Color.YELLOW);

        }
    }

}

package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.game.Vector2;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.view.IHumanInterface;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Old cli adapted to use some network changes
 */
public class Cli implements IHumanInterface
{
    private static final int HEIGHT = 7;
    private static final int LENGTH = 7;
    private static final int DOME_VALUE = 128;
    private static final int N_WORKER = 2;
    private static final int SERVER_ID = -1111;

    private INetworkAdapter virtualServer;
    private int playersNumber;
    private int idPlayer = -1;
    private Map<Integer,Color> colors = new HashMap<>();
    private PrintStream stream = new PrintStream(System.out,true);
    private Scanner scanner = new Scanner(System.in);

    public Cli(INetworkAdapter adapter){
        virtualServer = adapter;
    }

    public void onConnect(CommandWrapper cmdWrapper) {
        int targetID = cmdWrapper.getCommand(BaseCommand.class).getTarget();

        stream.println("[CLI] Received " + cmdWrapper.getType().name() + " command on onConnect");
        if (cmdWrapper.getType() == CommandType.JOIN && idPlayer == -1) {
            JoinCommand cmd = cmdWrapper.getCommand(JoinCommand.class);

            idPlayer = cmd.getTarget();

            if (cmd.isJoin()) {
                stream.println("[CLI] Join successful");
                stream.println("Connected to game, wait to start...");
            } else {
                if (cmd.isValidUsername()) {
                    stream.println("Can't join a game right now, try another time.");
                } else {
                    stream.println("Your username is not valid, choose another one");
                    String newUsername = null;
                    if (scanner.hasNext()) newUsername = scanner.next();
                    CommandWrapper newJoinCommand = new CommandWrapper(CommandType.JOIN, new JoinCommand(idPlayer, virtualServer.getServerID(), newUsername, true));
                    virtualServer.send(newJoinCommand);
                }
                System.out.println("[Cli] Received "+ cmdWrapper.getType());
                //stream.println("Can't join a game.");
            }
        }
    }

    @Override
    public void start()
    {
        Scanner scn = new Scanner(System.in);
        System.out.println("Insert your username: ");

        if(virtualServer.connect("127.0.0.1", virtualServer.getDefaultPort(), scn.nextLine()))
        {
            try
            {
                // we still have control and we must keep main alive
                while (true) Thread.sleep(10000);
            }catch (Exception ignored){}
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
        int targetID = cmdWrapper.getCommand(BaseCommand.class).getTarget();

        if(targetID != idPlayer && targetID != virtualServer.getBroadCastID())
        {
            System.out.println("Skipped command for "+ targetID+" because i'm "+ idPlayer);
            return; // skip command not mine
        }

        switch (cmdWrapper.getType()){
            case START:
                //System.out.println("Received start command");
                StartCommand startCommand = cmdWrapper.getCommand(StartCommand.class);
                playersNumber = startCommand.getPlayersID().length;
                initPlayers(startCommand);
                break;
            case FILTER_GODS:
                printGodsID();
                stream.println("Select "+playersNumber+" IDs you want to be allowed for this game :");
                int[] intData;
                intData = getIntFromLine(playersNumber);

                CommandWrapper cmw = new CommandWrapper(CommandType.FILTER_GODS,new FilterGodCommand(idPlayer,SERVER_ID,intData));
                virtualServer.send(cmw);
                break;
            case SELECT_FIRST_PLAYER:
                for (Integer i : colors.keySet())
                    stream.print(i + " ");
                stream.println();
                stream.println("Select first player for this game: ");
                int[] firstPlayer;
                firstPlayer = getIntFromLine(1);
                virtualServer.send(new CommandWrapper(CommandType.SELECT_FIRST_PLAYER, new FirstPlayerPickCommand(idPlayer,SERVER_ID,firstPlayer[0])));
                break;
            case PICK_GOD:
                PickGodCommand pickGodCommand = cmdWrapper.getCommand(PickGodCommand.class);
                stream.print("Available god ID for this game : ");
                for (int i : pickGodCommand.getGodID()){
                    stream.print(i + ", ");
                }
                stream.println();
                stream.println("Pick a god by selecting id : ");
                int[] god = getIntFromLine(1);
                virtualServer.send(new CommandWrapper(CommandType.PICK_GOD,new PickGodCommand(idPlayer,SERVER_ID,god[0])));
                break;
            case PLACE_WORKERS:
                WorkerPlaceCommand cmd = cmdWrapper.getCommand(WorkerPlaceCommand.class);
                for(int i = 0; i<cmd.getPositions().length;i++){
                    stream.print(i + ".(" + cmd.getPositions()[i].getX() + "," + cmd.getPositions()[i].getY() + ") ");
                }
                stream.println();
                stream.println("Select 2 available positions by its identifiers: ");
                Vector2[] selectedPositions;
                selectedPositions = getVector2FromLine(cmd.getPositions(),N_WORKER);
                virtualServer.send(new CommandWrapper(CommandType.PLACE_WORKERS,new WorkerPlaceCommand(idPlayer,SERVER_ID,selectedPositions)));
                break;
            case ACTION_TIME:
                    stream.println("ACTION TIME : currently not available  BUT VERY GOOD WORK ");

                break;
            case END_GAME:
                EndGameCommand endGameCommand = cmdWrapper.getCommand(EndGameCommand.class);
                if(endGameCommand.isWinner())
                    stream.println("\nYOU WIN !");
                    //TODO : disconnect ? (and in loser)
                else
                    stream.println("\nYOU LOSE !");
                break;
            case UPDATE:
                UpdateCommand updateCommand = cmdWrapper.getCommand(UpdateCommand.class);
                show(updateCommand);
                break;
        }

    }

    private Vector2[] getVector2FromLine(Vector2[] available, int size){
        Vector2[] positions = new Vector2[size];

        //scanner.nextLine();
        for(int i = 0; i<size; i++){
            if(scanner.hasNextInt()) positions[i] = available[scanner.nextInt()];
            else return positions;
        }
        return positions;
    }


    private int[] getIntFromLine(int size){
        int[] intData = new int[size];
        //scanner.nextLine();
        for(int i = 0; i<size; i++){
            if(scanner.hasNextInt()) intData[i] = scanner.nextInt();
            else return intData;
        }
        return  intData;
    }

    private void show(UpdateCommand updateCommand){
        Vector2[] workerPos = updateCommand.getV2Data().clone();
        int[] pairOwnerWorker = new int[1*2*2];
        System.arraycopy(updateCommand.getIntData(),48,pairOwnerWorker,0,1*2*2);

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
        string.append(" y | 0 | 1 | 2 | 3 | 4 | 5 | 6 |\n");
        string.append("x   ___ ___ ___ ___ ___ ___ ___ \n");
        for(int x = 0; x < HEIGHT; x++){
            string.append(x).append("  |");
            for(int y = 0; y<LENGTH; y++){
                int i;
                boolean flag=false;
                for(i=0; i<workerPos.length;i++){
                    if(workerPos[i].getX() == x && workerPos[i].getY() == y){
                        if(i == 0){
                            string.append(colors.get(pairOwnerWorker[i]).escape()).append(pairOwnerWorker[i+1]).append(Color.RESET).append(" ");
                            flag= true;
                        }else{
                            string.append(colors.get(pairOwnerWorker[(i*2)]).escape()).append(pairOwnerWorker[(i*2)+1]).append(Color.RESET).append(" ");
                            flag = true;
                        }
                    }
                }
                if(!flag) string.append("  ");
                if(updateCommand.getIntData()[x+y] >= DOME_VALUE)
                    string.append("# #");
                else
                    string.append(Integer.numberOfTrailingZeros(updateCommand.getIntData()[x+y]));

                string.append("|");
            }
            string.append("\n");
            string.append("__ |___|___|___|___|___|___|___|\n");
        }
        stream.print(string);
        //System.out.print(string);
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
        colors.put(idPlayer,Color.BLUE);
        colors.put(cmd.getPlayersID()[1],Color.RED);
        if(playersNumber>2)
            colors.put(cmd.getPlayersID()[2],Color.YELLOW);
    }

}
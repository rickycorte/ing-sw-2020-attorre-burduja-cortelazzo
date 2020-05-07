package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.game.Vector2;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;

import java.io.PrintStream;
import java.util.*;

public class Cli {
    private static final int HEIGHT = 7;
    private static final int LENGTH = 7;
    private static final int DOME_VALUE = 128;

    private INetworkAdapter virtualServer;
    private int playerNumber;
    private int idPlayer;
    private Map<Integer,Color> colors = new HashMap<>();
    private PrintStream stream = new PrintStream(System.out,true);


    public Cli(INetworkAdapter adapter){
        virtualServer = adapter;
    }

    public void onCommand(CommandWrapper cmdWrapper){
        BaseCommand baseCommand = cmdWrapper.getCommand(BaseCommand.class);

        if(baseCommand.isRequest()) {
            //request commands
            switch (cmdWrapper.getType()){
                case FILTER_GODS:
                    printGodsID();
                    System.out.println("Select "+playerNumber+" IDs");
                    int[] intData;
                    intData = null ;
                    //virtualServer.send(new Command());
                    break;
                case SELECT_FIRST_PLAYER:
                    //printPlayerID();
                    //intData[] = sendIntData(1);
                    //virtualServer.send(new Command());
                    break;
                case PLACE_WORKERS:

                    break;
            }

        }else{

            switch (cmdWrapper.getType()) {
                case JOIN:
                    JoinCommand joinCommand = cmdWrapper.getCommand(JoinCommand.class);
                    if(joinCommand.isJoin())
                        idPlayer = joinCommand.getTarget();
                    break;
                case START:
                    StartCommand startCommand = cmdWrapper.getCommand(StartCommand.class);
                    playerNumber = startCommand.getPlayersID().length;
                    initColors(startCommand);
                    break;
                case END_GAME:
                    EndGameCommand endGameCommand = cmdWrapper.getCommand(EndGameCommand.class);
                    if(endGameCommand.isWinner())
                        System.out.println("\nYOU WIN !");
                        //TODO : disconnect ? (and in loser)
                    else
                        System.out.println("\nYOU LOSE !");
                    break;
                case UPDATE:
                    UpdateCommand updateCommand = cmdWrapper.getCommand(UpdateCommand.class);
                    show(updateCommand);
                    break;
            }
        }
    }


    private void show(UpdateCommand updateCommand){
        Vector2[] workerPos = updateCommand.getV2Data().clone();
        int[] pairOwnerWorker = new int[playerNumber*2*2];
        System.arraycopy(updateCommand.getIntData(),48,pairOwnerWorker,0,playerNumber*2*2);

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
        //TODO : print CardCollection
    }

    private void clearScreen(){
        System.out.print("\033[2J");
        //System.out.print("\r");
        //System.out.print("\b\b\b\b\b");
        System.out.flush();
    }

    private void initColors(StartCommand cmd){
        //TODO : random ? and select my color from available?
        colors.put(idPlayer,Color.BLUE);
        colors.put(cmd.getPlayersID()[1],Color.RED);
        if(playerNumber>2)
            colors.put(cmd.getPlayersID()[2],Color.YELLOW);
    }

}

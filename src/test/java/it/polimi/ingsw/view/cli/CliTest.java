package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.game.Player;
import it.polimi.ingsw.game.Vector2;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


public class CliTest {

    private CommandWrapper serializer(CommandWrapper cmd){
        cmd.Serialize();
        return  cmd;
    }
    /*
    @Test
    void firstTest(){
        Cli cli = new Cli(null);

        cli.onCommand(serializer(new CommandWrapper(CommandType.JOIN,new JoinCommand(-1111,1,true))));

        int[] ids = new int[3];
        ids[0] = 1;
        ids[1] = 2;
        ids[2] = 3;
        cli.onCommand(serializer(new CommandWrapper(CommandType.START,new StartCommand(-1111,1,))));
        //colors set well ?

        int[] intArray = new int[48+(3*2*2)];
        //0-47 map value
        //48-59 pair ownerID workerID
        for(int i=0;i<48;i++)
            intArray[i] = 1;
        intArray[48] = 1; //ownerID 1
        intArray[49] = 0; //workerID 0
        intArray[50] = 1; //ownerID 1
        intArray[51] = 1; //workerID 1
        intArray[52] = 2; //ownerID 2
        intArray[53] = 0; //workerID 0
        intArray[54] = 2; //ownerID 2
        intArray[55] = 1; //workerID 1
        intArray[56] = 3; //ownerID 3
        intArray[57] = 0; //workerID 0
        intArray[58] = 3; //ownerID 3
        intArray[59] = 1; //workerID 1

        Vector2[] arrayPos = new Vector2[6];
        arrayPos[0] = new Vector2(0,0);
        arrayPos[1] = new Vector2(1,1);
        arrayPos[2] = new Vector2(1,2);
        arrayPos[3] = new Vector2(3,3);
        arrayPos[4] = new Vector2(6,0);
        arrayPos[5] = new Vector2(5,4);

        cli.onCommand(serializer(new CommandWrapper(CommandType.UPDATE,new UpdateCommand(-1111,1,intArray,arrayPos))));

        arrayPos[0] = new Vector2(1,0);
        cli.onCommand(serializer(new CommandWrapper(CommandType.UPDATE,new UpdateCommand(-1111,1,intArray,arrayPos))));

        cli.onCommand(serializer(new CommandWrapper(CommandType.END_GAME,new EndGameCommand(-1111,1,true))));
    }
     */


    @Test
    void shouldSendID(){
        Cli cli = new Cli(null);

        List<Player> connectedPlayers = new ArrayList<>();
        Player p1 = new Player(1,"kekko");
        Player p2 = new Player(2,"vlad");
        Player p3 = new Player(3,"ricky");
        connectedPlayers.add(p1);
        connectedPlayers.add(p2);
        connectedPlayers.add(p3);

        cli.onCommand(serializer(new CommandWrapper(CommandType.START,new StartCommand(-1111,1,connectedPlayers))));

        cli.onCommand(serializer((new CommandWrapper(CommandType.FILTER_GODS,new FilterGodCommand(-1111,1)))));

    }
}

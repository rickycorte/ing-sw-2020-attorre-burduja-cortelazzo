package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.NextAction;
import it.polimi.ingsw.game.Vector2;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.server.Network;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.naming.ldap.Control;

import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

public class controllerTest {

    Controller controller;

    @BeforeEach
    void setUp() {
        controller = new Controller(new Network());

        controller.onConnect(new Command(Command.CType.JOIN.toInt(),false,0,1111,"firstplayer"));
        controller.onConnect(new Command(Command.CType.JOIN.toInt(),false,1,1111,"secondplayer"));

    }

    @Test
    void connectPlayerTest(){
        //TODO: add adapter to parameters
        Controller controllerT = new Controller(new Network());

        //join with wrong data
        controllerT.onConnect(new Command(Command.CType.JOIN.toInt(),false,0,1111,123));
        assertTrue(controllerT.getConnected_players().isEmpty());

        //join successful
        controllerT.onConnect(new Command(Command.CType.JOIN.toInt(),false,0,1111,"1"));
        assertEquals(controllerT.getConnected_players().get(0).getId(),0);

        //join with username already picked
        controllerT.onConnect(new Command(Command.CType.JOIN.toInt(),false,1,1111,"1"));
        assertEquals(controllerT.getConnected_players().size(),1);

        //join successful
        controllerT.onConnect(new Command(Command.CType.JOIN.toInt(),false,1,1111,"2"));
        assertEquals(controllerT.getConnected_players().get(1).getId(),1);

        //join successful
        controllerT.onConnect(new Command(Command.CType.JOIN.toInt(),false,2,1111,"3"));
        assertEquals(controllerT.getConnected_players().get(2).getId(),2);

        //join a started game
        controllerT.onConnect(new Command(Command.CType.JOIN.toInt(),false,4,1111,"4"));
        assertEquals(controllerT.getConnected_players().size(),3);
    }

    @Test
    void startGameTest() {
        //start a new game with the connected players

        //non-host try to start
        controller.onCommand(new Command(Command.CType.START.toInt(),false,1,11111));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WAIT);

        //no command is Sent back
        assertNull(controller.getLastSent());

        //host try to start
        controller.onCommand(new Command(Command.CType.START.toInt(),false,0,11111));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_FILTER);
        assertEquals(controller.getLastSent().getType(),Command.CType.FILTER_GODS);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);

        //send back a Filter God command
        assertEquals(controller.getLastSent().getType(),Command.CType.FILTER_GODS);

        //start command
        controller.onCommand(new Command(Command.CType.START.toInt(),false,0,11111));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_FILTER);
        assertEquals(controller.getLastSent().getType(),Command.CType.FILTER_GODS);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);

        //expected FILTER_GODS command

        //command from not host player -> command discarded
        controller.onCommand(new Command(Command.CType.FILTER_GODS.toInt(),false,1,11111));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_FILTER);
        assertEquals(controller.getLastSent().getType(),Command.CType.FILTER_GODS);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);


        //command not expected -> command discarded
        controller.onCommand(new Command(Command.CType.SELECT_FIRST_PLAYER.toInt(),false,0,11111));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_FILTER);
        assertEquals(controller.getLastSent().getType(),Command.CType.FILTER_GODS);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);

        //correct header, content not correct -> command failed
        controller.onCommand(new Command(Command.CType.FILTER_GODS.toInt(),false,0,11111,new int[]{0,1,2,3}));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_FILTER);
        assertEquals(controller.getLastSent().getType(),Command.CType.FILTER_GODS);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);
    }


    @Test
    public void pickGod3PlayerTest(){
        //join a new player
        controller.onConnect(new Command(Command.CType.JOIN.toInt(),false,2,1111,"thirdplayer"));

        //Sent a request command for filter god
        assertEquals(controller.getLastSent().getType(),Command.CType.FILTER_GODS);
        assertEquals(controller.getLastSent().getSender(),0);
        Command SentAfterAutoStart = controller.getLastSent();

        //Start game command , but with 3 player game start automatically
        controller.onCommand(new Command(Command.CType.START.toInt(),false,0,11111));
        //command is discarded
        assertEquals(controller.getLastSent().getSender(),0);
        assertEquals(SentAfterAutoStart,controller.getLastSent());


        //Filter god
        controller.onCommand(new Command(Command.CType.FILTER_GODS.toInt(),false,0,11111,new int[]{1,2,3}));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        //Sent a command for pick god to id:1
        assertEquals(controller.getLastSent().getType(),Command.CType.PICK_GOD);
        assertEquals(controller.getLastSent().getTarget(),1);
        assertEquals(controller.getLastSent().getIntData().length,3); //last pack sendend contains available card id

        //id:1 Pick God n.3 command executed
        controller.onCommand(new Command(Command.CType.PICK_GOD.toInt(),false,1,1111, 3));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent command for pick god to id:2
        assertEquals(controller.getLastSent().getType(),Command.CType.PICK_GOD);
        assertEquals(controller.getLastSent().getTarget(),2);
        assertEquals(controller.getLastSent().getIntData().length,2); //last pack sendend contains available card id

        //id2 try to Pick God n.3 command failed
        controller.onCommand(new Command(Command.CType.PICK_GOD.toInt(),false,2,1111, 3));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent command for pick god to id:2
        assertEquals(controller.getLastSent().getType(),Command.CType.PICK_GOD);
        assertEquals(controller.getLastSent().getTarget(),2);
        assertEquals(controller.getLastSent().getIntData().length,2); //last pack sendend contains available card id

        //id0 try to pick God n.2 command discarded
        controller.onCommand(new Command(Command.CType.PICK_GOD.toInt(),false,0,1111, 2));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent command for pick god to id:2
        assertEquals(controller.getLastSent().getType(),Command.CType.PICK_GOD);
        assertEquals(controller.getLastSent().getTarget(),2);
        assertEquals(controller.getLastSent().getIntData().length,2); //last pack sendend contains available card id

        //id2 Pick God n.2 (should start autopick for id0 Pick God n.1)
        controller.onCommand(new Command(Command.CType.PICK_GOD.toInt(),false,2,1111, 2));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.FIRST_PLAYER_PICK);
        assertEquals(controller.getMatch().getHost().getGod().getId(),1);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);
        //Sent command for Select First Player to host
        assertEquals(controller.getLastSent().getType(),Command.CType.SELECT_FIRST_PLAYER);
        assertEquals(controller.getLastSent().getTarget(),0);

        //send first player selection by non host
        controller.onCommand(new Command(Command.CType.SELECT_FIRST_PLAYER.toInt(),false,1,1111,1));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.FIRST_PLAYER_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);
        assertEquals(controller.getLastSent().getType(),Command.CType.SELECT_FIRST_PLAYER);
        assertEquals(controller.getLastSent().getTarget(),0);

        //send first player selection by host,wrong content
        controller.onCommand(new Command(Command.CType.SELECT_FIRST_PLAYER.toInt(),false,0,1111,3));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.FIRST_PLAYER_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);
        assertEquals(controller.getLastSent().getType(),Command.CType.SELECT_FIRST_PLAYER);
        assertEquals(controller.getLastSent().getTarget(),0);

        //first player select
        controller.onCommand(new Command(Command.CType.SELECT_FIRST_PLAYER.toInt(),false,0,1111,1));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        //Sent command for Worker place to first player
        assertEquals(controller.getLastSent().getType(),Command.CType.PLACE_WORKERS);
        assertEquals(controller.getLastSent().getTarget(),1);
        assertEquals(controller.getLastSent().getV2Data().length,49);

    }

    @Test
    void pickGod2PlayerTest(){
        //start voluntary Game
        controller.onCommand(new Command(Command.CType.START.toInt(),false,0,11111));

        //filter gods
        controller.onCommand(new Command(Command.CType.FILTER_GODS.toInt(),false,0,11111,new int[]{1,2}));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        //Sent back a command with possible god ids
        assertEquals(controller.getLastSent().getType(),Command.CType.PICK_GOD);
        assertEquals(controller.getLastSent().getTarget(),controller.getMatch().getCurrentPlayer().getId());

        //pick god and go to selection of first player
        controller.onCommand(new Command(Command.CType.PICK_GOD.toInt(),false,1,11111,2));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.FIRST_PLAYER_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);

        //0 select a wrong player
    }

    @Test
    void workerPlacementTest(){
        controller.onConnect(new Command(Command.CType.JOIN.toInt(),false,2,1111,"thirdplayer"));
        controller.onCommand(new Command(Command.CType.FILTER_GODS.toInt(),false,0,11111,new int[]{1,2,3}));
        controller.onCommand(new Command(Command.CType.PICK_GOD.toInt(),false,1,1111, 3));
        controller.onCommand(new Command(Command.CType.PICK_GOD.toInt(),false,2,1111, 2));
        controller.onCommand(new Command(Command.CType.SELECT_FIRST_PLAYER.toInt(),false,0,1111,1));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        assertEquals(controller.getLastSent().getType(),Command.CType.PLACE_WORKERS);
        assertEquals(controller.getLastSent().getTarget(),1);
        assertEquals(controller.getLastSent().getV2Data().length,49);
        //1 is first player selected by host(0), all position are available

        //place workers for player 1
        Vector2[] available = controller.getLastSent().getV2Data();
        controller.onCommand(new Command(Command.CType.PLACE_WORKERS.toInt(),false,1,1111,new Vector2[]{available[0],available[48]}));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent a Place Workers command to 2, with 47 avilable position
        assertEquals(controller.getLastSent().getType(),Command.CType.PLACE_WORKERS);
        assertEquals(controller.getLastSent().getTarget(),2);
        assertEquals(controller.getLastSent().getV2Data().length,47);

        //2 try to place worker with wrong content
        available = controller.getLastSent().getV2Data();
        Vector2 wrongPos = new Vector2(0,0);
        controller.onCommand(new Command(Command.CType.PLACE_WORKERS.toInt(),false,2,1111,new Vector2[]{available[1],wrongPos}));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent a Place Workers command to 1, with 47 available postion
        assertEquals(controller.getLastSent().getType(),Command.CType.PLACE_WORKERS);
        assertEquals(controller.getLastSent().getTarget(),2);
        assertEquals(controller.getLastSent().getV2Data().length,47);

        //2 try to place worker with no content
        controller.onCommand(new Command(Command.CType.PLACE_WORKERS.toInt(),false,2,1111));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent a Place Workers command to 1, with 47 available postion
        assertEquals(controller.getLastSent().getType(),Command.CType.PLACE_WORKERS);
        assertEquals(controller.getLastSent().getTarget(),2);
        assertEquals(controller.getLastSent().getV2Data().length,47);

        //2 try to place worker with not complete content
        available = controller.getLastSent().getV2Data();
        controller.onCommand(new Command(Command.CType.PLACE_WORKERS.toInt(),false,2,1111,new Vector2[]{available[1]}));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent a Place Workers command to 1, with 47 available postion
        assertEquals(controller.getLastSent().getType(),Command.CType.PLACE_WORKERS);
        assertEquals(controller.getLastSent().getTarget(),2);
        assertEquals(controller.getLastSent().getV2Data().length,47);

        //2 place worker
        available = controller.getLastSent().getV2Data();
        controller.onCommand(new Command(Command.CType.PLACE_WORKERS.toInt(),false,2,1111,new Vector2[]{available[1],available[2]}));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);
        //Sent place worker command to last player
        assertEquals(controller.getLastSent().getType(),Command.CType.PLACE_WORKERS);
        assertEquals(controller.getLastSent().getTarget(),0);
        assertEquals(controller.getLastSent().getV2Data().length,45);

        //0 place worker
        available = controller.getLastSent().getV2Data();
        controller.onCommand(new Command(Command.CType.PLACE_WORKERS.toInt(),false,0,1111,new Vector2[]{available[3],available[4]}));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GAME);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        //ACTION is now starting
        assertEquals(controller.getLastSent().getType(),Command.CType.ACTION_TIME);
        assertEquals(controller.getLastSent().getTarget(),1);
    }

    @Test
    void gameActionsTest(){
        controller.onConnect(new Command(Command.CType.JOIN.toInt(),false,2,1111,"thirdplayer"));
        controller.onCommand(new Command(Command.CType.FILTER_GODS.toInt(),false,0,11111,new int[]{1,2,3}));
        controller.onCommand(new Command(Command.CType.PICK_GOD.toInt(),false,1,1111, 3));
        controller.onCommand(new Command(Command.CType.PICK_GOD.toInt(),false,2,1111, 2));
        controller.onCommand(new Command(Command.CType.SELECT_FIRST_PLAYER.toInt(),false,0,1111,1));
        Vector2[] available = controller.getLastSent().getV2Data();
        controller.onCommand(new Command(Command.CType.PLACE_WORKERS.toInt(),false,1,1111,new Vector2[]{available[0],available[48]}));
        available = controller.getLastSent().getV2Data();
        controller.onCommand(new Command(Command.CType.PLACE_WORKERS.toInt(),false,2,1111,new Vector2[]{available[0],available[1]}));
        available = controller.getLastSent().getV2Data();
        controller.onCommand(new Command(Command.CType.PLACE_WORKERS.toInt(),false,0,1111,new Vector2[]{available[0],available[1]}));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GAME);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        assertEquals(controller.getLastSent().getType(),Command.CType.ACTION_TIME);
        assertEquals(controller.getLastSent().getTarget(),1);

        //correct values in command are sent
        assertEquals(controller.getLastSent().getStringData()[0],controller.getMatch().getNextActions(controller.getConnected_players().get(1)).get(0).getActionName());
        assertEquals(controller.getLastSent().getIntData()[0],controller.getMatch().getNextActions(controller.getConnected_players().get(1)).get(0).getWorker());
        assertEquals(controller.getLastSent().getIntData()[1],controller.getMatch().getNextActions(controller.getConnected_players().get(1)).get(0).getAvailable_position().size());
        assertEquals(controller.getLastSent().getV2Data()[0].getX(),controller.getMatch().getNextActions(controller.getConnected_players().get(1)).get(0).getAvailable_position().get(0).getX());
        assertEquals(controller.getLastSent().getV2Data()[0].getY(),controller.getMatch().getNextActions(controller.getConnected_players().get(1)).get(0).getAvailable_position().get(0).getY());
        assertEquals(controller.getLastSent().getV2Data()[1],controller.getMatch().getNextActions(controller.getConnected_players().get(1)).get(0).getAvailable_position().get(1));

        Command exCmdSent = controller.getLastSent();
        //Action Time , 1 is first player

        //MAP with idPlayerWorker
        // | 1- | -- | -- | -- | -- | -- | -- |
        // | 2- | -- | -- | -- | -- | -- | -- |
        // | 2- | -- | -- | -- | -- | -- | -- |
        // | 0- | -- | -- | -- | -- | -- | -- |
        // | 0- | -- | -- | -- | -- | -- | -- |
        // | -- | -- | -- | -- | -- | -- | -- |
        // | -- | -- | -- | -- | -- | -- | 1- |

        //2 try to execute an action but it's not his turn
        controller.onCommand(new Command(Command.CType.ACTION_TIME.toInt(),false,2,1111,new int[]{0,0},new Vector2(0,2)));
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1); //player not changed
        assertEquals(controller.getLastSent().getTarget(),1);
        assertEquals(controller.getLastSent(),exCmdSent);

        //1 try to execute an action to a occupied cell
        controller.onCommand(new Command(Command.CType.ACTION_TIME.toInt(),false,1,1111,new int[]{0,0},new Vector2(0,2)));
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1); //player not changed
        assertEquals(controller.getLastSent().getTarget(),1);

        //1 successful execute action with worker 0, action 0, to an available pos
        //removed available = controller.getLastSent().getNextActionsData()[0].getAvailable_position().toArray(new Vector2[0]);
        available = controller.getLastSent().getV2Data();
        controller.onCommand(new Command(Command.CType.ACTION_TIME.toInt(),false,1,1111,new int[]{0,0},available[0]));
        assertEquals(controller.getConnected_players().get(1).getWorkers().get(0).getPosition().getX(),1);
        assertEquals(controller.getConnected_players().get(1).getWorkers().get(0).getPosition().getY(),0);
        //Sent a command with name and possible position of nextActions to 1
        assertEquals(controller.getLastSent().getTarget(),controller.getMatch().getCurrentPlayer().getId());

        //freed a position and Sent back correct available position
        available = controller.getLastSent().getV2Data();
        assertEquals(available[0].getX(),0);
        assertEquals(available[0].getY(),0);
        assertEquals(controller.getLastSent().getIntData()[1], 4);
        assertEquals(available[1].getX(),1);
        assertEquals(available[1].getY(),1);
        assertEquals(available[2].getX(),2);
        assertEquals(available[2].getY(),0);
        assertEquals(available[3].getX(),2);
        assertEquals(available[3].getY(),1);
        //the 4th is the other action possible, in this case no farther action possible
        assertEquals(available.length,4);

        //1 successful continue the execution of action with worker 0, action 0, available position
        controller.onCommand(new Command(Command.CType.ACTION_TIME.toInt(),false,1,1111,new int[]{0,0},available[0]));
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        //last action of 1 : end turn action
        assertEquals(controller.getLastSent().getType(), Command.CType.ACTION_TIME);
        assertEquals(controller.getLastSent().getTarget(),1);

        //last sent contains values for EndTurnAction
        assertEquals(controller.getLastSent().getStringData()[0],"End Turn");
        assertEquals(controller.getLastSent().getIntData()[0],0); //worker
        assertEquals(controller.getLastSent().getIntData()[1],1);//n. possible cell
        assertEquals(controller.getLastSent().getV2Data()[0].getX(),0);
        assertEquals(controller.getLastSent().getV2Data()[0].getY(),0);
        assertEquals(controller.getLastSent().getV2Data().length,1);

        //1 is forced to do an End Turn Action
        available = controller.getLastSent().getV2Data();
        controller.onCommand(new Command(Command.CType.ACTION_TIME.toInt(), false,1,1111,new int[]{0,0},available[0]));
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent a command to 2 to execute one of his possible action
        assertEquals(controller.getLastSent().getType(), Command.CType.ACTION_TIME);
        assertEquals(controller.getLastSent().getTarget(),2);

    }

    @Test
    void winCondition(){
        controller.onDisconnect(1);
        assertEquals(controller.getLastSent().getTarget(),0);
        assertEquals(controller.getLastSent().getType().toInt(),Command.CType.WINNER.toInt());
    }

}
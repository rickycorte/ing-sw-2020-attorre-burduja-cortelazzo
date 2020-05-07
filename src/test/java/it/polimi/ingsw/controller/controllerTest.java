package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.*;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.server.Network;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.naming.ldap.Control;

import java.util.Vector;
import java.util.logging.Filter;

import static org.junit.jupiter.api.Assertions.*;

public class controllerTest {

    Controller controller;

    private CommandWrapper serializer(CommandWrapper cmd){
        cmd.Serialize();
        return cmd;
    }

    @BeforeEach
    void setUp() {
        controller = new Controller(new Network());


        controller.onConnect(serializer(new CommandWrapper(CommandType.JOIN,new JoinCommand(CommandType.JOIN.toInt(),false,0,1111,"firstplayer",true))));
        controller.onConnect(serializer(new CommandWrapper(CommandType.JOIN,new JoinCommand(CommandType.JOIN.toInt(),false,1,1111,"secondplayer",true))));

    }

    @Test
    void connectPlayerTest(){
        Controller controllerT = new Controller(new Network());

        //join successful
        controllerT.onConnect(serializer(new CommandWrapper(CommandType.JOIN,new JoinCommand(CommandType.JOIN.toInt(),false,0,1111,"1",true))));
        assertEquals(controllerT.getConnected_players().get(0).getId(),0);

        //join with username already picked
        controllerT.onConnect(serializer(new CommandWrapper(CommandType.JOIN,new JoinCommand(CommandType.JOIN.toInt(),false,1,1111,"1",true))));
        assertEquals(controllerT.getConnected_players().size(),1);

        //join successful
        controllerT.onConnect(serializer(new CommandWrapper(CommandType.JOIN,new JoinCommand(CommandType.JOIN.toInt(),false,1,1111,"2",true ))));
        assertEquals(controllerT.getConnected_players().get(1).getId(),1);

        //join successful
        controllerT.onConnect(serializer(new CommandWrapper(CommandType.JOIN,new JoinCommand(CommandType.JOIN.toInt(),false,2,1111,"3",true))));
        assertEquals(controllerT.getConnected_players().get(2).getId(),2);

        //join a started game
        controllerT.onConnect(serializer(new CommandWrapper(CommandType.JOIN,new JoinCommand(CommandType.JOIN.toInt(),false,3,1111,"4",true))));
        assertEquals(controllerT.getConnected_players().size(),3);
    }

    @Test
    void startGameTest() {
        //start a new game with the connected players

        //non-host try to start
        controller.onCommand(serializer (new CommandWrapper(CommandType.START,new StartCommand(CommandType.START.toInt(),false,1,11111))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WAIT);

        //no command is Sent back
        assertNull(controller.getLastSent());

        //host try to start
        controller.onCommand(serializer (new CommandWrapper(CommandType.START,new StartCommand(CommandType.START.toInt(),false,0,11111))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_FILTER);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.FILTER_GODS);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);

        //send back a Filter God command
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.FILTER_GODS);

        //start command
        controller.onCommand(serializer (new CommandWrapper(CommandType.START,new StartCommand(CommandType.START.toInt(),false,0,11111))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_FILTER);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.FILTER_GODS);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);

        //expected FILTER_GODS command

        //command from not host player -> command discarded
        controller.onCommand(serializer (new CommandWrapper(CommandType.FILTER_GODS,new FilterGodCommand(CommandType.FILTER_GODS.toInt(),false,1,11111))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_FILTER);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.FILTER_GODS);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);


        //command not expected -> command discarded
        controller.onCommand(serializer (new CommandWrapper(CommandType.SELECT_FIRST_PLAYER,new FirstPlayerPickCommand(CommandType.SELECT_FIRST_PLAYER.toInt(),false,0,11111,1))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_FILTER);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.FILTER_GODS);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);

        //correct header, content not correct -> command failed
        controller.onCommand(serializer (new CommandWrapper(CommandType.FILTER_GODS,new FilterGodCommand(CommandType.FILTER_GODS.toInt(),false,0,11111,new int[]{0,1,2,3}))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_FILTER);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.FILTER_GODS);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);
    }


    @Test
    public void pickGod3PlayerTest(){
        //join a new player
        controller.onConnect(serializer(new CommandWrapper(CommandType.JOIN,new JoinCommand(CommandType.JOIN.toInt(),false,2,1111,"thirdplayer",true))));

        //Sent a request command for filter god
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.FILTER_GODS);
        assertEquals(serializer(controller.getLastSent()).getCommand(BaseCommand.class).getSender(),0);
        CommandWrapper Sent = serializer(controller.getLastSent());

        //Start game command , but with 3 player game start automatically
        controller.onCommand(serializer (new CommandWrapper(CommandType.START,new StartCommand(CommandType.START.toInt(),false,0,11111))));
        //command is discarded
        assertEquals(serializer(controller.getLastSent()).getCommand(BaseCommand.class).getSender(),0);
        assertEquals(Sent,serializer(controller.getLastSent()));


        //Filter god
        controller.onCommand(serializer (new CommandWrapper(CommandType.FILTER_GODS,new FilterGodCommand(CommandType.FILTER_GODS.toInt(),false,0,11111,new int[]{1,2,3}))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        //Sent a command for pick god to id:1
        FilterGodCommand filterGodCommand = serializer(controller.getLastSent()).getCommand(FilterGodCommand.class);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.PICK_GOD);
        assertEquals(filterGodCommand.getTarget(),1);
        assertEquals(filterGodCommand.getGodID().length,3); //last pack sendend contains available card id

        //id:1 Pick God n.3 command executed
        controller.onCommand(serializer (new CommandWrapper(CommandType.PICK_GOD,new PickGodCommand(CommandType.PICK_GOD.toInt(),false,1,1111, 3))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent command for pick god to id:2
        PickGodCommand pickGodCommand = serializer(controller.getLastSent()).getCommand(PickGodCommand.class);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.PICK_GOD);
        assertEquals(pickGodCommand.getTarget(),2);
        assertEquals(pickGodCommand.getGodID().length,2); //last pack sendend contains available card id

        //id2 try to Pick God n.3 command failed
        controller.onCommand(serializer (new CommandWrapper(CommandType.PICK_GOD,new PickGodCommand(CommandType.PICK_GOD.toInt(),false,2,1111, 3))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent command for pick god to id:2
        pickGodCommand = serializer(controller.getLastSent()).getCommand(PickGodCommand.class);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.PICK_GOD);
        assertEquals(pickGodCommand.getTarget(),2);
        assertEquals(pickGodCommand.getGodID().length,2); //last pack sendend contains available card id

        //id0 try to pick God n.2 command discarded
        controller.onCommand(serializer (new CommandWrapper(CommandType.PICK_GOD,new PickGodCommand(CommandType.PICK_GOD.toInt(),false,0,1111, 2))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent command for pick god to id:2
        pickGodCommand = serializer(controller.getLastSent()).getCommand(PickGodCommand.class);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.PICK_GOD);
        assertEquals(pickGodCommand.getTarget(),2);
        assertEquals(pickGodCommand.getGodID().length,2); //last pack sendend contains available card id

        //id2 Pick God n.2 (should start autopick for id0 Pick God n.1)
        controller.onCommand(serializer (new CommandWrapper(CommandType.PICK_GOD,new PickGodCommand(CommandType.PICK_GOD.toInt(),false,2,1111, 2))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.FIRST_PLAYER_PICK);
        assertEquals(controller.getMatch().getHost().getGod().getId(),1);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);
        //Sent command for Select First Player to host
        pickGodCommand = serializer(controller.getLastSent()).getCommand(PickGodCommand.class);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.SELECT_FIRST_PLAYER);
        assertEquals(pickGodCommand.getTarget(),0);

        //send first player selection by non host
        controller.onCommand(serializer (new CommandWrapper(CommandType.SELECT_FIRST_PLAYER,new FirstPlayerPickCommand(CommandType.SELECT_FIRST_PLAYER.toInt(),false,1,1111,1))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.FIRST_PLAYER_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.SELECT_FIRST_PLAYER);
        FirstPlayerPickCommand firstPlayerPickCommand = serializer(controller.getLastSent()).getCommand(FirstPlayerPickCommand.class);
        assertEquals(firstPlayerPickCommand.getTarget(),0);

        //send first player selection by host,wrong content
        controller.onCommand(serializer (new CommandWrapper(CommandType.SELECT_FIRST_PLAYER,new FirstPlayerPickCommand(CommandType.SELECT_FIRST_PLAYER.toInt(),false,0,1111,3))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.FIRST_PLAYER_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.SELECT_FIRST_PLAYER);
        firstPlayerPickCommand = serializer(controller.getLastSent()).getCommand(FirstPlayerPickCommand.class);
        assertEquals(firstPlayerPickCommand.getTarget(),0);

        //first player select
        controller.onCommand(serializer (new CommandWrapper(CommandType.SELECT_FIRST_PLAYER,new FirstPlayerPickCommand(CommandType.SELECT_FIRST_PLAYER.toInt(),false,0,1111,1))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        //Sent command for Worker place to first player
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.PLACE_WORKERS);
        WorkerPlaceCommand workerPlaceCommand = serializer(controller.getLastSent()).getCommand(WorkerPlaceCommand.class);
        assertEquals(workerPlaceCommand.getTarget(),1);
        assertEquals(workerPlaceCommand.getPositions().length,49);

    }

    @Test
    void pickGod2PlayerTest(){
        //start voluntary Game
        controller.onCommand(serializer (new CommandWrapper(CommandType.START,new StartCommand(CommandType.START.toInt(),false,0,11111))));

        //filter gods
        controller.onCommand(serializer (new CommandWrapper(CommandType.FILTER_GODS,new FilterGodCommand(CommandType.FILTER_GODS.toInt(),false,0,11111,new int[]{1,2}))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GOD_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        //Sent back a command with possible god ids
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.PICK_GOD);
        PickGodCommand pickGodCommand = serializer(controller.getLastSent()).getCommand(PickGodCommand.class);
        assertEquals(pickGodCommand.getTarget(),controller.getMatch().getCurrentPlayer().getId());

        //pick god and go to selection of first player
        controller.onCommand(serializer (new CommandWrapper(CommandType.PICK_GOD,new PickGodCommand(CommandType.PICK_GOD.toInt(),false,1,11111,2))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.FIRST_PLAYER_PICK);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);

        //0 select a wrong player

    }

    @Test
    void workerPlacementTest(){
        controller.onConnect(serializer(new CommandWrapper(CommandType.JOIN,new JoinCommand(CommandType.JOIN.toInt(),false,2,1111,"thirdplayer",true))));
        controller.onCommand(serializer (new CommandWrapper(CommandType.FILTER_GODS,new FilterGodCommand(CommandType.FILTER_GODS.toInt(),false,0,11111,new int[]{1,2,3}))));
        controller.onCommand(serializer (new CommandWrapper(CommandType.PICK_GOD,new PickGodCommand(CommandType.PICK_GOD.toInt(),false,1,1111, 3))));
        controller.onCommand(serializer (new CommandWrapper(CommandType.PICK_GOD,new PickGodCommand(CommandType.PICK_GOD.toInt(),false,2,1111, 2))));
        controller.onCommand(serializer (new CommandWrapper(CommandType.SELECT_FIRST_PLAYER,new FirstPlayerPickCommand(CommandType.SELECT_FIRST_PLAYER.toInt(),false,0,1111,1))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.PLACE_WORKERS);
        WorkerPlaceCommand workerPlaceCommand = serializer(controller.getLastSent()).getCommand(WorkerPlaceCommand.class);
        assertEquals(workerPlaceCommand.getTarget(),1);
        assertEquals(workerPlaceCommand.getPositions().length,49);
        //1 is first player selected by host(0), all position are available

        //place workers for player 1
        Vector2[] available = workerPlaceCommand.getPositions();
        controller.onCommand(serializer (new CommandWrapper(CommandType.PLACE_WORKERS,new WorkerPlaceCommand(CommandType.PLACE_WORKERS.toInt(),false,1,1111,new Vector2[]{available[0],available[48]}))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent a Place Workers command to 2, with 47 avilable position
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.PLACE_WORKERS);
        workerPlaceCommand = serializer(controller.getLastSent()).getCommand(WorkerPlaceCommand.class);
        assertEquals(workerPlaceCommand.getTarget(),2);
        assertEquals(workerPlaceCommand.getPositions().length,47);

        //2 try to place worker with wrong content
        available = workerPlaceCommand.getPositions();
        Vector2 wrongPos = new Vector2(0,0);
        controller.onCommand(serializer (new CommandWrapper(CommandType.PLACE_WORKERS,new WorkerPlaceCommand(CommandType.PLACE_WORKERS.toInt(),false,2,1111,new Vector2[]{available[1],wrongPos}))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent a Place Workers command to 1, with 47 available postion
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.PLACE_WORKERS);
        workerPlaceCommand = serializer(controller.getLastSent()).getCommand(WorkerPlaceCommand.class);
        assertEquals(workerPlaceCommand.getTarget(),2);
        assertEquals(workerPlaceCommand.getPositions().length,47);

        //2 try to place worker with no content
        Vector2 nullVector = null;
        controller.onCommand(serializer (new CommandWrapper(CommandType.PLACE_WORKERS,new WorkerPlaceCommand(CommandType.PLACE_WORKERS.toInt(),false,2,1111,nullVector))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent a Place Workers command to 1, with 47 available postion
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.PLACE_WORKERS);
        workerPlaceCommand = serializer(controller.getLastSent()).getCommand(WorkerPlaceCommand.class);
        assertEquals(workerPlaceCommand.getTarget(),2);
        assertEquals(workerPlaceCommand.getPositions().length,47);

        //2 try to place worker with not complete content
        available = workerPlaceCommand.getPositions();
        controller.onCommand(serializer (new CommandWrapper(CommandType.PLACE_WORKERS,new WorkerPlaceCommand(CommandType.PLACE_WORKERS.toInt(),false,2,1111,new Vector2[]{available[1]}))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent a Place Workers command to 1, with 47 available postion
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.PLACE_WORKERS);
        workerPlaceCommand = serializer(controller.getLastSent()).getCommand(WorkerPlaceCommand.class);
        assertEquals(workerPlaceCommand.getTarget(),2);
        assertEquals(workerPlaceCommand.getPositions().length,47);

        //2 place worker
        available = workerPlaceCommand.getPositions();
        controller.onCommand(serializer (new CommandWrapper(CommandType.PLACE_WORKERS,new WorkerPlaceCommand(CommandType.PLACE_WORKERS.toInt(),false,2,1111,new Vector2[]{available[1],available[2]}))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.WORKER_PLACE);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),0);
        //Sent place worker command to last player
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.PLACE_WORKERS);
        workerPlaceCommand = serializer(controller.getLastSent()).getCommand(WorkerPlaceCommand.class);
        assertEquals(workerPlaceCommand.getTarget(),0);
        assertEquals(workerPlaceCommand.getPositions().length,45);

        //0 place worker
        available = workerPlaceCommand.getPositions();
        controller.onCommand(serializer (new CommandWrapper(CommandType.PLACE_WORKERS,new WorkerPlaceCommand(CommandType.PLACE_WORKERS.toInt(),false,0,1111,new Vector2[]{available[3],available[4]}))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GAME);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        //ACTION is now starting
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.ACTION_TIME);
        ActionCommand actionCommand = serializer(controller.getLastSent()).getCommand(ActionCommand.class);
        assertEquals(actionCommand.getTarget(),1);
    }

    @Test
    void gameActionsTest(){
        controller.onConnect(serializer(new CommandWrapper(CommandType.JOIN,new JoinCommand(CommandType.JOIN.toInt(),false,2,1111,"thirdplayer",true))));
        controller.onCommand(serializer (new CommandWrapper(CommandType.FILTER_GODS,new FilterGodCommand(CommandType.FILTER_GODS.toInt(),false,0,11111,new int[]{1,2,3}))));
        controller.onCommand(serializer (new CommandWrapper(CommandType.PICK_GOD,new PickGodCommand(CommandType.PICK_GOD.toInt(),false,1,1111, 3))));
        controller.onCommand(serializer (new CommandWrapper(CommandType.PICK_GOD,new PickGodCommand(CommandType.PICK_GOD.toInt(),false,2,1111, 2))));
        controller.onCommand(serializer (new CommandWrapper(CommandType.SELECT_FIRST_PLAYER,new FirstPlayerPickCommand(CommandType.SELECT_FIRST_PLAYER.toInt(),false,0,1111,1))));
        WorkerPlaceCommand workerPlaceCommand = serializer(controller.getLastSent()).getCommand(WorkerPlaceCommand.class);
        Vector2[] available = workerPlaceCommand.getPositions();
        controller.onCommand(serializer (new CommandWrapper(CommandType.PLACE_WORKERS,new WorkerPlaceCommand(CommandType.PLACE_WORKERS.toInt(),false,1,1111,new Vector2[]{available[0],available[48]}))));
        workerPlaceCommand = serializer(controller.getLastSent()).getCommand(WorkerPlaceCommand.class);
        available = workerPlaceCommand.getPositions();
        controller.onCommand(serializer (new CommandWrapper(CommandType.PLACE_WORKERS,new WorkerPlaceCommand(CommandType.PLACE_WORKERS.toInt(),false,2,1111,new Vector2[]{available[0],available[1]}))));
        workerPlaceCommand = serializer(controller.getLastSent()).getCommand(WorkerPlaceCommand.class);
        available = workerPlaceCommand.getPositions();
        controller.onCommand(serializer (new CommandWrapper(CommandType.PLACE_WORKERS,new WorkerPlaceCommand(CommandType.PLACE_WORKERS.toInt(),false,0,1111,new Vector2[]{available[0],available[1]}))));
        assertEquals(controller.getMatch().getCurrentState(), Game.GameState.GAME);
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        assertEquals(serializer(controller.getLastSent()).getType(),CommandType.ACTION_TIME);
        ActionCommand actionCommand = serializer(controller.getLastSent()).getCommand(ActionCommand.class);
        assertEquals(actionCommand.getTarget(),1);

        //correct values in command are sent
        assertEquals(actionCommand.getActionName()[0],controller.getMatch().getNextActions(controller.getConnected_players().get(1)).get(0).getActionName());
        assertEquals(actionCommand.getIdWorkerNMove()[0],controller.getMatch().getNextActions(controller.getConnected_players().get(1)).get(0).getWorker());
        assertEquals(actionCommand.getIdWorkerNMove()[1],controller.getMatch().getNextActions(controller.getConnected_players().get(1)).get(0).getAvailable_position().size());
        assertEquals(actionCommand.getAvaialablePos()[0].getX(),controller.getMatch().getNextActions(controller.getConnected_players().get(1)).get(0).getAvailable_position().get(0).getX());
        assertEquals(actionCommand.getAvaialablePos()[0].getY(),controller.getMatch().getNextActions(controller.getConnected_players().get(1)).get(0).getAvailable_position().get(0).getY());
        assertEquals(actionCommand.getAvaialablePos()[1],controller.getMatch().getNextActions(controller.getConnected_players().get(1)).get(0).getAvailable_position().get(1));

        CommandWrapper exCmdSent = serializer(controller.getLastSent());
        //Action Time , 1 is first player

        //MAP with idPlayerWorker
        // | 1- | 2- | 2- | 0- | 0- | -- | -- |
        // | -- | -- | -- | -- | -- | -- | -- |
        // | -- | -- | -- | -- | -- | -- | -- |
        // | -- | -- | -- | -- | -- | -- | -- |
        // | -- | -- | -- | -- | -- | -- | -- |
        // | -- | -- | -- | -- | -- | -- | -- |
        // | -- | -- | -- | -- | -- | -- | 1- |

        //2 try to execute an action but it's not his turn
        controller.onCommand(serializer (new CommandWrapper(CommandType.ACTION_TIME,new ActionCommand(CommandType.ACTION_TIME.toInt(),false,2,1111,new int[]{0,0},new Vector2(0,2)))));
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1); //player not changed
        actionCommand = serializer(controller.getLastSent()).getCommand(ActionCommand.class);
        assertEquals(actionCommand.getTarget(),1);
        assertEquals(serializer(controller.getLastSent()),exCmdSent);

        //1 try to execute an action to a occupied cell
        controller.onCommand(serializer (new CommandWrapper(CommandType.ACTION_TIME,new ActionCommand(CommandType.ACTION_TIME.toInt(),false,1,1111,new int[]{0,0},new Vector2(0,2)))));
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1); //player not changed
        actionCommand = serializer(controller.getLastSent()).getCommand(ActionCommand.class);
        assertEquals(actionCommand.getTarget(),1);

        //1 successful execute action with worker 0, action 0, to an available pos
        //removed available = serializer(controller.getLastSent()).getNextActionsData()[0].getAvailable_position().toArray(new Vector2[0]);
        available = actionCommand.getAvaialablePos();
        controller.onCommand(serializer (new CommandWrapper(CommandType.ACTION_TIME,new ActionCommand(CommandType.ACTION_TIME.toInt(),false,1,1111,new int[]{0,0},available[0]))));
        assertEquals(controller.getConnected_players().get(1).getWorkers().get(0).getPosition().getX(),1);
        assertEquals(controller.getConnected_players().get(1).getWorkers().get(0).getPosition().getY(),0);
        //Sent a command with name and possible position of nextActions to 1
        actionCommand = serializer(controller.getLastSent()).getCommand(ActionCommand.class);
        assertEquals(actionCommand.getTarget(),controller.getMatch().getCurrentPlayer().getId());

        //freed a position and Sent back correct available position
        available = actionCommand.getAvaialablePos();
        assertEquals(available[0].getX(),0);
        assertEquals(available[0].getY(),0);
        assertEquals(actionCommand.getIdWorkerNMove()[1], 4);
        assertEquals(available[1].getX(),1);
        assertEquals(available[1].getY(),1);
        assertEquals(available[2].getX(),2);
        assertEquals(available[2].getY(),0);
        assertEquals(available[3].getX(),2);
        assertEquals(available[3].getY(),1);
        //the 4th is the other action possible, in this case no farther action possible
        assertEquals(available.length,4);

        //1 successful continue the execution of action with worker 0, action 0, available position
        controller.onCommand(serializer (new CommandWrapper(CommandType.ACTION_TIME,new ActionCommand(CommandType.ACTION_TIME.toInt(),false,1,1111,new int[]{0,0},available[0]))));
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),1);
        //last action of 1 : end turn action
        assertEquals(serializer(controller.getLastSent()).getType(), CommandType.ACTION_TIME);
        actionCommand = serializer(controller.getLastSent()).getCommand(ActionCommand.class);
        assertEquals(actionCommand.getTarget(),1);

        //last sent contains values for EndTurnAction
        assertEquals(actionCommand.getActionName()[0],"End Turn");
        assertEquals(actionCommand.getIdWorkerNMove()[0],0); //worker
        assertEquals(actionCommand.getIdWorkerNMove()[1],1);//n. possible cell
        assertEquals(actionCommand.getAvaialablePos()[0].getX(),0);
        assertEquals(actionCommand.getAvaialablePos()[0].getY(),0);
        assertEquals(actionCommand.getAvaialablePos().length,1);

        //1 is forced to do an End Turn Action
        available = actionCommand.getAvaialablePos();
        controller.onCommand(serializer (new CommandWrapper(CommandType.ACTION_TIME,new ActionCommand(CommandType.ACTION_TIME.toInt(), false,1,1111,new int[]{0,0},available[0]))));
        assertEquals(controller.getMatch().getCurrentPlayer().getId(),2);
        //Sent a command to 2 to execute one of his possible action
        assertEquals(serializer(controller.getLastSent()).getType(), CommandType.ACTION_TIME);
        actionCommand = serializer(controller.getLastSent()).getCommand(ActionCommand.class);
        assertEquals(actionCommand.getTarget(),2);

    }

    @Test
    void winCondition(){
        controller.onDisconnect(1);
        EndGameCommand endGameCommand = serializer(controller.getLastSent()).getCommand(EndGameCommand.class);
        assertEquals(endGameCommand.getTarget(),0);
        assertEquals(endGameCommand.getType(),CommandType.END_GAME.toInt());
        assertTrue(endGameCommand.isWinner);
    }

}
package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MoveActionTest {


    Map m;
    Player p1, p2;
    Worker w1p1, w1p2, w2p2, w3p2;
    MoveAction moveAct;
    GameConstraints gc;


    /**
     * short version to build level times in a map
     */
    void buildPos(Map m, int x, int y, int level)
    {
        Vector2 t = new Vector2(x,y);
        if(m.isInsideMap(t))
        {
            for(int i = 0; i < level; i++)
            {
                m.build(t);
            }
        }
    }


        /*
        Map scheme used in tests (covers lots of possible setups)
        0--,1,2,3 -> build level
        k -> p1 workers
        t -> p2 workers
        @ -> dome

        xy  00 01 02 03 04 05 06
        00 |--|@4|-1|t-|-1|--|--|
        01 |@4|@4|@-|k-|-1|t1|--|
        02 |--|--|--|-2|t1|--|--|
        03 |--|--|-1|-3|--|--|--|
        04 |--|--|--|@4|--|--|--|
        05 |--|--|--|--|--|--|--|
        06 |--|--|--|--|--|--|--|

     */


    @BeforeEach
    void init()
    {
        moveAct = new MoveAction();
        gc = new GameConstraints();

        // create players
        p1 = new Player(1, "Kazuma");
        p2 = new Player(2, "Megumin");

        //create workers
        w1p1 = new Worker(p1);
        p1.addWorker(w1p1);

        w1p2 = new Worker(p2);
        w2p2 = new Worker(p2);
        w3p2 = new Worker(p2);
        p2.addWorker(w1p2);
        p2.addWorker(w2p2);
        p2.addWorker(w3p2);

        //create map
        m = new Map();
        buildPos(m, 0,2,1);
        buildPos(m,0,4,1);
        buildPos(m,1,4,1);
        buildPos(m,1,5,1);
        buildPos(m,2,3,2);
        buildPos(m,2,4,1);
        buildPos(m,3,2,1);
        buildPos(m,3,3,3);
        buildPos(m,4,3,4);
        buildPos(m,0,1,4);
        buildPos(m,1,1,4);
        buildPos(m,1,0,4);
        m.buildDome(new Vector2(1,2));

        //place workers
        w1p1.setPosition(new Vector2(1,3));
        w1p2.setPosition(new Vector2(0,3));
        w2p2.setPosition(new Vector2(2,4));
        w3p2.setPosition(new Vector2(1,5));

        m.setWorkers(p1);
        m.setWorkers(p2);
    }


    @Test
    void shouldReturn8CellsNoConstraints(){
        // testing the basic scenario
        Player p1 = new Player(1,"uno");
        Worker w = new Worker(p1);
        Vector2 pos = new Vector2(3,3);  // tested with multiple positions
        Vector2 pos2 = new Vector2(4,4);
        w.setPosition(pos);
        Map m = new Map();
        MoveAction ma = new MoveAction();
        GameConstraints gc = new GameConstraints();

            ArrayList<Vector2> cells = ma.possibleCells(w,m,gc);
            assertEquals(8, cells.size());
            assertTrue(cells.contains(pos2));

    }

    @Test
    void shouldReturn7CellsCloseWorkersNoConstrains(){
        // testing, no costraints and 2 workers are close
        Player player1 = new Player(1,"uno");
        Player player2 = new Player(2,"due");

        Worker w1 = new Worker(player1);
        Vector2 p1 = new Vector2(3,3);
        w1.setPosition(p1);
        player1.addWorker(w1);

        assertEquals(p1, w1.getPosition());

        Worker w2 = new Worker(player2);
        Vector2 p2 = new Vector2(2,2);
        w2.setPosition(p2);
        player2.addWorker(w2);

        Map m = new Map();
        m.setWorkers(player1);
        m.setWorkers(player2);

        assertEquals(1, player1.getWorkers().size());

        GameConstraints gc = new GameConstraints();

        MoveAction ma = new MoveAction();


            ArrayList<Vector2>  cells = ma.possibleCells(w1,m,gc);
            assertEquals(7, cells.size());

    }
    @Test
    void shouldReturn8CellsCloseWorkersCanSwap(){
        // testing swap constraint , two workers are close
        Player player1 = new Player(1,"uno");
        Player player2 = new Player(2,"due");

        Worker w1 = new Worker(player1);
        Vector2 p1 = new Vector2(3,3);
        w1.setPosition(p1);
        player1.addWorker(w1);

        assertEquals(p1, w1.getPosition());

        Worker w2 = new Worker(player2);
        Vector2 p2 = new Vector2(2,2);
        w2.setPosition(p2);
        player2.addWorker(w2);

        Map m = new Map();
        m.setWorkers(player1);
        m.setWorkers(player2);

        assertEquals(1, player1.getWorkers().size());

        GameConstraints gc = new GameConstraints();
        gc.add(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT);

        MoveAction ma = new MoveAction();


            ArrayList<Vector2>  cells = ma.possibleCells(w1,m,gc);
            assertEquals(8, cells.size());

    }
    @Test
    void shouldReturn8CellsCloseWorkersCanPush(){
        // testing push constraint, two workers are close
        Player player1 = new Player(1,"uno");
        Player player2 = new Player(2,"due");

        Worker w1 = new Worker(player1);
        Vector2 p1 = new Vector2(3,3);
        w1.setPosition(p1);
        player1.addWorker(w1);

        assertEquals(p1, w1.getPosition());

        Worker w2 = new Worker(player2);
        Vector2 p2 = new Vector2(2,3);
        w2.setPosition(p2);
        player2.addWorker(w2);

        Map m = new Map();
        m.setWorkers(player1);
        m.setWorkers(player2);

        assertEquals(1, player1.getWorkers().size());

        GameConstraints gc = new GameConstraints();
        gc.add(GameConstraints.Constraint.CAN_PUSH_CONSTRAINT);

        MoveAction ma = new MoveAction();


            ArrayList<Vector2>  cells = ma.possibleCells(w1,m,gc);
            assertEquals(8, cells.size());

    }

    @Test
    void simpleMoveTest(){
        Map m = new Map();
        Player player1 = new Player(1,"uno");
        player1.setGod(new Card(3,"Athena", null));
        Worker w1 = new Worker(player1);
        w1.setPosition(new Vector2(3,3));
        player1.addWorker(w1);
        m.setWorkers(player1);

        assertEquals(1, m.getWorkers().size());     // there should be one worker on the map

        Vector2 p2 = new Vector2(2,3);                 // where i would like to move
        GameConstraints gc = new GameConstraints();

        BehaviourNode bn = BehaviourNode.makeRootNode(new MoveAction());
        try {

            int outcome = (bn.getAction()).run(w1, p2, m, gc);
            assertEquals(0, outcome);
            assertEquals(p2, w1.getPosition());
            assertTrue(m.isCellEmpty(new Vector2(3,3)));
        }catch (NotAllowedMoveException e){}

    }
    @Test
    void pushSwapTest(){
        Map m = new Map();
        Player player1 = new Player(1,"uno");
        Player player2 = new Player(2,"due");
        player1.setGod(new Card(3,"Athena", null));
        player2.setGod(new Card(8,"Pan", null));
        Worker w1 = new Worker(player1);
        Worker w2 = new Worker(player2);
        Vector2 pos1 = new Vector2(3,3);
        w1.setPosition(pos1);
        Vector2 pos2 = new Vector2(2,3);
        w2.setPosition(pos2);
        player1.addWorker(w1);
        player2.addWorker(w2);
        GameConstraints gc = new GameConstraints();
        m.setWorkers(player2);
        m.setWorkers(player1);
        int outcome;
        BehaviourNode bn = BehaviourNode.makeRootNode(new MoveAction());
        gc.add(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT);
        try{
            assertNotEquals(pos2, w1.getPosition());
            outcome = bn.getAction().run(w1,pos2,m,gc);
            assertEquals(0,outcome);
            assertEquals(pos2, w1.getPosition());
            assertEquals(pos1, w2.getPosition());
        }catch (NotAllowedMoveException e){
            System.out.println("this is not a valid move");
        }
        gc.remove(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT);
        gc.add(GameConstraints.Constraint.CAN_PUSH_CONSTRAINT);
        w1.setPosition(pos1);
        w2.setPosition(pos2);
        Vector2 push_pos = new Vector2(1,3); // if execution is ok, w2 should be pushed there
        try{
            assertNotEquals(pos2, w1.getPosition());
            assertEquals(pos2, w2.getPosition());
            outcome = bn.getAction().run(w1,pos2,m,gc);
            assertEquals(0,outcome);
            assertEquals(pos2, w1.getPosition());
            assertEquals(push_pos, w2.getPosition());
        }catch (NotAllowedMoveException e){
            System.out.println("this is not a valid move");
        }
    }

    @Test
    void blockMoveUpTest(){
        Map m = new Map();
        Player player1 = new Player(1,"uno");
        player1.setGod(new Card(3,"Athena", null));
        Worker w1 = new Worker(player1);
        Vector2 pos1 = new Vector2(3,3);
        w1.setPosition(pos1);
        Vector2 pos2 = new Vector2(2,3);
        player1.addWorker(w1);
        GameConstraints gc = new GameConstraints();
        m.setWorkers(player1);
        int outcome;
        BehaviourNode bn = BehaviourNode.makeRootNode(new MoveAction());
        try{
            m.build(pos2);
            outcome = bn.getAction().run(w1,pos2,m,gc);
            assertEquals(0, outcome);
            assertEquals(pos2, w1.getPosition());
            w1.setPosition(pos1);
            gc.add(GameConstraints.Constraint.BLOCK_MOVE_UP);
            outcome = bn.getAction().run(w1,pos2,m,gc);

        }catch (NotAllowedMoveException e){
            System.out.println("move is not allowed");
        }

    }

    @Test
    void shouldBlockMoveBackInMoveAgain()
    {
        Map m = new Map();
        Player p1 = new Player(1,"uno");
        Worker w1 = new Worker(p1);
        w1.setPosition(new Vector2(3,3));
        w1.setLastLocation(new Vector2(2,2));
        p1.addWorker(w1);
        m.setWorkers(p1);

        Action mvb = new MoveAgainAction();

        assertThrows(NotAllowedMoveException.class, () -> { mvb.run(w1, new Vector2(2,2), m, new GameConstraints()); } );
    }

    @Test
    void shouldReturnCorrectAllowedCellsNoConstraints()
    {
        ArrayList<Vector2> cells;
        //refer to map (top of file) to check correct positions manually
        cells = moveAct.possibleCells(w1p1, m, null);
        assertEquals(4, cells.size());
        assertTrue(cells.contains(new Vector2(0,2)));
        assertTrue(cells.contains(new Vector2(0,4)));
        assertTrue(cells.contains(new Vector2(1,4)));
        assertTrue(cells.contains(new Vector2(2,2)));

        //use w1p2 to check out of map
        cells = moveAct.possibleCells(w1p2, m, null);
        assertEquals(3, cells.size());
        assertTrue(cells.contains(new Vector2(0,2)));
        assertTrue(cells.contains(new Vector2(0,4)));
        assertTrue(cells.contains(new Vector2(1,4)));

        // move p1 to 3,3
        w1p1.setPosition(new Vector2(3,3));
        cells = moveAct.possibleCells(w1p1, m, null);
        assertEquals(6, cells.size());
        assertTrue(cells.contains(new Vector2(2,2)));
        assertTrue(cells.contains(new Vector2(2,3)));
        assertTrue(cells.contains(new Vector2(3,2)));
        assertTrue(cells.contains(new Vector2(4,2)));
        assertTrue(cells.contains(new Vector2(3,4)));
        assertTrue(cells.contains(new Vector2(4,4)));
    }

    @Test
    void shouldNotMoveWithNoConstraints()
    {
        assertThrows(NotAllowedMoveException.class,
                ()-> { moveAct.run(w1p1, new Vector2(1,2),m, gc);},
                "Can't move into a dome");
        assertEquals(new Vector2(1,3), w1p1.getPosition());
        assertNull(w1p1.getLastLocation());

        assertThrows(NotAllowedMoveException.class,
                ()-> { moveAct.run(w1p1, new Vector2(2,3),m, gc);},
                "Can't move to a place with height diff > 1");
        assertEquals(new Vector2(1,3), w1p1.getPosition());
        assertNull(w1p1.getLastLocation());

        assertThrows(NotAllowedMoveException.class,
                ()-> { moveAct.run(w1p1, new Vector2(0,3),m, gc);},
                "Can't move into a used place");
        assertEquals(new Vector2(1,3), w1p1.getPosition());
        assertNull(w1p1.getLastLocation());

        assertThrows(NotAllowedMoveException.class,
                ()-> { moveAct.run(w1p1, new Vector2(3,2),m, gc);},
                "Can't move to a far cell");
        assertEquals(new Vector2(1,3), w1p1.getPosition());
        assertNull(w1p1.getLastLocation());

        assertThrows(NotAllowedMoveException.class,
                ()-> { moveAct.run(w1p1, new Vector2(8,8),m, gc);},
                "Can't move to invalid cell");
        assertEquals(new Vector2(1,3), w1p1.getPosition());
        assertNull(w1p1.getLastLocation());

    }

    @Test
    void shouldMoveAndWinNoConstraint()
    {
        try
        {
            assertEquals(0, moveAct.run(w1p1, new Vector2(2,2),m, gc)); // level 0 to 0 diagonal
            assertEquals(0, moveAct.run(w1p1, new Vector2(3,2),m, gc)); // level 0 to 1 vertical
            assertEquals(new Vector2(2,2), w1p1.getLastLocation()); // check last location is updated
            assertEquals(0, moveAct.run(w1p1, new Vector2(2,3),m, gc)); // level 2 to 3 diagonal
            assertTrue(moveAct.run(w1p1, new Vector2(3,3) ,m, gc) > 0, "Should win by moving to level 3"); // level 2 to 3 move vertical (+ win)
            assertEquals(0, moveAct.run(w1p1, new Vector2(3,4) ,m, gc)); // jump down
        }
        catch (NotAllowedMoveException e)
        {
            fail("No exception should be thrown with valid moves");
        }
    }

    @Test
    void shouldSetAndResetBlockMoveIfLockSetIsEnabled()
    {
        moveAct = new MoveAction(GameConstraints.Constraint.SET_BLOCK_MOVE_UP);

        try{
            // hor move no set
            moveAct.run(w1p1, new Vector2(2,2),m, gc);
            assertFalse(gc.check(GameConstraints.Constraint.BLOCK_MOVE_UP));

            //mov up lock
            moveAct.run(w1p1, new Vector2(3,2),m, gc);
            assertTrue(gc.check(GameConstraints.Constraint.BLOCK_MOVE_UP));

            //reset on move not up
            moveAct.run(w1p1, new Vector2(4,2),m, gc);
            assertFalse(gc.check(GameConstraints.Constraint.BLOCK_MOVE_UP));

        }catch (NotAllowedMoveException e)
        {
            fail("No exception should be thrown with valid moves");
        }
    }

    @Test
    void shouldLoseIfNoMoveIsAllowed()
    {
        //move lock and no other options
        buildPos(m,2,2,1);
        gc.add(GameConstraints.Constraint.BLOCK_MOVE_UP);
        assertEquals(0, moveAct.possibleCells(w1p1, m, gc).size());
        try
        {
            assertTrue(moveAct.run(w1p1, new Vector2(2, 2), m, gc) < 0);
        } catch (NotAllowedMoveException e) { fail("The move should lead to lose condition and do nothing"); }
        assertEquals(new Vector2(1,3), w1p1.getPosition());

        // check with towers at edge
        w1p1.setPosition(new Vector2(0,0));
        try
        {
        assertTrue(moveAct.run(w1p1, new Vector2(1, 1), m, gc) < 0);
        } catch (NotAllowedMoveException e) { fail("The move should lead to lose condition and do nothing"); }
        assertEquals(new Vector2(0,0), w1p1.getPosition());


    }

    @Test
    void shouldNotPushOutsideMap()
    {
        moveAct = new MoveAction(GameConstraints.Constraint.CAN_PUSH_CONSTRAINT);

        assertFalse(moveAct.possibleCells(w1p1, m, null).contains(new Vector2(0,3))); // check don't contain wrong push pos

        assertThrows(NotAllowedMoveException.class, ()-> { moveAct.run(w1p1, new Vector2(0,3),m,gc);});

    }

    @Test
    void shouldNotPushWithUsedCell()
    {
        moveAct = new MoveAction(GameConstraints.Constraint.CAN_PUSH_CONSTRAINT);
        w1p1.setPosition(new Vector2(0,6));
        assertFalse(moveAct.possibleCells(w1p1, m, null).contains(new Vector2(1,5)));

        assertThrows(NotAllowedMoveException.class, ()-> { moveAct.run(w1p1, new Vector2(1,5),m, gc); });
    }

    @Test
    void shouldNotPushUnreachablePos()
    {
        moveAct = new MoveAction(GameConstraints.Constraint.CAN_PUSH_CONSTRAINT);
        m.build(new Vector2(2,4));
        assertFalse(moveAct.possibleCells(w1p1, m, null).contains(new Vector2(2,4)));
        assertThrows(NotAllowedMoveException.class, ()-> { moveAct.run(w1p1, new Vector2(2,4),m, gc); });
    }

    @Test
    void shouldNotPushMyOtherWorker()
    {
        moveAct = new MoveAction(GameConstraints.Constraint.CAN_PUSH_CONSTRAINT);
        assertFalse(moveAct.possibleCells(w2p2, m, null).contains(new Vector2(1,5)));
        assertThrows(NotAllowedMoveException.class, ()-> { moveAct.run(w2p2, new Vector2(1,5),m, gc); });
    }

    @Test
    void shouldNotSwapMyOtherWorker()
    {
        moveAct = new MoveAction(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT);
        assertFalse(moveAct.possibleCells(w2p2, m, null).contains(new Vector2(1,5)));
        assertThrows(NotAllowedMoveException.class, ()-> { moveAct.run(w2p2, new Vector2(1,5),m, gc); });
    }

    @Test
    void shouldNotSwapUnreachableWorker()
    {
        moveAct = new MoveAction(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT);
        m.build(new Vector2(2,4));
        assertFalse(moveAct.possibleCells(w1p1, m, null).contains(new Vector2(2,4)));
        assertThrows(NotAllowedMoveException.class, ()-> { moveAct.run(w1p1, new Vector2(2,4),m, gc); });
    }

    @Test
    void shouldWinByFall()
    {
        moveAct = new MoveAction(GameConstraints.Constraint.WIN_BY_GOING_DOWN);
        try{

            // jump from 2 to 0
            w1p1.setPosition(new Vector2(2,3));
            assertTrue(moveAct.run(w1p1, new Vector2(2,2),m, gc) > 0);

            // jump from 3 to 1
            w1p1.setPosition(new Vector2(3,3));
            assertTrue(moveAct.run(w1p1, new Vector2(3,2),m, gc) > 0);

            // jump from 3 to 0
            w1p1.setPosition(new Vector2(3,3));
            assertTrue(moveAct.run(w1p1, new Vector2(3,4),m, gc) > 0);

        }catch (NotAllowedMoveException e)
        {
            fail("No exception should be thrown with valid moves");
        }
    }

    @Test
    void shouldNotWinByFall()
    {
        moveAct = new MoveAction(GameConstraints.Constraint.WIN_BY_GOING_DOWN);

        //check height diff == 1 does nothing
        w1p1.setPosition(new Vector2(0,4));
        try
        {
            assertEquals(0,moveAct.run(w1p1, new Vector2(0,5), m, gc)); // 1 to 0

            w1p1.setPosition(new Vector2(2,3));
            assertEquals(0,moveAct.run(w1p1, new Vector2(3,2), m, gc)); // 2 to 1

            w1p1.setPosition(new Vector2(3,3));
            assertEquals(0,moveAct.run(w1p1, new Vector2(2,3), m, gc)); // 3 to 2
        }
        catch (NotAllowedMoveException e)
        {
            fail("No exception should be thrown with valid moves");
        }
    }
}

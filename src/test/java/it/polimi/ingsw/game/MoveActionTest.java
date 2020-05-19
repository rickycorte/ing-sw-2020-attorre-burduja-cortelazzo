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

        xy  00 01 02 03 04
        00 |--|@4|-1|t-|-1|
        01 |@4|@4|@-|k-|-1|
        02 |--|--|--|-2|t1|
        03 |--|--|-1|-3|--|
        04 |t-|--|--|@4|--|

        w1p1 == k (1,3)
        w1p2 == (0,3)
        w2p2 == (2,4)
        w3p2 == (0,4)

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
        w3p2.setPosition(new Vector2(4,0));

        m.setWorkers(p1);
        m.setWorkers(p2);
    }


    @Test
    void shouldPushOtherWorker()
    {
        moveAct = new MoveAction(GameConstraints.Constraint.CAN_PUSH_CONSTRAINT);
        try
        {
            assertEquals(0, moveAct.run(w1p2, new Vector2(1,3),m, gc));
            assertFalse(m.isCellEmpty(new Vector2(1,3)));
            assertEquals(new Vector2(1,3), w1p2.getPosition());
            assertEquals(new Vector2(2, 3), w1p1.getPosition());
        }
        catch (NotAllowedMoveException e)
        {
            fail("No exception should be thrown with valid moves");
        }
    }

    @Test
    void shouldSwapWithOtherWorker()
    {
        moveAct = new MoveAction(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT);
        try
        {
            assertEquals(0, moveAct.run(w1p1, new Vector2(2,4),m, gc));
            assertEquals(new Vector2(2,4), w1p1.getPosition());
            assertEquals(new Vector2(1, 3), w2p2.getPosition());
        }
        catch (NotAllowedMoveException e)
        {
            fail("No exception should be thrown with valid moves");
        }
    }

    @Test
    void shouldBlockMoveUpTest(){
        gc.add(GameConstraints.Constraint.BLOCK_MOVE_UP);

        assertThrows(NotAllowedMoveException.class, ()-> {  moveAct.run(w1p1, new Vector2(2,3), m, gc); });
    }

    @Test
    void shouldReturnSameLevelCellsIfBlockMoveUp()
    {
        gc.add(GameConstraints.Constraint.BLOCK_MOVE_UP);

        var cells = moveAct.possibleCells(w1p1,m, gc);
        assertEquals(1, cells.size());
        assertTrue(cells.contains(new Vector2(2,2)));
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
    void shouldReturnAllowedCellsNoConstraints()
    {
        ArrayList<Vector2> cells;
        //refer to map (top of file) to check correct positions manually
        cells = moveAct.possibleCells(w1p1, m, null);
        assertEquals(4, cells.size());
        assertTrue(cells.contains(new Vector2(0,2)));
        assertTrue(cells.contains(new Vector2(0,4)));
        assertTrue(cells.contains(new Vector2(1,4)));
        assertTrue(cells.contains(new Vector2(2,2)));

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
    void shouldReturnAllowedCellsNearBorderNoConstaints()
    {
        //use w1p2 to check out of map
        var cells = moveAct.possibleCells(w1p2, m, null);
        assertEquals(3, cells.size());
        assertTrue(cells.contains(new Vector2(0,2)));
        assertTrue(cells.contains(new Vector2(0,4)));
        assertTrue(cells.contains(new Vector2(1,4)));
    }

    @Test
    void shouldMoveSameLevelNoConstraints()
    {
        try
        {
            // level 0
            assertEquals(0, moveAct.run(w1p1, new Vector2(2,2),m, gc)); // level 0 to 0 diagonal
            assertTrue(m.isCellEmpty(new Vector2(1,3)));
            assertFalse(m.isCellEmpty(new Vector2(2,2)));
            assertEquals(new Vector2(1,3), w1p1.getLastLocation());
            assertEquals(new Vector2(2,2), w1p1.getPosition());

            // level 1
            assertEquals(0, moveAct.run(w1p2, new Vector2(0,4),m, gc));
            assertTrue(m.isCellEmpty(new Vector2(0,3)));
            assertFalse(m.isCellEmpty(new Vector2(0,4)));
            assertEquals(new Vector2(0,3), w1p2.getLastLocation());
            assertEquals(new Vector2(0,4), w1p2.getPosition());
        }
        catch (NotAllowedMoveException e)
        {
            fail("No exception should be thrown with valid moves");
        }
    }

    @Test
    void shouldMoveDownNoConstraints()
    {
        try
        {
            w1p1.setPosition(new Vector2(3,3)); // jump from 3

            assertEquals(0, moveAct.run(w1p1, new Vector2(3,4),m, gc));
            assertTrue(m.isCellEmpty(new Vector2(3,3)));
            assertFalse(m.isCellEmpty(new Vector2(3,4)));
            assertEquals(new Vector2(3,4), w1p1.getPosition());
        }
        catch (NotAllowedMoveException e)
        {
            fail("No exception should be thrown with valid moves");
        }
    }

    @Test
    void shouldMoveUpNoConstraints()
    {
        try
        {
            assertEquals(0, moveAct.run(w1p1, new Vector2(1,4),m, gc));
            assertTrue(m.isCellEmpty(new Vector2(1,3)));
            assertFalse(m.isCellEmpty(new Vector2(1,4)));
            assertEquals(new Vector2(1,4), w1p1.getPosition());
        }
        catch (NotAllowedMoveException e)
        {
            fail("No exception should be thrown with valid moves");
        }
    }

    @Test
    void shouldNotMoveToADome()
    {
        assertThrows(NotAllowedMoveException.class,
                ()-> { moveAct.run(w1p1, new Vector2(1,2),m, gc);});
        assertEquals(new Vector2(1,3), w1p1.getPosition());
        assertNull(w1p1.getLastLocation());
    }

    @Test
    void shouldNoMoveToTooHighCell()
    {
        // high cells have height difference of 2+
        assertThrows(NotAllowedMoveException.class,
                ()-> { moveAct.run(w1p1, new Vector2(2,3),m, gc);});
        assertEquals(new Vector2(1,3), w1p1.getPosition());
        assertNull(w1p1.getLastLocation());
    }

    @Test
    void shouldNotMoveToUsedCell()
    {
        assertThrows(NotAllowedMoveException.class,
                ()-> { moveAct.run(w1p1, new Vector2(0,3),m, gc);});
        assertEquals(new Vector2(1,3), w1p1.getPosition());
        assertNull(w1p1.getLastLocation());
    }

    @Test
    void shouldNotMoveToFarCell()
    {
        //far cells have distance of 2+
        assertThrows(NotAllowedMoveException.class,
                ()-> { moveAct.run(w1p1, new Vector2(3,2),m, gc);});
        assertEquals(new Vector2(1,3), w1p1.getPosition());
        assertNull(w1p1.getLastLocation());
    }

    @Test
    void shouldNotMoveToInvalidCell()
    {
        assertThrows(NotAllowedMoveException.class,
                ()-> { moveAct.run(w1p1, new Vector2(8,8),m, gc);});
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
    }

    @Test
    void shouldLoseIfNoMoveIsAllowedOnBorder()
    {
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
        w1p2.setPosition(new Vector2(4,3));
        w2p2.setPosition(new Vector2(4,2));
        assertFalse(moveAct.possibleCells(w1p2, m, null).contains(new Vector2(4,2)));

        assertThrows(NotAllowedMoveException.class, ()-> { moveAct.run(w1p2, new Vector2(4,2),m, gc); });
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
        w3p2.setPosition(new Vector2(1,4));
        assertFalse(moveAct.possibleCells(w3p2, m, null).contains(new Vector2(2,4)));
        assertThrows(NotAllowedMoveException.class, ()-> { moveAct.run(w3p2, new Vector2(2,4),m, gc); });
    }

    @Test
    void shouldNotSwapMyOtherWorker()
    {
        moveAct = new MoveAction(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT);
        w3p2.setPosition(new Vector2(1,4));
        assertFalse(moveAct.possibleCells(w3p2, m, null).contains(new Vector2(2,4)));
        assertThrows(NotAllowedMoveException.class, ()-> { moveAct.run(w3p2, new Vector2(2,4),m, gc); });
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

        try
        {

            //check height diff == 1 does nothing
            w1p1.setPosition(new Vector2(3,2));
            assertEquals(0,moveAct.run(w1p1, new Vector2(3,1), m, gc)); // 1 to 0

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

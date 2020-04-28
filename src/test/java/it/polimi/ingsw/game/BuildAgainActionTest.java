package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class BuildAgainActionTest {


    Map m;
    Player p1, p2;
    Worker w1p1, w1p2;
    BuildAction buildAction;
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
        00 |--|--|-3|t-|-1|--|--|
        01 |--|--|@-|k-|-2|--|--|
        02 |--|--|--|-2|@4|--|--|
        03 |--|--|--|--|--|--|--|
        04 |--|--|--|--|--|--|--|
        05 |--|--|--|--|--|--|--|
        06 |--|--|--|--|--|--|--|

     */


    @BeforeEach
    void init()
    {
        gc = new GameConstraints();

        // create players
        p1 = new Player(1, "Kazuma");
        p2 = new Player(2, "Megumin");

        //create workers
        w1p1 = new Worker(p1);
        p1.addWorker(w1p1);

        w1p2 = new Worker(p2);
        p2.addWorker(w1p2);

        //create map
        m = new Map();
        buildPos(m,0,2,3);
        buildPos(m,0,4,1);
        buildPos(m,1,4,2);
        buildPos(m,2,4,4);
        buildPos(m,2,3,2);

        m.buildDome(new Vector2(1,2));

        //place workers
        w1p1.setPosition(new Vector2(1,3));
        w1p2.setPosition(new Vector2(0,3));

        m.setWorkers(p1);
        m.setWorkers(p2);
    }


    @Test
    void shouldNotBuildDomeIfLocked()
    {
        buildAction = new BuildAgainAction(GameConstraints.Constraint.BLOCK_DOME_BUILD);

        assertFalse(buildAction.possibleCells(w1p1, m, gc).contains(new Vector2(0,3)));

        assertThrows(NotAllowedMoveException.class, ()-> { buildAction.run(w1p1, new Vector2(0,3), m, gc); });
        assertThrows(NotAllowedMoveException.class, ()-> { buildAction.run(w1p1, new Vector2(1,2), m, gc); });
    }

    @Test
    void shouldBuildOnlyInTheSameCellIfLocked()
    {

        buildAction = new BuildAgainAction(GameConstraints.Constraint.BLOCK_DIFF_CELL_BUILD);
        w1p1.setLastBuildLocation(new Vector2(2,2));

        assertEquals(1, buildAction.possibleCells(w1p1, m, null).size());
        assertTrue(buildAction.possibleCells(w1p1, m, null).contains(new Vector2(2,2)));

        try
        {
            assertEquals(0, buildAction.run(w1p1, new Vector2(2,2) , m, gc) );
            assertEquals(1, m.getLevel(new Vector2(2,2)));

        }catch (NotAllowedMoveException e)
        {
            fail("Correct build should not throw any exception");
        }

    }

    @Test
    void shouldBuildOnlyInDifferentCellIfLocked()
    {
        buildAction = new BuildAgainAction(GameConstraints.Constraint.BLOCK_SAME_CELL_BUILD);
        w1p1.setLastBuildLocation(new Vector2(2,2));

        assertFalse(buildAction.possibleCells(w1p1, m, null).contains(new Vector2(2,2)));
        assertEquals(4, buildAction.possibleCells(w1p1, m, null).size());

        assertThrows(NotAllowedMoveException.class, ()-> { buildAction.run(w1p1, new Vector2(2,2), m, gc); });
    }


}

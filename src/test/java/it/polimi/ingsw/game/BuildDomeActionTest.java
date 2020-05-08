package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuildDomeActionTest
{

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

        All later tests are based on this map, it's recommend to draw a copy on the paper
        to easily follow the moves and the checks done

     */


    @BeforeEach
    void init()
    {
        buildAction = new BuildDomeAction();
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
    void shouldNotBuildDomeOnDome()
    {
        assertThrows(NotAllowedMoveException.class, ()-> { buildAction.run(w1p1, new Vector2(1,2), m, gc); });
    }

    @Test
    void shouldBuildDomeAtAnyLevel()
    {
        buildAction = new BuildDomeAction();

        try
        {
            assertEquals(0, buildAction.run(w1p1, new Vector2(2,2) , m, gc) );
            assertTrue(m.isCellDome(new Vector2(2,2)));

            assertEquals(0, buildAction.run(w1p1, new Vector2(0,4) , m, gc) );
            assertTrue(m.isCellDome(new Vector2(0,4)));

        }
        catch (NotAllowedMoveException e)
        {
            fail("Correct build should not throw any exception");
        }
    }
}
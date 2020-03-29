package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MapTest {

    Map map ;

    @BeforeEach
    void setUp() {
        map = new Map();
    }

    @Test
    void testCompleteCell(){
        Vector2 pos = new Vector2(0,0);
        try {
            for(int i = 0; i < 4 ; i++)
                map.build(pos);
            assertEquals(4,map.getLevel(pos));
            assertTrue(map.isCellDome(pos));
            assertThrows(CellCompletedException.class, () -> map.build(pos));
        } catch (CellCompletedException e) {
            fail("Build in a completed cell");
        } catch (OutOfMapException e) {
            fail("Build in a not valid cell");
        }
    }

    @Test
    void testBuildDome(){
        Vector2 pos = new Vector2(0,0);
        try {
            map.buildDome(pos);
            assertEquals(1,map.getLevel(pos));
            assertTrue(map.isCellDome(pos));
            assertThrows(CellCompletedException.class, () -> map.buildDome(pos));
        } catch (CellCompletedException e) {
            fail("Build in a completed cell");
        } catch (OutOfMapException e) {
            fail("Build in a not valid cell");
        }
    }

    //test for OutOfMapException and isInsideMap
    @Test
    void testInsideOutside(){
        Vector2 pos = new Vector2(6,6);
        try{
            assertFalse(map.isCellDome(pos)); //isInside == true
            pos.set(7,6);
            assertThrows(OutOfMapException.class, () -> map.isCellDome(pos)); //isInside == false
        } catch (OutOfMapException e) {
            fail("Checking a not valid cell");
        }
    }

    //Create a player and its 2 workers, add and remove workers in map
    @Test
    void testWorkersList(){
        Vector2 pos = new Vector2(0,0);
        Vector2 post = new Vector2(6,6);
        Player p = new Player(1, "FirstPlayer");
        Worker w1 = new Worker(p);
        Worker w2 = new Worker(p);

        w1.setPos(pos);
        w2.setPos(post);

        p.setWorker(w1);
        p.setWorker(w2);

        map.setWorkers(p);
        try {
            assertFalse(map.isCellEmpty(pos));
            assertFalse(map.isCellEmpty(post));
        } catch (OutOfMapException e) {
            fail("Checking a not valid cell");
        }

        map.removeWorkers(p);
        try {
            assertTrue(map.isCellEmpty(pos));
            assertTrue(map.isCellEmpty(post));
        } catch (OutOfMapException e) {
            fail("Checking a not valid cell");
        }

    }
}

package it.polimi.ingsw.game;

import com.sun.source.tree.AssertTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MapTest {

    Map map ;

    @BeforeEach
    void setUp() {
        map = new Map();
    }

    @Test
    void shouldHaveAllLevelZeroCells()
    {
        int[][] m = map.getMap();
        // m is a square
        for(int i =0; i< m.length; i++)
        {
            for(int j =0; j < m.length; j++)
            {
                Vector2 p = new Vector2(i,j);
                assertEquals(0, map.getLevel(p));
                assertTrue(map.isCellEmpty(p));
                assertFalse(map.isCellDome(p));
            }
        }
    }


    @Test
    void shouldBeInsideMap()
    {
        //map is 5x5 starting from 0
        assertTrue(map.isInsideMap(new Vector2(0,0)));

        assertTrue(map.isInsideMap(new Vector2(4,0)));

        assertTrue(map.isInsideMap(new Vector2(4,4)));

        assertTrue(map.isInsideMap(new Vector2(1,3)));
    }

    @Test
    void shouldNotBeInsideMap()
    {
        assertFalse(map.isInsideMap(new Vector2(-1,0))); // x < 0

        assertFalse(map.isInsideMap(new Vector2(5,0))); // x > 6

        assertFalse(map.isInsideMap(new Vector2(5,-1))); // y < 0

        assertFalse(map.isInsideMap(new Vector2(6,7))); //y > 6
    }

    @Test
    void shouldBuildOneLevel()
    {
        Vector2 pos = new Vector2(0,0);

        // 0 -> 1
        map.build(pos);
        assertEquals(1, map.getLevel(pos));
        assertFalse(map.isCellDome(pos));
        assertTrue(map.isCellEmpty(pos));

        // 1 -> 2
        map.build(pos);
        assertEquals(2, map.getLevel(pos));
        assertFalse(map.isCellDome(pos));
        assertTrue(map.isCellEmpty(pos));
    }


    @Test
    void shouldBuildDomeAtLevelFour(){
        Vector2 pos = new Vector2(0,0);

        for(int i = 0; i < 4 ; i++)
            assertTrue(map.build(pos));
        assertEquals(4,map.getLevel(pos));
        assertTrue(map.isCellDome(pos));

        assertFalse(map.build(pos));
    }

    @Test
    void shouldNotBuildOnDome()
    {
        Vector2 pos = new Vector2(0,0);

        map.buildDome(pos);
        assertFalse(map.build(pos));
    }

    @Test
    void shouldNotBuildADomeToALevelFourDome()
    {
        Vector2 pos = new Vector2(0,0);

        for(int i = 0; i < 4 ; i++)
            assertTrue(map.build(pos));

        assertFalse(map.build(pos));
        assertFalse(map.buildDome(pos));
    }

    @Test
    void shouldBuildDomeAtAnyLevel(){
        Vector2 pos = new Vector2(0,0);

        assertTrue(map.buildDome(pos));
        assertEquals(1,map.getLevel(pos));
        assertTrue(map.isCellDome(pos));
        assertFalse(map.buildDome(pos));
    }

    @Test
    void shouldNotBuildOutsideMap()
    {
        assertFalse(map.build(new Vector2(-100, -92)));
    }

    @Test
    void shouldNotBuildDomeOutsideMap()
    {
        assertFalse(map.buildDome(new Vector2(-100, -92)));
    }

    @Test
    void shouldReturnMinusOneIfLevelOutsideMap()
    {
        assertEquals(-1, map.getLevel(new Vector2(-100, -92)));
    }

    @Test
    void shouldNeverBeADomeOutsideMap()
    {
        assertFalse(map.isCellDome(new Vector2(-100, 87)));
    }

    @Test
    void shouldAlwaysBeEmptyIfCellOutsideMap()
    {
        assertTrue(map.isCellEmpty(new Vector2(-100,862)));
    }


    //Create a player and its 2 workers, add and remove workers in map
    @Test
    void shouldAddWorkers(){

        Player p = new Player(1, "FirstPlayer");
        Worker w1 = new Worker(0, p, new Vector2(0,0));
        Worker w2 = new Worker(1, p, new Vector2(4,4));
        p.addWorker(w1);
        p.addWorker(w2);

        Player p2 = new Player(2,"padoru");
        Worker w3 = new Worker(0, p2, new Vector2(3,3));
        p2.addWorker(w3);

        map.setWorkers(p);
        map.setWorkers(p2);

        var mw = map.getWorkers();
        assertEquals(3, mw.size());
        assertTrue(mw.contains(w1));
        assertTrue(mw.contains(w2));
        assertTrue(mw.contains(w3));
    }

    @Test
    void shouldRemoveWorkers()
    {
        Player p = new Player(1, "FirstPlayer");
        Worker w1 = new Worker(0, p, new Vector2(0,0));
        Worker w2 = new Worker(1, p, new Vector2(4,4));
        p.addWorker(w1);
        p.addWorker(w2);

        Player p2 = new Player(2,"padoru");
        Worker w3 = new Worker(0, p2, new Vector2(3,3));
        p2.addWorker(w3);

        map.setWorkers(p);
        map.setWorkers(p2);

        map.removeWorkers(p);

        var mw = map.getWorkers();
        assertEquals(1, mw.size());
        assertTrue(mw.contains(w3));
    }

    @Test
    void shouldReturnNullWorkerIfCellIsEmpty()
    {
        assertNull(map.getWorker(new Vector2(0,0)));
    }

    @Test
    void shouldReturnNullWorkerIfOutsideMap()
    {
        assertNull(map.getWorker(new Vector2(-973,0)));
    }

    @Test
    void shouldReturnWorkerByPosition()
    {
        Player p = new Player(1, "FirstPlayer");
        Worker w1 = new Worker(0, p, new Vector2(0,0));
        Worker w2 = new Worker(1, p, new Vector2(4,4));
        p.addWorker(w1);
        p.addWorker(w2);

        Player p2 = new Player(2,"padoru");
        Worker w3 = new Worker(0, p2, new Vector2(3,3));
        p2.addWorker(w3);

        map.setWorkers(p);
        map.setWorkers(p2);

        assertEquals(w1, map.getWorker(new Vector2(0,0)));
        assertEquals(w2, map.getWorker(new Vector2(4,4)));
        assertEquals(w3, map.getWorker(new Vector2(3,3)));
    }

    @Test
    void shouldFillCellsWhenWorkersAreAdded()
    {
        Player p = new Player(1, "FirstPlayer");
        Worker w1 = new Worker(0, p, new Vector2(0,0));
        Worker w2 = new Worker(1, p, new Vector2(4,4));
        p.addWorker(w1);
        p.addWorker(w2);

        Player p2 = new Player(2,"padoru");
        Worker w3 = new Worker(0, p2, new Vector2(3,3));
        p2.addWorker(w3);

        map.setWorkers(p);
        map.setWorkers(p2);


        assertFalse(map.isCellEmpty(new Vector2(0,0)));
        assertFalse(map.isCellEmpty(new Vector2(4,4)));
        assertFalse(map.isCellEmpty(new Vector2(3,3)));
    }


    @Test
    void shouldReturnACopyOfTheMap(){

        Vector2 pos = new Vector2(0,0);
        Player p = new Player(1, "FirstPlayer");
        Worker w1 = new Worker(p);
        w1.setPosition(pos);
        p.addWorker(w1);
        map.setWorkers(p);
        map.build(new Vector2(0,0));
        map.buildDome(new Vector2(0,4));

        Map newMap = new Map(map);

        assertEquals(map.getWorkers().get(0),newMap.getWorkers().get(0));

        assertArrayEquals(map.getMap(),newMap.getMap());

        for (int i = 0; i < Map.LENGTH ; i++)
            for (int j = 0; j < Map.HEIGHT; j++)
                assertEquals(map.getLevel(new Vector2(i,j)),newMap.getLevel(new Vector2(i,j)));

    }
}

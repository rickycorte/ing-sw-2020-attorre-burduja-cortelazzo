package it.polimi.ingsw.game;

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
    void testCompleteCell(){
        Vector2 pos = new Vector2(0,0);

        for(int i = 0; i < 4 ; i++)
            assertTrue(map.build(pos));
        assertEquals(4,map.getLevel(pos));
        assertTrue(map.isCellDome(pos));
        assertFalse(map.build(pos));
    }

    @Test
    void testBuildDome(){
        Vector2 pos = new Vector2(0,0);

        assertTrue(map.buildDome(pos));
        assertEquals(1,map.getLevel(pos));
        assertTrue(map.isCellDome(pos));
        assertFalse(map.buildDome(pos));
    }


    //Create a player and its 2 workers, add and remove workers in map
    @Test
    void testWorkersList(){
        Vector2 pos = new Vector2(0,0);
        Vector2 post = new Vector2(6,6);
        Player p = new Player(1, "FirstPlayer");
        Worker w1 = new Worker(p);
        Worker w2 = new Worker(p);

        w1.setPosition(pos);
        w2.setPosition(post);

        p.addWorker(w1);
        p.addWorker(w2);

        map.setWorkers(p);

        assertFalse(map.isCellEmpty(pos));
        assertFalse(map.isCellEmpty(post));


        map.removeWorkers(p);

        assertTrue(map.isCellEmpty(pos));
        assertTrue(map.isCellEmpty(post));


    }

    @Test
    void testMapToFile(){
        Vector2 pos = new Vector2(0,0);

        //setting
        map.build(pos);
        map.build(pos);
        map.build(pos);
        pos.set(6,6);
        map.buildDome(pos);
        pos.set(3,4);
        map.build(pos);
        map.buildDome(pos);

        //save and re-setting
        map.writeMapOut("map.bin");
        pos.set(0, 0);
        map.build(pos);
        pos.set(1, 1);
        map.build(pos);
        pos.set(1, 2);
        map.build(pos);

        pos.set(0, 0);
        assertEquals(4,map.getLevel(pos));
        assertTrue(map.isCellDome(pos));
        pos.set(1, 1);
        assertEquals(1,map.getLevel(pos));
        pos.set(1, 2);
        assertEquals(1,map.getLevel(pos));
        pos.set(3, 4);
        assertEquals(2,map.getLevel(pos));
        assertTrue(map.isCellDome(pos));
        pos.set(6, 6);
        assertEquals(1,map.getLevel(pos));
        assertTrue(map.isCellDome(pos));
        //loading
        map.readMapOut("map.bin");
        pos.set(0,0);
        assertEquals(3,map.getLevel(pos));
        assertFalse(map.isCellDome(pos));
        pos.set(1, 1);
        assertEquals(0,map.getLevel(pos));
        pos.set(1, 2);
        assertEquals(0,map.getLevel(pos));
        pos.set(3,4);
        assertEquals(2,map.getLevel(pos));
        assertTrue(map.isCellDome(pos));
        pos.set(6,6);
        assertEquals(1, map.getLevel(pos));
        assertTrue(map.isCellDome(pos));
        //sovrascrittura
        //re-setting
        pos.set(1, 1);
        map.build(pos);
        assertEquals(1,map.getLevel(pos));
        //save re-setted
        map.writeMapOut("map.bin");

        pos.set(1,1);
        map.build(pos);
        assertEquals(2,map.getLevel(pos));

        map.readMapOut("map.bin");
        assertEquals(1,map.getLevel(pos));
    }

    @Test
    void copyConstructorTest(){

        map.readMapOut("map00closed.bin");
        Vector2 pos = new Vector2(0,0);
        Player p = new Player(1, "FirstPlayer");
        Worker w1 = new Worker(p);
        w1.setPosition(pos);
        p.addWorker(w1);
        map.setWorkers(p);

        Map newMap = new Map(map);

        assertEquals(map.getWorkers().get(0),newMap.getWorkers().get(0));

        for (int i = 0; i<7 ; i++)
            for (int j = 0; j<7; j++)
                assertEquals(map.getLevel(new Vector2(i,j)),newMap.getLevel(new Vector2(i,j)));

    }
}

package it.polimi.ingsw.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkerTest
{

    @Test
    void shouldBeEquals()
    {
        Player p = new Player(0,"shish");
        Worker w1 = new Worker(0, p, new Vector2(0,0));
        Worker w2 = new Worker(0, p, new Vector2(0,0));

        assertEquals(w1, w2);

        //self
        assertEquals(w1,w1);
    }

    @Test
    void shouldNotBeEqualToNull()
    {
        Player p = new Player(0,"shish");
        Worker w1 = new Worker(0, p, new Vector2(0,0));
        assertNotEquals(null, w1);
    }

    @Test
    void shouldNotBeEqualToRandomObject()
    {
        Player p = new Player(0,"shish");
        Worker w1 = new Worker(0, p, new Vector2(0,0));
        assertNotEquals(new Vector2(0,0), w1);
        assertNotEquals(new Object(), w1);
    }

    @Test
    void shouldNotBeEqualWithDifferentID()
    {
        Player p = new Player(0,"shish");
        Worker w1 = new Worker(0, p, new Vector2(0,0));
        Worker w2 = new Worker(1, p, new Vector2(0,0));

        assertNotEquals(w1, w2);
    }

    @Test
    void shouldNotBeEqualWithDifferentOwner()
    {
        Player p = new Player(0,"shish");
        Worker w1 = new Worker(0, p, new Vector2(0,0));
        Worker w2 = new Worker(0, new Player(1,"hye"), new Vector2(0,0));

        assertNotEquals(w1, w2);
    }

    @Test
    void shouldNotBeEqualWithDifferentPosition()
    {
        Player p = new Player(0,"shish");
        Worker w1 = new Worker(0, p, new Vector2(0,0));
        Worker w2 = new Worker(0, p, new Vector2(0,1));

        assertNotEquals(w1, w2);
    }

}
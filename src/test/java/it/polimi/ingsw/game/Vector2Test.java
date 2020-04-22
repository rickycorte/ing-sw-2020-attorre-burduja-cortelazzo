package it.polimi.ingsw.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vector2Test
{

    @Test
    void shouldCopyVector()
    {
        Vector2 p1 = new Vector2(1,5);
        Vector2 p2 = p1.copy();

        assertEquals(p1.getX(), p2.getX());
        assertEquals(p1.getY(), p2.getY());
        assertFalse(p1 == p2); // compare pointers to see if are different and thus p2 is a real copy
    }

    @Test
    void shouldCalculateDistance()
    {
        Vector2 p1 = new Vector2(0,0);

        // distance with self
        assertEquals(0, p1.distance(p1));

        // horizontal distance
        assertEquals(1, p1.distance(new Vector2(1,0)));
        assertEquals(5, p1.distance(new Vector2(-5,0)));

        // vertical distance
        assertEquals(1, p1.distance(new Vector2(0,1)));
        assertEquals(5, p1.distance(new Vector2(0,-5)));

        // diagonal should be accurate only at distance 1
        assertEquals(1, p1.distance(new Vector2(1,1)));
        assertEquals(1, p1.distance(new Vector2(-1,-1)));
        assertEquals(1, p1.distance(new Vector2(1,-1)));
        assertEquals(1, p1.distance(new Vector2(-1,1)));

    }
}
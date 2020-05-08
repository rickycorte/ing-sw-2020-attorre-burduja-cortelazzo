package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vector2Test
{

    Vector2 p1;

    @BeforeEach
    void setup()
    {
        p1 = new Vector2(0,0);
    }

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
    void shouldCalculateAccurateDistanceForAdjacentPositions()
    {
        assertEquals(1, p1.distance(new Vector2(1,1)));
        assertEquals(1, p1.distance(new Vector2(-1,-1)));
        assertEquals(1, p1.distance(new Vector2(1,-1)));
        assertEquals(1, p1.distance(new Vector2(-1,1)));
    }

    @Test
    void shouldHaveZeroDistanceWithSelf()
    {
        assertEquals(0, p1.distance(p1));
    }

    @Test
    void shouldCalculateAccurateVerticalDistance()
    {
        assertEquals(1, p1.distance(new Vector2(0,1)));

        assertEquals(5, p1.distance(new Vector2(0,-5)));
    }

    @Test
    void shouldCalculateAccurateHorizontalDistance()
    {
        assertEquals(1, p1.distance(new Vector2(1,0)));
        assertEquals(5, p1.distance(new Vector2(-5,0)));
    }

}
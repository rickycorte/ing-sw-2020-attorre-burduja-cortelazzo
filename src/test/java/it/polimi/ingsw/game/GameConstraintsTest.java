package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameConstraintsTest
{

    GameConstraints c;

    @BeforeEach
    void setUp()
    {
        c = new GameConstraints();
    }

    @Test
    void shouldAddNothing(){

        GameConstraints temp = new GameConstraints(c);
        c.add(GameConstraints.Constraint.NONE);
        c.add((GameConstraints) null);

        assertTrue(c.equals(temp));
    }

    @Test
    void shouldDisableNoneConstraintIfSomethingIsSet()
    {
        // nothing set -> NONE is true
        assertTrue(c.check(GameConstraints.Constraint.NONE));

        c.add(GameConstraints.Constraint.TEST);

        // something is set NONE is false
        assertFalse(c.check(GameConstraints.Constraint.NONE));
    }

    @Test
    void shouldAddAConstraint()
    {
        c.add(GameConstraints.Constraint.TEST);

        c.add(GameConstraints.Constraint.BLOCK_MOVE_UP);

        assertTrue(c.check(GameConstraints.Constraint.BLOCK_MOVE_UP) );
    }

    @Test
    void shouldAddMultipleTimesTheSameConstraintOnlySetOnce()
    {
        c.add(GameConstraints.Constraint.BLOCK_MOVE_UP);
        c.add(GameConstraints.Constraint.BLOCK_MOVE_UP); // also check double set
        assertTrue(c.check(GameConstraints.Constraint.BLOCK_MOVE_UP) );

        c.add(GameConstraints.Constraint.BLOCK_MOVE_UP); // triple set
        assertTrue(c.check(GameConstraints.Constraint.BLOCK_MOVE_UP) );
    }

    @Test
    void shouldAddAGroupOfConstraints()
    {
        c.add(GameConstraints.Constraint.BLOCK_MOVE_UP);
        c.add(GameConstraints.Constraint.TEST);

        //add a group of constrains
        GameConstraints t2 = new GameConstraints();
        t2.add(GameConstraints.Constraint.BLOCK_MOVE_UP); // this should be "re-set" causing no error
        t2.add(c);
        assertTrue(c.equals(t2));
    }

    @Test
    void shouldRemoveConstraint()
    {
        c.add(GameConstraints.Constraint.TEST);
        c.add(GameConstraints.Constraint.BLOCK_MOVE_UP);
        c.remove(GameConstraints.Constraint.TEST);

        // only affect selected constraint
        assertFalse(c.check(GameConstraints.Constraint.TEST) && c.check(GameConstraints.Constraint.BLOCK_MOVE_UP));
    }

    @Test
    void shouldRemoveMultipleTimesTheSameConstraintOnlyDisableOnce()
    {
        c.add(GameConstraints.Constraint.BLOCK_MOVE_UP);
        c.add(GameConstraints.Constraint.TEST);

        //double remove
        c.remove(GameConstraints.Constraint.TEST);
        c.remove(GameConstraints.Constraint.TEST);
        assertFalse(c.check(GameConstraints.Constraint.TEST) && c.check(GameConstraints.Constraint.BLOCK_MOVE_UP));

        // triple
        c.remove(GameConstraints.Constraint.TEST);
        assertFalse(c.check(GameConstraints.Constraint.TEST) && c.check(GameConstraints.Constraint.BLOCK_MOVE_UP));

    }

    @Test
    void shouldCheckNothing()
    {
        c.add(GameConstraints.Constraint.TEST);

        assertFalse(c.check(GameConstraints.Constraint.NONE));

        c.clear();

        assertTrue(c.check(GameConstraints.Constraint.NONE));
    }

    @Test
    void shouldCheckConstraint()
    {
        c.add(GameConstraints.Constraint.TEST);

        assertTrue(c.check(GameConstraints.Constraint.TEST));
        assertFalse(c.check(GameConstraints.Constraint.NONE), "checking NONE should return false if a constraint is set");
    }

    @Test
    void shouldClearConstraints()
    {
        c.clear();
        assertTrue(c.check(GameConstraints.Constraint.NONE));
    }

    @Test
    void shouldEquals()
    {
        c.add(GameConstraints.Constraint.TEST);
        GameConstraints c2 = new GameConstraints(), c3 = new GameConstraints();
        c2.add(c);
        assertTrue(c.equals(c2));
        assertFalse(c.equals(c3));
    }
}
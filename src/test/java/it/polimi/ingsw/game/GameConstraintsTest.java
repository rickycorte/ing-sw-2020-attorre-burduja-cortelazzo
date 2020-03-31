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

        assertTrue(c.equals(temp));
    }

    @Test
    void shouldAddAConstraint()
    {
        c.add(GameConstraints.Constraint.BLOCK_NON_LO_SO);

        c.add(GameConstraints.Constraint.BLOCK_MOVE_UP);
        c.add(GameConstraints.Constraint.BLOCK_MOVE_UP); // also check double set

        assertTrue(c.check(GameConstraints.Constraint.NONE) == false && c.check(GameConstraints.Constraint.BLOCK_MOVE_UP) );

        //add a group of constrains
        GameConstraints t2 = new GameConstraints();
        t2.add(GameConstraints.Constraint.BLOCK_MOVE_UP); // this should be "re-set" causing no error
        t2.add(c);
        assertTrue(c.equals(t2));
    }

    @Test
    void shouldRemoveConstraint()
    {
        c.add(GameConstraints.Constraint.BLOCK_NON_LO_SO);
        c.add(GameConstraints.Constraint.BLOCK_MOVE_UP);
        c.remove(GameConstraints.Constraint.BLOCK_NON_LO_SO);
        c.remove(GameConstraints.Constraint.BLOCK_NON_LO_SO); // also check double remove

        assertFalse(c.check(GameConstraints.Constraint.BLOCK_NON_LO_SO));
    }

    @Test
    void shouldCheckNothing()
    {
        c.add(GameConstraints.Constraint.BLOCK_NON_LO_SO);

        assertFalse(c.check(GameConstraints.Constraint.NONE));

        c.clear();

        assertTrue(c.check(GameConstraints.Constraint.NONE));
    }

    @Test
    void shouldCheckConstraint()
    {
        c.add(GameConstraints.Constraint.BLOCK_NON_LO_SO);

        assertTrue(c.check(GameConstraints.Constraint.BLOCK_NON_LO_SO));
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
        c.add(GameConstraints.Constraint.BLOCK_NON_LO_SO);
        GameConstraints c2 = new GameConstraints(), c3 = new GameConstraints();
        c2.add(c);
        assertTrue(c.equals(c2));
        assertFalse(c.equals(c3));
    }
}
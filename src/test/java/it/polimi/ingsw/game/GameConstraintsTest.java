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
}
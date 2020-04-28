package it.polimi.ingsw.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EndTurnActionTest
{

    @Test
    void shouldDoNothing()
    {
        EndTurnAction ea = new EndTurnAction();
        Worker w = new Worker(null);
        Map m = new Map();
        GameConstraints gc = new GameConstraints();

        assertEquals(0, ea.run(w, new Vector2(1,1), m, gc));
        assertEquals(0, m.getLevel(new Vector2(1,1)));
        assertNull(w.getOwner());
        assertEquals(new Vector2(0,0), w.getPosition());

        // does nothing so everything as parameter is fine
        assertEquals(0, ea.run(null,null, null, null));
    }

    @Test
    void shouldReturnOneValidCell()
    {
        EndTurnAction ea = new EndTurnAction();
        Worker w = new Worker(null);
        Map m = new Map();

        var cells = ea.possibleCells(w,m,null);

        assertEquals(1, cells.size());
        assertTrue(m.isInsideMap(cells.get(0)));

        //every parameter should be fine

        cells = ea.possibleCells(null, null,null);

        assertEquals(1, cells.size());
        assertTrue(m.isInsideMap(cells.get(0)));
    }

}
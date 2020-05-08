package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EndTurnActionTest
{

    EndTurnAction ea;
    Worker w;
    Map m;
    GameConstraints gc;

    @BeforeEach
    void setup()
    {
        ea = new EndTurnAction();
        w = new Worker(null);
        m = new Map();
        gc = new GameConstraints();
    }

    @Test
    void shouldDoNothing()
    {
        assertEquals(0, ea.run(w, new Vector2(1,1), m, gc));
        assertEquals(0, m.getLevel(new Vector2(1,1)));
        assertNull(w.getOwner());
        assertEquals(new Vector2(0,0), w.getPosition());
    }

    @Test
    void shouldDoNothingWithTrashData()
    {
        // does nothing so everything as parameter is fine
        assertEquals(0, ea.run(null,null, null, null));
    }

    @Test
    void shouldReturnOneValidCell()
    {
        var cells = ea.possibleCells(w,m,null);

        assertEquals(1, cells.size());
        assertTrue(m.isInsideMap(cells.get(0)));
    }

    @Test
    void shouldReturnOneValidCellWithTrashData()
    {
        //every parameter should be fine
        var cells = ea.possibleCells(null, null,null);

        assertEquals(1, cells.size());
        assertTrue(m.isInsideMap(cells.get(0)));
    }

}
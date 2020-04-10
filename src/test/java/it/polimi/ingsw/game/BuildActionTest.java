package it.polimi.ingsw.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class BuildActionTest {



    @Test
    void should_find_3_cells(){
        Worker w = new Worker(null);
        Map m = new Map();
        Vector2 p1 = new Vector2(0,0);
        w.setPos(p1);
        assertEquals(p1, w.getPos());
        BuildAction a = new BuildAction();
        try {
            ArrayList<Vector2> cells = a.possibleCells(w, m, null, null);
            assertEquals(3, cells.size());
        }catch (OutOfMapException e)
        {
            System.out.println("i've thrown an exception_T");
        }
    }
    @Test
    void should_find_5_cells(){
        Worker w = new Worker(null);
        Map m = new Map();
        Vector2 p1 = new Vector2(0,3);
        w.setPos(p1);
        assertEquals(p1, w.getPos());
        BuildAction a = new BuildAction();
        try {
            ArrayList<Vector2> cells = a.possibleCells(w, m, null, null);
            assertEquals(5, cells.size());
        }catch (OutOfMapException e)
        {
            System.out.println("i've thrown an exception");
        }
    }
    @Test
    void should_find_8_cells(){
        Worker w = new Worker(null);
        Map m = new Map();
        Vector2 p1 = new Vector2(3,3);

        w.setPos(p1);
        assertEquals(p1, w.getPos());
        BuildAction a = new BuildAction();
        try {
            ArrayList<Vector2> cells = a.possibleCells(w, m, null, null);
            assertEquals(8, cells.size());
        }catch (OutOfMapException e)
        {
            System.out.println("i've thrown an exception");
        }
    }





}


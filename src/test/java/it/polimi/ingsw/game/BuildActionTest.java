package it.polimi.ingsw.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class BuildActionTest {



    @Test
    void should_find_3_cells() {
        Worker w = new Worker(null);
        Map m = new Map();
        Vector2 w_pos = new Vector2(0, 0);
        w.setPos(w_pos);
        assertEquals(w_pos, w.getPos());
        BuildAction a = new BuildAction();

        ArrayList<Vector2> cells = a.possibleCells(w, m, null, null);
        assertEquals(3, cells.size());

    }
    @Test
    void should_find_5_cells() {
        Worker w = new Worker(null);
        Map m = new Map();
        Vector2 p1 = new Vector2(0, 3);
        w.setPos(p1);
        assertEquals(p1, w.getPos());
        BuildAction a = new BuildAction();

        ArrayList<Vector2> cells = a.possibleCells(w, m, null, null);
        assertEquals(5, cells.size());
    }
    @Test
    void should_find_8_cells() {
        Worker w = new Worker(null);
        Map m = new Map();
        Vector2 p1 = new Vector2(3, 3);

        w.setPos(p1);
        assertEquals(p1, w.getPos());
        BuildAction a = new BuildAction();

        ArrayList<Vector2> cells = a.possibleCells(w, m, null, null);
        assertEquals(8, cells.size());

    }


    @Test
    void simplebuild_test(){
        Player player = new Player(1,"uno");
        Map m = new Map();
        Vector2 w_pos = new Vector2(3,3);
        Vector2 b_pos = new Vector2(3,4);
        Vector2 t_pos = new Vector2(3,4);
        assertTrue(b_pos.equals(t_pos));
        Worker w = new Worker(player);
        w.setPos(w_pos);
        player.addWorker(w);
        m.setWorkers(player);

        BehaviourNode bn = BehaviourNode.makeRootNode(new BuildAction());
        assertEquals("BuildNONE", bn.getAction().display_name);

        GameConstraints gc = new GameConstraints();
        ArrayList<Vector2> cells = bn.getAction().possibleCells(w,m,gc,bn);
        System.out.println(cells.size());
        /*
        for( Vector2 cell : cells ){
            System.out.println(cell.getX());
            System.out.println(cell.getY());
            System.out.println("\n");
        }

         */
        assertTrue(cells.contains(b_pos));

        try {
            int outcome = bn.getAction().run(w,b_pos,m,gc,bn);
            assertEquals(0, outcome);
            assertEquals(1, m.getLevel(b_pos));
            outcome = bn.getAction().run(w,b_pos,m,gc,bn);
            outcome = bn.getAction().run(w,b_pos,m,gc,bn);
            outcome = bn.getAction().run(w,b_pos,m,gc,bn);
            assertEquals(4, m.getLevel(b_pos));


        }catch (NotAllowedMoveException e){
            System.out.println("This move is not allowed");
        }
    }





}


package it.polimi.ingsw.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class BuildActionTest {

    @Test
    void shouldFind3Cells() {
        Worker w = new Worker(null);
        Map m = new Map();
        Vector2 w_pos = new Vector2(0, 0);
        w.setPosition(w_pos);
        assertEquals(w_pos, w.getPosition());
        BuildAction a = new BuildAction();

        ArrayList<Vector2> cells = a.possibleCells(w, m, null);
        assertEquals(3, cells.size());

    }
    @Test
    void shouldFind5Cells() {
        Worker w = new Worker(null);
        Map m = new Map();
        Vector2 p1 = new Vector2(0, 3);
        w.setPosition(p1);
        assertEquals(p1, w.getPosition());
        BuildAction a = new BuildAction();

        ArrayList<Vector2> cells = a.possibleCells(w, m, null);
        assertEquals(5, cells.size());
    }
    @Test
    void shouldFind8Cells() {
        Worker w = new Worker(null);
        Map m = new Map();
        Vector2 p1 = new Vector2(3, 3);

        w.setPosition(p1);
        assertEquals(p1, w.getPosition());
        BuildAction a = new BuildAction();

        ArrayList<Vector2> cells = a.possibleCells(w, m, null);
        assertEquals(8, cells.size());

    }


    @Test
    void shouldBuild(){
        Player player = new Player(1,"uno");
        Map m = new Map();
        Vector2 w_pos = new Vector2(3,3);
        Vector2 b_pos = new Vector2(3,4);
        Vector2 t_pos = new Vector2(3,4);
        assertTrue(b_pos.equals(t_pos));
        Worker w = new Worker(player);
        w.setPosition(w_pos);
        player.addWorker(w);
        m.setWorkers(player);

        BehaviourNode bn = BehaviourNode.makeRootNode(new BuildAction());
        assertEquals("BuildNONE", bn.getAction().displayName);

        GameConstraints gc = new GameConstraints();
        ArrayList<Vector2> cells = bn.getAction().possibleCells(w,m,gc);

        assertTrue(cells.contains(b_pos));

        try {
            int outcome = bn.getAction().run(w,b_pos,m,gc);
            assertEquals(0, outcome);
            assertEquals(1, m.getLevel(b_pos));
            outcome = bn.getAction().run(w,b_pos,m,gc);
            outcome = bn.getAction().run(w,b_pos,m,gc);
            outcome = bn.getAction().run(w,b_pos,m,gc);
            assertEquals(4, m.getLevel(b_pos));


        }catch (NotAllowedMoveException e){
            System.out.println("This move is not allowed");
        }
    }


}


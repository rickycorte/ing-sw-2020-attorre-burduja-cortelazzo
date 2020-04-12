package it.polimi.ingsw.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class BuildDomeActionTest {


    @Test
    void shouldBuildADome(){
        Player player = new Player(1, "uno");
        Vector2 p1 = new Vector2(3,3);
        Vector2 p2 = new Vector2(2,3);
        BehaviourNode bn = BehaviourNode.makeRootNode(null);
        BuildDomeAction bda = new BuildDomeAction();
        BehaviourNode bm = bn.setNext(bda);
        Map m = new Map();
        GameConstraints gc = new GameConstraints();
        Worker w = new Worker(player);
        w.setPos(p1);
        int outcome  = 3;
        try {
            ArrayList<Vector2> cells = bda.possibleCells(w,m,gc,bm);
            assertEquals(8, cells.size());
            assertTrue(cells.contains(p2));

            assertFalse(m.isCellDome(p2));

            outcome = bda.run(w,p2,m,gc,bm);
            assertEquals(0, outcome);

            assertTrue(m.isCellDome(p2));

        }catch (NotAllowedMoveException e){
            System.out.println("exception");
        }



    }


}

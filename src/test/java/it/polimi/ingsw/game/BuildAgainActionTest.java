package it.polimi.ingsw.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class BuildAgainActionTest {


    @Test
    void should_find_1_cell_same_space_noDome(){
        GameConstraints gc = new GameConstraints();
        gc.add(GameConstraints.Constraint.BLOCK_DIFF_CELL_BUILD); //testing the scenario when you can only build on the same cell

        Map m = new Map();
        Worker w = new Worker(null);
        Vector2 p1 = new Vector2(3,3);
        w.setPosition(p1);
        w.setLastBuildLocation(new Vector2(2,2));

        BuildAgainAction baa = new BuildAgainAction();

        BehaviourGraph bh = BehaviourGraph.makeEmptyGraph();

        ArrayList cells = baa.possibleCells(w,m,gc);
        assertEquals(1, cells.size());

    }

    @Test
    void should_find_7_cells_different_space(){

        Map m = new Map();
        Worker w = new Worker(null);
        Vector2 p1 = new Vector2(3,3);
        w.setPosition(p1);
        w.setLastBuildLocation(new Vector2(2,3));

        BuildAgainAction baa = new BuildAgainAction(GameConstraints.Constraint.BLOCK_SAME_CELL_BUILD);  //testing the scenario when you can only build on diff cells

        ArrayList cells = baa.possibleCells(w,m,null);
        assertEquals(7, cells.size());

    }

    @Test
    void should_find_8_cells() {
        //build again with no constrains
        GameConstraints gc = new GameConstraints();

        Map m = new Map();
        Worker w = new Worker(null);
        Vector2 p1 = new Vector2(3, 3);
        w.setPosition(p1);

        BuildAgainAction baa = new BuildAgainAction();

        ArrayList cells = baa.possibleCells(w, m, gc);
        assertEquals(8, cells.size());


    }

    @Test
    void buildAgainTest(){
        Map m = new Map();
        Player player = new Player(1,"uno");
        Worker w = new Worker(player);
        //pos
        Vector2 p1 = new Vector2(3, 3);
        Vector2 p2 = new Vector2(2,3);
        Vector2 p3 = new Vector2(4,3);

        //prepare map
        w.setPosition(p1);
        m.setWorkers(player);

        BehaviourNode bn = BehaviourNode.makeRootNode(new BuildAction()).setNext(new BuildAgainAction()).getRoot();

        GameConstraints gc = new GameConstraints();

        try
        {
            gc.add(GameConstraints.Constraint.BLOCK_SAME_CELL_BUILD);
            //first normal build
            bn.getAction().run(w,p2,m,gc);
            bn = bn.getNextNode(0);

            var cells = bn.getAction().possibleCells(w,m,gc);
            assertEquals(7, cells.size());
            assertFalse(cells.contains(p2));
            assertEquals(0, bn.getAction().run(w,p3,m,gc));

        }catch (OutOfGraphException e)
        {
            fail("Out of graph should not be thrown here");
        }
        catch (NotAllowedMoveException e)
        {
            fail("cant do this action");
        }

    }

}

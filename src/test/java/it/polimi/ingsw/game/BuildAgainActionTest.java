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
        w.setPos(p1);

        BuildAction ba = new BuildAction();
        BuildAgainAction baa = new BuildAgainAction();

        BehaviourGraph bh = BehaviourGraph.makeEmptyGraph();



        BehaviourNode n = BehaviourNode.makeRootNode(null);
        BehaviourNode c1 = n.setNext(ba);
        c1.setPos(new Vector2(2,2));
        BehaviourNode c2 = c1.setNext(baa);

            ArrayList cells = baa.possibleCells(w,m,gc,c2);
            assertEquals(1, cells.size());

    }

    @Test
    void should_find_7_cells_different_space(){
        GameConstraints gc = new GameConstraints();
        gc.add(GameConstraints.Constraint.BLOCK_SAME_CELL_BUILD); //testing the scenario when you can only build on diff cells
        gc.add(GameConstraints.Constraint.BLOCK_DOME_BUILD);  // you now can't build domes

        Map m = new Map();
        Worker w = new Worker(null);
        Vector2 p1 = new Vector2(3,3);
        w.setPos(p1);

        BehaviourNode n = BehaviourNode.makeRootNode(null);
        BuildAction ba = new BuildAction();
        BuildAgainAction baa = new BuildAgainAction();

        BehaviourNode c1 = n.setNext(ba);
        Vector2 p2 = new Vector2(2,3);
        c1.setPos(p2);
        BehaviourNode c2 = c1.setNext(baa);
        assertEquals(p2, c2.getParent().getPos());
            ArrayList cells = baa.possibleCells(w,m,gc,c2);
            assertEquals(7, cells.size());

    }

    @Test
    void should_find_8_cells() {
        //build again with no constrains
        GameConstraints gc = new GameConstraints();

        Map m = new Map();
        Worker w = new Worker(null);
        Vector2 p1 = new Vector2(3, 3);
        w.setPos(p1);

        BehaviourNode n = BehaviourNode.makeRootNode(null);
        BuildAction ba = new BuildAction();
        BuildAgainAction baa = new BuildAgainAction();

        BehaviourNode c1 = n.setNext(ba);
        Vector2 p2 = new Vector2(0, 0);
        c1.setPos(p2);
        BehaviourNode c2 = c1.setNext(baa);
        assertEquals(p2, c2.getParent().getPos());

        ArrayList cells = baa.possibleCells(w, m, gc, c2);
        assertEquals(8, cells.size());


    }

    @Test
    void buildAgainTest(){
        Map m = new Map();
        Player player = new Player(1,"uno");
        Worker w = new Worker(player);
        Vector2 p1 = new Vector2(3, 3);
        Vector2 p2 = new Vector2(2,3);
        Vector2 p3 = new Vector2(4,3);
        w.setPos(p1);
        m.setWorkers(player);

        BehaviourNode bn = BehaviourNode.makeRootNode(new BuildAction());
        bn.setPos(p2);
        BehaviourNode bm = bn.setNext(new BuildAgainAction());

        GameConstraints gc = new GameConstraints();
        gc.add(GameConstraints.Constraint.BLOCK_SAME_CELL_BUILD);
        //gc.add(GameConstraints.Constraint.BLOCK_DIFF_CELL_BUILD);
        ArrayList<Vector2> cells = bm.getAction().possibleCells(w,m,gc,bm);
        assertEquals(7,cells.size());
        int outcome = 3;
        try {
             outcome = bm.getAction().run(w,p3,m,gc,bm);
            assertEquals(0, outcome);
        }catch (NotAllowedMoveException e){
            System.out.println("cant do this action");
        }
        System.out.println(outcome);






    }

}

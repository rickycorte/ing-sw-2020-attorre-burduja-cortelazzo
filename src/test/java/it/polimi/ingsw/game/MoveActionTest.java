package it.polimi.ingsw.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MoveActionTest {


    @Test
    void should_return_8_cells_no_constraints(){
        // testing the basic scenario
        Player p1 = new Player(1,"uno");
        Worker w = new Worker(p1);
        Vector2 pos = new Vector2(3,3);  // tested with multiple positions
        Vector2 pos2 = new Vector2(4,4);
        w.setPos(pos);
        Map m = new Map();
        MoveAction ma = new MoveAction();
        GameConstraints gc = new GameConstraints();

            ArrayList<Vector2> cells = ma.possibleCells(w,m,gc,null);
            assertEquals(8, cells.size());
            assertTrue(cells.contains(pos2));

    }

    @Test
    void should_return_7_cells_close_workers_no_constrains(){
        // testing, no costraints and 2 workers are close
        Player player1 = new Player(1,"uno");
        Player player2 = new Player(2,"due");

        Worker w1 = new Worker(player1);
        Vector2 p1 = new Vector2(3,3);
        w1.setPos(p1);
        player1.setWorker(w1);

        assertEquals(p1, w1.getPos());

        Worker w2 = new Worker(player2);
        Vector2 p2 = new Vector2(2,2);
        w2.setPos(p2);
        player2.setWorker(w2);

        Map m = new Map();
        m.setWorkers(player1);
        m.setWorkers(player2);

        assertEquals(1, player1.getWorkers().size());

        GameConstraints gc = new GameConstraints();

        MoveAction ma = new MoveAction();


            ArrayList<Vector2>  cells = ma.possibleCells(w1,m,gc, null);
            assertEquals(7, cells.size());

    }
    @Test
    void should_return_8_cells_close_workers_can_swap(){
        // testing swap constraint , two workers are close
        Player player1 = new Player(1,"uno");
        Player player2 = new Player(2,"due");

        Worker w1 = new Worker(player1);
        Vector2 p1 = new Vector2(3,3);
        w1.setPos(p1);
        player1.setWorker(w1);

        assertEquals(p1, w1.getPos());

        Worker w2 = new Worker(player2);
        Vector2 p2 = new Vector2(2,2);
        w2.setPos(p2);
        player2.setWorker(w2);

        Map m = new Map();
        m.setWorkers(player1);
        m.setWorkers(player2);

        assertEquals(1, player1.getWorkers().size());

        GameConstraints gc = new GameConstraints();
        gc.add(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT);

        MoveAction ma = new MoveAction();


            ArrayList<Vector2>  cells = ma.possibleCells(w1,m,gc, null);
            assertEquals(8, cells.size());

    }
    @Test
    void should_return_8_cells_close_workers_can_push(){
        // testing push constraint, two workers are close
        Player player1 = new Player(1,"uno");
        Player player2 = new Player(2,"due");

        Worker w1 = new Worker(player1);
        Vector2 p1 = new Vector2(3,3);
        w1.setPos(p1);
        player1.setWorker(w1);

        assertEquals(p1, w1.getPos());

        Worker w2 = new Worker(player2);
        Vector2 p2 = new Vector2(2,3);
        w2.setPos(p2);
        player2.setWorker(w2);

        Map m = new Map();
        m.setWorkers(player1);
        m.setWorkers(player2);

        assertEquals(1, player1.getWorkers().size());

        GameConstraints gc = new GameConstraints();
        gc.add(GameConstraints.Constraint.CAN_PUSH_CONSTRAINT);

        MoveAction ma = new MoveAction();


            ArrayList<Vector2>  cells = ma.possibleCells(w1,m,gc, null);
            assertEquals(8, cells.size());

    }

    @Test
    void Simple_Move_test(){
        Map m = new Map();
        Player player1 = new Player(1,"uno");
        player1.setGod(new Card(3,"Athena", null));
        Worker w1 = new Worker(player1);
        w1.setPos(new Vector2(3,3));
        player1.setWorker(w1);
        m.setWorkers(player1);

        assertEquals(1, m.getWorkers().size());     // there should be one worker on the map

        Vector2 p2 = new Vector2(2,3);                 // where i would like to move
        GameConstraints gc = new GameConstraints();

        BehaviourNode bn = BehaviourNode.makeRootNode(new MoveAction());
        try {

            int outcome = (bn.getAction()).run(w1, p2, m, gc, bn);
            assertEquals(0, outcome);
            assertEquals(p2, w1.getPos());
            assertTrue(m.isCellEmpty(new Vector2(3,3)));
        }catch (NotAllowedMoveException e){}

    }
    @Test
    void push_swap_test(){
        Map m = new Map();
        Player player1 = new Player(1,"uno");
        Player player2 = new Player(2,"due");
        player1.setGod(new Card(3,"Athena", null));
        player2.setGod(new Card(8,"Pan", null));
        Worker w1 = new Worker(player1);
        Worker w2 = new Worker(player2);
        Vector2 pos1 = new Vector2(3,3);
        w1.setPos(pos1);
        Vector2 pos2 = new Vector2(2,3);
        w2.setPos(pos2);
        player1.setWorker(w1);
        player2.setWorker(w2);
        GameConstraints gc = new GameConstraints();
        m.setWorkers(player2);
        m.setWorkers(player1);
        int outcome;
        BehaviourNode bn = BehaviourNode.makeRootNode(new MoveAction());
        gc.add(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT);
        try{
            assertNotEquals(pos2, w1.getPos());
            outcome = bn.getAction().run(w1,pos2,m,gc,bn);
            assertEquals(0,outcome);
            assertEquals(pos2, w1.getPos());
            assertEquals(pos1, w2.getPos());
        }catch (NotAllowedMoveException e){
            System.out.println("this is not a valid move");
        }
        gc.remove(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT);
        gc.add(GameConstraints.Constraint.CAN_PUSH_CONSTRAINT);
        w1.setPos(pos1);
        w2.setPos(pos2);
        Vector2 push_pos = new Vector2(1,3); // if execution is ok, w2 should be pushed there
        try{
            assertNotEquals(pos2, w1.getPos());
            assertEquals(pos2, w2.getPos());
            outcome = bn.getAction().run(w1,pos2,m,gc,bn);
            assertEquals(0,outcome);
            assertEquals(pos2, w1.getPos());
            assertEquals(push_pos, w2.getPos());
        }catch (NotAllowedMoveException e){
            System.out.println("this is not a valid move");
        }
    }

    @Test
    void block_move_up_test(){
        Map m = new Map();
        Player player1 = new Player(1,"uno");
        player1.setGod(new Card(3,"Athena", null));
        Worker w1 = new Worker(player1);
        Vector2 pos1 = new Vector2(3,3);
        w1.setPos(pos1);
        Vector2 pos2 = new Vector2(2,3);
        player1.setWorker(w1);
        GameConstraints gc = new GameConstraints();
        m.setWorkers(player1);
        int outcome;
        BehaviourNode bn = BehaviourNode.makeRootNode(new MoveAction());
        try{
            m.build(pos2);
            outcome = bn.getAction().run(w1,pos2,m,gc,bn);
            assertEquals(0, outcome);
            assertEquals(pos2, w1.getPos());
            w1.setPos(pos1);
            gc.add(GameConstraints.Constraint.BLOCK_MOVE_UP);
            outcome = bn.getAction().run(w1,pos2,m,gc,bn);

        }catch (CellCompletedException e){
            System.out.println("cell is complete");
        }catch (NotAllowedMoveException e){
            System.out.println("move is not allowed");
        }


    }


}

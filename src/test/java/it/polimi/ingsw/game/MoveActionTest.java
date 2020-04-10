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
        w.setPos(pos);
        Map m = new Map();
        MoveAction ma = new MoveAction();
        GameConstraints gc = new GameConstraints();
        try {
            ArrayList<Vector2> cells = ma.possibleCells(w,m,gc,null);
            assertEquals(8, cells.size());
        }catch (OutOfMapException e){
            System.out.println("i've thrown an exception");
        }
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

        try{
            ArrayList<Vector2>  cells = ma.possibleCells(w1,m,gc, null);
            assertEquals(7, cells.size());
        }catch (OutOfMapException e){
            System.out.println("test exception");
        }
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

        try{
            ArrayList<Vector2>  cells = ma.possibleCells(w1,m,gc, null);
            assertEquals(8, cells.size());
        }catch (OutOfMapException e){
            System.out.println("test exception");
        }
    }
    @Test
    void should_return_8_cells_close_workers_can_push(){
        // testing push costraint, two workers are close
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

        try{
            ArrayList<Vector2>  cells = ma.possibleCells(w1,m,gc, null);
            assertEquals(8, cells.size());
        }catch (OutOfMapException e){
            System.out.println("test exception");
        }
    }

}

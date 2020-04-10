package it.polimi.ingsw.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
public class MoveAgainActionTest {



    @Test
    void possibleCells_with_both_constraints(){

        GameConstraints gc = new GameConstraints();
        Player player1 = new Player(1,"uno");
        Map m = new Map();
        Worker w = new Worker(player1);
        Vector2 p1 = new Vector2(3,3);
        w.setPos(p1);

        BehaviourNode n = BehaviourNode.makeRootNode(null);
        MoveAction ma1 = new MoveAction();
        MoveAction ma2 = new MoveAction();
        MoveAgainAction maa = new MoveAgainAction();

        BehaviourNode c1 = n.setNext(ma1);
        Vector2 p2 = new Vector2(2,3);  //the pos i started the turn in
        c1.setPos(p2);

        BehaviourNode c2 = c1.setNext(ma2);
        BehaviourNode c3 = c2.setNext(maa);
        assertEquals(p2, c2.getParent().getPos());

        gc.add(GameConstraints.Constraint.BLOCK_SAME_CELL_MOVE);
        try {
            ArrayList cells = maa.possibleCells(w,m,gc,c3);
            assertEquals(7, cells.size());
        }catch (OutOfMapException e){
            System.out.println("i've thrown an exception");
        }
        gc.remove(GameConstraints.Constraint.BLOCK_SAME_CELL_MOVE);
        try {
            ArrayList cells = maa.possibleCells(w,m,gc,c3);
            assertEquals(8, cells.size());
        }catch (OutOfMapException e){
            System.out.println("i've thrown an exception");
        }

    }
}

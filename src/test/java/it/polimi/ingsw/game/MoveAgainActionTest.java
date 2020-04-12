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



        /*
        BehaviourNode n = BehaviourNode.makeRootNode(null);
        MoveAction ma1 = new MoveAction();
        MoveAction ma2 = new MoveAction();
        MoveAgainAction maa = new MoveAgainAction();

        BehaviourNode c1 = n.setNext(ma1);
        c1.setPos(p2);

        BehaviourNode c2 = c1.setNext(ma2);
        BehaviourNode c3 = c2.setNext(maa);
        assertEquals(p2, c2.getParent().getPos());


         */
        Vector2 p2 = new Vector2(2,3);  //the pos i started the turn in
        BehaviourNode move = BehaviourNode.makeRootNode(new MoveAction());
        move.setPos(p2);
        BehaviourNode moveAgain = move.setNext(new MoveAgainAction());

        gc.add(GameConstraints.Constraint.BLOCK_SAME_CELL_MOVE);

            ArrayList cells = moveAgain.getAction().possibleCells(w,m,gc,moveAgain);
            assertEquals(7, cells.size());

        gc.remove(GameConstraints.Constraint.BLOCK_SAME_CELL_MOVE);

            cells = moveAgain.getAction().possibleCells(w,m,gc,moveAgain);
            assertEquals(8, cells.size());


    }

    @Test
    void moveAgain_with_both_constraints(){
        Vector2 pos1 = new Vector2(3,3);
        Vector2 pos2 = new Vector2(2,3);
        Vector2 pos3 = new Vector2(1,3);

        Map m = new Map();

        Player player = new Player(1,"uno");
        Worker w = new Worker(player);
        w.setPos(pos1);
        player.setGod(new Card(4,"Athena", null));
        player.setWorker(w);
        m.setWorkers(player);

        BehaviourNode move = BehaviourNode.makeRootNode(new MoveAction());
        move.setPos(pos2);
        BehaviourNode moveAgain = move.setNext(new MoveAgainAction());

        GameConstraints gc = new GameConstraints();
        int outcome;
        try{
            assertEquals(pos1, w.getPos());
            outcome = move.getAction().run(w,pos2,m,gc,moveAgain);
            assertEquals(0,outcome);
            assertEquals(pos2, w.getPos());
            w.setPos(pos1);
            assertEquals(pos1, w.getPos());
            gc.add(GameConstraints.Constraint.BLOCK_SAME_CELL_MOVE);
            //outcome = move.getAction().run(w,pos2,m,gc,moveAgain);
            assertThrows(NotAllowedMoveException.class, () -> {move.getAction().run(w,pos2,m,gc,moveAgain);});



        }catch (NotAllowedMoveException e){
            System.out.println("not allowed");
        }


    }

}

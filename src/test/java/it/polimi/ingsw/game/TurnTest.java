package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TurnTest {
    GameConstraints gc;
    Map map;
    Worker w, w2;
    Player p;
    BehaviourGraph testSeq;
    Card card;
    Turn turn;

    @BeforeEach
    void setup(){

        gc = new GameConstraints();

        //create a map
        map = new Map();
        Vector2 pos = new Vector2(0,0);
        //the closed point is 0,0 so are dome (0,1)(1,0)(1,1)
        map.build(pos);
        pos.set(0,1);
        map.buildDome(pos);
        pos.set(1,0);
        map.buildDome(pos);
        pos.set(1,1);
        map.buildDome(pos);

        //setup players
        p = new Player(1, "first");
        w = new Worker(0, p, new Vector2(0,0));
        w2 = new Worker(1, p, new Vector2(5,5));

        p.addWorker(w);
        p.addWorker(w2);

        // create card
        testSeq = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction())
                        .setNext(new BuildAction())
                        .getRoot()
        );

        card = new Card(1, "first", testSeq);
        //final setup
        map.setWorkers(p);
        p.setGod(card);
        turn = new Turn(p);
    }

    @Test
    void shouldBeAbleToMove()
    {
        //worker set to be one in the closed point one free to move, turn have movement possibility
        assertTrue(turn.canStillMove(map, gc));

    }

    @Test
    void shouldNotBeAbleToMove()
    {
        //worker 2 is removed
        map.getWorkers().remove(1);
        p.getWorkers().remove(1);

        //the only worker now has no movement possibility
        assertFalse(turn.canStillMove(map,gc));
    }


    // no more relevant functions must be tested because they are just
    // "redirects" of internal function calls
    // if they pass their own tests then results will be correct

}

package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TurnTest {
    Map map;

    @BeforeEach
    void createMap(){
        //create a map with a closed point
        map = new Map();
        Vector2 pos = new Vector2(0,0);
        //the closed point is 0,0 so are dome (0,1)(1,0)(1,1)
        try {
            map.build(pos);
            pos.set(0,1);
            map.buildDome(pos);
            pos.set(1,0);
            map.buildDome(pos);
            pos.set(1,1);
            map.buildDome(pos);
            map.writeMapOut("map00closed.bin");
            //map with (0,0) closed point saved
        }catch(CellCompletedException e){
            fail("Should never arrive");
        }
    }

    @Test
    void CanStillMoveTestSimpleGraph() {
        Player p = new Player(1, "first");

        Worker w = new Worker(p);
        Vector2 pos = new Vector2(0, 0);

        Worker w2 = new Worker(p);
        Vector2 pos2 = new Vector2(5,5);

        GameConstraints gc = new GameConstraints();

        BehaviourGraph testSeq = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction())
                        .setNext(new BuildAction())
                        .getRoot()
        );

        Card card = new Card(1, "first", testSeq);

        map.readMapOut("map00closed.bin");
        p.setGod(card);
        p.addWorker(w);
        p.addWorker(w2);
        map.setWorkers(p);
        w.setPosition(pos);
        w2.setPosition(pos2);
        Turn turn = new Turn(p);

        //worker settend to be one in the closed point one free to move, turn have movement possibility
        assertTrue(turn.canStillMove(map, gc));

        //worker 2 is removed
        map.getWorkers().remove(1);
        p.getWorkers().remove(1);

        //the only worker now has no movement possibility
        assertFalse(turn.canStillMove(map,gc));
    }

    @Test
    void CanStillMoveTestAdvancedGraph(){
        Player p = new Player(1, "first");

        Worker w = new Worker(p);
        Vector2 pos = new Vector2(0, 0);

        Worker w2 = new Worker(p);
        Vector2 pos2 = new Vector2(5,5);

        GameConstraints gc = new GameConstraints();

        BehaviourGraph testInitOR = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction())
                        .addBranch(new BuildAction())
                        .getRoot()
        ).appendSubGraph(
                BehaviourNode.makeRootNode(new BuildAction())
                        .addBranch(new BuildAction())
                        .getRoot()
        );

        Card card = new Card(1, "first", testInitOR);

        map.readMapOut("map00closed.bin");
        p.setGod(card);
        p.addWorker(w);
        p.addWorker(w2);
        map.setWorkers(p);
        w.setPosition(pos);
        w2.setPosition(pos2);
        Turn turn = new Turn(p);

        //worker settend to be one in the closed point one free to move, turn have movement possibility
        assertTrue(turn.canStillMove(map, gc));

        //worker 2 is removed
        map.getWorkers().remove(1);
        p.getWorkers().remove(1);

        //the only worker now has no movement possibility
        assertFalse(turn.canStillMove(map,gc));
    }
}

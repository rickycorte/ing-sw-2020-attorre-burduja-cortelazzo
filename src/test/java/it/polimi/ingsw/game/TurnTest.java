package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

class TurnTest
{
    GameConstraints gc;
    Map map;
    Worker w, w2;
    Player p;
    BehaviourGraph testSeq;
    Card card;
    Turn turn;

    @BeforeEach
    void setup()
    {

        gc = new GameConstraints();

        //create a map
        map = new Map();
        Vector2 pos = new Vector2(0, 0);
        //the closed point is 0,0 so are dome (0,1)(1,0)(1,1)
        map.build(pos);
        pos.set(0, 1);
        map.buildDome(pos);
        pos.set(1, 0);
        map.buildDome(pos);
        pos.set(1, 1);
        map.buildDome(pos);

        //setup players
        p = new Player(1, "first");
        w = new Worker(0, p, new Vector2(0, 0));
        w2 = new Worker(1, p, new Vector2(5, 5));

        p.addWorker(w);
        p.addWorker(w2);

        // create card
        testSeq = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction())
                        .setNext(new BuildAction())
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
        assertFalse(turn.canStillMove(map, gc));
    }


    @Test
    void shouldNotAddUndoActionIfDisabled() throws NotAllowedMoveException, OutOfGraphException
    {
        w.setPosition(new Vector2(3, 3)); // move in a free space
        // first move
        assertEquals(1, turn.getNextAction(w, map, gc).size());
        turn.selectWorker(0);
        turn.runAction(0, new Vector2(3, 2), map, gc);

        // second move
        assertEquals(1, turn.getNextAction(w, map, gc).size());
        turn.runAction(0, new Vector2(3, 3), map, gc);
    }


    @Test
    void shouldNotAddUndoActionOnFirstMove()
    {
        turn = new Turn(p, true);
        w.setPosition(new Vector2(3, 3)); // move in a free space
        assertEquals(1, turn.getNextAction(w, map, gc).size());
    }

    @Test
    void shouldAddUndoOnNotFirstMoves() throws NotAllowedMoveException, OutOfGraphException
    {

        turn = new Turn(p, true);
        w.setPosition(new Vector2(3, 3)); // move in a free space
        // first move
        assertEquals(1, turn.getNextAction(w, map, gc).size());
        turn.selectWorker(0);
        turn.runAction(0, new Vector2(3, 2), map, gc);

        // second move (we expect undo only here!)
        assertEquals(2, turn.getNextAction(w, map, gc).size());
        turn.runAction(0, new Vector2(3, 3), map, gc);

        // third action should have undo too
        assertEquals(2, turn.getNextAction(w, map, gc).size());
    }


    boolean isMapEquals(Map m1, Map m2)
    {
        for (int i = 0; i < Map.LENGTH; i++)
        {
            for (int j = 0; j < Map.HEIGHT; j++)
                if (m1.getMap()[i][j] != m2.getMap()[i][j])
                    return false;
        }
        return true;
    }

    @Test
    void shouldUndoMoveAndResetToRootIfSecondAction() throws NotAllowedMoveException, OutOfGraphException
    {
        turn = new Turn(p, true);
        w.setPosition(new Vector2(3, 3)); // move in a free space
        turn.selectWorker(0);

        turn.runAction(0, new Vector2(3, 2), map, gc);

        // worker pos changes
        assertTrue(!w.getPosition().equals(new Vector2(3, 3)));

        // only one pos in undo
        assertEquals(1, turn.getNextAction(map, gc).get(1).getAvailablePositions().size());
        // undo should point to the last target pos
        assertEquals(new Vector2(3, 2), turn.getNextAction(map, gc).get(1).getAvailablePositions().get(0));
        // undo should point to selected worker
        assertEquals(w.getId(), turn.getNextAction(map, gc).get(1).getWorkerID());

        assertEquals(0, turn.runAction(1, new Vector2(3, 2), map, gc)); // run undo (0 is build)

        //worker rest
        assertTrue(w.getPosition().equals(new Vector2(3, 3)));

        //check if we went back to root
        assertTrue(p.getGod().getGraph().isAtRoot());
    }

    @Test
    void shouldUndoAndNotResetToRootIfThirdActionOrLater() throws NotAllowedMoveException, OutOfGraphException
    {
        turn = new Turn(p, true);
        w.setPosition(new Vector2(3, 3)); // move in a free space
        turn.selectWorker(0);

        turn.runAction(0, new Vector2(3, 2), map, gc); // move

        Map oldMap = new Map(map); // save map before build
        BehaviourNode oldNode = p.getGod().getGraph().getCurrentNode();

        turn.runAction(0, new Vector2(3, 3), map, gc); // build

        assertFalse(isMapEquals(oldMap, map)); // map changed

        assertEquals(0, turn.runAction(1, new Vector2(3, 3), map, gc)); // run undo (0 is build)
        assertEquals(new Vector2(3, 2), w.getPosition());
        assertTrue(isMapEquals(oldMap, map));
        assertEquals(oldNode, p.getGod().getGraph().getCurrentNode());
    }


    @Test
    void shouldNotAddUndoActionIfFirstMoveFail()
    {
        turn = new Turn(p, true);
        w.setPosition(new Vector2(3, 3)); // move in a free space
        turn.selectWorker(0);
        assertThrows(NotAllowedMoveException.class, () -> {
            turn.runAction(0, new Vector2(6, 6), map, gc);
        }); // this run fails
        assertEquals(1, turn.getNextAction(map, gc).get(1).getAvailablePositions().size()); // no undo only move

    }


    @Test
    void shouldNotUndoTwiceTheSameAction() throws NotAllowedMoveException, OutOfGraphException
    {
        turn = new Turn(p, true);
        w.setPosition(new Vector2(3, 3)); // move in a free space
        turn.selectWorker(0);

        turn.runAction(0, new Vector2(3, 2), map, gc); // move
        turn.runAction(1, new Vector2(3, 2), map, gc); // undo

        turn.selectWorker(0); // reselect worker
        // no undo -> size 1
        assertEquals(1, turn.getNextAction(map,gc).size());
        // throw on action fail
        assertThrows(NotAllowedMoveException.class, ()->{turn.runAction(1, new Vector2(3, 2), map, gc);});
    }

    @Test
    void shouldNotUndoOlderActions() throws NotAllowedMoveException, OutOfGraphException
    {
        turn = new Turn(p, true);
        w.setPosition(new Vector2(3, 3)); // move in a free space
        turn.selectWorker(0);

        turn.runAction(0, new Vector2(3, 2), map, gc); // move
        turn.runAction(0, new Vector2(3, 3), map, gc); // build
        turn.runAction(0, new Vector2(3, 3), map, gc); // build again

        turn.runAction(0, new Vector2(3, 3), map, gc); // undo last build

        // no undo for first build -> size 1
        assertEquals(1, turn.getNextAction(map,gc).size());

        //redo
        turn.runAction(0, new Vector2(3, 3), map, gc); // build again
        // fail attempt to undo on an already used node
        assertThrows(NotAllowedMoveException.class, ()->{turn.runAction(0, new Vector2(3, 3), map, gc);});
    }


    @Test
    void shouldNotUndoAfterTimeout() throws NotAllowedMoveException, OutOfGraphException, InterruptedException, ReflectiveOperationException
    {
        turn = new Turn(p, true);
        w.setPosition(new Vector2(3, 3)); // move in a free space
        turn.selectWorker(0);

        turn.runAction(0, new Vector2(3, 2), map, gc); // move
        turn.getNextAction(map, gc);

        Thread.sleep(Turn.MAX_UNDO_MILLI + 2);
        assertThrows(NotAllowedMoveException.class, ()->{turn.runAction(1, new Vector2(3, 3), map, gc);});
    }
}

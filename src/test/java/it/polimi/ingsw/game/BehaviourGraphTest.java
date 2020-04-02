package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BehaviourGraphTest
{
    private class TestAction extends Action {

        GameConstraints.Constraint local_constr;


        public TestAction(GameConstraints.Constraint local_constr)
        {
            display_name = "test_action "+ local_constr.toString();
            this.local_constr = local_constr;
        }

        @Override
        public int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException
        {
            globalConstrains.add(local_constr);
            return 0;
        }

        public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc, BehaviourNode node) throws OutOfMapException{
            return null;
        }
    }

    BehaviourGraph testSeq;

    @BeforeEach
    void init()
    {
        testSeq = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new TestAction(GameConstraints.Constraint.BLOCK_MOVE_UP))
                        .setNext(new TestAction(GameConstraints.Constraint.BLOCK_MOVE_UP))
                        .getRoot()
        );
    }

    @Test
    void shouldEndAndResetExecution()
    {
        assertFalse(testSeq.isExecutionEnded());

        try
        {
            testSeq.selectAction(0);
            testSeq.selectAction(0);
            assertTrue(testSeq.isExecutionEnded());

            //reset
            testSeq.resetExecutionStatus();
            assertFalse(testSeq.isExecutionEnded());

        }
        catch (Exception e) {
            fail("There should be an action for every node if execution is not ended");
        }

    }

    @Test
    void shouldSelectActionAndRun()
    {
        assertThrows(OutOfGraphException.class, () -> {testSeq.selectAction(-1);});
        assertThrows(OutOfGraphException.class, () -> {testSeq.selectAction(2);});
        assertThrows(OutOfGraphException.class, () -> {testSeq.selectAction(18);});

        try
        {
            GameConstraints c = new GameConstraints();
            testSeq.selectAction(0); // select text action
            testSeq.runSelectedAction(null, null, null, c);
            assertTrue(c.check(GameConstraints.Constraint.BLOCK_MOVE_UP));
            //refer to upper testAction class to understand why this check is made make sure the function is run

            c.clear();
            testSeq.runSelectedAction(null, null, null, c); // double run this should be skipped
            assertFalse(c.check(GameConstraints.Constraint.BLOCK_MOVE_UP));

            testSeq.selectAction(0); // move to graph end
            testSeq.runSelectedAction(null, null, null, c); // nothing should happen
            assertFalse(c.check(GameConstraints.Constraint.BLOCK_MOVE_UP));


        }catch (Exception e){
            fail("There should be an action for every node if execution is not ended");
        }

    }

    @Test
    void shouldAppendSubGraph()
    {
        testSeq.resetExecutionStatus();
        assertEquals(1, testSeq.getNextActionNames().length);

        BehaviourNode branch = BehaviourNode.makeRootNode(new TestAction(GameConstraints.Constraint.BLOCK_MOVE_UP))
                .setNext(new BuildAction())
                .getRoot();

        testSeq.appendSubGraph(branch);
        testSeq.appendSubGraph(null);

        assertEquals(2, testSeq.getNextActionNames().length);
        assertEquals(branch.getAction().displayName(), testSeq.getNextActionNames()[1]);

    }

    @Test
    void shouldMakeEmptyGraph()
    {
        BehaviourGraph g = BehaviourGraph.makeEmptyGraph();
        assertNotEquals(null, g);
        assertEquals(0, g.getNextActionNames().length);
        assertTrue(g.isExecutionEnded());
    }
}
package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BehaviourGraphTest
{
    // class to test correct action behaviour on graph
    private class TestAction extends Action {

        GameConstraints.Constraint local_constr;

        public TestAction()
        {
            this.local_constr = GameConstraints.Constraint.NONE;
        }

        public TestAction(GameConstraints.Constraint local_constr)
        {
            displayName = "test_action "+ local_constr.toString();
            this.local_constr = local_constr;
        }

        @Override
        public int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException
        {
            if(globalConstrains != null) globalConstrains.add(local_constr);
            return 309217;
        }

        public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc) {
            var list = new ArrayList<Vector2>();
            list.add(new Vector2(0,0));
            return list;
        }
    }

    // class to test wrong moves on graph behaviour
    private class TestActionThrow extends Action {


        @Override
        public int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException
        {
            throw new NotAllowedMoveException();
        }

        public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc) {
            return null;
        }
    }

    //**********************************************************************************************************************************************************************

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
    void shouldThrowWithBrokenSelectionIndex()
    {
        assertThrows(OutOfGraphException.class, () -> {testSeq.selectAction(-1);});
        assertThrows(OutOfGraphException.class, () -> {testSeq.selectAction(2);});
        assertThrows(OutOfGraphException.class, () -> {testSeq.selectAction(18);});
    }

    @Test
    void shouldSelectActionAndRun()
    {
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
            testSeq.runSelectedAction(null, null, null, c);
            assertTrue(c.check(GameConstraints.Constraint.BLOCK_MOVE_UP));


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


    // for next action tests
    // the test are good because all actions are used only from their base class interface
    // so an action or another its fine as the are able to pass their own tests

    // helper to keep test code clean
    // return the name of the action located in the node id in the branch of graph g
    String getActionName(BehaviourGraph g, int id)
    {
        try
        {

            return g.getBehaviourNode().getNextNode(id).getAction().displayName();
        }
        catch (OutOfGraphException e)
        {
            return null; // break tests :3
        }
    }


    @Test
    void shouldReturnNextActionsWithOneChild()
    {
        Worker w1 = new Worker(0, null, null);
        // we use directly text action to get a copy of allowed moves because its fixed (see implementation at the beginning)
        TestAction ta = new TestAction();

        var actions = testSeq.getNextActions(w1, null, null);
        assertEquals(1, actions.size());
        assertEquals(getActionName(testSeq,0), actions.get(0).getActionName());
        assertEquals(ta.possibleCells(null,null, null), actions.get(0).getAvailable_position());
    }

    @Test
    void shouldReturnNextActionsWithMoreBranches()
    {
        Worker w1 = new Worker(0, null, null);

        // we use directly text action to get a copy of allowed moves because its fixed (see implementation at the beginning)
        TestAction ta = new TestAction();

        testSeq.appendSubGraph(BehaviourNode.makeRootNode(new TestAction()))
                .appendSubGraph(BehaviourNode.makeRootNode(new TestAction())); // 3 branches

        var actions = testSeq.getNextActions(w1, null, null);
        assertEquals(3, actions.size());



        //check all branches
        assertEquals(getActionName(testSeq,0), actions.get(0).getActionName());
        assertEquals(ta.possibleCells(null,null, null), actions.get(0).getAvailable_position());

        assertEquals(getActionName(testSeq,1), actions.get(1).getActionName());
        assertEquals(ta.possibleCells(null,null, null), actions.get(1).getAvailable_position());

        assertEquals(getActionName(testSeq,2), actions.get(2).getActionName());
        assertEquals(ta.possibleCells(null,null, null), actions.get(2).getAvailable_position());

    }

    @Test
    void shouldNotMoveForwardOnActionError()
    {
        BehaviourGraph graph = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new TestActionThrow()).setNext(new TestAction()).getRoot());

        BehaviourNode prevAction = graph.getBehaviourNode();

        try{
            graph.selectAction(0);
            graph.runSelectedAction(null, null, null, null);
        }
        catch (OutOfGraphException e)
        {
            fail("Unexpected exception");
        }
        catch (NotAllowedMoveException ignored)
        {
            // 100% sure we go here because TestActionThrow always trows this exception
        }

        assertEquals(prevAction, graph.getBehaviourNode());

    }

    @Test
    void shouldRunEveryAction()
    {
        BehaviourGraph graph = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new TestAction()).setNext(new TestAction()).getRoot());
        try{
            graph.selectAction(0);
            // we check 309217 because run of TestAction returns that fixed and unusual number
            assertEquals(309217, graph.runSelectedAction(null, null, null, null));

            graph.selectAction(0);
            assertEquals(309217, graph.runSelectedAction(null, null, null, null));

        }
        catch (OutOfGraphException e)
        {
            fail("Unexpected out of graph");
        }
        catch (NotAllowedMoveException e)
        {
            fail("Unexpected not allowed move");
        }
    }
}
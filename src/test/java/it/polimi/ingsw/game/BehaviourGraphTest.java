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
            display_name = "test_action "+ local_constr.toString();
            this.local_constr = local_constr;
        }

        @Override
        public int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException
        {
            if(globalConstrains != null) globalConstrains.add(local_constr);
            return 309217;
        }

        public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc) {
            return null;
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

    @Test
    void testNextActions() throws OutOfGraphException {
        Player p = new Player(0, "first");
        Worker w1 = new Worker(p);
        Worker w2 = new Worker(p);
        Map map = new Map();
        GameConstraints c = new GameConstraints();
        Vector2 posw1 = new Vector2(1,1);
        Vector2 posw2 = new Vector2(1,2);

        map.readMapOut("map.bin");

        w1.setPosition(posw1);
        p.addWorker(w1);
        w2.setPosition(posw2);
        p.addWorker(w2);

        map.setWorkers(p);

        BehaviourGraph testInitOR = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction())
                        .addBranch(new BuildAction())
                        .addBranch(new MoveAction())
                        .getRoot()
        ).appendSubGraph(
                BehaviourNode.makeRootNode(new BuildAction())
                        .addBranch(new BuildAgainAction())
                        .getRoot()
        );
        testInitOR.resetExecutionStatus();
        ArrayList<NextAction> nextActions = new ArrayList<>(testInitOR.getNextActions(w1, map, c)) ;

        assertEquals(2 , nextActions.size());
        assertEquals("MoveNONE",(nextActions.get(0)).getAction_name());
        assertEquals("BuildNONE",(nextActions.get(1)).getAction_name());

        testInitOR.selectAction(0);
        nextActions.clear();
        nextActions = testInitOR.getNextActions(w1,map,c);
        assertEquals(2 , nextActions.size());
        assertEquals("BuildNONE", nextActions.get(0).getAction_name());
        assertEquals("MoveNONE", nextActions.get(1).getAction_name());

        testInitOR.resetExecutionStatus();
        testInitOR.selectAction(1);
        nextActions.clear();
        nextActions = testInitOR.getNextActions(w1,map,c);
        assertEquals(1, nextActions.size());
        assertEquals("BuildAgainNONE",nextActions.get(0).getAction_name());
        //


        BehaviourGraph test = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction())
                        .addBranch(new BuildAction())
                        .addBranch(new MoveAction())
                        .getRoot()
        );
        test.resetExecutionStatus();
        ArrayList<NextAction> nextActions2 = test.getNextActions(w1, map, c);

        assertEquals(1,nextActions2.size());
        assertEquals("MoveNONE",nextActions2.get(0).getAction_name());

        test.selectAction(0);
        nextActions2.clear();
        nextActions2 = test.getNextActions(w1,map,c);

        assertEquals(2, nextActions2.size());
        assertEquals("BuildNONE",nextActions2.get(0).getAction_name());
        assertEquals("MoveNONE", nextActions2.get(1).getAction_name());



        BehaviourGraph testMiddleOr = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction())
                        .addBranch(new BuildAction())
                        .addBranch(new MoveAction())
                        .mergeBranches(new MoveAction())
                        .getRoot()
        );

        nextActions = testMiddleOr.getNextActions(w1,map,c);

        assertEquals(1,nextActions.size());
        assertEquals("MoveNONE",nextActions.get(0).getAction_name());

        nextActions.clear();
        testMiddleOr.selectAction(0);
        nextActions = testMiddleOr.getNextActions(w1,map,c);
        assertEquals(2,nextActions.size());
        assertEquals("BuildNONE", nextActions.get(0).getAction_name());
        assertEquals("MoveNONE",nextActions.get(1).getAction_name());

        nextActions.clear();
        testMiddleOr.selectAction(0);
        nextActions = testMiddleOr.getNextActions(w1,map,c);
        assertEquals(1,nextActions.size());
        assertEquals("MoveNONE",nextActions.get(0).getAction_name());

    }


    @Test
    void shouldNotMoveForwardOnActionError()
    {
        BehaviourGraph graph = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new TestActionThrow()).setNext(new TestAction()).getRoot());

        BehaviourNode prevAction = graph.getCurrent_node();

        try{
            graph.selectAction(0);
            graph.runSelectedAction(null, null, null, null);
        }catch (OutOfGraphException e)
        {
            fail("Unexpected exception");
        }
        catch (NotAllowedMoveException ignored)
        {
            // 100% sure we go here because TestActionThrow always trows this exception
        }

        assertEquals(prevAction, graph.getCurrent_node());

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

        }catch (OutOfGraphException e)
        {
            fail("Unexpected out of graph");
        }
        catch (NotAllowedMoveException e)
        {
            fail("Unexpected not allowed move");
        }
    }
}
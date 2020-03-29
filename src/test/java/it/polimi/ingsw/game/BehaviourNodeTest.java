package it.polimi.ingsw.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BehaviourNodeTest
{


    @Test
    void getNextActionNames()
    {
        BehaviourNode n = BehaviourNode.makeRootNode(null);

        assertEquals(0, n.getNextActionNames().length);
        assertTrue(n.getNextActionNames().length == n.getNextActionCount());

        MoveAction m1 = new MoveAction();
        BuildAction b1 = new BuildAction();
        n.addBranch(m1).addBranch(b1);

        assertEquals(2, n.getNextActionNames().length);
        assertTrue(n.getNextActionNames().length == n.getNextActionCount());

        assertEquals(m1.displayName(), n.getNextActionNames()[0]);
        assertEquals(b1.displayName(), n.getNextActionNames()[1]);
    }

    @Test
    void shouldGetNextNode()
    {
        BehaviourNode n = BehaviourNode.makeRootNode(null);
        MoveAction m1 = new MoveAction(), m2 = new MoveAction(), m3 = new MoveAction();
        BehaviourNode innerNode = BehaviourNode.makeRootNode(m2);
        n.addBranch(m1).addBranch(innerNode).addBranch(m3);

        // check out of range
        assertThrows(OutOfGraphException.class, () -> n.getNextNode(-1));
        assertThrows(OutOfGraphException.class, () -> n.getNextNode(3));
        assertThrows(OutOfGraphException.class, () -> n.getNextNode(10));

        //check real values
        try
        {
            assertEquals(m1, n.getNextNode(0).getAction());
            assertEquals(innerNode, n.getNextNode(1));
            assertEquals(m3, n.getNextNode(2).getAction());
        }
        catch(Exception e) {
            // already tested exception we should not reach here
            fail("Unexpected exception: "+ e.getClass().toString() + " - " + e.getMessage());
        }
    }

    @Test
    void shouldMakeRootNode()
    {
        BehaviourNode n = BehaviourNode.makeRootNode(null);
        assertEquals(null, n.getParent());
        assertEquals(null, n.getAction());

        MoveAction ma = new MoveAction();
        n = BehaviourNode.makeRootNode(ma);
        assertEquals(null, n.getParent());
        assertEquals(ma, n.getAction());

        assertEquals(n, n.getRoot());
    }

    @Test
    void shouldAddBranch()
    {
        BehaviourNode n = BehaviourNode.makeRootNode(null);
        assertEquals(0, n.getNextActionCount());

        MoveAction m1 = new MoveAction(), m2 = new MoveAction();
        BehaviourNode ck = n.addBranch(m1).addBranch(m2);

        assertEquals(ck, n);
        assertEquals(2, n.getNextActionCount());

        try
        {
            assertEquals(m1, n.getNextNode(0).getAction());
            assertEquals(n, n.getNextNode(0).getParent());

            assertEquals(m2, n.getNextNode(1).getAction());
            assertEquals(n, n.getNextNode(1).getParent());
        }
        catch(Exception e) {
            // already tested exception we should not reach here
            fail("Unexpected exception: "+ e.getClass().toString() + " - " + e.getMessage());
        }

    }

    @Test
    void mergeBranches()
    {
        BehaviourNode r = BehaviourNode.makeRootNode(null);
        MoveAction m1 = new MoveAction(), m2 = new MoveAction(), end = new MoveAction();

        r.addBranch(BehaviourNode.makeRootNode(new BuildAction()).setNext(m1).getRoot());
        r.addBranch(m2);

        BehaviourNode b = r.mergeBranches(end);

        assertNotEquals(r, b);
        assertEquals(end, b.getAction());
        try{
            assertEquals(1, r.getNextNode(0).getNextNode(0).getNextActionCount());
            assertEquals(1, r.getNextNode(1).getNextActionCount());

            assertEquals(end, r.getNextNode(0).getNextNode(0).getNextNode(0).getAction());
            assertEquals(end, r.getNextNode(1).getNextNode(0).getAction());
        }
        catch(Exception e) {
            // already tested exception we should not reach here
            fail("Unexpected exception: "+ e.getClass().toString() + " - " + e.getMessage());
        }

    }


    @Test
    void setNext()
    {
        BehaviourNode n = BehaviourNode.makeRootNode(null);
        MoveAction m1 = new MoveAction(), m2 = new MoveAction();

        BehaviourNode ck = n.setNext(m2);

        assertEquals(1, n.getNextActionCount());
        assertNotEquals(n, ck);

        try
        {
            assertEquals(0, ck.getNextActionCount());
            assertEquals(m2, n.getNextNode(0).getAction());
            assertEquals(n, ck.getParent());
        }
        catch(Exception e) {
            // already tested exception we should not reach here
            fail("Unexpected exception: "+ e.getClass().toString() + " - " + e.getMessage());
        }

    }


    @Test
    void getRoot()
    {
        BehaviourNode r = BehaviourNode.makeRootNode(null);
        assertEquals(r, r.getRoot());

        BehaviourNode ck = r.addBranch(new MoveAction()).addBranch(new MoveAction()).mergeBranches(new BuildAction()).setNext(new MoveAction()).getRoot();
        assertEquals(r, ck);
    }
}
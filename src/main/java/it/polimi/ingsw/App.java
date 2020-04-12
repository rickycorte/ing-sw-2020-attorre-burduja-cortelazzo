package it.polimi.ingsw;

import it.polimi.ingsw.game.*;

import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{

    public static void testGraph(BehaviourGraph g)
    {
        Scanner in = new Scanner(System.in);
        int n = 0;

        while (!g.isExecutionEnded())
        {

            String s = "";
            for (int i =0; i< g.getNextActionNames().length; i++)
            {
                s += i + " - " + g.getNextActionNames()[i]+ "\n";
            }

            System.out.print("\n@ Chose:\n" + s);
            System.out.println("What you want to do:");
            n = in.nextInt();
            try
            {
                g.selectAction(n);
                g.runSelectedAction(null, null, null, null);
            }catch (Exception e) {
                System.out.println("Ops something went wrong: " + e.getClass().toString());
            }

        }

        System.out.println("Done :e\n");
    }

    public static void main( String[] args )
    {


        BehaviourGraph testSeq = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction())
            .setNext(new BuildAction())
            .getRoot()
        );

        BehaviourGraph testMiddleOr = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction())
                        .addBranch(new BuildAction())
                        .addBranch(new MoveAction())
                        .mergeBranches(new MoveAction())
                        .getRoot()
        );

        BehaviourGraph testInitOR = BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction())
                        .addBranch(new BuildAction())
                        .getRoot()
        ).appendSubGraph(
                BehaviourNode.makeRootNode(new BuildAction())
                        .addBranch(new BuildAction())
                        .getRoot()
        );

        testGraph(testSeq);
        testGraph(testMiddleOr);
        testGraph(testInitOR);


    }
}

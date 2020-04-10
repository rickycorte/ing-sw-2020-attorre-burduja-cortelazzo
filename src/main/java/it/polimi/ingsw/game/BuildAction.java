package it.polimi.ingsw.game;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * This action allows players to build structures around the map
 */
public class BuildAction extends Action
{
    GameConstraints localConstrains;

    BuildAction(GameConstraints.Constraint localConstrains)
    {
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(localConstrains);
        display_name = "Build";
    }

    public BuildAction() {
        this(GameConstraints.Constraint.NONE);
    }

    /**
     *
     * @param w target worker used in this action
     * @param target target position where the action should take place
     * @param m map where the action is executed
     * @param gc collection of Constraints
     * @param node where the action is waking place
     * @return 1 if i won, 0 to continue, -1 if i cant build anywhere, -2 if i cant build in target pos
     * @throws NotAllowedMoveException
     */
    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints gc, BehaviourNode node) throws NotAllowedMoveException
    {
        try {
            ArrayList<Vector2> cells = node.getAction().possibleCells(w,m,gc,node);
            if(cells.size() != 0) {
                if (cells.contains(target)) {
                    m.build(target);
                    node.setPos(target); //will be used in case of a second build
                } else return -2;

            }else return -1;
        }catch (CellCompletedException e){
            e.printStackTrace();
        }catch (OutOfMapException e1){
            e1.printStackTrace();
        }

        return 0;
    }

    /**
     *
     * @param w worker doing the job
     * @param m current map
     * @param gc list of constraints
     * @param node won't be used for now (getting a null is ok)
     * @return an ArrayList of Vector2 objects, representing all the possible cells i can run the action
     * @throws OutOfMapException if the cell i'm checking is outside the map
     */
    @Override
    public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc, BehaviourNode node) throws OutOfMapException{
        ArrayList<Vector2> cells = new ArrayList<>();
        int x = w.getPos().getX();
        int y = w.getPos().getY();
        for( int i = (x-1); i <= (x+1); i++){
            for( int j = (y-1); j <= (y+1); j++) {
                Vector2 temp = new Vector2(i, j);
                if (i != x || j != y) {
                    try {
                        if (m.isInsideMap(temp) && !(m.isCellDome(temp)) && (m.isCellEmpty(temp))) {
                            cells.add(temp);
                        }
                    }catch (OutOfMapException e){
                        System.out.println("i've thrown an exception");
                    }
                }
            }
        }
        return cells;
    }
}

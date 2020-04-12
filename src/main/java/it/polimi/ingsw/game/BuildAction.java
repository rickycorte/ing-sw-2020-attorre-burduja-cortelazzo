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


    private NotAllowedMoveException NotAllowedMoveException;

    BuildAction(GameConstraints.Constraint localConstrains)
    {
        this.net_id = 20;
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(localConstrains);
        display_name = "Build" + localConstrains.toString();
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
     * @param current_node where the action is waking place
     * @return 1 = I won, 0 = continue, -1 = I lost (i have no possibleCells options)
     * @throws NotAllowedMoveException ex. target is not valid
     */
    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints gc, BehaviourNode current_node) throws NotAllowedMoveException {
        try {
            ArrayList<Vector2> cells = current_node.getAction().possibleCells(w, m, gc, current_node);
            if (cells.size() != 0) {
                if (cells.contains(target)) {
                    m.build(target);
                    current_node.setPos(target); //will be used in case of a second build
                } else {
                    throw new NotAllowedMoveException();
                }
            } else
                return -1;
        } catch (CellCompletedException e) {
            throw new NotAllowedMoveException();
        }
        return 0;
    }

    /**
     * @param w worker doing the job
     * @param m current map
     * @param gc list of constraints
     * @param node won't be used for now (getting a null is ok)
     * @return an ArrayList of Vector2 objects, representing all the possible cells i can run the action
     */
    @Override
    public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc, BehaviourNode node) {
        ArrayList<Vector2> cells = new ArrayList<>();
        int my_x = w.getPos().getX();
        int my_y = w.getPos().getY();
        for (int i = (my_x - 1); i <= (my_x + 1); i++) {
            for (int j = (my_y - 1); j <= (my_y + 1); j++) {
                Vector2 temp = new Vector2(i, j);
                if (i != my_x || j != my_y) {
                    if (m.isInsideMap(temp) && !(m.isCellDome(temp)) && (m.isCellEmpty(temp))) {
                        cells.add(temp);
                    }
                }
            }
        }
        return cells;
    }
}

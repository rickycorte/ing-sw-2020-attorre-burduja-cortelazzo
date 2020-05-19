package it.polimi.ingsw.game;

import java.util.ArrayList;

/**
 * This action allows players to build structures around the map
 */
public class BuildAction extends Action
{
    GameConstraints localConstrains;

    public BuildAction() {

        this.localConstrains = new GameConstraints();
        displayName = "Build";
    }


    /**
     * Build into a cell
     * @param w worker that performs the build
     * @param target target position where build
     * @param m current map
     * @param gc constraints to apply
     */
    protected void build(Worker w, Vector2 target, Map m, GameConstraints gc)
    {
        m.build(target);
        w.setLastBuildLocation(target); //will be used in case of a second build
    }

    /**
     * Apply build action to a cell and update game state
     * @param w target worker used in this action
     * @param target target position where the action should take place
     * @param m map where the action is executed
     * @param gc collection of Constraints
     * @return 1 = I won, 0 = continue, -1 = I lost (i have no possibleCells options)
     * @throws NotAllowedMoveException ex. target is not valid
     */
    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints gc) throws NotAllowedMoveException {

        GameConstraints gcm = mergeConstraints(localConstrains, gc);

        ArrayList<Vector2> allowed_cells = possibleCells(w, m, gcm);
        if (allowed_cells.size() == 0)
            return -1;

        if (allowed_cells.contains(target))
            build(w, target, m, gc);
        else
            throw new NotAllowedMoveException();
        return 0;
    }

    /**
     * Check a move (valid place position) is allowed or not
     * @param w current worker
     * @param target target build position
     * @param m current map
     * @param gc constraints to apply (could be used in overloads)
     * @return true if a build can performed in target cell
     */
    protected boolean isValidMove(Worker w, Vector2 target, Map m, GameConstraints gc)
    {
        return m.isInsideMap(target) && !w.getPosition().equals(target) && !m.isCellDome(target) && m.isCellEmpty(target);
    }

    /**
     * Return all valid cells where a build can be performed
     * @param w worker doing the job
     * @param m current map
     * @param gc list of constraints
     * @return an ArrayList of Vector2 objects, representing all the possible cells i can run the action
     */
    @Override
    public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc) {

        GameConstraints gmc = mergeConstraints(localConstrains, gc);

        ArrayList<Vector2> cells = new ArrayList<>();

        int x = w.getPosition().getX();
        int y = w.getPosition().getY();

        for (int i = (x - 1); i <= (x + 1); i++)
        {
            for (int j = (y - 1); j <= (y + 1); j++)
            {
                Vector2 temp = new Vector2(i, j);
                if(isValidMove(w, temp, m, gmc))
                    cells.add(temp);
            }

        }
        return cells;
    }
}

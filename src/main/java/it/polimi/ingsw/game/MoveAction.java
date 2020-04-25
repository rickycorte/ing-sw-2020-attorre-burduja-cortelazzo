package it.polimi.ingsw.game;

import java.util.ArrayList;

/**
 * This action allow players to move workers around the map
 * Also checks if I won before ending the run method
 */
public class MoveAction extends Action {

    protected GameConstraints localConstrains;


    MoveAction(GameConstraints.Constraint localConstrains) {
        this.netId = 10;
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(localConstrains);
        displayName = "Move"+ localConstrains.toString();
    }

    public MoveAction() {
        this(GameConstraints.Constraint.NONE);
    }


    /**
     * Check if this worker can swap with another worker
     * @param gc global constraints
     * @return return true if can swap position with another worker
     */
    protected boolean canSwap(GameConstraints gc)
    {
        return gc.check(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT);
    }

    /**
     * Check if this worker can push another worker
     * @param gc global constraints
     * @return return true if can push position with another worker
     */
    protected boolean canPush(GameConstraints gc)
    {
        return gc.check(GameConstraints.Constraint.CAN_PUSH_CONSTRAINT);
    }

    /**
     * Check if this worker can move up or not
     * @param gc global constraints
     * @return true if can move up
     */
    protected boolean canMoveUp(GameConstraints gc)
    {
        return !gc.check(GameConstraints.Constraint.BLOCK_MOVE_UP);
    }

    /**
     * Check if a worker in a position is mine or not
     * @param me owner to check
     * @param target position of the worker to check
     * @param m current map
     * @return true is the worker is mine
     */
    protected boolean isWorkerMine(Player me, Vector2 target, Map m)
    {
        return m.getWorker(target).getOwner().equals(me);
    }


    /**
     * Calculate the new position of the target worker if a push action is done
     * @param w my worker that pushes the other
     * @param target other worker position to push
     * @return new position for target worker
     */
    protected Vector2 calculatePushPos(Worker w, Vector2 target)
    {
        int x = target.getX() + (target.getX() - w.getPosition().getX());
        int y = target.getY() + (target.getY() - w.getPosition().getX());
        return new Vector2(x, y);
    }

    /**
     * Check if a move is valid or not
     * @param w worker to use for the check
     * @param target target position to check
     * @param m current map
     * @param gc constrains to apply
     * @return true if the move is valid, false if not
     */
    protected boolean isValidMove(Worker w, Vector2 target, Map m, GameConstraints gc)
    {
        // target outside map
        if(!m.isInsideMap(target))
            return false;

        // same cell or distance grater than 1
        if(target.equals(w.getPosition()) || target.distance(w.getPosition()) != 1)
            return false;

        int hDiff = m.getLevel(target) - m.getLevel(w.getPosition());

        // can t move to a cell that has a high difference grater than 1 (or 0 if athena skill is enabled)
        // or its a dome
        int maxDiff = (canMoveUp(gc) ? 1 : 0);
        if(hDiff > maxDiff  || m.isCellDome(target))
            return  false;

        // block move to the same cell
        if(gc.check(GameConstraints.Constraint.BLOCK_SAME_CELL_MOVE) && target.equals(w.getLastLocation()))
        {
            return false;
        }

        if(m.isCellEmpty(target))
        {
            return true;
        }
        else
        {
            if(isWorkerMine(w.getOwner(), target, m))
            {
                return false; // no action can operate on my workers
            }
            else
            {
                //push must check if the "push dest" is inside map
                if(canPush(gc))
                {
                    return m.isInsideMap(calculatePushPos(w, target));
                }
                return canSwap(gc);
            }
        }

    }


    /**
     * Execute a move action
     * @param w worker to move
     * @param target target position
     * @param m current map
     * @param gc constraints to apply
     */
    protected void move(Worker w, Vector2 target, Map m, GameConstraints gc)
    {
        if ((m.isCellEmpty(target)))
        {
            w.setPosition(target);
        }
        else
        {
            Worker other = m.getWorker(target);
            if(canSwap(gc))
            {
                Vector2 swp = w.getPosition().copy();
                w.setPosition(other.getPosition());
                other.setPosition(swp);

            }
            if (canPush(gc))
            {
                other.setPosition(calculatePushPos(w, target));
                w.setPosition(target);
            }
        }
    }

    /**
     * Execute the move action
     * @param w target worker used in this action
     * @param target target position where the action should take place
     * @param m map where the action is executed
     * @param globalConstrains global game constrains that should be applied before action execution
     * @return 1 if won, 0 = to continue, -1 if i can't move anywhere
     * @throws NotAllowedMoveException if for some reason i can't move in target pos
     */
    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException
    {
        // athena should disable her lock at the beginning of the turn
        // and reset it later if moves up
        if(localConstrains.check(GameConstraints.Constraint.SET_BLOCK_MOVE_UP))
            globalConstrains.remove(GameConstraints.Constraint.BLOCK_MOVE_UP);

        //merge local and global constrains to avoid multiple checks
        GameConstraints gc = mergeConstraints(localConstrains, globalConstrains);

        ArrayList<Vector2> allowed_cells = possibleCells(w, m, globalConstrains);

        if (allowed_cells.size() == 0)
            return  -1;  // if i have nowhere to go -> i lost

        if (allowed_cells.contains(target))
        {
            w.setLastLocation(w.getPosition().copy()); // update last position for next possible moves
            move(w, target, m, gc);
        }
        else
        {
            throw new NotAllowedMoveException();
        }

        // and reset it later if moves up
        if(localConstrains.check(GameConstraints.Constraint.SET_BLOCK_MOVE_UP) && m.getLevel(w.getPosition()) > m.getLevel(w.getLastLocation()))
            globalConstrains.add(GameConstraints.Constraint.BLOCK_MOVE_UP);

        return winCheck(w, m, gc, w.getLastLocation());
    }


    /**
     * Check if a win condition is met
     * @param w worker i just moved
     * @param m map where action is taking place
     * @param gc collection of constraints
     * @param prev_pos the pos i started the turn in, used to check for "WIN_BY_GOING_DOWN" constraint
     * @return 1 if i won, 0 else
     */
    protected int winCheck(Worker w, Map m, GameConstraints gc, Vector2 prev_pos){
            if(m.getLevel(w.getPosition()) == 3)
                return 1;
            int difference =  m.getLevel(prev_pos) - m.getLevel(w.getPosition());
            if((difference > 1) && (gc.check(GameConstraints.Constraint.WIN_BY_GOING_DOWN)))
                return 1;
        return 0;
    }

    /**
     * Return a list of all valid cells for a move
     * @param w target worker
     * @param m map where action is taking place
     * @param gc Collection of various constraints
     * @return an arrayList of vector2 objects representing all the possible cells i can move to
     */

    @Override
    public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc) {

        GameConstraints gmc = mergeConstraints(localConstrains, gc);

        ArrayList<Vector2> cells = new ArrayList<>();

        int x = w.getPosition().getX();
        int y = w.getPosition().getY();

        for (int i = (x - 1); i <= (x + 1); i++) {
            for (int j = (y - 1); j <= (y + 1); j++) {
                Vector2 temp = new Vector2(i, j);

                if(isValidMove(w, temp, m, gmc))
                {
                    cells.add(temp);
                }
            }
        }
        return cells;
    }


}

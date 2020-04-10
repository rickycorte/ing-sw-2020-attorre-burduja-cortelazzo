package it.polimi.ingsw.game;

import java.util.ArrayList;

public class MoveAgainAction extends Action {

    GameConstraints localConstrains;

    MoveAgainAction(GameConstraints.Constraint localConstrains) {
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(localConstrains);
        display_name = "Move Again";
    }

    public MoveAgainAction() {
        this(GameConstraints.Constraint.NONE);
    }

    /**
     *
     * @param w target worker used in this action
     * @param target target position where the action should take place
     * @param m map where the action is executed
     * @param globalConstrains global game constrains that should be applied before action execution
     * @param node of the action
     * @return 1 if won, 0 = to continue, -1 if i can't move, -2 if for some reason i can't move in target pos
     * @throws NotAllowedMoveException
     */

    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains, BehaviourNode node) throws NotAllowedMoveException {
        Vector2 prev_move = node.getParent().getPos();
        int outcome = 0;
        Worker target_worker = new Worker(null);  // will be used in case there is a worker in target pos
        try{
            ArrayList<Vector2> cells = node.getAction().possibleCells(w,m,globalConstrains, node);
            if(cells.size() == 0)
                return outcome = -1;

            if(cells.contains(target)){
                if(m.isCellEmpty(target))
                    w.setPos(target);

            }else
                return outcome = -2;


        }catch (OutOfMapException e){
            e.printStackTrace();
        }


        outcome = win_check(w,m,globalConstrains,prev_move);
        return outcome;
    }

    /**
     *
     * @param w target worker
     * @param m the map where the action is taking place
     * @param gc library of game constraints
     * @param node the node where the action is taking place
     * @return an arrayLift of vector2 cells representing the cells i can move to
     * @throws OutOfMapException
     */

    @Override
    public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc, BehaviourNode node) throws OutOfMapException {
        ArrayList<Vector2> cells = new ArrayList<>();

        int x = w.getPos().getX();
        int y = w.getPos().getY();
        int max_difference = 1;

        ArrayList<Worker> my_workers = w.getOwner().getWorkers();

        Vector2 began_turn = node.getParent().getParent().getPos();
        boolean is_mine = false;

        for (int i = (x - 1); i <= (x + 1); i++) {
            for (int j = (y - 1); j <= (y + 1); j++) {
                Vector2 temp = new Vector2(i, j);
                try {
                    int difference = ((m.getLevel(temp)) - (m.getLevel(w.getPos())));
                    if ((m.isInsideMap(temp)) && !(m.isCellDome(temp)) && (i != x || j != y)) {
                        if (gc.check(GameConstraints.Constraint.BLOCK_MOVE_UP)) {
                            max_difference = 0;
                        }
                        if (difference <= max_difference) {
                            if (m.isCellEmpty(temp)) {
                                if((i == began_turn.getX()) && (j == began_turn.getY()) ) { //if checking the cell i began the turn in
                                    if (!(gc.check(GameConstraints.Constraint.BLOCK_SAME_CELL_MOVE)))
                                        cells.add(temp);
                                }else
                                    cells.add(temp);
                            } else {
                                for (Worker worker : my_workers) {
                                    if (worker.getPos().getX() == i && worker.getPos().getY() == j)
                                        is_mine = true;
                                }
                                if (!is_mine) {
                                    if (gc.check(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT)) {
                                        cells.add(temp);
                                    }
                                    if (gc.check(GameConstraints.Constraint.CAN_PUSH_CONSTRAINT)) {
                                        int push_pos_x = i + (i - w.getPos().getX());
                                        int push_pos_y = j + (j - w.getPos().getY());
                                        Vector2 push_pos = new Vector2(push_pos_x, push_pos_y);
                                        if ((m.isInsideMap(push_pos)) && (m.isCellEmpty(push_pos)))
                                            cells.add(temp);
                                    }
                                }
                            }
                        }
                    }
                } catch (OutOfMapException e) {
                    System.out.println("i've thrown an exception");
                }
            }
        }
        return cells;
    }

    /**
     *
     * @param w worker i just moved
     * @param m map where action is taking place
     * @param gc collection of constraints
     * @param prev_pos the pos i started the turn in, used to check for "WIN_BY_GOING_DOWN" constraint
     * @return 1 if i won, 0 else
     */
    public int win_check(Worker w, Map m, GameConstraints gc, Vector2 prev_pos){

        try {
            if(m.getLevel(w.getPos()) == 3)
                return 1;
            int difference =  m.getLevel(prev_pos) - m.getLevel(w.getPos());
            if((difference > 1) && (gc.check(GameConstraints.Constraint.WIN_BY_GOING_DOWN)))
                return 1;
        }catch (OutOfMapException e){
            System.out.println("i've thrown an exception!");
        }
        return 0;

    }
}



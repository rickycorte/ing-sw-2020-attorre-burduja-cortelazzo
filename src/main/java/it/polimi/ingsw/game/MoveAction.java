package it.polimi.ingsw.game;

import java.net.http.HttpResponse;
import java.util.ArrayList;

/**
 * This action allow players to move workers around the map
 * Also checks if I won before ending the run method
 */
public class MoveAction extends Action {

    GameConstraints localConstrains;


    MoveAction(GameConstraints.Constraint localConstrains) {
        this.net_id = 10;
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(localConstrains);
        display_name = "Move"+ localConstrains.toString();
    }

    public MoveAction() {
        this(GameConstraints.Constraint.NONE);
    }


    /**
     * @param w target worker used in this action
     * @param target target position where the action should take place
     * @param m map where the action is executed
     * @param globalConstrains global game constrains that should be applied before action execution
     * @param current_node the node of the action
     * @return 1 if won, 0 = to continue, -1 if i can't move anywhere
     * @throws NotAllowedMoveException if for some reason i can't move in target pos
     */
    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains, BehaviourNode current_node) throws NotAllowedMoveException {
        current_node.setPos(w.getPos()); // will be used in case of a second move
        int outcome = 0;
        Worker target_worker = new Worker(null);  // will be used in case there is a worker in target pos

        if (w.getOwner().getGod().getId() == 3) { //if i'm playing as Athena
            if (globalConstrains.check(GameConstraints.Constraint.BLOCK_MOVE_UP))
                globalConstrains.remove(GameConstraints.Constraint.BLOCK_MOVE_UP);
        }
        ArrayList<Vector2> cells = (current_node.getAction()).possibleCells(w, m, globalConstrains, current_node);
        if (cells.size() == 0)
            return outcome = -1;  // if i have nowhere to go -> i lost
        if (cells.contains(target)) {
            if ((m.isCellEmpty(target)))
                w.setPos(target);
            else {
                ArrayList<Worker> workers = m.getWorkers();
                for (Worker worker : workers) {
                    if (worker.getPos().getY() == target.getY() && worker.getPos().getX() == target.getX())
                        target_worker = worker;
                }
                if (globalConstrains.check(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT)) {
                    target_worker.setPos(w.getPos());
                    w.setPos(target);
                }
                if (globalConstrains.check(GameConstraints.Constraint.CAN_PUSH_CONSTRAINT)) {
                    int push_pos_x = target_worker.getPos().getX() + (target_worker.getPos().getX() - w.getPos().getX());
                    int push_pos_y = target_worker.getPos().getY() + (target_worker.getPos().getY() - w.getPos().getY());
                    Vector2 push_pos = new Vector2(push_pos_x, push_pos_y);
                    w.setPos(target_worker.getPos());
                    target_worker.setPos(push_pos);
                }
            }
        } else {
            throw new NotAllowedMoveException();
        }
        if ((w.getOwner().getGod().getId() == 3) && (m.getLevel(w.getPos()) > m.getLevel(current_node.getPos()))) // if i'm athena and moved up -> others cant
            globalConstrains.add(GameConstraints.Constraint.BLOCK_MOVE_UP);
        outcome = win_check(w, m, globalConstrains, current_node.getPos());
        return outcome;
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
            if(m.getLevel(w.getPos()) == 3)
                return 1;
            int difference =  m.getLevel(prev_pos) - m.getLevel(w.getPos());
            if((difference > 1) && (gc.check(GameConstraints.Constraint.WIN_BY_GOING_DOWN)))
                return 1;
        return 0;
    }

    /**
     *
     * @param w target worker
     * @param m map where action is taking place
     * @param gc Collection of various constraints
     * @param node current node, used to find the pos i started the turn in
     * @return an arrayList of vector2 objects representing all the possible cells i can move to
     */

    @Override
    public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc, BehaviourNode node) {
        ArrayList<Vector2> cells = new ArrayList<>();

        int x = w.getPos().getX();
        int y = w.getPos().getY();
        int max_difference = 1;

        ArrayList<Worker> my_workers = w.getOwner().getWorkers();
        boolean is_mine = false;

        for (int i = (x - 1); i <= (x + 1); i++) {
            for (int j = (y - 1); j <= (y + 1); j++) {
                Vector2 temp = new Vector2(i, j);

                int difference = ((m.getLevel(temp)) - (m.getLevel(w.getPos())));
                if ((m.isInsideMap(temp)) && !(m.isCellDome(temp)) && (i != x || j != y)) { // checking if temp is inside the map, is not a dome and is not my pos
                    if (gc.check(GameConstraints.Constraint.BLOCK_MOVE_UP)) {               // checking if i can go up
                        max_difference = 0;
                    }
                    if (difference <= max_difference) {
                        if (m.isCellEmpty(temp)) {
                            cells.add(temp);
                        } else {
                            for (Worker worker : my_workers) {
                                if (worker.getPos().getX() == i && worker.getPos().getY() == j)
                                    is_mine = true;
                            }
                            if (!is_mine)
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
        }
        return cells;
    }


}

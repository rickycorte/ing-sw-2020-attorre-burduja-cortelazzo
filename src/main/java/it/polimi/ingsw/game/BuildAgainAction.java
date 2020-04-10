package it.polimi.ingsw.game;

import java.util.ArrayList;


public class BuildAgainAction extends Action {


    GameConstraints localConstrains;

    BuildAgainAction(GameConstraints.Constraint localConstrains)
    {
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(localConstrains);
        display_name = "Build Again";
    }

    public BuildAgainAction() {
        this(GameConstraints.Constraint.NONE);
    }

    /**
     *
     * @param w target worker used in this action
     * @param target target position where the action should take place
     * @param m map where the action is executed
     * @param globalConstrains global game constrains that should be applied before action execution
     * @param node current node
     * @return 1 if i won, 0 to continue, -1 if i cant build anywhere, -2 if i cant build in target pos
     * @throws NotAllowedMoveException
     */
    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains, BehaviourNode node) throws NotAllowedMoveException {

        try {
            ArrayList<Vector2> cells = node.getAction().possibleCells(w,m,globalConstrains,node);
            if(cells.size() != 0) {
                if (cells.contains(target)) {
                    m.build(target);
                } else return -2;
            }else return -1;
        }catch (CellCompletedException e){
            e.printStackTrace();
        }catch (OutOfMapException e1){
            e1.printStackTrace();
        }
        System.out.println("This was a nice double build");
        return 0;
    }

    /**
     *
     * @param w worker doing the job
     * @param m current map
     * @param gc list of constraints
     * @param node  the current node i'm in, will be used to check where i built last time
     * @return an ArrayList of Vector2 objects, representing all the possible cells i can run the action
     * @throws OutOfMapException if the cell i'm checking is outside the map
     */
    @Override
    public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc, BehaviourNode node) throws OutOfMapException {
        ArrayList<Vector2> cells = new ArrayList<>();
        int x = w.getPos().getX();
        int y = w.getPos().getY();
        Vector2 prev_build = node.getParent().getPos();
        int max_height = 3;
        if (gc.check(GameConstraints.Constraint.BLOCK_DOME_BUILD))
            max_height = 2;
        for (int i = (x-1); i <= (x+1); i++) {
            for (int j = (y - 1); j <= (y + 1); j++) {
                Vector2 temp = new Vector2(i, j);
                if (i != x || j != y) {
                    try {
                        if (m.isInsideMap(temp) && !(m.isCellDome(temp)) && (m.isCellEmpty(temp))) {
                            if (m.getLevel(temp) <= max_height) {
                                if (gc.check(GameConstraints.Constraint.BLOCK_DIFF_CELL_BUILD)) {
                                    if (i == prev_build.getX() && j == prev_build.getY())
                                        cells.add(temp);
                                }else {
                                    if (gc.check(GameConstraints.Constraint.BLOCK_SAME_CELL_BUILD)) {
                                        if (i != prev_build.getX() || j != prev_build.getY())
                                            cells.add(temp);
                                    }else
                                        cells.add(temp);
                                }
                            }
                        }
                    }catch (OutOfMapException e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return cells;
    }


}






package it.polimi.ingsw.game;

import java.util.ArrayList;

public class BuildDomeAction extends Action{
    GameConstraints localConstrains;

    BuildDomeAction(GameConstraints.Constraint localConstrains)
    {
        this.net_id = 22;
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(localConstrains);
        display_name = "BuildingDome" + localConstrains.toString();
    }

    public BuildDomeAction() {
        this(GameConstraints.Constraint.NONE);
    }

    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains, BehaviourNode node) throws NotAllowedMoveException {
        try {
            ArrayList<Vector2> cells = node.getAction().possibleCells(w, m, globalConstrains, node);
            if (cells.size() != 0) {
                if (cells.contains(target)) {
                    m.buildDome(target);
                    node.setPos(target); //will be used in case of a second build
                } else throw new NotAllowedMoveException();
            } else return -1;
        } catch (CellCompletedException e) {
            throw new NotAllowedMoveException();
        }
        return 0;
    }


    public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc, BehaviourNode node) {
        ArrayList<Vector2> cells = new ArrayList<>();
        int x = w.getPosition().getX();
        int y = w.getPosition().getY();
        for (int i = (x - 1); i <= (x + 1); i++) {
            for (int j = (y - 1); j <= (y + 1); j++) {
                Vector2 temp = new Vector2(i, j);
                if (i != x || j != y) {
                    if (m.isInsideMap(temp) && !(m.isCellDome(temp)) && (m.isCellEmpty(temp))) {
                        cells.add(temp);
                    }
                }
            }
        }
        return cells;
    }
}

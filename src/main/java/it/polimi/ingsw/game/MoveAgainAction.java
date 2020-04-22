package it.polimi.ingsw.game;

import java.util.ArrayList;


/**
 * Class used if the god can move for a second time but not back on the same call where it was
 * This class is only a wrapper to MoveAction(GameConstraints.Constraint.BLOCK_SAME_CELL_MOVE)
 */

public class MoveAgainAction extends MoveAction {

    MoveAgainAction(GameConstraints.Constraint localConstrains) {
        this.net_id = 11;
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(localConstrains);
        this.localConstrains.add(GameConstraints.Constraint.BLOCK_SAME_CELL_MOVE);
        display_name = "Move Again"+ localConstrains.toString();
    }

    public MoveAgainAction() {
        this(GameConstraints.Constraint.NONE);
    }

}



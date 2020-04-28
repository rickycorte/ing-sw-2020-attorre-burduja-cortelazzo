package it.polimi.ingsw.game;


/**
 * Class used if the god can move for a second time but not back on the same call where it was
 * This class is only a wrapper to MoveAction(GameConstraints.Constraint.BLOCK_SAME_CELL_MOVE)
 */

public class MoveAgainAction extends MoveAction {

    MoveAgainAction(GameConstraints.Constraint localConstrains) {
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(localConstrains);
        this.localConstrains.add(GameConstraints.Constraint.BLOCK_SAME_CELL_MOVE);
        displayName = "Move Again"+ localConstrains.toString();
    }

    public MoveAgainAction() {
        this(GameConstraints.Constraint.NONE);
    }

}



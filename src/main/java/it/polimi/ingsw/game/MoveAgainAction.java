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

        displayName = "Move ("+GameConstraints.Constraint.BLOCK_SAME_CELL_MOVE.toString();

        if(localConstrains != GameConstraints.Constraint.NONE)
            displayName += ", " + localConstrains.toString();

        displayName+= ")";
    }

    public MoveAgainAction() {
        this(GameConstraints.Constraint.NONE);
    }

}



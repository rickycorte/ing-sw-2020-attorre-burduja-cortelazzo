package it.polimi.ingsw.game;

import java.util.ArrayList;

/**
 * This action allow players to move workers around the map
 */
public class MoveAction extends Action
{

    GameConstraints localConstrains;

    MoveAction(GameConstraints.Constraint localConstrains) {
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(localConstrains);
        display_name = "Move";
    }

    public MoveAction()
    {
        this(GameConstraints.Constraint.NONE);
    }

    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException
    {
        System.out.println("This was a nice move");
        return 0;
    }

    @Override
    public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc, BehaviourNode node) throws OutOfMapException{
        return null;
    }

}

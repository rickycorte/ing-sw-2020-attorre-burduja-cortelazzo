package it.polimi.ingsw.game;

import java.util.ArrayList;


public class BuildAgainAction extends BuildAction {

    BuildAgainAction(GameConstraints.Constraint localConstrains)
    {
        this.net_id = 21;
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(localConstrains);
        display_name = "BuildAgain"+ localConstrains.toString();
    }

    BuildAgainAction(GameConstraints.Constraint constraint1, GameConstraints.Constraint constraint2){
        this.net_id = 21;
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(constraint1);
        this.localConstrains.add(constraint2);
        display_name = "BuildAgain"+ localConstrains.toString();
    }

    public BuildAgainAction() {
        this(GameConstraints.Constraint.NONE);
    }


    @Override
    protected boolean isValidMove(Worker w, Vector2 target, Map m, GameConstraints gc)
    {
        boolean base =  super.isValidMove(w, target, m, gc);
        if(!base) return false;

        if(gc.check(GameConstraints.Constraint.BLOCK_DOME_BUILD) && m.getLevel(target) >= 3)
            return false;
        else if(gc.check(GameConstraints.Constraint.BLOCK_DIFF_CELL_BUILD) && !target.equals(w.getLastBuildLocation()))
            return false;
        else if(gc.check(GameConstraints.Constraint.BLOCK_SAME_CELL_BUILD) &&  target.equals(w.getLastBuildLocation()))
            return false;

        return true;
    }


}






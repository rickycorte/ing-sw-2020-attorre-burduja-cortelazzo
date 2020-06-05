package it.polimi.ingsw.game;

/**
 * Class that represents a second build action and offers several constraints to customize its behaviour
 */
public class BuildAgainAction extends BuildAction {

    public BuildAgainAction(GameConstraints.Constraint localConstrains)
    {
        this(localConstrains, GameConstraints.Constraint.NONE);
    }

    public BuildAgainAction(GameConstraints.Constraint constraint1, GameConstraints.Constraint constraint2){
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(constraint1);
        this.localConstrains.add(constraint2);

        displayName = "Build Again";

        if(constraint1 != GameConstraints.Constraint.NONE && constraint2 != GameConstraints.Constraint.NONE)
        {
            displayName += " ("+constraint1.toString() + ", "+constraint2.toString()+")";
        }
        else if(constraint1 != GameConstraints.Constraint.NONE)
        {
            displayName +=" ("+constraint1.toString()+")";
        }
        else if(constraint2 != GameConstraints.Constraint.NONE)
        {
            displayName +=" ("+constraint2.toString()+")";
        }

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






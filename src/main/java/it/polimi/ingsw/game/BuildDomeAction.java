package it.polimi.ingsw.game;

/**
 * This action extends buildAction and places a dome in the selected cell
 */
public class BuildDomeAction extends BuildAction {

    BuildDomeAction(GameConstraints.Constraint localConstrains)
    {
        this.netId = 22;
        this.localConstrains = new GameConstraints();
        this.localConstrains.add(localConstrains);
        displayName = "BuildingDome" + localConstrains.toString();
    }

    public BuildDomeAction() {
        this(GameConstraints.Constraint.NONE);
    }


    @Override
    protected void build(Worker w, Vector2 target, Map m, GameConstraints gc)
    {
        m.buildDome(target);
        w.setLastLocation(target);
    }
}

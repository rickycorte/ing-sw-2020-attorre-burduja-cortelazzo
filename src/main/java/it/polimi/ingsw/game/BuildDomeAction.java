package it.polimi.ingsw.game;

/**
 * This action extends buildAction and places a dome in the selected cell
 */
public class BuildDomeAction extends BuildAction {

    public BuildDomeAction() {

        this.localConstrains = new GameConstraints();
        displayName = "Build Dome";
    }


    @Override
    protected void build(Worker w, Vector2 target, Map m, GameConstraints gc)
    {
        m.buildDome(target);
        w.setLastLocation(target);
    }
}

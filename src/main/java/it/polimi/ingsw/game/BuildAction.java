package it.polimi.ingsw.game;

/**
 * This action allow players to build structures around the map
 */
public class BuildAction extends Action
{


    public BuildAction()
    {
        display_name = "Build";
    }

    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException
    {
        System.out.println("This was a nice build");
        return 0;
    }

}

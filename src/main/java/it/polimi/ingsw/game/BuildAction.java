package it.polimi.ingsw.game;

import java.util.ArrayList;
import java.util.List;

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
    @Override
    public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc, BehaviourNode node) throws OutOfMapException{
        ArrayList<Vector2> cells = new ArrayList<>();
        for( int i = w.getPos().x--; i != (w.getPos().x++)+1; i++){
            for( int j = w.getPos().y--; j != (w.getPos().y++)+1; j++){
                Vector2 temp = new Vector2(i,j);
                if ((m.isInsideMap(temp) && !(m.isCellDome(temp)) && (m.isCellEmpty(temp)))){
                    cells.add(temp);
                }
            }
        }
        return cells;
    }

}

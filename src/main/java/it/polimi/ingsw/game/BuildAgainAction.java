package it.polimi.ingsw.game;

import java.util.ArrayList;
import  it.polimi.ingsw.game.GameConstraints;

public class BuildAgainAction extends Action {



    public BuildAgainAction()
    {
        display_name = "BuildAgain";
    }

    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints globalConstrains) throws NotAllowedMoveException
    {
        System.out.println("This was a nice double build");
        return 0;
    }

    @Override
    public ArrayList<Vector2> possibleCells(Worker w, Map m, GameConstraints gc, BehaviourNode node) throws OutOfMapException {

        ArrayList<Vector2> cells = new ArrayList<>();
        int x = w.getPos().getX();
        int y = w.getPos().getY();

        for( int i = x--; i <= x++; i++){
            for( int j = y--; j <= y++; j++){
                Vector2 temp = new Vector2(i,j);
                if(!temp.equals(w.getPos())){
                if ((m.isInsideMap(temp) && !(m.isCellDome(temp)) && (m.isCellEmpty(temp)))) {
                    if (gc.check(GameConstraints.Constraint.BLOCK_SAME_CELL_BUILD)) {  //if true -> can't build on the same space
                        Vector2 prev_build_pos = (node.getParent()).getPos();
                        if (prev_build_pos != temp)
                            cells.add(temp);
                    } else {
                        if (m.getLevel(temp) < 3) //assures next build won't be a dome
                            cells.add(temp);
                    }
                }
                }
            }
        }
        return cells;

    }
}




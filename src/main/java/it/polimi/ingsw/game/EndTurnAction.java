package it.polimi.ingsw.game;

import java.util.ArrayList;


public class EndTurnAction extends Action {


    EndTurnAction()
    {
        this.netId = 30;
        displayName = "End Turn";
    }



    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints gc){
        return 0;
    }
    @Override
    public ArrayList<Vector2> possibleCells(Worker w, Map m ,GameConstraints gc){

        var arr = new ArrayList<Vector2>();
        arr.add(new Vector2(0,0));
        return arr; // random valid data to make this element "selectable"
    }

}

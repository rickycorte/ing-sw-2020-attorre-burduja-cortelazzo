package it.polimi.ingsw.game;

import java.util.ArrayList;


public class EndTurnAction extends Action {


    EndTurnAction()
    {
        this.net_id = 30;
        display_name = "End Turn";
    }



    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints gc, BehaviourNode node){
        return 0;
    }
    @Override
    public ArrayList<Vector2> possibleCells(Worker w, Map m ,GameConstraints gc, BehaviourNode node){

        var arr = new ArrayList<Vector2>();
        arr.add(new Vector2(0,0));
        return arr; // random valid data to make this element "selectable"
    }

}

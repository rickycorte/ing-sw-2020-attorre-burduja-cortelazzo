package it.polimi.ingsw.game;

import java.util.ArrayList;


/**
 * This class represent a "exit" from a behaviour graph and should be used when optional parts
 * of the god behaviour are optionals
 * Thia actions must do nothing
 * Example use:
 *  Demeter: Your Worker MAY build one additional time...
 */
public class EndTurnAction extends Action {


    /**
     * Create a new end turn action to use as last node in a behaviour graph
     */
    EndTurnAction()
    {
        displayName = "End Turn";
    }



    @Override
    public int run(Worker w, Vector2 target, Map m, GameConstraints gc){
        return 0;   // end turn does nothing!
    }


    @Override
    public ArrayList<Vector2> possibleCells(Worker w, Map m ,GameConstraints gc){

        var arr = new ArrayList<Vector2>();
        arr.add(new Vector2(0,0));
        return arr; // random valid data to make this element "selectable"
    }

    @Override
    public String toString() {
        return displayName;
    }
}

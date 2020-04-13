package it.polimi.ingsw.game;

public class Turn
{

    public Turn(Player p) {}

    public void selectWorker(int target) {}


    /**
     * Execute an action and get result
     * equals 0 continue
     * greater then 0 current player met a win condition
     * lesser than 0 current player can't complete action and met a lose condition
     * @param id
     * @param target
     * @param m
     * @param globalConstrains
     * @return
     */
    public int runAction(int id, Vector2 target, Map m, GameConstraints globalConstrains) { return 0; }

    public boolean canStillMove() { return false; }

    public boolean isEnded() { return false; }
}

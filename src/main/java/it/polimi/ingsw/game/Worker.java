package it.polimi.ingsw.game;

public class Worker
{
    private Player owner;
    private Vector2 pos;

    public Worker(Player owner){
        this.owner = owner;
    }
    public Vector2 getPos() {
        return pos;
    }

    public Player getOwner() {
        return owner;
    }

    public void setPos(Vector2 pos){
        this.pos = pos;
    }
}

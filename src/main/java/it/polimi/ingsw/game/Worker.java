package it.polimi.ingsw.game;

import java.util.Objects;

public class Worker
{
    private int id;
    private Player owner;
    private Vector2 pos;

    public Worker(Player owner){
        this(-1, owner, new Vector2(0,0));
    }

    public Worker(int id, Player owner, Vector2 position){
        this.id = id;
        this.owner = owner;
        this.pos = position;
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

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Worker worker = (Worker) o;
        return id == worker.id &&
                owner.equals(worker.owner) &&
               pos.equals(worker.pos);
    }

}

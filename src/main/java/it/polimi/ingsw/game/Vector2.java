package it.polimi.ingsw.game;

/**
 * This class represents a position in the map
 */
public class Vector2
{
    int x,y;

    public Vector2(int x, int y)
    {
        set(x,y);
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public void set(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
}

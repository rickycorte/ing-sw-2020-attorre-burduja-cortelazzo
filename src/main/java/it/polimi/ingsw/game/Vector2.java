package it.polimi.ingsw.game;

/**
 * This class represents a position in the map
 */
public class Vector2 {
    int x, y;

    public Vector2(int x, int y) {
        set(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } //if compared with myself
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Vector2 test_pos = (Vector2) obj;
        return Integer.compare(x, test_pos.x) == 0 && Integer.compare(y, test_pos.y) == 0;
    }
}

package it.polimi.ingsw.view.cli;

/**
 * This class represents a player
 */
public class Player {
    private int id;
    private String username;
    private Color color;

    public Player(int id, String username, Color color) {
        this.id = id;
        this.username = username;
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id == player.id;
    }

    public String escapePlayerColor(){
        return color.escape();
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}

package it.polimi.ingsw.view;

public class Card {
    private int id;
    private String name;
    private String description;
    private String power;

    public Card(int id, String name, String description, String power) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.power = power;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPower() {
        return power;
    }
}

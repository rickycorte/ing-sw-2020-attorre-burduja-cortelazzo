package it.polimi.ingsw.view.cli;

import java.util.Random;

public enum Color {
    RED("\u001b[31m"),
    GREEN("\u001b[32m"),
    YELLOW("\u001b[33m"),
    BLUE("\u001b[34m"),
    PURPLE("\u001b[35m"),
    SOFT_WHITE("\u001b[90m");

    static final String RESET = "\u001B[0m";

    private String escape;

    Color(String escape){
        this.escape = escape;
    }

    public String escape(){
        return escape;
    }
}

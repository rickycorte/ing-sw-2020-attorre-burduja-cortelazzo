package it.polimi.ingsw.view.cli;

/**
 * Colors enum, escape ANSI sequences
 */
public enum Color {
    LIME("\33[38;5;112m"),
    SUNSET("\33[38;5;210m"),
    SUNRISE("\33[38;5;229m"),
    MIDNIGHT("\33[38;5;20m"),
    PURPLE_PUNCH("\33[38;5;56m"),
    SMALL_FIRE("\33[38;5;134m"),
    BIG_FIRE("\033[38;5;196m"),

    BLUE("\033[38;5;21m"),
    WHITE("\u001b[37m"),
    BOLD_ON("\033[1m"),
    CLICKABLE("\033[38;5;137m"),
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

package it.polimi.ingsw.view.gui;

/**
 * This class holds the settings of the application, available to all the scene controllers
 */
public class Settings {

    public enum Themes {LIGHT, DARK};
    private Themes theme;

    /**
     * Settings constructor
     */
    Settings(){
        theme = Themes.LIGHT;
    }

    /**
     * Theme setter
     * @param theme theme to set
     */
    void setTheme(Themes theme){
        this.theme = theme;
    }

    /**
     * Theme getter
     * @return set theme
     */
    Themes getTheme(){
        return theme;
    }
}

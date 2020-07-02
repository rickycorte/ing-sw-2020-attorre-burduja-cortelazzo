package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * This class is the controller for the "Wait" scene
 */
public class WaitSceneController implements Initializable {
    private Settings settings;

    @FXML
    private Pane mainPane;
    @FXML
    private Label hostLabel;
    @FXML
    private Label centerLabel;
    @FXML
    private Button startGameButton;
    @FXML
    private ImageView loadingImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiManager.getInstance().setWaitSceneController(this);
        settings = GuiManager.getInstance().getSettings();
        startGameButton.setDisable(true);

        initializeStyleSheet();
        initializeLabels();

        URL url = getClass().getResource("/img/common/blue_loading.gif");
        try (InputStream stream = url.openStream()) {
            loadingImage.setImage(new Image(stream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------Initialize Methods--------------------------------------------------------

    /**
     * Initializes both center and host labels
     */
    private void initializeLabels() {
        if (GuiManager.getInstance().imHost()) {
            hostLabel.setText("You are the host");
            centerLabel.setText("Waiting for worthy opponents...");
        } else {
            hostLabel.setText(null);
            centerLabel.setText("Waiting for the host to start the game...");
        }
    }

    /**
     * Setts the style sheet according to the settings
     */
    private void initializeStyleSheet() {
        ArrayList<Parent> toStyle = initializeToStyleList();
        for(Parent node: toStyle){
            node.getStylesheets().clear();
            if(settings.getTheme() == Settings.Themes.LIGHT)
                node.getStylesheets().add("css/lightTheme.css");
            else
                node.getStylesheets().add("css/darkTheme.css");
        }
    }

    /**
     * Collects in an array list all the parents that need to be styled
     * @return ArrayList of parents that need to be styled
     */
    private ArrayList<Parent> initializeToStyleList() {
        ArrayList<Parent> toStyle = new ArrayList<>();
        toStyle.add(mainPane);
        toStyle.add(hostLabel);
        toStyle.add(centerLabel);
        toStyle.add(startGameButton);
        return toStyle;
    }

    //---------------------------------------Button Click Handlers------------------------------------------------------

    /**
     * Handles the start button click
     */
    @FXML
    void onStartGameButtonClick() {
        GuiManager.getInstance().getServerConnection().send(StartCommand.makeRequest(GuiManager.getInstance().getServerConnection().getClientID(), GuiManager.getInstance().getServerConnection().getServerID()));
    }

    //-----------------------------------Command Handlers---------------------------------------------------------------

    /**
     * Allows the host to start the game in 2 player mode
     */
    void onSecondClientConnection(CommandWrapper cmd) {
        String secondUsername = cmd.getCommand(JoinCommand.class).getUsername();
        centerLabel.setText("A player named "+ secondUsername +" has connected\nYou can now start the game or wait for a third player");
        startGameButton.setDisable(false);

    }

    /**
     * Handles the start game command
     * @param startCommand command from the server
     */
    void onStart(StartCommand startCommand) {
        GuiManager.setLayout("fxml/chooseGodsScene.fxml");
    }

    /**
     * Handles the disconnection of a player in waiting room
     * @param cmd disconnection command coming from the server
     */
    public void onDisconnect(CommandWrapper cmd) {
        if (cmd.getCommand(LeaveCommand.class).getNumberRemainingPlayers() < 2) {
            centerLabel.setText("A player has disconnected\nNot enough players to start a game\nWaiting...");
            startGameButton.setDisable(true);
        }
    }
}


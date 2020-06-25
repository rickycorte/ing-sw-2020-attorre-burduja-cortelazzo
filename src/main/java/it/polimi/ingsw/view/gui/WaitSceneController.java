package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static javafx.scene.paint.Color.RED;
import static javafx.scene.paint.Color.WHITE;

public class WaitSceneController {

    @FXML
    private Pane mainPane;
    @FXML
    private Label centerLabel;
    @FXML
    private Button startGameButton;
    @FXML
    private ImageView loadingImage;

    @FXML
    public void initialize() {
        GuiManager.getInstance().setWaitSceneController(this);
        startGameButton.setDisable(true);
        startGameButton.setText("Start Game");

        if(GuiManager.getInstance().imHost())
            centerLabel.setText("Waiting for worthy opponents...");
        else
            centerLabel.setText("Waiting for the host to start the game...");

        URL url = getClass().getResource("/img/common/blue_loading.gif");
        try (InputStream stream = url.openStream()) {
            loadingImage.setImage(new Image(stream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the start button click
     */
    @FXML
    void onStartGameButtonClick() {
        GuiManager.getInstance().getServerConnection().send(StartCommand.makeRequest(GuiManager.getInstance().getServerConnection().getClientID(), GuiManager.getInstance().getServerConnection().getServerID()));
    }

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

    public void onDisconnect(CommandWrapper cmd) {
        if (cmd.getCommand(LeaveCommand.class).getNumberRemainingPlayers() < 2) {
            centerLabel.setText("A player has disconnected\nNot enough players to start a game\nWaiting...");
            startGameButton.setDisable(true);
        }
    }
}


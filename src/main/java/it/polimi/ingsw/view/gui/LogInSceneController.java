package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.controller.JoinCommand;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;


import javafx.scene.control.TextField;

public class LogInSceneController {

    private GuiManager guiManager;

    private static Pane mainPane_instance;
    @FXML
    private Pane mainPane;
    @FXML
    private Label upperLabel;
    @FXML
    private Label lowerLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private Button connectButton;
    @FXML
    private Button backButton;

    @FXML
    public void initialize() {
        guiManager = GuiManager.getInstance();
        guiManager.setLogInSceneController(this);
        connectButton = new Button();
        backButton = new Button();
        mainPane_instance = mainPane;

        usernameField.setOnKeyPressed((e)-> {
            if (e.getCode() == KeyCode.ENTER) {
                onConnectButtonClick();
            }
        });
    }
        //TODO handle properly false ack join
    /**
     * Handles back button click
     * Goes to main scene
     */
    @FXML
    private void onBackButtonClick() {
        GuiManager.setLayout("fxml/mainScene.fxml");
    }

    /**
     * Handles connect button click
     * Reads username from username field and attempts join
     */
    @FXML
    private void onConnectButtonClick() {
        final String username = usernameField.getText();
        if (username.isBlank()) {
            upperLabel.setText("Blank username");
            lowerLabel.setText("Enter another username");
            return;
        }

        backButton.setDisable(true);
        connectButton.setDisable(true);

        if (!(GuiManager.getInstance().connect(username))) {
            upperLabel.setText("Couldn't connect");
            lowerLabel.setText("Try again later");
            //GuiManager.setLayout("fxml/mainScene.fxml");
        }
    }

    /**
     * Handles Join response
     * @param cmd join command received
     */
    void onAckJoin(CommandWrapper cmd) {
        if(GuiManager.getInstance().isForMe(cmd)) {
            JoinCommand joinCommand = cmd.getCommand(JoinCommand.class);
            if (joinCommand.isJoin()) {
                GuiManager.setLayout("fxml/waitScene.fxml");
            } else {
                //GuiManager.setLayout("fxml/mainScene.fxml");
                upperLabel.setText("Couldn't connect");
                if (!joinCommand.isValidUsername()) {
                    lowerLabel.setText("Username taken");
                } else
                    lowerLabel.setText("Try again later");
            }
        }
    }
}


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
    private Label infoLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField serverField;
    @FXML
    private TextField portField;

    private Button connectButton;

    private Button backButton;

    @FXML
    public void initialize() {
        guiManager = GuiManager.getInstance();
        guiManager.setLogInSceneController(this);
        connectButton = new Button();
        backButton = new Button();
        mainPane_instance = mainPane;

        infoLabel.setText("Enter your username, server ip and port to connect");

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
        String username = usernameField.getText();
        String serverIP = serverField.getText();
        String serverPort = portField.getText();
        if (username.isBlank()) {
            infoLabel.setText("Blank username \n You need to have one if you want to play");
            return;
        }

        backButton.setDisable(true);
        connectButton.setDisable(true);

        if(!serverIP.isBlank() && !serverPort.isBlank()){
            GuiManager.getInstance().connect(serverIP, Integer.parseInt(serverPort), username);
        }else{
            GuiManager.getInstance().connect(username);
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
                infoLabel.setText("Couldn't connect");
                if (!joinCommand.isValidUsername()) {
                    infoLabel.setText("Username taken");
                } else
                    infoLabel.setText("Try again later");
            }
        }
    }
}


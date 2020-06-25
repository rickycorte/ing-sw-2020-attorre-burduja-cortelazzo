package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.controller.JoinCommand;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;


import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import static javafx.scene.paint.Color.*;


public class LogInSceneController {
    private boolean connected;

    private boolean connectToCustomServer;                  //flag to know if i want to connect to a custom server or not
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
    @FXML
    private Button connectButton;
    @FXML
    private Button backButton;
    @FXML
    private CheckBox customServerCheckBox;
    @FXML
    private VBox ambientPane;
    @FXML
    private Label serverLabel;
    @FXML
    private Label portLabel;

    @FXML
    public void initialize() {
        GuiManager.getInstance().setLogInSceneController(this);
        connected = false;
        connectToCustomServer = false;
        infoLabel.setText("Enter your username to connect \n");
        serverField.setOpacity(0);
        serverLabel.setOpacity(0);
        portField.setOpacity(0);
        portLabel.setOpacity(0);
        usernameField.setOnKeyPressed((e)-> {
            if (e.getCode() == KeyCode.ENTER) {
                onConnectButtonClick();
            }
        });
        deactivateCustomServerControls();
        EventHandler eventHandler = (EventHandler<ActionEvent>) event -> {
            if(event.getSource() instanceof CheckBox){
                if(customServerCheckBox.isSelected()) {
                    activateCustomServerControls();
                    connectToCustomServer = true;
                }else{
                    deactivateCustomServerControls();
                    connectToCustomServer = false;
                }
            }
        };
        customServerCheckBox.setOnAction(eventHandler);

    }



    /**
     * Activates text fields for the custom server ip and port
     */
    private void activateCustomServerControls(){
        serverField.setDisable(false);
        doFadeTransition(serverLabel,600,0,1);
        doFadeTransition(serverField, 600, 0, 1);
        portField.setDisable(false);
        doFadeTransition(portField,600,0,1);
        doFadeTransition(portLabel, 600, 0, 1);

    }

    /**
     * Deactivate text fields for the custom server ip and port
     */
    private void deactivateCustomServerControls(){
        serverField.setDisable(true);
        doFadeTransition(serverField, 600, 1, 0);
        doFadeTransition(serverLabel,600,1,0);
        portField.setDisable(true);
        doFadeTransition(portLabel,600,1,0);
        doFadeTransition(portField, 600, 1, 0);
    }

    /**
     * Method that initializes a fade transition and plays it
     * @param node node to fade
     * @param duration duration of the transition
     * @param from initial opacity value
     * @param to final opacity value
     */
    private void doFadeTransition(Node node, int duration, int from, int to){
        FadeTransition ft = new FadeTransition(Duration.millis(duration), node);
        ft.setFromValue(from);
        ft.setToValue(to);
        ft.setCycleCount(1);
        ft.play();
    }

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
     * Reads username - serverIP - port and attempts join
     */
    @FXML
    private void onConnectButtonClick() {
        String username = usernameField.getText();
        String serverIP = serverField.getText();
        String serverPort = portField.getText();
        if (username.isBlank()) {
            infoLabel.setText("Blank username\nYou need one if you want to join");
            return;
        }
        backButton.setDisable(true);
        connectButton.setDisable(true);
        if (!connected) {
            if (connectToCustomServer) {
                GuiManager.getInstance().connect(serverIP, Integer.parseInt(serverPort), username);
            } else {
                if (GuiManager.getInstance().connect(username)) {
                    connected = true;
                    infoLabel.setText("Connected to server");
                } else {
                    connected = false;
                    connectButton.setDisable(false);
                    backButton.setDisable(false);
                    infoLabel.setText("Couldn't connect to server\nTry again later");
                }
            }
        }else{ // retry with another username
            GuiManager.getInstance().send(JoinCommand.makeRequest(GuiManager.getInstance().getServerConnection().getClientID(),
                    GuiManager.getInstance().getServerConnection().getServerID(),
                    username));
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
                GuiManager.getInstance().setMyUsername(joinCommand.getUsername());
                GuiManager.setLayout("fxml/waitScene.fxml");
            } else {
                connectButton.setDisable(false);
                backButton.setDisable(false);
                infoLabel.setText("Username too short or already taken, try another one\n");
            }
        }
    }
}


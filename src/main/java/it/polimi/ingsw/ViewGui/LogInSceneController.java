package it.polimi.ingsw.ViewGui;

import it.polimi.ingsw.controller.CommandType;
import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.controller.JoinCommand;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;


import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

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
     * Reads username from username field and attempts join
     */
    //TODO handle properly false ack join
    @FXML
    private void onConnectButtonClick() {
        final String username = usernameField.getText();

        backButton.setDisable(true);
        connectButton.setDisable(true);

        if (GuiManager.getInstance().connect(username)) {
            // change to waiting scene
        } else {
            upperLabel.setText("Couldn't connect");
            lowerLabel.setText("Try again later");
            GuiManager.setLayout("fxml/mainScene.fxml");
        }
    }

        //TODO make it connect when i hit enter
    public void buttonPressed(KeyEvent e){
        if(e.getCode().toString().equals("ENTER")){
            onConnectButtonClick();
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
                GuiManager.setLayout("fxml/mainScene.fxml");
                /*
                upperLabel.setText("Couldn't connect");
                if (!joinCommand.isValidUsername()) {
                    lowerLabel.setText("Username taken");
                } else
                    lowerLabel.setText("Try again later");

                 */
            }
        }
    }
}


package it.polimi.ingsw.ViewGui;

import it.polimi.ingsw.controller.CommandType;
import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.controller.StartCommand;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import javafx.scene.control.Button;

public class WaitSceneController {

    private GuiManager guiManager;



    @FXML
    private Pane mainPane;
    @FXML
    private Label centerLabel;
    @FXML
    private Button startGameButton;

    @FXML
    public void initialize() {
        guiManager = GuiManager.getInstance();
        guiManager.setWaitSceneController(this);
        startGameButton.setDisable(true);
        startGameButton.setText("Start Game");

    }

    /**
     * Handles the start button click
     */
    @FXML
    void onStartGameButtonClick(){
        GuiManager.getInstance().getServerConnection().send(new CommandWrapper(CommandType.START, new StartCommand(GuiManager.getInstance().getServerConnection().getClientID(), GuiManager.getInstance().getServerConnection().getServerID())));
    }

    /**
     * Allows the host to start the game in 2 player mode
     */
    //TODO check that the ack join is true, disable in case the second client has disconnected meanwhile
    void onSecondClientConnection(){
        centerLabel.setText("A second player has connected, you can start the game or wait for a third player");
        startGameButton.setDisable(false);
    }

    /**
     * Handles the start game command
     * @param startCommand command from the server
     */
    void onStart(StartCommand startCommand) {
        GuiManager.setLayout("fxml/chooseGodsScene.fxml");
    }
}


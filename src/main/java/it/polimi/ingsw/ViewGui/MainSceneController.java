package it.polimi.ingsw.ViewGui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

public class MainSceneController {



    @FXML
    private Pane mainPane;
    @FXML
    private Button startButton;
    @FXML
    private Button exitButton;

    @FXML
    public void initialize() {
    }


    /**
     * Handles the start button click
     */
    @FXML
    private void onStartButtonClick(){
        GuiManager.setLayout("fxml/loginScene.fxml");
    }

    /**
     * Handles the exit button click
     */
    @FXML
    private void onExitButtonClick(){
        System.exit(0);
    }

}

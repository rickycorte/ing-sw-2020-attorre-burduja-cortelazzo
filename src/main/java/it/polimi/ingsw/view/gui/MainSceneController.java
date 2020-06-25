package it.polimi.ingsw.view.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.control.Alert;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainSceneController  {
    @FXML
    public ImageView logo;
    @FXML
    private Pane mainPane;
    @FXML
    private Button startButton;
    @FXML
    private Button exitButton;

    @FXML
    public void initialize() {
        GuiManager.getInstance().setMainSceneController(this);
    }

    /**
     * Handles the start button click
     */
    @FXML
    private void onStartButtonClick(){
        GuiManager.setLayout("fxml/loginScene.fxml");
    }

    @FXML
    private void onGodsButtonClick(){
        GuiManager.setLayout("fxml/godsScene.fxml");
    }

    /**
     * Informs the client that the server is no longer reachable
     */
    void onServerShutDown(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fatal error");
        alert.setHeaderText("Something went wrong, couldn't reach the server anymore :(");
        alert.setContentText("Don't worry, our best engineers are working on it");
        alert.showAndWait();
    }

    /**
     * Handles the exit button click
     */
    @FXML
    private void onExitButtonClick(){
        System.exit(0);
    }

}

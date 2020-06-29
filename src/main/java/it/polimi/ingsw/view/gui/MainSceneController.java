package it.polimi.ingsw.view.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.control.Alert;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable {
    private Settings settings;

    @FXML
    public ImageView logo;
    @FXML
    private Pane mainPane;
    @FXML
    private Button startButton;
    @FXML
    private Button godsButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button exitButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiManager.getInstance().setMainSceneController(this);
        settings = GuiManager.getInstance().getSettings();
        initializeStyleSheet();
    }

    //----------------------------------------Initialize Methods--------------------------------------------------------
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
        toStyle.add(startButton);
        toStyle.add(godsButton);
        toStyle.add(settingsButton);
        toStyle.add(exitButton);
        return toStyle;
    }

    //---------------------------------------Button Click Handlers------------------------------------------------------

    /**
     * Handles the start button click
     */
    @FXML
    private void onStartButtonClick(){
        GuiManager.setLayout("fxml/loginScene.fxml");
    }

    /**
     * Handles the gods button click
     */
    @FXML
    private void onGodsButtonClick(){
        GuiManager.setLayout("fxml/godsScene.fxml");
    }

    /**
     * Handles the settings button click
     * @param event user's button click
     */
    @FXML
    public void onSettingsButtonClick(ActionEvent event) {
        GuiManager.setLayout("fxml/settingsScene.fxml");
    }

    /**
     * Handles the exit button click
     */
    @FXML
    private void onExitButtonClick(){
        System.exit(0);
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Method called when the server is no longer reachable
     */
    void onServerShutDown(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fatal error");
        alert.setHeaderText("Something went wrong, couldn't reach the server anymore :(");
        alert.setContentText("Don't worry, our best engineers are working on it\nMeanwhile try restarting the game");
        Optional<ButtonType> res = alert.showAndWait();
        if(res.get() == ButtonType.OK){
            System.exit(0);
        }
    }
}

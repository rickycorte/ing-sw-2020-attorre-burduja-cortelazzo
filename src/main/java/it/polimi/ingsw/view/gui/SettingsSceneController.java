package it.polimi.ingsw.view.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SettingsSceneController implements Initializable {
    private Settings settings;

    @FXML
    private AnchorPane root;
    @FXML
    private VBox ambientPane;
    @FXML
    private CheckBox themeCheckBox;
    @FXML
    private Button backButton;
    @FXML
    private Button saveButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiManager.getInstance().setSettingsController(this);
        settings = GuiManager.getInstance().getSettings();
        initializeStyleSheet();
        initializeCheckBox();
    }

    //--------------------------------------------Initialize Methods----------------------------------------------------

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
        toStyle.add(root);
        toStyle.add(ambientPane);
        toStyle.add(backButton);
        toStyle.add(saveButton);
        return toStyle;
    }

    /**
     * Initializes the checkbox according to the settings
     */
    private void initializeCheckBox(){
        if(settings.getTheme() == Settings.Themes.LIGHT)
            themeCheckBox.setSelected(false);
        else
            themeCheckBox.setSelected(true);
    }

    //--------------------------------------------Button Click Handlers-------------------------------------------------

    /**
     * Handles the user's click on back button
     * @param event user's click
     */
    @FXML
    public void onBackButtonClick(ActionEvent event) {
        GuiManager.setLayout("fxml/mainScene.fxml");
    }

    /**
     * Handles the user's click on save button
     * @param event user's click
     */
    @FXML
    public void onSaveButtonClick(ActionEvent event) {
        if (themeCheckBox.isSelected()){
            settings.setTheme(Settings.Themes.DARK);
            GuiManager.setLayout("fxml/mainScene.fxml");
        }else {
            settings.setTheme(Settings.Themes.LIGHT);
            GuiManager.setLayout("fxml/mainScene.fxml");
        }
    }
}

package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.view.Card;
import it.polimi.ingsw.view.CardCollection;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class GodsSceneController implements Initializable {

    private int currentGod;                                     //God viewed
    private CardCollection cardCollection;                      //Collection of all the gods
    private Settings settings;                                  //Reference to the settings class

    @FXML
    private AnchorPane mainPane;
    @FXML
    private Button prevGodButton;
    @FXML
    private Button nextGodButton;
    @FXML
    private Button okButton;
    @FXML
    private ImageView godImage;
    @FXML
    private Label powerLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private VBox centerRoot;
    @FXML
    private HBox secondRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiManager.getInstance().setGodsController(this);
        settings = GuiManager.getInstance().getSettings();
        initializeStyleSheet();
        cardCollection = new CardCollection();
        currentGod = 1;
        prevGodButton.setDisable(true);
        initializeBindings();
        okButton.setStyle("-fx-scale-x: 1.2");
        okButton.setStyle("-fx-scale-y: 1.2");
        prevGodButton.setStyle("-fx-scale-y: 0.8");
        prevGodButton.setStyle("-fx-scale-x: 0.8");
        nextGodButton.setStyle("-fx-scale-y: 0.8");
        nextGodButton.setStyle("-fx-scale-x: 0.8");
        loadGod(currentGod);
    }

    //---------------------------------------Initialize Methods---------------------------------------------------------

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
        toStyle.add(okButton);
        toStyle.add(prevGodButton);
        toStyle.add(nextGodButton);
        toStyle.add(centerRoot);
        toStyle.add(descriptionLabel);
        toStyle.add(powerLabel);
        return toStyle;
    }

    /**
     * This methods initializes bindings to make things resizable
     */
    private void initializeBindings(){
        secondRoot.minHeightProperty().bind(centerRoot.heightProperty().multiply(0.8));
        secondRoot.maxHeightProperty().bind(centerRoot.heightProperty().multiply(0.9));
        godImage.setPreserveRatio(true);
        godImage.fitHeightProperty().bind(secondRoot.heightProperty().multiply(0.9));
    }

    //---------------------------------Button Click Handlers------------------------------------------------------------

    /**
     * Handles prev god button click by loading the previous god
     * @param actionEvent user's click
     */
    @FXML
    public void onPrevGodClick(ActionEvent actionEvent) {
        if(currentGod == 8)
            currentGod = 6;
        else
            currentGod--;
        if(currentGod == 1) {
            prevGodButton.setDisable(true);
        }
        loadGod(currentGod);
        nextGodButton.setDisable(false);
    }

    /**
     * Handles the back button click by going to the main scene
     * @param actionEvent user's click
     */
    @FXML
    public void onOkButtonClick(ActionEvent actionEvent) {
        GuiManager.setLayout("fxml/mainScene.fxml");
    }

    /**
     * Handles the next button click by loading the next god
     * @param actionEvent user's click
     */
    @FXML
    public void onNextButtonClick(ActionEvent actionEvent) {
        if(currentGod == 6)
            currentGod = 8;
        else
            currentGod ++;
        if(currentGod == 10) {
            nextGodButton.setDisable(true);
        }
        loadGod(currentGod);
        prevGodButton.setDisable(false);
    }

    //-------------------------------------Utility Methods--------------------------------------------------------------

    /**
     * Loads a god image given it's ID
     * @param godID ID of the god to load
     */
    private void loadGod(int godID){
        Card card = cardCollection.getCard(godID);
        descriptionLabel.setText(card.getDescription());
        powerLabel.setText(card.getPower());

        URL url = getClass().getResource(String.format("/img/gods/%02d.png", godID));
        try (InputStream stream = url.openStream()) {
            godImage.setImage(new Image(stream));
            doFadeTransition(godImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Does a simple fade transition of 600 millis on a given node
     * @param node to play the transition on
     */
    private void doFadeTransition(Node node){
        FadeTransition ft = new FadeTransition(Duration.millis(600), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setCycleCount(1);
        ft.play();
    }
}

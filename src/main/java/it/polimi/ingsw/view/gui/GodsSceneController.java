package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.view.Card;
import it.polimi.ingsw.view.CardCollection;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class GodsSceneController implements Initializable {

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

    private int currentGod;

    private CardCollection cardCollection;

    @FXML
    public void initialize(){

    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        prevGodButton.setOpacity(0);
    }

    void initializeBindings(){
        secondRoot.minHeightProperty().bind(centerRoot.heightProperty().multiply(0.8));
        secondRoot.maxHeightProperty().bind(centerRoot.heightProperty().multiply(0.9));
        godImage.setPreserveRatio(true);
        godImage.fitHeightProperty().bind(secondRoot.heightProperty().multiply(0.9));
    }

    void loadGod(int godID){
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
     * initializes a simple fade transition of 600 millis on a given node
     * @param node to play the transition on
     */
    private void doFadeTransition(Node node){
        FadeTransition ft = new FadeTransition(Duration.millis(600), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setCycleCount(1);
        ft.play();
    }

    /**
     * Handles prev god button click by loading the previous god
     * @param actionEvent user's click
     */
    @FXML
    public void onPrevGodClick(ActionEvent actionEvent) {
        if(nextGodButton.getOpacity() == 0)
            nextGodButton.setOpacity(1);
        if(currentGod == 8)
            currentGod = 6;
        else
            currentGod--;
        if(currentGod == 1) {
            prevGodButton.setDisable(true);
            prevGodButton.setOpacity(0);
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
        if(prevGodButton.getOpacity() == 0)
            prevGodButton.setOpacity(1);
        if(currentGod == 6)
            currentGod = 8;
        else
            currentGod ++;
        if(currentGod == 10) {
            nextGodButton.setDisable(true);
            nextGodButton.setOpacity(0);
        }
        loadGod(currentGod);
        prevGodButton.setDisable(false);


    }
}

package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.controller.FilterGodCommand;
import it.polimi.ingsw.controller.PickGodCommand;
import it.polimi.ingsw.view.CardCollection;
import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class ChooseGodsSceneController implements Initializable {

    private List<Integer> chosenGodsList;
    private int chosenGod;
    private State state;
    private enum State{
        WAITING, PICKING, FILTERING
    }
    private CardCollection cardCollection;

    @FXML
    private GridPane gridPane;
    @FXML
    private Label topLabel;
    @FXML
    private Button sendButton;
    @FXML
    private Button cancelButton;

    @FXML
    public void initialize()  { }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiManager.getInstance().setChooseGodsSceneController(this);
        chosenGodsList = new ArrayList<>();
        cardCollection = new CardCollection();
        state = State.WAITING;
        topLabel.setText("Here are all the available gods");
        initializeButtons();
        enableButtons(false);
        initializeImages();
    }

    /**
     * This method initializes the buttons's text
     */
    private void initializeButtons() {
        cancelButton.setText("Cancel Selection");
        if (GuiManager.getInstance().imHost())
            sendButton.setText("Choose Gods");
        else
            sendButton.setText("Choose God");
    }

    /**
     * This method enables/disables all the buttons
     */
    private void enableButtons(boolean enable){
        sendButton.setDisable(!enable);
        cancelButton.setDisable(!enable);
    }

    /**
     * Enables / Disables grid pane controls
     * @param par boolean to indicate if to enable or disable
     */
    void enableControls(boolean par) {
        for (Node aChild : gridPane.getChildren()) {
            aChild.setDisable(!par);
        }
    }

    /**
     * Handles the click on Cancel Button by resetting selected gods
     * @param actionEvent user's click on the button
     */
    @FXML
    public void onCancelButtonClick(ActionEvent actionEvent) {
        chosenGodsList = new ArrayList<>();
        chosenGod = 0;
        resetImagesEffect();
        enableButtons(false);
        enableControls(true);
    }

    /**
     * Handles the click of Send Button by sending the selected worker/workers
     * @param actionEvent user's click on the button
     */
    @FXML
    public void onSendButtonClick(ActionEvent actionEvent) {
        if(GuiManager.getInstance().imHost()){
            int[] chosenGodIDsArray = listToArray(chosenGodsList);
            GuiManager.getInstance().send(FilterGodCommand.makeReply(GuiManager.getInstance().getServerConnection().getClientID(), GuiManager.getInstance().getServerConnection().getServerID(), chosenGodIDsArray));
            enableButtons(false);
            GuiManager.setLayout("fxml/firstPlayerPickScene.fxml");
        }else {
            GuiManager.getInstance().send(PickGodCommand.makeReply(GuiManager.getInstance().getServerConnection().getClientID(), GuiManager.getInstance().getServerConnection().getServerID(), chosenGod));
            enableButtons(false);
            GuiManager.setLayout("fxml/gameScene.fxml");
        }
    }

    /**
     * Utility method, converts a list to an array
     * @param chosenGodsList list to convert
     * @return converted list
     */
    private int[] listToArray(List<Integer> chosenGodsList){
        int[] chosenGodsArray = new int[chosenGodsList.size()];
        for(int i = 0; i < chosenGodsList.size(); i++){
            chosenGodsArray[i] = chosenGodsList.get(i);
        }
        return chosenGodsArray;
    }

    /**
     * Handles the user's click on a tile
     * @param mouseEvent the user's click
     */
    @FXML
    private void onClicked(javafx.scene.input.MouseEvent mouseEvent) {
        ImageView my_image = (ImageView) mouseEvent.getSource();    //Image I clicked on

        if (my_image.isDisabled()) return;

        if (state == State.FILTERING) {
            cancelButton.setDisable(false);

            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.GREEN);
            dropShadow.setRadius(17.0);
            my_image.setEffect(dropShadow);

            int godToAdd = Integer.parseInt(my_image.getId());
            if(!chosenGodsList.contains(godToAdd)) {
                chosenGodsList.add(Integer.parseInt(my_image.getId()));
            }

            if (chosenGodsList.size() == GuiManager.getInstance().getConnectedIDS().length ) {
                sendButton.setDisable(false);
                enableControls(false);
            }
        } else if (state == State.PICKING) {
            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.GREEN);
            my_image.setEffect(dropShadow);

            chosenGod = Integer.parseInt(my_image.getId());
            enableButtons(true);
            enableControls(false);
        }
    }

    /**
     * Loads all the images on the grid pane
     */
    private void initializeImages() {
        int row = 0;
        int col = 0;
        for (int i = 1; i < 11; i++) {
            if(i == 7) continue; //Hermes is not included in the game
            ImageView image = new ImageView();
            URL url = getClass().getResource(String.format("/img/gods/%02d.png", i));
            try (InputStream stream = url.openStream()) {
                image.setImage(new Image(stream));
            } catch (IOException e) {
                e.printStackTrace();
            }

            image.setId(String.valueOf(i));
            image.setOnMouseEntered((e)-> {
                if(!chosenGodsList.contains(Integer.valueOf( image.getId()))) {
                    image.setStyle("-fx-effect: dropshadow(gaussian, #ffffff, 15, 0.2, 0, 0)");
                }
            });
            image.setOnMouseExited((e)-> {
                image.setStyle(null);
            });

            ColorAdjust sat = new ColorAdjust();
            sat.setSaturation(-0.8);
            image.setEffect(sat);

            String description = cardCollection.getCard(i).getPower();
            Tooltip.install(image, new Tooltip(description));

            gridPane.add(image, row, col);
            col++;
            if(col == 3) {
                col = 0;
                row++;
            }

            image.fitHeightProperty().bind((gridPane.heightProperty().multiply(0.29)));
            image.fitWidthProperty().bind((gridPane.widthProperty().multiply(0.29)));
            image.setOnMouseClicked(this::onClicked);
            image.setPreserveRatio(true);
            image.setPickOnBounds(true);
            GridPane.setHalignment(image, HPos.CENTER);
            GridPane.setValignment(image, VPos.CENTER);
        }
    }

    /**
     * Handles the Filter Gods command by entering filter state
     * @param cmd command from the server
     */
    void onFilterGodsCommand( CommandWrapper cmd){
        if(GuiManager.getInstance().isForMe(cmd)){
            startFilterMode();
        }else{
            topLabel.setText("The host is picking the allowed Gods");
        }
    }


    /**
     * Handle the pick god command by entering pick state
     * @param cmd command from the server
     */
    void onPickGodCommand( CommandWrapper cmd){
        if(GuiManager.getInstance().isForMe(cmd)){
            int[] allowedIDs = cmd.getCommand(PickGodCommand.class).getAllowedGodsIDS();
            startPickMode(allowedIDs);
        }else{
            topLabel.setText("Other players are picking their God, wait for your turn");
        }
    }

    /**
     * Starts the filtering mode by enabling the controls
     */
    private void startFilterMode(){
        topLabel.setText("Choose the Gods you want to be allowed in this game");
        this.state = State.FILTERING;
        resetImagesEffect();
        enableControls(true);
    }

    /**
     * This method resets the effect of all the images on the grid
     */
    private void resetImagesEffect(){
        ObservableList<Node> images = gridPane.getChildren();
        for(Node aNode: images){
            aNode.setEffect(null);
        }
    }

    /**
     * Starts picking state by removing all the not allowed IDs
     * @param ids IDs allowed for this game
     */
    private void startPickMode(int[] ids) {
        topLabel.setText("Your turn to choose, click on a God to choose it");
        enableControls(true);
        this.state = State.PICKING;
        List<Node> list = new ArrayList<>();
        for (Node aChild : gridPane.getChildren()) {
            boolean should_remove = true;
            if (aChild instanceof ImageView) {
                for (int i : ids) {
                    if (i == Integer.parseInt(aChild.getId())) {
                        aChild.setEffect(null);
                        should_remove = false;
                        break;
                    }
                }
                if (should_remove) {
                    list.add(aChild);
                }
            }
        }
        gridPane.getChildren().removeAll(list);
    }
}


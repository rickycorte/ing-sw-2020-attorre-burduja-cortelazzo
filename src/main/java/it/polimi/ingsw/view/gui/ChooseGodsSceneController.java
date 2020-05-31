package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.controller.FilterGodCommand;
import it.polimi.ingsw.controller.PickGodCommand;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

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

    @FXML
    private GridPane gridPane;
    @FXML
    private Label topLabel;
    @FXML
    private Button sendButton;
    @FXML
    private Button cancelButton;

    @FXML
    public void initialize()  {

    }

    //TODO add cards info panel
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiManager.getInstance().setChooseGodsSceneController(this);
        int numberOfConnectedPlayers = GuiManager.getInstance().getConnectedIDS().length;
        //chosenGodIDsArray = new int[numberOfConnectedPlayers];
        chosenGodsList = new ArrayList<>();
        state = State.WAITING;
        topLabel.setText("HERE ARE ALL THE AVAILABLE GODS");
        initializeButtons();
        enableButtons(false);
        initializeImages();
    }

    /**
     * This method initializes the buttons's text
     */
    private void initializeButtons() {
        cancelButton.setText("Cancel Selection");
        if (imTheHost())
            sendButton.setText("Send Gods");
        else
            sendButton.setText("Choose God");
    }

    /**
     * This method enables all the buttons
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
        //chosenGodIDsArray = new Integer[GuiManager.getInstance().getConnectedIDS().length];
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
        if(imTheHost()){
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
    int[] listToArray(List<Integer> chosenGodsList){
        int[] chosenGodsArray = new int[chosenGodsList.size()];
        for(int i = 0; i < chosenGodsList.size(); i++){
            chosenGodsArray[i] = chosenGodsList.get(i);
        }
        return chosenGodsArray;
    }

    /**
     * This method checks if i'm the host or not
     * @return true if i'm the host, false otherwise
     */
    private boolean imTheHost() {
        return GuiManager.getInstance().getServerConnection().getClientID() == GuiManager.getInstance().getHostID();
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
            dropShadow.setRadius(15.0);

            my_image.setEffect(dropShadow);

            int godToAdd = Integer.parseInt(my_image.getId());
            if(!chosenGodsList.contains(godToAdd)) {
                chosenGodsList.add(Integer.parseInt(my_image.getId()));
            }
            //chosenGodIDs[index] = Integer.parseInt(my_image.getId());
            //index++;
            if (chosenGodsList.size() == GuiManager.getInstance().getConnectedIDS().length ) {
                sendButton.setDisable(false);
                enableControls(false);
            }
            System.out.println("Image pressed");
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
    void initializeImages() {
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
                if(!chosenGodsList.contains(Integer.valueOf( image.getId())))
                    image.setStyle("-fx-effect: dropshadow(gaussian, #ffffff, 15, 0.2, 0, 0)");
            });
            image.setOnMouseExited((e)-> {
                image.setStyle(null);
            });

            ColorAdjust sat = new ColorAdjust();
            sat.setSaturation(-0.8);
            image.setEffect(sat);

            gridPane.add(image, row, col);
            col++;
            if(col == 3) {
                col = 0;
                row++;
            }
            //image.setFitHeight(112);
            //image.setFitWidth(175);
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
            topLabel.setText("WAIT, THE HOST IS CHOOSING THE ALLOWED CARDS");
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
            topLabel.setText("WAIT, OTHER PLAYERS ARE CHOOSING THEIR GOD");
        }
    }

    /**
     * Starts the filtering mode by enabling the controls
     */
    void startFilterMode(){
        topLabel.setText("CHOOSE GODS YOU WANT IN THIS GAME");
        this.state = State.FILTERING;
        resetImagesEffect();
        enableControls(true);
    }

    /**
     * This method resets the effect of all the images on the grid
     */
    void resetImagesEffect(){
        ObservableList<Node> images = gridPane.getChildren();
        for(Node aNode: images){
            aNode.setEffect(null);
            //aNode.setStyle(null);
        }
    }

    /**
     * Starts picking state by removing all the not allowed IDs
     * @param ids IDs allowed for this game
     */
    void startPickMode(int[] ids) {
        topLabel.setText("CLICK ON A CARD TO CHOOSE IT");
        enableControls(true);
        this.state = State.PICKING;
        List<Node> list = new ArrayList<>();
        for (Node aChild : gridPane.getChildren()) {
            boolean should_remove = true;
            if (aChild instanceof ImageView) {
                for (int i : ids) {
                    if (i == Integer.parseInt(aChild.getId())) {
                        //aChild.setDisable(true);
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


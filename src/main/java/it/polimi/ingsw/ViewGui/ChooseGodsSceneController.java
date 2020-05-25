package it.polimi.ingsw.ViewGui;

import it.polimi.ingsw.controller.CommandType;
import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.controller.FilterGodCommand;
import it.polimi.ingsw.controller.PickGodCommand;
import it.polimi.ingsw.network.client.Client;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class ChooseGodsSceneController implements Initializable {

    int[] godIDS;
    int index;

    int state; // 0 - default mode. 1 - need to pick my god. 2 - need to pick gods for others.

    @FXML
    private GridPane gridPane;

    @FXML
    private Label topLabel;


    @FXML
    public void initialize()  {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiManager.getInstance().setChooseGodsSceneController(this);
        int numberOfConnectedPlayers = GuiManager.getInstance().getConnectedIDS().length;
        godIDS = new int[numberOfConnectedPlayers];
        index = 0;
        state = 0;
        topLabel.setText("Here are all the available cards");
        initializeImages();
    }

    /**
     * Enables / Disables grid pane
     * @param par boolean to indicate if to enable or disable
     */
    void enableControls(boolean par) {
        for (Node aChild : gridPane.getChildren()) {
            aChild.setDisable(!par);
        }
    }

    /**
     * Handles the user's click on a tile
     * @param mouseEvent the user's click
     */
    @FXML
    public void onClicked(javafx.scene.input.MouseEvent mouseEvent) {
        ImageView my_image = (ImageView) mouseEvent.getSource();    //Image i clicked on
        if (my_image.isDisabled()) return;
        if (state == 2) {
            godIDS[index] = Integer.parseInt(my_image.getId());
            index++;
            if (index == GuiManager.getInstance().getConnectedIDS().length ) {
                GuiManager.getInstance().send(new CommandWrapper(CommandType.FILTER_GODS, new FilterGodCommand(GuiManager.getInstance().getServerConnection().getClientID(), GuiManager.getInstance().getServerConnection().getServerID(), godIDS)));
                enableControls(false);
                GuiManager.setLayout("fxml/firstPlayerPickScene.fxml");
            }
            System.out.println("Image pressed");
        } else if (state == 1) {
            int image_int = Integer.parseInt(my_image.getId());
            GuiManager.getInstance().send(new CommandWrapper(CommandType.PICK_GOD, new PickGodCommand(GuiManager.getInstance().getServerConnection().getClientID(), GuiManager.getInstance().getServerConnection().getServerID(), image_int)));
            enableControls(false);
            GuiManager.setLayout("fxml/gameScene.fxml");
        }
    }

    //TODO greyout images when i cant click them, add hover effect, add click effect, add cards info panel, a lot of stuff...

    /**
     * Loads all the images on the grid pane
     */
    void initializeImages() {
        int row = 0;
        int col = 0;
        for (int i = 1; i < 11; i++) {
            if(i == 7) continue;
            ImageView image = new ImageView();
            URL url = getClass().getResource(String.format("/img/gods/%02d.png", i));
            try (InputStream stream = url.openStream()) {
                image.setImage(new Image(stream));
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setSaturation(-0.8);
            image.setEffect(colorAdjust);
             */
            image.setId(String.valueOf(i));
            gridPane.add(image, col, row);
            row++;
            if(row == 3) {
                row = 0;
                col++;
            }
            image.setFitHeight(112);
            image.setFitWidth(175);
            image.setOnMouseClicked(this::onClicked);
            image.setPreserveRatio(true);
            image.setPickOnBounds(true);
            GridPane.setHalignment(image, HPos.CENTER);
            GridPane.setValignment(image, VPos.CENTER);
        }
    }

    /**
     * Handles the Filter Gods command by entering filter mode
     * @param cmd command from the server
     */
    void onFilterGodsCommand( CommandWrapper cmd){
        if(GuiManager.getInstance().isForMe(cmd)){
            startFilterMode();
        }else{
            topLabel.setText("Wait while the host is choosing the allowed cards");
        }
    }

    /**
     * Handle the pick god command
     * @param cmd command from the server
     */
    void onPickGodCommand( CommandWrapper cmd){
        if(GuiManager.getInstance().isForMe(cmd)){
            int[] allowedIDs = cmd.getCommand(PickGodCommand.class).getGodID();
            startPickMode(allowedIDs);
        }else{
            topLabel.setText("Wait, other players are choosing their cards");
        }
    }

    /**
     * Starts the filtering mode by enabling the controls
     */
    void startFilterMode(){
        topLabel.setText("Choose the cards you want in this game");
        this.state = 2;
        enableControls(true);
    }

    /**
     * Starts picking mode by removing all the not allowed IDs
     * @param ids IDs allowed for this game
     */
    void startPickMode(int[] ids) {
        topLabel.setText("Click on a card to choose it");
        enableControls(true);
        this.state = 1;
        List<Node> list = new ArrayList<>();
        for (Node aChild : gridPane.getChildren()) {
            boolean should_remove = true;
            if (aChild instanceof ImageView) {
                for (int i : ids) {
                    if (i == Integer.parseInt(aChild.getId())) {
                        //aChild.setDisable(true);
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


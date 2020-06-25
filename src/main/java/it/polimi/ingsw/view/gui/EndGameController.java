package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.controller.EndGameCommand;
import it.polimi.ingsw.controller.JoinCommand;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class EndGameController implements Initializable {
    private int winnerID;

    private EndGameCommand endGameCommand;

    private CommandWrapper commandWrapper;
    @FXML
    private Label infoLabel;
    @FXML
    private StackPane winnerPane;
    @FXML
    private ImageView winnerGod;
    @FXML
    private ImageView winnerGlow;
    @FXML
    private VBox ambientPane;
    @FXML
    private HBox outerBox;
    @FXML
    private VBox innerBox;
    @FXML
    private ImageView leftImage;
    @FXML
    private ImageView rightImage;

    private Map<Integer, String> idsUsernamesMap;

    private Map<Integer, Integer> idsGodsMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiManager.getInstance().setEndGameController(this);
        winnerID = GuiManager.getInstance().getWinnerID();
        idsGodsMap = GuiManager.getInstance().getIDsGodsMap();
        idsUsernamesMap= GuiManager.getInstance().getIDsUsernameMap();
        initializeBindings();
    }

    /**
     * Initializes bindings to make everything scale correctly
     */
    private void initializeBindings(){
        outerBox.minHeightProperty().bind(ambientPane.heightProperty().multiply(0.8));
        innerBox.maxHeightProperty().bind(outerBox.heightProperty().multiply(0.95));
        winnerPane.maxHeightProperty().bind(innerBox.heightProperty().multiply(0.96));
        winnerPane.minHeightProperty().bind(innerBox.heightProperty().multiply(0.94));
        winnerPane.minWidthProperty().bind(outerBox.widthProperty().multiply(0.4));

        leftImage.setPreserveRatio(true);
        leftImage.fitWidthProperty().bind(outerBox.widthProperty().multiply(0.3));

        rightImage.setPreserveRatio(true);
        rightImage.fitWidthProperty().bind(outerBox.widthProperty().multiply(0.3));

    }

    /**
     * Initializes the label
     */
    void initializeLabel(){
        if(winnerID > 0) {
            String winnerUsername = idsUsernamesMap.get(winnerID);
            infoLabel.setText(winnerUsername + " won, congratulations");
        }else if( endGameCommand.isMatchStillRunning()){
            infoLabel.setText("You Lost...");
        }else if(endGameCommand.isMatchInterrupted()){
            infoLabel.setText("Game interrupted because a player has disconnected");
        }
    }

    /**
     * Initializes the image god the winner god
     */
    void initializeGods(){
        URL podiumURL = getClass().getResource("/img/common/podium_gold.png");
        ImageView godPodium = getImageImageViewByURL(podiumURL);
        godPodium.setPreserveRatio(true);
        godPodium.fitWidthProperty().bind(winnerGod.fitWidthProperty().multiply(1.1));
        winnerPane.getChildren().add(godPodium);

        int winnerGodID = idsGodsMap.get(winnerID);
        URL url1 = getClass().getResource(String.format("/img/gods/podium/%02d.png", winnerGodID));
        winnerGod = getImageImageViewByURL(url1);
        winnerGod.setPreserveRatio(true);
        //winnerGod.fitWidthProperty().bind(winnerPane.widthProperty().multiply(0.7));
        winnerGod.setTranslateY(-70.0);
        winnerGod.fitHeightProperty().bind(winnerPane.heightProperty().multiply(0.8));
        winnerPane.getChildren().add(winnerGod);
        //winnerGod.fitHeightProperty().bind(outerBox.heightProperty().multiply(0.9));
    }

    /**
     * Utility method - Gets an ImageView given the URL
     * @param url URL of the ImageView
     * @return ImageView in the given URL
     */
    private ImageView getImageImageViewByURL(URL url) {
        ImageView imageView = new javafx.scene.image.ImageView();
        try(InputStream inputStream = url.openStream()){
            imageView.setImage(new Image(inputStream));
        }catch (IOException e){
            System.out.println("[GAME SCENE] Couldn't access buildings resources");
            e.printStackTrace();
        }
        return imageView;
    }

    /**
     * Handles the end game command
     * @param cmd end game command
     */
    void onEndGameCommand(CommandWrapper cmd) {
        endGameCommand = cmd.getCommand(EndGameCommand.class);
        if(endGameCommand.getWinnerID() < 0){
            leftImage.setImage(null);
            rightImage.setImage(null);
            initializeLabel();

        }else {
            initializeGods();
            initializeLabel();
        }
    }

    /**
     * Handles the user's click on quit game button
     */
    @FXML
    void onQuitButtonClick(){
        System.exit(0);
    }

    /**
     * Handles user's click on play another game button
     */
    @FXML
    void onAnotherButtonClick(){
        GuiManager.getInstance().send(
                JoinCommand.makeRequest(GuiManager.getInstance().getServerConnection().getClientID(),
                        GuiManager.getInstance().getServerConnection().getServerID(),
                        GuiManager.getInstance().getMyUsername())
        );
    }
}

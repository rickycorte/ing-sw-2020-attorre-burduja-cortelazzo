package it.polimi.ingsw.view.gui;

import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;



public class EndGameController implements Initializable {
    private int winnerID;

    @FXML
    private Label infoLabel;
    @FXML
    private StackPane winnerPane;
    @FXML
    private ImageView winnerGod;
    @FXML
    private ImageView winnerGlow;
    @FXML
    private StackPane secondPlayerPane;
    @FXML
    private ImageView player2God;
    @FXML
    private StackPane thirdPlayerPane;
    @FXML
    private ImageView player3God;

    private Map<Integer, String> idsUsernamesMap;

    private Map<Integer, Integer> idsGodsMap;




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiManager.getInstance().setEndGameController(this);
        winnerID = GuiManager.getInstance().getWinnerID();
        idsGodsMap = GuiManager.getInstance().getIDsGodsMap();
        idsUsernamesMap= GuiManager.getInstance().getIDsUsernameMap();

        initializeLabel();

        initializeGods();

        //infoLabel.setText(String.valueOf(winnerID));

    }

    void initializeLabel(){
        String winnerUsername = idsUsernamesMap.get(winnerID);
        infoLabel.setText(winnerUsername + " won, congrats");
    }

    void initializeGods(){
        URL glowUrl = getClass().getResource("/img/common/glow.png");
        winnerGlow = getImageImageViewByURL(glowUrl);

        winnerGlow.setPreserveRatio(true);
        winnerGlow.fitWidthProperty().bind(winnerPane.widthProperty().multiply(0.9));
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(5), winnerGlow);
        rotateTransition.setAutoReverse(true);
        rotateTransition.setCycleCount(5);
        rotateTransition.setByAngle(180);
        rotateTransition.play();
        winnerPane.getChildren().add(winnerGlow);

        int winnerGodID = idsGodsMap.get(winnerID);
        URL url1 = getClass().getResource(String.format("/img/gods/podium/%02d.png", winnerGodID));
        winnerGod = getImageImageViewByURL(url1);
        winnerGod.setPreserveRatio(true);
        winnerGod.fitWidthProperty().bind(winnerPane.widthProperty().multiply(0.7));
        winnerGod.setTranslateY(-70.0);
        winnerPane.getChildren().add(winnerGod);
    }


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
}

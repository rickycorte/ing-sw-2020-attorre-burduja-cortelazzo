package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.controller.FirstPlayerPickCommand;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;


public class FirstPlayerPickSceneController implements Initializable {

    @FXML
    public StackPane player1Pane;
    @FXML
    public StackPane player2Pane;
    @FXML
    public StackPane player3Pane;
    @FXML
    private Button button1;
    @FXML
    private Button button2;
    @FXML
    private Button button3;
    @FXML
    private Label label;
    @FXML
    private ImageView player1Podium;
    @FXML
    private ImageView player1God;
    @FXML
    private ImageView player2Podium;
    @FXML
    private ImageView player2God;
    @FXML
    private ImageView player3Podium;
    @FXML
    private ImageView player3God;

    private Map<Integer, Integer> IDsGodIDsMap;

    private Map<Integer, String> idsUsernamesMap;

    private FirstPlayerPickCommand receivedCommand;

    @FXML
    public void initialize(){
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiManager.getInstance().setFirstPlayerPickSceneController(this);
        label.setText("Wait, other players are choosing their Gods");
        disableButtons();
        setPodiums();
    }

    /**
     * Disables all the buttons
     */
    private void disableButtons(){
        button1.setDisable(true);
        button2.setDisable(true);
        button3.setDisable(true);
    }

    /**
     * Handles the first player pick command
     * @param cmd command coming from the server
     */
    void onFirstPlayerPickCommand(CommandWrapper cmd){
        receivedCommand = cmd.getCommand(FirstPlayerPickCommand.class);
        IDsGodIDsMap = GuiManager.getInstance().getIDsGodsMap();
        idsUsernamesMap = GuiManager.getInstance().getIDsUsernameMap();
        setButtons();
        setGodImages();
        label.setText("Choose the first player");
    }

    /**
     * Gets the podium url and calls method to set it
     */
    private void setPodiums(){
        URL urlPodium = getClass().getResource("/img/common/podium.png");
        setupPodiumPane(urlPodium, player1Podium, player1Pane);
        setupPodiumPane(urlPodium, player2Podium, player2Pane);
        setupPodiumPane(urlPodium, player3Podium, player3Pane);
    }

    /**
     * Places podium image on the corresponding pane
     * @param podiumURL Podium image url
     * @param playerPodium Player's podium ImageView
     * @param playerPane Player's StackPane
     */
    private void setupPodiumPane(URL podiumURL, ImageView playerPodium, StackPane playerPane){
        playerPodium = getImageImageViewByURL(podiumURL);
        playerPodium.setPreserveRatio(true);
        playerPodium.fitWidthProperty().bind(playerPane.widthProperty().multiply(0.9));
        playerPane.getChildren().add(playerPodium);
    }

    /**
     * Set up the player pane with the corresponding god image
     * @param godID player's god ID
     * @param playerGod ImageView where the god image should be placed
     * @param playerPane StackPane where the ImageView should be placed
     */
    private void setupGodImagePane(int godID, ImageView playerGod, StackPane playerPane){
        URL url = getClass().getResource(String.format("/img/gods/podium/%02d.png", godID));
        playerGod = getImageImageViewByURL(url);
        playerGod.setPreserveRatio(true);
        playerGod.fitWidthProperty().bind(playerPane.widthProperty().multiply(0.9));
        playerGod.setTranslateY(-70.0);
        playerPane.getChildren().add(playerGod);
        doFadeTransition(playerGod, 600, 0,1);
    }

    /**
     * Setts the Gods images
     */
    private void setGodImages(){
        int god1 = IDsGodIDsMap.get(getKey(idsUsernamesMap, button1.getText()));
        setupGodImagePane(god1, player1God, player1Pane);

        int god2 = IDsGodIDsMap.get(getKey(idsUsernamesMap, button2.getText()));
        setupGodImagePane(god2, player2God, player2Pane);

        if(receivedCommand.getPlayers().length == 3){
            int god3 = IDsGodIDsMap.get(getKey(idsUsernamesMap, button3.getText()));
            setupGodImagePane(god3, player3God, player3Pane);
        }else{
            doFadeTransition(player3Pane, 600, 1, 0);
        }
    }

    /**
     * Sets up the buttons with the corresponding connected user names
     */
    private void setButtons() {
        button1.setText(receivedCommand.getPlayers()[0].getUsername());
        button1.setOnMouseClicked(this :: onButtonClick);
        button1.setDisable(false);

        button2.setText(receivedCommand.getPlayers()[1].getUsername());
        button2.setOnMouseClicked(this :: onButtonClick);
        button2.setDisable(false);

        if(receivedCommand.getPlayers().length == 3) {
            button3.setText(receivedCommand.getPlayers()[2].getUsername());
            button3.setOnMouseClicked(this::onButtonClick);
            button3.setDisable(false);
        }else{
            doFadeTransition(button3, 600, 1, 0);
        }
    }

    /**
     * Method that initializes a fade transition and plays it
     * @param node node to fade
     * @param duration duration of the transition
     * @param from initial opacity value
     * @param to final opacity value
     */
    private void doFadeTransition(Node node, int duration, int from, int to){
        FadeTransition ft = new FadeTransition(Duration.millis(duration), node);
        ft.setFromValue(from);
        ft.setToValue(to);
        ft.setCycleCount(1);
        ft.play();
    }

    /**
     * Utility method, gets the key given a value
     * @param integerStringMap Map to work with
     * @param username Value i'm looking for
     * @param <K> set of keys
     * @param <V> set of values
     * @return the found key
     */
    private <K, V> Integer getKey(Map<Integer, String> integerStringMap, String username){
        for(Map.Entry<Integer,String> entry : integerStringMap.entrySet()){
            if(entry.getValue().equals(username)){
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Utility method
     * @param url URL of a image
     * @return ImageView from URL
     */
    private ImageView getImageImageViewByURL(URL url) {
        ImageView imageView = new ImageView();
        try(InputStream inputStream = url.openStream()){
            imageView.setImage(new Image(inputStream));
        }catch (IOException e){
            System.out.println("[GAME SCENE] Couldn't access resources");
            e.printStackTrace();
        }
        return imageView;
    }

    /**
     * Handles the button click
     * @param mouseEvent user's button click
     */
    @FXML
    void onButtonClick(MouseEvent mouseEvent){
        Button my_button = (Button) mouseEvent.getSource();
        disableButtons();
        int chosenPlayerID = getKey(idsUsernamesMap, my_button.getText());
        GuiManager.getInstance().send(FirstPlayerPickCommand.makeReply(GuiManager.getInstance().getServerConnection().getClientID(), GuiManager.getInstance().getServerConnection().getServerID(), chosenPlayerID));
        GuiManager.setLayout("fxml/gameScene.fxml");
    }
}

package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.controller.FirstPlayerPickCommand;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

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
    void disableButtons(){
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

    void setPodiums(){
        URL urlPodium = getClass().getResource("/img/common/podium.png");
        player1Podium = getImageImageViewByURL(urlPodium);
        player1Podium.setPreserveRatio(true);
        player1Podium.fitWidthProperty().bind(player1Pane.widthProperty().multiply(0.9));
        //podium1.setStyle("-fx-alignment: bottom-center");
        player1Pane.getChildren().add(player1Podium);

        player2Podium = getImageImageViewByURL(urlPodium);
        player2Podium.setPreserveRatio(true);
        player2Podium.fitWidthProperty().bind(player2Pane.widthProperty().multiply(0.9));
        player2Pane.getChildren().add(player2Podium);


        player3Podium = getImageImageViewByURL(urlPodium);
        player3Podium.setPreserveRatio(true);
        player3Podium.fitWidthProperty().bind(player3Pane.widthProperty().multiply(0.9));
        player3Pane.getChildren().add(player3Podium);
    }



    void setGodImages(){
        int god1 = IDsGodIDsMap.get(getKey(idsUsernamesMap, button1.getText()));
        URL url1 = getClass().getResource(String.format("/img/gods/podium/%02d.png", god1));
        player1God = getImageImageViewByURL(url1);
        player1God.setPreserveRatio(true);
        player1God.fitWidthProperty().bind(player1Pane.widthProperty().multiply(0.9));
        player1God.setTranslateY(-70.0);
        player1Pane.getChildren().add(player1God);

        int god2 = IDsGodIDsMap.get(getKey(idsUsernamesMap, button2.getText()));
        URL url2 = getClass().getResource(String.format("/img/gods/podium/%02d.png", god2));
        player2God = getImageImageViewByURL(url2);
        player2God.setPreserveRatio(true);
        player2God.fitWidthProperty().bind(player2Pane.widthProperty().multiply(0.9));
        player2God.setTranslateY(-70.0);
        player2Pane.getChildren().add(player2God);

        if(receivedCommand.getPlayers().length == 3){
            int god3 = IDsGodIDsMap.get(getKey(idsUsernamesMap, button3.getText()));
            URL url3 = getClass().getResource(String.format("/img/gods/podium/%02d.png", god3));
            player3God = getImageImageViewByURL(url3);
            player3God.setPreserveRatio(true);
            player3God.fitWidthProperty().bind(player3Pane.widthProperty().multiply(0.9));
            player3God.setTranslateY(-70.0);
            player3Pane.getChildren().add(player3God);
        }


    }
    /**
     * Sets up the buttons with the corresponding connected user names
     */
    void setButtons() {


        //button1.setId(String.valueOf(receivedCommand.getPlayers()[0].getId()));
        button1.setText(receivedCommand.getPlayers()[0].getUsername());
        button1.setOnMouseClicked(this :: onButtonClick);
        button1.setDisable(false);

        //button2.setId(String.valueOf(receivedCommand.getPlayers()[1].getId()));
        button2.setText(receivedCommand.getPlayers()[1].getUsername());
        button2.setOnMouseClicked(this :: onButtonClick);
        button2.setDisable(false);

        if(receivedCommand.getPlayers().length == 3) {
            //button3.setId(String.valueOf(receivedCommand.getPlayers()[2].getId()));
            button3.setText(receivedCommand.getPlayers()[2].getUsername());
            button3.setOnMouseClicked(this::onButtonClick);
            button3.setDisable(false);
        }
    }

    <K, V> Integer getKey(Map<Integer, String> integerStringMap, String username){
        for(Map.Entry<Integer,String> entry : integerStringMap.entrySet()){
            if(entry.getValue().equals(username)){
                return entry.getKey();
            }
        }
        return null;
    }

    private ImageView getImageImageViewByURL(URL url) {
        ImageView imageView = new ImageView();
        try(InputStream inputStream = url.openStream()){
            imageView.setImage(new Image(inputStream));
        }catch (IOException e){
            System.out.println("[GAME SCENE] Couldn't access buildings resources");
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
        //int chosenPlayerID = Integer.parseInt(my_button.getId());

        int chosenPlayerID = getKey(idsUsernamesMap, my_button.getText());
        GuiManager.getInstance().send(FirstPlayerPickCommand.makeReply(GuiManager.getInstance().getServerConnection().getClientID(), GuiManager.getInstance().getServerConnection().getServerID(), chosenPlayerID));
        GuiManager.setLayout("fxml/gameScene.fxml");
    }
}

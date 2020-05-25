package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.CommandType;
import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.controller.FirstPlayerPickCommand;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;


public class FirstPlayerPickSceneController implements Initializable {

    @FXML
    private Button button1;
    @FXML
    private Button button2;
    @FXML
    private Button button3;
    @FXML
    private Label label;

    private int[] ids;
    private String[] usernames;

    @FXML
    public void initialize(){

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiManager.getInstance().setFirstPlayerPickSceneController(this);
        int connectedPlayers = GuiManager.getInstance().getConnectedIDS().length;
        ids = new int[connectedPlayers];
        usernames = new String[connectedPlayers];
        disableButtons();
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
        FirstPlayerPickCommand firstPlayerPickCommand = cmd.getCommand(FirstPlayerPickCommand.class);
        ids = firstPlayerPickCommand.getPlayersID();
        usernames = firstPlayerPickCommand.getUsernames();

        setButtons( ids ,usernames);

        label.setText("Choose the first player");
    }

    /**
     * Sets up the buttons with the corresponding connected user names
     * @param ids Connected IDs
     * @param usernames Connected user names
     */
    void setButtons(int[] ids, String[] usernames) {

        button1.setId(String.valueOf(ids[0]));
        button1.setText(usernames[0]);
        button1.setOnMouseClicked(this :: onButtonClick);
        button1.setDisable(false);

        button2.setId(String.valueOf(ids[1]));
        button2.setText(usernames[1]);
        button2.setOnMouseClicked(this :: onButtonClick);
        button2.setDisable(false);

        if(ids.length == 3) {
            button3.setId(String.valueOf(ids[2]));
            button3.setText(usernames[2]);
            button3.setOnMouseClicked(this::onButtonClick);
            button3.setDisable(false);
        }
    }

    /**
     * Handles the button click
     * @param mouseEvent user's button click
     */
    void onButtonClick(javafx.scene.input.MouseEvent mouseEvent){
        Button my_button = (Button) mouseEvent.getSource();
        CommandWrapper firstPlayerPick = new CommandWrapper(CommandType.SELECT_FIRST_PLAYER, new FirstPlayerPickCommand(GuiManager.getInstance().getServerConnection().getClientID(), GuiManager.getInstance().getServerConnection().getServerID(), Integer.parseInt(my_button.getId())));
        disableButtons();
        GuiManager.getInstance().send(firstPlayerPick);
        GuiManager.setLayout("fxml/gameScene.fxml");
    }
}

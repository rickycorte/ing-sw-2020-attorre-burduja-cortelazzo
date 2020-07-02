package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.controller.JoinCommand;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * This class is the controller for the "Log In" scene
 */
public class LogInSceneController implements Initializable {
    private boolean connected;                              //flag to indicate if i'm connected to the server or not
    private boolean connectToCustomServer;                  //flag to know if i want to connect to a custom server or not
    private Settings settings;                              //reference to the settings class

    @FXML
    private Pane mainPane;
    @FXML
    private Label infoLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField serverField;
    @FXML
    private TextField portField;
    @FXML
    private Button connectButton;
    @FXML
    private Button backButton;
    @FXML
    private CheckBox customServerCheckBox;
    @FXML
    private VBox ambientPane;
    @FXML
    private Label serverLabel;
    @FXML
    private Label portLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiManager.getInstance().setLogInSceneController(this);
        connected = false;
        connectToCustomServer = false;
        settings = GuiManager.getInstance().getSettings();
        initializeStyleSheet();
        infoLabel.setText("Enter your username to connect\n");
        serverField.setOpacity(0);
        serverLabel.setOpacity(0);
        portField.setOpacity(0);
        portLabel.setOpacity(0);
        usernameField.setOnKeyPressed((e)-> {
            if (e.getCode() == KeyCode.ENTER) {
                onConnectButtonClick();
            }
        });

        deactivateCustomServerControls();

        EventHandler eventHandler = (EventHandler<ActionEvent>) event -> {
            if(event.getSource() instanceof CheckBox){
                if(customServerCheckBox.isSelected()) {
                    activateCustomServerControls();
                    connectToCustomServer = true;
                }else{
                    deactivateCustomServerControls();
                    connectToCustomServer = false;
                }
            }
        };
        customServerCheckBox.setOnAction(eventHandler);
    }

    //-----------------------------------Initialize Methods-------------------------------------------------------------

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
        toStyle.add(infoLabel);
        toStyle.add(portField);
        toStyle.add(portLabel);
        toStyle.add(serverField);
        toStyle.add(serverLabel);
        toStyle.add(ambientPane);
        toStyle.add(backButton);
        toStyle.add(connectButton);
        toStyle.add(usernameField);
        return toStyle;
    }

    //---------------------------------Button Click Handlers------------------------------------------------------------

    /**
     * Handles back button click
     * Goes to main scene
     */
    @FXML
    private void onBackButtonClick() {
        GuiManager.setLayout("fxml/mainScene.fxml");
    }

    /**
     * Handles connect button click
     * Reads username - serverIP - port and attempts join
     */
    @FXML
    private void onConnectButtonClick() {
        String username = usernameField.getText();
        String serverIP = serverField.getText();
        String serverPort = portField.getText();
        if (username.isBlank()) {
            infoLabel.setText("Blank username\nYou need one if you want to join");
            return;
        }
        backButton.setDisable(true);
        connectButton.setDisable(true);
        infoLabel.setText("Connecting...");
        if (!connected) {
            if (connectToCustomServer) {
                if(!serverPort.isBlank()) {
                    if(GuiManager.getInstance().connect(serverIP, Integer.parseInt(serverPort), username))
                        handleReachedNetwork();
                    else
                        handleUnreachableNetwork();
                }
                else {
                    if(GuiManager.getInstance().connect(serverIP, GuiManager.getInstance().getServerConnection().getDefaultPort(), username))
                        handleReachedNetwork();
                    else
                        handleUnreachableNetwork();
                }
            } else {
                if (GuiManager.getInstance().connect(null , GuiManager.getInstance().getServerConnection().getDefaultPort() ,username)){
                    handleReachedNetwork();
                } else {
                    handleUnreachableNetwork();
                }
            }
        }else{ // retry with another username
            GuiManager.getInstance().send(JoinCommand.makeRequest(GuiManager.getInstance().getServerConnection().getClientID(),
                    GuiManager.getInstance().getServerConnection().getServerID(),
                    username));
        }
    }

    //-----------------------------------------------Handler Methods----------------------------------------------------

    /**
     * Handles the case when the network is unreachable
     */
    private void handleUnreachableNetwork(){
        connected = false;
        infoLabel.setText("Server is unreachable\nVerify the address");
        connectButton.setDisable(false);
        backButton.setDisable(false);
    }

    /**
     * Handles the case when the network is successfully reached
     */
    private void handleReachedNetwork(){
        connected = true;
        infoLabel.setText("Connected!");
        connectButton.setDisable(true);
        backButton.setDisable(false);
    }

    /**
     * Handles Join response
     * @param cmd join command received
     */
    void onAckJoin(CommandWrapper cmd) {
        if(GuiManager.getInstance().isForMe(cmd)) {
            JoinCommand joinCommand = cmd.getCommand(JoinCommand.class);
            if (joinCommand.isJoin()) {
                GuiManager.getInstance().setMyUsername(joinCommand.getUsername());
                GuiManager.setLayout("fxml/waitScene.fxml");
            } else {
                connectButton.setDisable(false);
                backButton.setDisable(false);
                infoLabel.setText("Username too short or already taken, try another one\n");
            }
        }
    }

    //--------------------------------------Utility Methods-------------------------------------------------------------

    /**
     * Activates text fields for the custom server ip and port
     */
    private void activateCustomServerControls(){
        serverField.setDisable(false);
        doFadeTransition(serverLabel,600,0,1);
        doFadeTransition(serverField, 600, 0, 1);
        portField.setDisable(false);
        doFadeTransition(portField,600,0,1);
        doFadeTransition(portLabel, 600, 0, 1);

    }

    /**
     * Deactivate text fields for the custom server ip and port
     */
    private void deactivateCustomServerControls(){
        serverField.setDisable(true);
        doFadeTransition(serverField, 600, 1, 0);
        doFadeTransition(serverLabel,600,1,0);
        portField.setDisable(true);
        doFadeTransition(portLabel,600,1,0);
        doFadeTransition(portField, 600, 1, 0);
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

}


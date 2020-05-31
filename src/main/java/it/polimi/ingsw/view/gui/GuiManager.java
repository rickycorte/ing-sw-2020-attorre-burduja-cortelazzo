package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.controller.compact.CompactPlayer;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.server.Server;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handle the interactions between Client and Gui
 */
public class GuiManager implements ICommandReceiver {

    private static GuiManager instance = null;
    private Scene aScene;
    private INetworkAdapter serverConnection;
    private int[] connectedPlayersIDS;              //is constructed at the incoming Start command
    private boolean isConnected = false;

    private Map<Integer, String> idUsernameMap = new ConcurrentHashMap<>();     //is constructed at incoming FirstPlayerPick command
    private Map<Integer, Integer> idGodMap = new HashMap<>();                   //is constructed at incoming FirstPlayerPick command
    private int winnerID;

    private int hostID;
    //SceneControllers

    private MainSceneController mainSceneController;
    private LogInSceneController logInSceneController;
    private WaitSceneController waitSceneController;
    private ChooseGodsSceneController chooseGodsSceneController;
    private FirstPlayerPickSceneController firstPlayerPickSceneController;
    private GameSceneController gameSceneController;
    private EndGameController endGameController;




    GuiManager(){
        super();
    }


    /**
     * Gets GuiManager instance
     * @return GuiManager instance
     */
     synchronized static GuiManager getInstance(){
        if(instance == null) {
            instance = new GuiManager();
        }
        return instance;
    }


    /**
     * This method sets a layout from an FXML file and returns the scene controller
     *
     * @param path  path to the layout file
     * @param <T> type of scene controller
     * @return scene controller
     */
    static <T> T setLayout( String path){
        FXMLLoader loader = new FXMLLoader();
        URL path_url = GuiManager.class.getClassLoader().getResource(path);
        loader.setLocation(path_url);

        Pane pane;
        try{
            pane = loader.load();
            //System.out.println("[GuiManager] Pane loaded");
        }catch (IOException e){
            System.out.println("[Gui_Manager] Couldn't load pane" );
            e.printStackTrace();
            return null;
        }
        getInstance().aScene.setRoot(pane);
        return loader.getController();
    }


    //Scene Controllers


    void setMainSceneController(MainSceneController mainSceneController){
        this.mainSceneController = mainSceneController;
    }

    void setLogInSceneController(LogInSceneController logInSceneController){
        this.logInSceneController = logInSceneController;
    }

    void setWaitSceneController(WaitSceneController waitSceneController){
        this.waitSceneController = waitSceneController;
    }
    void setChooseGodsSceneController(ChooseGodsSceneController chooseGodsSceneController){
        this.chooseGodsSceneController = chooseGodsSceneController;
    }
    void setFirstPlayerPickSceneController(FirstPlayerPickSceneController firstPlayerPickSceneController) {
        this.firstPlayerPickSceneController = firstPlayerPickSceneController;
    }

    void setGameSceneController(GameSceneController gameSceneController){
        this.gameSceneController = gameSceneController;
    }
    void setEndGameController(EndGameController endGameController){
        this.endGameController = endGameController;
    }

    void setServerConnection(INetworkAdapter serverConnection) {
        this.serverConnection = serverConnection;
    }

     INetworkAdapter getServerConnection(){
        return serverConnection;
    }

    void setScene(Scene scene){
        this.aScene = scene;
    }

    /**
     * checks if an incoming command is directed to me
     * @param cmd received command
     * @return true / false accordingly
     */
    boolean isForMe(CommandWrapper cmd){
        BaseCommand command = cmd.getCommand(BaseCommand.class);
        return GuiManager.getInstance().getServerConnection().getClientID() == command.getTarget() || command.getTarget() == Server.BROADCAST_ID;
    }


    /**
     * ICommandReceiver interface onConnect implementation
     * @param cmd join command
     */
    @Override
    public void onConnect(CommandWrapper cmd) {
        if(isForMe(cmd)) {
            if (cmd.getType() == CommandType.JOIN) {
                Platform.runLater(() -> {
                    GuiManager.getInstance().logInSceneController.onAckJoin(cmd);
                });
            }
        }else{
            if (cmd.getType() == CommandType.JOIN){
                Platform.runLater(() -> {
                    GuiManager.getInstance().waitSceneController.onSecondClientConnection();
                });
            }
        }
    }

    /**
     * ICommandReceiver interface onDisconnect implementation
     * @param cmd disconnect command
     */
    @Override
    public void onDisconnect(CommandWrapper cmd) {
        //handle Leave
        if(isForMe(cmd)){

        }
    }

    /**
     * ICommandReceiver interface onCommand implementation
     * @param cmd command to process
     */
    @Override
    public void onCommand(CommandWrapper cmd) {
            switch (cmd.getType()) {
                case START:
                    connectedPlayersIDS = cmd.getCommand(StartCommand.class).getPlayersID();
                    Platform.runLater(() -> {
                        GuiManager.getInstance().waitSceneController.onStart(cmd.getCommand(StartCommand.class));
                        chooseGodsSceneController.enableControls(false);
                    });
                    break;
                case FILTER_GODS:
                    setHostID(cmd.getCommand(FilterGodCommand.class).getTarget());
                    Platform.runLater(() -> chooseGodsSceneController.onFilterGodsCommand(cmd));
                    break;
                case PICK_GOD:
                    Platform.runLater(() -> chooseGodsSceneController.onPickGodCommand(cmd));
                    break;
                case SELECT_FIRST_PLAYER:
                    mapPlayers(cmd.getCommand(FirstPlayerPickCommand.class));
                    if (isForMe(cmd))
                        Platform.runLater(() -> firstPlayerPickSceneController.onFirstPlayerPickCommand(cmd));
                    break;
                case PLACE_WORKERS:
                    Platform.runLater(() -> GuiManager.getInstance().gameSceneController.onPlaceWorkersCommand(cmd));
                    break;
                case ACTION_TIME: {
                    Platform.runLater(() -> gameSceneController.onActionCommand(cmd));
                    break;
                }
                case END_GAME:{
                    winnerID = cmd.getCommand(EndGameCommand.class).getWinnerID();
                    Platform.runLater(() -> gameSceneController.onEndGame(cmd));
                }
                case UPDATE:
                    Platform.runLater(() -> gameSceneController.onUpdateCommand(cmd));
                    break;
            }
    }

    /**
     * Map all the connected client IDs to their respective username and godID
     * @param firstPlayerPickCommand command to get the information from
     */
    void mapPlayers(FirstPlayerPickCommand firstPlayerPickCommand){
        CompactPlayer[] connectedCompactPlayers = firstPlayerPickCommand.getPlayers();
        int[] connectedIDs = new int[connectedCompactPlayers.length];
        String[] connectedUsernames = new String[connectedCompactPlayers.length];
        int[] connectedGods = new int[connectedCompactPlayers.length];
        int index = 0;

        for(CompactPlayer aCompactPlayer : connectedCompactPlayers){
            connectedIDs[index] = aCompactPlayer.getId();
            connectedUsernames[index] = aCompactPlayer.getUsername();
            connectedGods[index] = aCompactPlayer.getGodID();
            index++;
        }

        for(int i = 0; i < connectedIDs.length; i++){
            idUsernameMap.put(connectedIDs[i], connectedUsernames[i]);
            idGodMap.put(connectedIDs[i], connectedGods[i]);
        }

        System.out.printf("MAP players:: %s,, %s,, %s\n", Arrays.toString(connectedIDs), Arrays.toString(connectedUsernames),
                Arrays.toString(connectedGods));
        Platform.runLater(()-> {
            if (gameSceneController != null) {
                gameSceneController.mapPlayers();
            }
        });
    }

    /**
     * Method used by the scenes to send a command
     * @param cmd command to send
     */
    public void send(CommandWrapper cmd){
        this.serverConnection.send(cmd);
    }

    /**
     * Method used to connect to a server with an username
     * @param username username to connect with
     * @return true / false according on how the connection went
     */
    public boolean connect(String username) {
        isConnected = getServerConnection().connect("127.0.0.1", getServerConnection().getDefaultPort(), username);
        return isConnected;
    }

    /**
     * Method used to disconnect from a server
     */
    public void disconnect() {
        if (isConnected) {
            getServerConnection().disconnect();
            isConnected = false;
        }
    }

    Map<Integer, String> getIDsUsernameMap(){
        return this.idUsernameMap;
    }

    int[] getConnectedIDS(){
        return this.connectedPlayersIDS;
    }

    List<Integer> getAllPlayerIds() {
        return new ArrayList<>(this.idUsernameMap.keySet());
    }

    Map<Integer, Integer> getIDsGodsMap() {
        return this.idGodMap;
    }

    public Scene getScene() {
        return aScene;
    }

    public int getWinnerID() {
        return winnerID;
    }

    public void setHostID(int hostID) {
        this.hostID = hostID;
    }

    public int getHostID() {
        return hostID;
    }
}

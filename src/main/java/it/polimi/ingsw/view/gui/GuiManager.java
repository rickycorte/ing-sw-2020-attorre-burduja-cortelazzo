package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.controller.compact.CompactPlayer;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.server.Server;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements the ICommandReceiver on the Gui side
 * It handles the incoming messages from the server by forwarding them to the specific scene controller
 * Sends the messages coming from the client to the server
 */
public class GuiManager implements ICommandReceiver {
    private Game.GameState state;                                               //Game state indicator, helps directing incoming commands
    private static GuiManager instance = null;                                  //GuiManager instance (singleton pattern)
    private Scene aScene;                                                       //Reference to the scene, used for .setRoot()
    private INetworkAdapter serverConnection;                                   //Interface to communicate with the server
    private boolean isConnected = false;                                        //Flag to indicate whether i'm connected to the server or not
    private Settings settings = new Settings();                                 //Reference to the settings class

    private String myUsername;                                                  //Constructed at Join command
    private int[] connectedPlayersIDS;                                          //Constructed at Start command
    private Map<Integer, String> idUsernameMap = new ConcurrentHashMap<>();     //Constructed at incoming FirstPlayerPick command
    private Map<Integer, Integer> idGodMap = new HashMap<>();                   //Constructed at incoming FirstPlayerPick command
    private int winnerID;                                                       //Set at End Game command
    private int hostID;                                                         //Set at Start command

    //--------------SceneControllers------------------------------------------------------------------------------------

    private MainSceneController mainSceneController;
    private LogInSceneController logInSceneController;
    private WaitSceneController waitSceneController;
    private ChooseGodsSceneController chooseGodsSceneController;
    private FirstPlayerPickSceneController firstPlayerPickSceneController;
    private GameSceneController gameSceneController;
    private EndGameController endGameController;
    private SettingsSceneController settingsSceneController;
    private GodsSceneController godsSceneController;

    //------------------------------------------------------------------------------------------------------------------

    GuiManager(){
        super();
    }

    /**
     * Gets GuiManager instance (singleton pattern)
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
            //System.out.println("[Gui Manager] Pane loaded");
        }catch (IOException e){
            System.out.println("[Gui Manager] Couldn't load pane");
            e.printStackTrace();
            return null;
        }
        getInstance().aScene.setRoot(pane);
        return loader.getController();
    }

    //----------------------------Scene Controller Setters--------------------------------------------------------------

    /**
     * Setts the Main Scene Controller
     * @param mainSceneController main scene controller to set
     */
    void setMainSceneController(MainSceneController mainSceneController){
        this.mainSceneController = mainSceneController;
    }

    /**
     * Setts the Log In Scene Controller
     * @param logInSceneController log in scene controller to set
     */
    void setLogInSceneController(LogInSceneController logInSceneController){
        this.logInSceneController = logInSceneController;
    }

    /**
     * Setts the Wait Scene Controller
     * @param waitSceneController wait scene controller to set
     */
    void setWaitSceneController(WaitSceneController waitSceneController){
        this.waitSceneController = waitSceneController;
    }

    /**
     * Setts the Choose Gods Scene Controller
     * @param chooseGodsSceneController choose gods scene controller to set
     */
    void setChooseGodsSceneController(ChooseGodsSceneController chooseGodsSceneController){
        this.chooseGodsSceneController = chooseGodsSceneController;
    }

    /**
     * Setts the First Player Pick Scene Controller
     * @param firstPlayerPickSceneController first player pick scene controller to set
     */
    void setFirstPlayerPickSceneController(FirstPlayerPickSceneController firstPlayerPickSceneController) {
        this.firstPlayerPickSceneController = firstPlayerPickSceneController;
    }

    /**
     * Setts the Game Scene Controller
     * @param gameSceneController game scene controller to set
     */
    void setGameSceneController(GameSceneController gameSceneController){ this.gameSceneController = gameSceneController; }

    /**
     * Setts the End Game Scene Controller
     * @param endGameController end game scene controller to set
     */
    void setEndGameController(EndGameController endGameController){
        this.endGameController = endGameController;
    }

    /**
     * Setts the Settings Scene Controller
     * @param settingsSceneController settings scene controller to set
     */
    void setSettingsController(SettingsSceneController settingsSceneController) { this.settingsSceneController = settingsSceneController; }

    /**
     * Setts the Gods Scene Controller
     * @param godsSceneController gods scene controller to set
     */
    void setGodsController(GodsSceneController godsSceneController) {
        this.godsSceneController = godsSceneController;
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Setts the INetworkAdapter (connection to the server)
     * @param serverConnection adapter representing the connection to the server
     */
    void setServerConnection(INetworkAdapter serverConnection) {
        this.serverConnection = serverConnection;
    }

    /**
     * Gets the INetworkAdapter (connection to the server)
     * @return adapter to communicate with the server
     */
     INetworkAdapter getServerConnection(){
        return serverConnection;
    }

    /**
     * Setter for the scene
     * @param scene scene to set
     */
    void setScene(Scene scene){
        this.aScene = scene;
    }

    /**
     * Setter for my username
     * @param username my username
     */
    void setMyUsername(String username){
        myUsername = username;
    }

    /**
     * Getter for my username
     * @return my username
     */
    String getMyUsername(){
        return myUsername;
    }

    /**
     * checks if an incoming command is directed to me
     * @param cmd received command
     * @return true if command's target is me or broadcast
     */
    boolean isForMe(CommandWrapper cmd){
        BaseCommand command = cmd.getCommand(BaseCommand.class);
        return serverConnection.getClientID() == command.getTarget() || command.getTarget() == Server.BROADCAST_ID;
    }

    //--------------------------------ICommandReceiver methods implementation-------------------------------------------

    /**
     * ICommandReceiver interface onConnect implementation
     * @param cmd join command
     */
    @Override
    public void onConnect(CommandWrapper cmd) {
        JoinCommand joinCommand = cmd.getCommand(JoinCommand.class);
        if (isForMe(cmd)) {
            hostID = joinCommand.getHostPlayerID();
            state = Game.GameState.WAIT;
            isConnected = joinCommand.isJoin();
            Platform.runLater(() -> {
                GuiManager.getInstance().logInSceneController.onAckJoin(cmd);
            });
        } else {
            Platform.runLater(() -> {
                GuiManager.getInstance().waitSceneController.onSecondClientConnection(cmd);
            });
        }
    }

    /**
     * ICommandReceiver interface onDisconnect implementation
     * @param cmd disconnect command
     */
    @Override
    public void onDisconnect(CommandWrapper cmd) {
        if (cmd == null) {
            setLayout("fxml/mainScene.fxml");
            Platform.runLater(() -> mainSceneController.onServerShutDown());
        } else if (state == Game.GameState.WAIT)
            Platform.runLater(() -> waitSceneController.onDisconnect(cmd));
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
                    state = Game.GameState.GOD_FILTER;
                    setHostID(cmd.getCommand(FilterGodCommand.class).getTarget());
                    Platform.runLater(() -> chooseGodsSceneController.onFilterGodsCommand(cmd));
                    break;
                case PICK_GOD:
                    state = Game.GameState.GOD_PICK;
                    Platform.runLater(() -> chooseGodsSceneController.onPickGodCommand(cmd));
                    break;
                case SELECT_FIRST_PLAYER:
                    state = Game.GameState.FIRST_PLAYER_PICK;
                    mapPlayers(cmd.getCommand(FirstPlayerPickCommand.class));
                    if (isForMe(cmd))
                        Platform.runLater(() -> firstPlayerPickSceneController.onFirstPlayerPickCommand(cmd));
                    break;
                case PLACE_WORKERS:
                    state = Game.GameState.GAME;
                    Platform.runLater(() -> GuiManager.getInstance().gameSceneController.onPlaceWorkersCommand(cmd));
                    break;
                case ACTION_TIME: {
                    state = Game.GameState.GAME;
                    Platform.runLater(() -> gameSceneController.onActionCommand(cmd));
                    break;
                }
                case END_GAME: {
                    state = Game.GameState.END;
                    EndGameCommand endGameCommand = cmd.getCommand(EndGameCommand.class);
                    winnerID = endGameCommand.getWinnerID();

                    if(endGameCommand.isMatchStillRunning()){
                        if(endGameCommand.getTarget() == serverConnection.getClientID()){
                            setLayout("fxml/endGameScene.fxml");
                            Platform.runLater(() -> endGameController.onEndGameCommand(cmd));
                        }
                    }else{
                        Platform.runLater(() -> setLayout("fxml/endGameScene.fxml"));
                        //setLayout("fxml/endGameScene.fxml");
                        Platform.runLater(() -> endGameController.onEndGameCommand(cmd));
                    }
                }
                case UPDATE:
                    if (state == Game.GameState.GAME)
                        Platform.runLater(() -> gameSceneController.onUpdateCommand(cmd));
                    break;
            }
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * Map all the connected client IDs to their respective username and godID
     * @param firstPlayerPickCommand command to get the information from
     */
    private void mapPlayers(FirstPlayerPickCommand firstPlayerPickCommand){
        CompactPlayer[] connectedCompactPlayers = firstPlayerPickCommand.getPlayers();
        idUsernameMap.clear();
        idGodMap.clear();
        for(CompactPlayer aCompactPlayer : connectedCompactPlayers){
            idUsernameMap.put(aCompactPlayer.getId(), aCompactPlayer.getUsername());
            idGodMap.put(aCompactPlayer.getId(), aCompactPlayer.getGodID());
        }
    }

    /**
     * Method used by scene controllers to send a command
     * @param cmd command to send
     */
    public void send(CommandWrapper cmd){
        this.serverConnection.send(cmd);
    }

    /**
     * Method user to connect to a server with an username, a server IP and a port
     * @param serverIP server's address
     * @param port port to connect to
     * @param username username to connect with
     */
    public boolean connect(String serverIP, int port, String username){
        if(serverIP != null){
            isConnected = serverConnection.connect(serverIP, port, username);

        }else{
            isConnected = serverConnection.connect("127.0.0.1", serverConnection.getDefaultPort(), username);
        }
        return isConnected;
    }

    /**
     * Method used to disconnect from a server
     */
    public void disconnect() {
        if (isConnected) {
            getServerConnection().disconnect();
            send(LeaveCommand.makeRequest(serverConnection.getClientID(),serverConnection.getServerID()));
            isConnected = false;
        }
    }

    /**
     *Getter for the map of [ K - ClientID : V - Client Username ]
     * @return id - username map
     */
    Map<Integer, String> getIDsUsernameMap(){
        return this.idUsernameMap;
    }

    /**
     * Getter for the connected players IDS
     * @return an array of connected players IDs
     */
    int[] getConnectedIDS(){
        return this.connectedPlayersIDS;
    }

    /**
     * Getter for the connected players IDs
     * @return an arrayList of connected players IDs
     */
    List<Integer> getAllPlayerIds() {
        return new ArrayList<>(this.idUsernameMap.keySet());
    }

    /**
     * Getter for the map of [ K - ClientID : V - GodID ]
     * @return client ID - god ID map
     */
    Map<Integer, Integer> getIDsGodsMap() {
        return this.idGodMap;
    }

    /**
     * Gets the reference to the scene
     * @return scene reference
     */
    Scene getScene() {
        return aScene;
    }

    /**
     * Getter for the winner's client ID
     * @return winner's client ID
     */
    int getWinnerID() {
        return winnerID;
    }

    /**
     * Setter for host client ID
     * @param hostID ID to set
     */
    private void setHostID(int hostID) {
        this.hostID = hostID;
    }

    /**
     * Method to know if i'm the host or not
     * @return true if i'm the host of the game, false otherwise
     */
    boolean imHost(){
        return serverConnection.getClientID() == hostID;
    }

    /**
     * Getter for the settings class
     * @return a reference to the settings class
     */
    public Settings getSettings() {
        return this.settings;
    }

    /**
     * Getter for the game state
     * @return game state i'm in
     */
    public Game.GameState getState() {
        return state;
    }
}

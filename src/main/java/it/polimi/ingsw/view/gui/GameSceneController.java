package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.controller.compact.CompactMap;
import it.polimi.ingsw.controller.compact.CompactSelectedAction;
import it.polimi.ingsw.controller.compact.CompactWorker;
import it.polimi.ingsw.game.Action;
import it.polimi.ingsw.game.NextAction;
import it.polimi.ingsw.game.Vector2;
import it.polimi.ingsw.view.CardCollection;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;


public class GameSceneController implements Initializable {

    private boolean my_turn;
    private Tile[][] tiles;
    private GuiManager guiManager;
    private Vector2[] my_workers;
    private Vector2[] chosenWorkerPos;
    private ArrayList<Vector2> workers_placement;
    private int selectedWorker;
    private enum Colors {BLUE, RED, YELLOW};
    private Map<Integer, Colors> colorsMap;
    private Map<Integer, URL> workerImages;
    private Map<Integer, String> idsUsernamesMap;
    private Map<Integer, Integer> idsGodsMap;
    private ActionCommand receivedCommand;
    private int actionID;
    private int godPowerActionID;
    private int currentPlayerID;
    private CardCollection cardCollection;
    private Settings settings;
    private int myClientID;
    private int serverID;
    private int state;                  // 0 - waiting for other players/for game to start
                                        // 1 - placing workers state
                                        // 2 - running action state

    @FXML
    private AnchorPane rootLeft;
    @FXML
    private AnchorPane rootRight;
    @FXML
    private AnchorPane rootcenter;
    @FXML
    private AnchorPane root;
    @FXML
    private BorderPane borderpane;
    @FXML
    private ImageView background;
    @FXML
    private Button endTurnButton;
    @FXML
    private Button godPowerButton;
    @FXML
    private Button cancellWorkerSelection;
    @FXML
    private Button undoButton;
    @FXML
    private Button quitButton;
    @FXML
    private Label gameLabel;
    @FXML
    private StackPane stack_id;
    @FXML
    private ImageView map;
    @FXML
    private ImageView myGod;
    @FXML
    private GridPane mapGrid;
    @FXML
    private StackPane myPane;
    @FXML
    private GridPane controllsGrid;
    @FXML
    private VBox rightBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        guiManager = GuiManager.getInstance();
        guiManager.setGameSceneController(this);
        settings = guiManager.getSettings();
        cardCollection = new CardCollection();
        myClientID = guiManager.getServerConnection().getClientID();
        serverID = guiManager.getServerConnection().getServerID();
        idsUsernamesMap = guiManager.getIDsUsernameMap();
        idsGodsMap = guiManager.getIDsGodsMap();
        colorsMap = new HashMap<>();
        workerImages = new HashMap<>();
        my_workers = new Vector2[2];
        state = 0;
        my_turn = false;

        initializeStyleSheet();
        initializeBindings();
        initializeTiles();
        disableAllButtons(true);
        gameLabel.setText("Waiting for the game to start");
    }

    //-------------------------------------------Initialize Methods-----------------------------------------------------
    /**
     * Setts the style sheet according to the settings
     */
    private void initializeStyleSheet() {
        if(settings.getTheme() == Settings.Themes.LIGHT){
            URL backGroundURL = getClass().getResource("/img/common/SantoriniBoard_nomap.png");
            background.setImage(getImageImageViewByURL(backGroundURL));
            URL mapURL = getClass().getResource("/img/common/map.png");
            map.setImage(getImageImageViewByURL(mapURL));
            gameLabel.setStyle("-fx-effect: dropshadow(gaussian, #000000, 1, 0.2, 0, 0)");

        }else{
            URL backGroundURL = getClass().getResource("/img/common/dark/SantoriniBoardDark.png");
            background.setImage(getImageImageViewByURL(backGroundURL));
            URL mapURL = getClass().getResource("/img/common/dark/mapDark.png");
            map.setImage(getImageImageViewByURL(mapURL));
            gameLabel.setStyle("-fx-effect: dropshadow(gaussian, #ffffff, 1, 0.2, 0, 0)");
        }

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
        //toStyle.add(root);
        toStyle.add(gameLabel);
        toStyle.add(godPowerButton);
        toStyle.add(undoButton);
        toStyle.add(cancellWorkerSelection);
        toStyle.add(endTurnButton);
        toStyle.add(quitButton);
        toStyle.add(gameLabel);
        toStyle.add(rootRight);
        //toStyle.add(mapGrid);
        return toStyle;
    }

    /**
     * Initializes height and width bindings to make everything resizable
     */
    private void initializeBindings() {
        background.fitWidthProperty().bind(borderpane.widthProperty());
        background.fitHeightProperty().bind(borderpane.heightProperty());

        Scene scene = guiManager.getScene();
        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());

        borderpane.minWidthProperty().bind(root.widthProperty());
        borderpane.minHeightProperty().bind(root.heightProperty());

        rootcenter.maxHeightProperty().bind(borderpane.heightProperty().multiply(0.75));
        rootcenter.maxWidthProperty().bind(borderpane.widthProperty().multiply(0.75));

        rootRight.minWidthProperty().bind(borderpane.widthProperty().multiply(0.25));
        rootRight.maxWidthProperty().bind(borderpane.widthProperty().multiply(0.25));

        rootRight.minHeightProperty().bind(borderpane.heightProperty());
        rootRight.maxHeightProperty().bind(borderpane.heightProperty());

        rootLeft.minHeightProperty().bind(borderpane.heightProperty());
        rootLeft.maxHeightProperty().bind(borderpane.heightProperty());

        rootLeft.minWidthProperty().bind(borderpane.widthProperty().multiply(0.25));
        rootLeft.maxWidthProperty().bind(borderpane.widthProperty().multiply(0.25));


        StackPane.setAlignment(map, Pos.CENTER);
        StackPane.setAlignment(mapGrid, Pos.CENTER);
        mapGrid.setAlignment(Pos.CENTER);

        map.setPreserveRatio(true);
        map.fitWidthProperty().bind(stack_id.widthProperty().multiply(0.99));
        map.fitHeightProperty().bind(stack_id.heightProperty().multiply(0.99));

        rightBox.maxWidthProperty().bind(rootRight.widthProperty().multiply(0.75));
        rightBox.maxHeightProperty().bind(rootRight.heightProperty().multiply(0.8));

        controllsGrid.maxWidthProperty().bind(rightBox.widthProperty().multiply(0.9));
        //controllsGrid.maxHeightProperty().bind(rootRight.heightProperty().multiply(0.45));
        controllsGrid.setTranslateY(-20);

    }

    /**
     * Initializes my god pane by placing the right image
     */
    private void initializeMyGod() {
        int myGodID = idsGodsMap.get(myClientID);
        URL url1 = getClass().getResource(String.format("/img/gods/noinfo/%02d.png", myGodID));
        myGod.setImage(getImageImageViewByURL(url1));
        myGod.setPreserveRatio(true);
        myGod.setTranslateY(-40);
        myGod.fitWidthProperty().bind(rootLeft.widthProperty().multiply(0.95));
        Tooltip.install(myGod, new Tooltip( cardCollection.getCard(idsGodsMap.get(myClientID)).getPower()));
        //myPane.getChildren().add(myGod);
        URL panelURL;
        if(settings.getTheme() == Settings.Themes.LIGHT)
            panelURL = getClass().getResource("/img/common/leftPanel.png");
        else
            panelURL = getClass().getResource("/img/common/dark/leftPanelDark.png");
        ImageView panel = new ImageView();
        panel.setImage(getImageImageViewByURL(panelURL));
        panel.setPreserveRatio(true);
        panel.fitHeightProperty().bind(rootLeft.heightProperty());
        myPane.getChildren().add(panel);
    }

    /**
     * This method initializes buttons by setting their text / background / onAction method
     */
    private void initializeButtons() {
        String powerName = getPowerName();
        if(powerName.equals("Passive")){
            godPowerButton.setDisable(true);
            godPowerButton.setOpacity(0);
        }else {
            godPowerButton.setText(powerName);
            godPowerButton.setOnMouseClicked(this::onGodPowerButtonClick);
        }
        endTurnButton.setOnMouseClicked(this::onEndTurnButtonClick);
        cancellWorkerSelection.setOnMouseClicked(this::onCancellWorkerSelectionClick);
        undoButton.setOnMouseClicked(this::onUndoButtonClick);
        quitButton.setOnMouseClicked(this::onQuitButtonClick);
    }

    /**
     * This method initializes the mapGrid pane by placing a Tile on each cell
     */
    private void initializeTiles() {
        tiles = new Tile[5][5];
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Tile tile = new Tile();
                tile.setOnMouseClicked((e) -> {
                    System.out.println("[GAME SCENE] Tile clicked");
                    onTileClick(e);
                    ///
                });

                tile.pos = new Vector2(row, col);
                tile.workerImages = workerImages;
                mapGrid.add(tile, col, row);
                tile.setMapGrid(mapGrid);
                tile.minWidthProperty().bind(map.fitWidthProperty().multiply(0.165));
                tile.minHeightProperty().bind(map.fitHeightProperty().multiply(0.198));
                tile.settings = settings;
                tiles[row][col] = tile;
            }
        }
    }

    //-------------------------------------------Command Handlers-------------------------------------------------------
    /**
     * Handles Action commands
     * @param cmd action command to handle
     */
    void onActionCommand(CommandWrapper cmd) {
        receivedCommand = cmd.getCommand(ActionCommand.class);
        if (guiManager.isForMe(cmd)) {
            my_turn = true;
            state = 2;
            if (onlyEndTurnAvailable(receivedCommand)) {
                guiManager.send(ActionCommand.makeReply(myClientID, serverID, 0, selectedWorker, receivedCommand.getAvailableActions()[0].getAvailablePositions().get(0)));
                return;
            }

            if (isUndoAvailable()) {
                undoButton.setDisable(false);
                Timeline timeline = new Timeline();
                timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(5), new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        undoButton.setDisable(true);
                        if (onlyEndTurnAndUndoAvailable(receivedCommand))
                            //disableAllButtons(true);
                            onEndTurnButtonClick(null);
                    }
                }));
                timeline.play();
            }

            if (getAvailableWorkers(receivedCommand).size() == 2) { // both of my workers have possible actions
                selectedWorker = -1;
                highlightAvailableWorkers(getAvailableWorkers(receivedCommand), myClientID);
                gameLabel.setText("Click on a Worker you want to select");
            } else {
                selectedWorker = getAvailableWorkers(receivedCommand).get(0);
                actionID = 0;
                bindButtonsActions();
                List<NextAction> actionsForWorker = actionsForWorker(receivedCommand.getAvailableActions(), selectedWorker);
                List<Vector2> possibleCells = actionsForWorker.get(actionID).getAvailablePositions();
                Vector2[] cellsToHighlight = new Vector2[possibleCells.size()];
                cellsToHighlight = possibleCells.toArray(cellsToHighlight);
                if (!(actionsForWorker.get(actionID).getActionName().startsWith("Undo")) && !(actionsForWorker.get(actionID).getActionName().startsWith("End Turn"))) {
                    highlightAvailableTiles(cellsToHighlight, colorsMap.get(myClientID));
                    gameLabel.setText("Choose a position");
                }
            }
        } else {
            my_turn = false;
            currentPlayerID = receivedCommand.getTarget();
            gameLabel.setText("Wait, it's " + idsUsernamesMap.get(currentPlayerID) + "'s turn");
            if(getAvailableWorkers(receivedCommand).size() == 2){
                highlightAvailableWorkers(getAvailableWorkers(receivedCommand), currentPlayerID);
            }else{
                List<NextAction> actions = actionsForWorker(receivedCommand.getAvailableActions(), getAvailableWorkers(receivedCommand).get(0));
                Vector2[] positionsToHighlight = getAllPositions(actions);
                highlightAvailableTiles(positionsToHighlight, colorsMap.get(currentPlayerID));
            }
        }
    }

    /**
     * This method handles the Place Workers command by highlighting the available positions
     * @param cmd Place Workers command
     */
    void onPlaceWorkersCommand(CommandWrapper cmd) {
        mapPlayers();
        initializeButtons();
        initializeMyGod();
        WorkerPlaceCommand workerPlaceCommand = cmd.getCommand(WorkerPlaceCommand.class);
        if (guiManager.isForMe(cmd)) {
            my_turn = true;
            state = 1; // means it's my turn to place workers
            gameLabel.setText("It's your turn, choose where to place your workers");
            workers_placement = new ArrayList<>();
            chosenWorkerPos = new Vector2[2];

            Colors myColor = colorsMap.get(myClientID);
            highlightAvailableTiles(workerPlaceCommand.getPositions(), myColor);
        } else {
            my_turn = false;
            currentPlayerID = workerPlaceCommand.getTarget();
            gameLabel.setText("Wait, " + idsUsernamesMap.get(currentPlayerID) + " is placing workers");
            highlightAvailableTiles(workerPlaceCommand.getPositions(), colorsMap.get(currentPlayerID));
        }
    }

    /**
     * Handles the update command by redrawing each tile
     * @param cmd update command coming from the server
     */
    void onUpdateCommand(CommandWrapper cmd) {
        UpdateCommand updateCommand = cmd.getCommand(UpdateCommand.class);
        CompactMap updatedMap = updateCommand.getUpdatedMap();
        CompactWorker[] updatedCompactWorkers = updatedMap.getWorkers();

        updateLevels(updatedMap);
        updateWorkers(updatedCompactWorkers);
        renderTiles();
    }

    //--------------------------------------------User's Interaction Handlers-------------------------------------------
    /**
     * Handle god power button
     * @param mouseEvent user click on the button
     */
    @FXML
    private void onGodPowerButtonClick(MouseEvent mouseEvent) {
        actionID = godPowerActionID;
        unHighlightTiles();
        NextAction[] availableActions = receivedCommand.getAvailableActions();  //all the actions
        List<NextAction> actionsForWorker = actionsForWorker(availableActions, selectedWorker); //actions for selected worker
        List<Vector2> possibleCells = actionsForWorker.get(actionID).getAvailablePositions();

        Vector2[] cellsToHighlight = new Vector2[possibleCells.size()];
        cellsToHighlight = possibleCells.toArray(cellsToHighlight);

        highlightAvailableTiles(cellsToHighlight, colorsMap.get(myClientID));
        disableAllButtons(true);
    }

    /**
     * Handles the leave game button click by asking for confirmation
     * @param mouseEvent user's click
     */
    @FXML
    private void onQuitButtonClick(MouseEvent mouseEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quit Game Confirmation");
        alert.setHeaderText("Are you sure you want to leave?");
        alert.setContentText("Click Ok to quit the client or cancel to continue playing");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            guiManager.send(LeaveCommand.makeRequest(myClientID, serverID));
            System.exit(0);
        }
    }

    /**
     * Handle the End Turn button
     * @param mouseEvent user's click on the button
     */
    @FXML
    private void onEndTurnButtonClick(MouseEvent mouseEvent) {
        actionID = getActionID("End Turn");
        guiManager.send(ActionCommand.makeReply(myClientID, serverID, actionID, selectedWorker, new Vector2(0,0)));
        disableAllButtons(true);
    }

    /**
     * Handle user's click on cancel worker selection button
     * @param mouseEvent user's click on the button
     */
    @FXML
    private void onCancellWorkerSelectionClick(MouseEvent mouseEvent) {
        selectedWorker = -1;
        unHighlightTiles();
        disableAllButtons(true);
        List<Integer> workers = getAvailableWorkers(receivedCommand);
        highlightAvailableWorkers(workers, myClientID);
    }

    /**
     * Handle user's click on undo button
     * @param mouseEvent user's click on the button
     */
    @FXML
    private void onUndoButtonClick(MouseEvent mouseEvent) {
        actionID = getActionID("Undo");
        List<Vector2> posToSend = posForUndo();
        guiManager.send(ActionCommand.makeReply(myClientID, serverID, actionID, selectedWorker, posToSend.get(0)
        ));
        endTurnButton.setDisable(true);
        undoButton.setDisable(true);
    }

    /**
     * Handle mouse clicks on a tile
     * @param mouseEvent user mouse click
     */
    private void onTileClick(javafx.scene.input.MouseEvent mouseEvent) {
        if (my_turn) {
            if (state == 1) { // workers placement phase
                Tile clickedTile = findTileByMouseEvent(mouseEvent);
                if (clickedTile.workerId == -1) { //there is no worker on the tile
                    workers_placement.add(clickedTile.pos);
                    clickedTile.putWorker(workers_placement.size() - 1, myClientID);
                    clickedTile.render();
                    if (workers_placement.size() == 2) {
                        my_workers = workers_placement.toArray(my_workers);
                        sendMyWorkers(my_workers);
                        disableAllButtons(true);
                        unHighlightTiles();
                        my_turn = false;
                        state = 0;
                    }
                } else {
                    gameLabel.setText("There's already a worker on this tile...");
                }
            } else if (state == 2) { // i'm in the move/build phase
                Tile clickedTile = findTileByMouseEvent(mouseEvent);
                if (selectedWorker == -1) { // if I haven't selected a worker yet
                    if ((clickedTile.workerId != -1)
                            && clickedTile.workerOwnerID == myClientID                                //the worker I just clicked is mine
                            && getAvailableWorkers(receivedCommand).contains(clickedTile.workerId)) { // the worker is available to be chosen
                        selectedWorker = clickedTile.workerId;
                        unHighlightTiles();
                        gameLabel.setText("Now choose a position");
                        if (getAvailableWorkers(receivedCommand).size() > 1)
                            cancellWorkerSelection.setDisable(false);
                        NextAction[] availableActions = receivedCommand.getAvailableActions();  //all the actions
                        List<NextAction> actionsForWorker = actionsForWorker(availableActions, selectedWorker); //all the actions for the selected worker
                        bindButtonsActions();
                        if (actionsForWorker.size() == 1) { // i have one possible action
                            actionID = 0;
                            List<Vector2> possibleCells = actionsForWorker.get(actionID).getAvailablePositions();
                            Vector2[] cellsToHighlight = new Vector2[possibleCells.size()];
                            cellsToHighlight = possibleCells.toArray(cellsToHighlight);
                            highlightAvailableTiles(cellsToHighlight, colorsMap.get(myClientID));
                        } else { // the worker has multiple possible actions
                            List<Vector2> possibleCells = actionsForWorker.get(actionID).getAvailablePositions();
                            Vector2[] cellsToHighlight = new Vector2[possibleCells.size()];
                            cellsToHighlight = possibleCells.toArray(cellsToHighlight);
                            if (!(availableActions[actionID].getActionName().startsWith("End Turn")) && !(availableActions[actionID].getActionName().startsWith("Undo"))) {
                                highlightAvailableTiles(cellsToHighlight, colorsMap.get(myClientID));
                            }
                        }
                    }
                } else { // handle execution, I've already selected a worker
                    if (getAvailableWorkers(receivedCommand).size() > 1)
                        cancellWorkerSelection.setDisable(false);
                    Vector2 posToSend = clickedTile.pos;
                    guiManager.send(ActionCommand.makeReply(myClientID, serverID, actionID, selectedWorker, posToSend));
                    unHighlightTiles();
                    disableAllButtons(true);
                    my_turn = false;
                    state = 0;
                }
            }
        } else {
            gameLabel.setText("Wait, it's " + idsUsernamesMap.get(currentPlayerID) + "'s turn");
        }
    }

    //------------------------------------------Update Methods----------------------------------------------------------

    /**
     * Method that updates the levels
     * @param updatedMap new map coming in the update command
     */
    private void updateLevels(CompactMap updatedMap) {
        int col = 0;
        int row = 0;

        for (int i = 0; i < updatedMap.getLength() * updatedMap.getHeight(); i++) {
            Tile tileToUpdate = tiles[row][col];
            int newLevel = updatedMap.getLevel(row, col);
            boolean hasDome = updatedMap.isDome(row, col);
            tileToUpdate.level = newLevel;
            tileToUpdate.hasDome = hasDome;

            col++;
            if (col == 5) {
                row++;
                col = 0;
            }
        }
    }

    /**
     * Method that updates the workers
     * @param updatedWorkers array containing the updated info about the workers
     */
    private void updateWorkers(CompactWorker[] updatedWorkers) {
        for (int col = 0; col < 5; col++) {
            for (int row = 0; row < 5; row++) {
                tiles[row][col].putWorker(-1, -1);
            }
        }
        for (CompactWorker compactWorker : updatedWorkers) {
            Tile tileToUpdate = tiles[compactWorker.getPosition().getX()][compactWorker.getPosition().getY()];
            tileToUpdate.putWorker(compactWorker.getWorkerID(), compactWorker.getOwnerID());
            tileToUpdate.ownerUsername = idsUsernamesMap.get(compactWorker.getOwnerID());
            //tileToUpdate.godName = cardCollection.getCard(  idsGodsMap.get(guiManager.getServerConnection().getClientID())).getName();
            tileToUpdate.godName = cardCollection.getCard(idsGodsMap.get(compactWorker.getOwnerID())).getName();
        }
    }

    /**
     * This method iterates thru all the tiles of the mapGrid and renders them
     */
    private void renderTiles() {
        for (int col = 0; col < 5; col++) {
            for (int row = 0; row < 5; row++) {
                Tile tileToRender = tiles[row][col];
                tileToRender.render();
            }
        }
    }

    //-------------------------------------Other - Utility Methods------------------------------------------------------
    /**
     * Highlights the available tiles
     * @param availablePositions array representing all the available positions
     * @param color representing the color of the highlight
     */
    private void highlightAvailableTiles(Vector2[] availablePositions, Colors color) {
        for (Vector2 availablePosition : availablePositions) {
            Tile tileToHighlight = tiles[availablePosition.getX()][availablePosition.getY()];
            tileToHighlight.highlightTile(color);
        }
    }

    /**
     * This method removes the highlight image of a tile if it has one
     */
    private void unHighlightTiles() {
        for (int col = 0; col < mapGrid.getColumnCount(); col++) {
            for (int row = 0; row < mapGrid.getRowCount(); row++) {
                Tile tileToUnhighlight = tiles[col][row];
                tileToUnhighlight.unhighlight();
            }
        }
    }

    /**
     * Gets all the positions in a list of actions
     * @param actions List of actions
     * @return an array of all the positions
     */
    private Vector2[] getAllPositions(List<NextAction> actions){
        ArrayList<Vector2> positions = new ArrayList<>();
        for(NextAction action : actions){
            if(!action.getActionName().startsWith("Undo") && !action.getActionName().startsWith("End Turn")) {
                List<Vector2> availablePositions = action.getAvailablePositions();
                for (Vector2 pos : availablePositions) {
                    if (!positions.contains(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        Vector2[] allPositions = new Vector2[positions.size()];
        allPositions = positions.toArray(allPositions);
        return allPositions;
    }

    /**
     * Checks if the incoming command has only the end turn option
     * @param actionCommand received command
     * @return true if the only actions is an End Turn action
     */
    private boolean onlyEndTurnAvailable(ActionCommand actionCommand){
        return actionCommand.getAvailableActions().length == 1 && actionCommand.getAvailableActions()[0].getActionName().equals("End Turn");
    }

    /**
     * Method that binds buttons to the respective incoming available actions
     */
    private void bindButtonsActions() {
        int actionIndex = 0;
        //actionsForWorker(receivedCommand.getAvailableActions());
        receivedCommand.getAvailableActions();
        for (NextAction anAction : actionsForWorker(receivedCommand.getAvailableActions(), selectedWorker)) {
            String actionName = anAction.getActionName();
            if (actionName.startsWith("Undo"))
                undoButton.setDisable(false);
            else if (actionName.startsWith("End Turn"))
                endTurnButton.setDisable(false);
            else if (actionName.contains("Again") || actionName.startsWith("Build Dome") || actionIndex > 0) {
                godPowerActionID = actionIndex;
                godPowerButton.setDisable(false);
            }
            actionIndex++;
        }
    }

    /**
     * This method checks if Undo is an available action
     * @return true / false accordingly
     */
    private boolean isUndoAvailable() {
        NextAction[] availableActions = receivedCommand.getAvailableActions();
        List<NextAction> actionsForWorker = actionsForWorker(availableActions, selectedWorker);
        for (NextAction nextAction : actionsForWorker) {
            if (nextAction.getActionName().startsWith("Undo")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get's all the available actions for a worker
     * @param availableActions Array containing all the available actions
     * @param workerID ID of the worker
     * @return a list of actions for the worker
     */
    private List<NextAction> actionsForWorker(NextAction[] availableActions, int workerID) {
        List<NextAction> actions = new ArrayList<>();
        for (NextAction anAction : availableActions) {
            if (anAction.getWorkerID() == workerID)
                actions.add(anAction);
        }
        return actions;
    }

    /**
     * Gets god's special power name
     * @return power name
     */
    private String getPowerName(){
        int myGodID = idsGodsMap.get(myClientID);
        switch (myGodID){
            case 2:
                return "Move Again";
            case 4:
                return "Build Dome";
            case 5:
            case 6:
                return "Build Again";
            case 10:
                return "Build";
            default:
                return "Passive";
        }
    }

    /**
     * Returns the position to construct the undo command with
     * @return undo position
     */
    private List<Vector2> posForUndo(){
        NextAction[] nextActions = receivedCommand.getAvailableActions();
        for(int i = 0; i< nextActions.length;i++){
            if(nextActions[i].getActionName().startsWith("Undo"))
                return nextActions[i].getAvailablePositions();
        }
        return null;
    }

    /**
     * Disables / enables all buttons
     * @param par if true will disable all, if false will enable all
     */
    private void disableAllButtons(boolean par){
        endTurnButton.setDisable(par);
        godPowerButton.setDisable(par);
        cancellWorkerSelection.setDisable(par);
        //quitButton.setDisable(par);
        undoButton.setDisable(par);
    }

    /**
     * Gets the action id given it's name
     * @param actionName representing action's name
     * @return action's id
     */
    private int getActionID(String actionName){
        NextAction[] actions = receivedCommand.getAvailableActions();
        for(int i = 0; i < actions.length; i++){
            if(actions[i].getActionName().startsWith(actionName))
                return i;
        }
        return -1;
    }

    /**
     * This method gets the clicked Tile from the mouse event
     * @param mouseEvent user click
     * @return tile clicked
     */
    private Tile findTileByMouseEvent(javafx.scene.input.MouseEvent mouseEvent){
        Node clickedNode = mouseEvent.getPickResult().getIntersectedNode();
        Tile clickedTile;
        if (clickedNode instanceof Tile) {
            clickedTile = (Tile) clickedNode;
        } else {
            Node parent = clickedNode.getParent();
            clickedTile = (Tile) parent;
        }
        return clickedTile;
    }

    /**
     * Sends the chosen positions to place my workers
     * @param w_pos array of positions I chose
     */
    private void sendMyWorkers(Vector2[] w_pos) {
        guiManager.send(WorkerPlaceCommand.makeWrapped(myClientID, serverID, w_pos));
    }

    /**
     * Checks if my only two options are undo and end turn
     * @param receivedCommand command received from the server
     * @return true if my only options are undo and end turn
     */
    private boolean onlyEndTurnAndUndoAvailable(ActionCommand receivedCommand) {
        if( receivedCommand.getAvailableActions().length == 2);
            if (receivedCommand.getAvailableActions()[0].getActionName().startsWith("End Turn") || receivedCommand.getAvailableActions()[0].getActionName().startsWith("Undo"))
                if (receivedCommand.getAvailableActions()[1].getActionName().startsWith("End Turn") || receivedCommand.getAvailableActions()[1].getActionName().startsWith("Undo"))
                    return true;
        return false;
    }

    /**
     * Gets available workers in an action command
     * @param receivedCommand received action command
     * @return list of integers representing all available workers
     */
    private List<Integer> getAvailableWorkers(ActionCommand receivedCommand){
        List<Integer> availableWorkers = new ArrayList<>();
        for(int i = 0; i<receivedCommand.getAvailableActions().length; i++){
            Integer x = receivedCommand.getAvailableActions()[i].getWorkerID();
            if(!availableWorkers.contains(x)){
                availableWorkers.add(x);
            }
        }
        return availableWorkers;
    }

    /**
     * Highlights available workers
     * @param availableWorkers List of integers representing worker ids
     * @param clientID worker's owner client ID
     */
    private void highlightAvailableWorkers(List<Integer> availableWorkers, int clientID){
        for( Integer workerID : availableWorkers ){
            highlightWorker(workerID, clientID, colorsMap.get(clientID));
        }
    }

    /**
     * Highlights a worker
     * @param workerID worker's ID
     * @param clientID worker owner ID
     * @param color color of the highlight
     */
    private void highlightWorker(int workerID, int clientID, Colors color){
        for(int col = 0; col < 5 ; col++){
            for (int row = 0; row < 5; row++) {
                Tile tileToHighlight = tiles[col][row];
                if(tileToHighlight.workerId == workerID && tileToHighlight.workerOwnerID == clientID){
                    tileToHighlight.highlightTile(color);
                }
            }
        }
    }

    /**
     * Utility method - gets the ImageView fron and URL
     * @param url URL representing the address of the image
     * @return ImageView accessed via the URL
     */
    private Image getImageImageViewByURL(URL url) {
        try(InputStream inputStream = url.openStream()){
            return new Image(inputStream);
        }catch (IOException e){
            System.out.println("[GAME SCENE] Couldn't access buildings resources");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method maps each client to a specific worker color image URL
     * Method called by the GuiManager on the FirstPlayerPick command
     */
    void mapPlayers() {
        List<Integer> clientIds = guiManager.getAllPlayerIds();
        Collections.sort(clientIds);
        Queue<Colors> colorsQueue = new ArrayDeque<>(Arrays.asList(
                Colors.BLUE,
                Colors.RED,
                Colors.YELLOW
        ));
        Queue<URL> images = new ArrayDeque<>(Arrays.asList(
                getClass().getResource("/img/workers/worker_blue.png"),
                getClass().getResource("/img/workers/worker_red.png"),
                getClass().getResource("/img/workers/worker_yellow.png")
        ));
        for (Integer client : clientIds) {
            URL url = images.poll();
            Colors color = colorsQueue.poll();
            workerImages.put(client, url);
            colorsMap.put(client, color);
        }
    }

    /**
     * Checks if End Turn is among the possible actions
     * @return true / false accordingly
     */
    private boolean isEndTurnAvailable(){
        NextAction[] availableActions = receivedCommand.getAvailableActions();
        for(NextAction anAction : availableActions){
            if(anAction.getActionName().startsWith("End Turn"))
                return true;
        }
        return false;
    }
    /**
     * Calls the unHighlightWorker method on each Tile
     */
    private void unHighlightWorkers() {
        for (int col = 0; col < 5; col++) {
            for (int row = 0; row < 5; row++) {
                Tile tile = tiles[col][row];
                //tile.unhighlight("highlight_worker");
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * This class represents a grid cell of the mapGrid
     * It extends StackPane so that it's easy to place/remove elements to/from a mapGrid cell
     */
    private static class Tile extends StackPane {
        private int workerOwnerID = -1;         //initially has no worker owner id
        private String ownerUsername = null;
        private String godName = null;
        private int workerId = -1;              //initially has no worker id
        private int level = 0;                  //initially the Tile has no building on it
        private boolean hasDome = false;        //initially the Tile has no dome on it
        private Vector2 pos;                    //the position of this tile inside the gridMap
        private Map<Integer, URL> workerImages; //map to have a reference of each player's worker color
        private GridPane mapGrid;
        private Settings settings;


        /**
         * Places a worker on this Tile
         * @param workerId      the worker to place
         * @param workerOwnerID worker owner's id to place
         */
        void putWorker(int workerId, int workerOwnerID) {
            this.workerId = workerId;
            this.workerOwnerID = workerOwnerID;
        }

        /**
         * Highlights the Tile by placing a highlight image
         */
        private void highlightTile(Colors color) {
            ImageView highlight = initializeHighlight(color);
            this.getChildren().add(highlight);
        }

        /**
         * Removes the highlight image from the Tile if it has one
         */
        private void unhighlight() {
            Node toRemove = null;
            ObservableList<Node> imagesOnTile = this.getChildren();
            for (Node node : imagesOnTile) {
                if (node.getId() != null) {
                    if (node.getId().startsWith("highlight")) {
                        toRemove = node;
                        break;
                    }
                    break;
                }
            }
            this.getChildren().remove(toRemove);
        }

        /**
         * Renders the Tile from scratch
         */
        private void render() {
            getChildren().clear();
            placeBuildingImage();
            placeWorkerImage();
            if (this.workerId != -1) {
                Tooltip.install(this, new Tooltip("Owner: " + ownerUsername + "\nGod: " + godName + "\nLvL: " + level ));
            } else
                Tooltip.install(this, new Tooltip("LvL: " + level));
        }

        /**
         * Render method - Places the corresponding image of a worker
         */
        private void placeWorkerImage() {
            int workerOwnerID = this.workerOwnerID;
            int workerID = this.workerId;

            if (workerID == -1) {
                return;
            }
            URL url = workerImages.get(workerOwnerID);
            ImageView my_worker = getImageImageViewByURL(url);
            my_worker.setPreserveRatio(true);
            double workerScaleFactor = getScaleFactor(this);
            my_worker.fitWidthProperty().bind(this.mapGrid.widthProperty().multiply(0.2).multiply(workerScaleFactor));
            this.getChildren().add(my_worker);
        }

        private double getScaleFactor(Tile tile) {
            int tileLevel = tile.level;
            switch (tileLevel){
                case 0:
                    return 0.65;
                case 1:
                    return 0.7;
                case 2:
                    return 0.75;
                case 3:
                    return 0.8;
            }
            return -1;
        }

        /**
         * Render method - Places the corresponding image of the Tile level
         */
        private void placeBuildingImage() {
            int levelToPlace = this.level;
            switch (levelToPlace) {
                case 0:{
                    break;
                }
                case 1: {
                    URL url;
                    if(settings.getTheme() == Settings.Themes.LIGHT)
                        url = getClass().getResource("/img/buildings/Level_1.png");
                    else
                        url = getClass().getResource("/img/buildings/dark/Level_1.png");
                    ImageView levelImage = getImageImageViewByURL(url);
                    levelImage.setPreserveRatio(true);
                    levelImage.fitWidthProperty().bind(this.widthProperty().multiply(0.9));
                    this.getChildren().add(levelImage);
                    break;
                }
                case 2: {
                    URL url;
                    if(settings.getTheme() == Settings.Themes.LIGHT)
                        url = getClass().getResource("/img/buildings/Level_2.png");
                    else
                        url = getClass().getResource("/img/buildings/dark/Level_2.png");
                    ImageView levelImage = getImageImageViewByURL(url);
                    levelImage.setPreserveRatio(true);
                    levelImage.fitWidthProperty().bind(this.widthProperty().multiply(0.9));
                    this.getChildren().add(levelImage);
                    break;
                }
                default: {
                    URL url;
                    if(settings.getTheme() == Settings.Themes.LIGHT)
                        url = getClass().getResource("/img/buildings/Level_3.png");
                    else
                        url = getClass().getResource("/img/buildings/dark/Level_3.png");
                    ImageView levelImage = getImageImageViewByURL(url);
                    levelImage.setPreserveRatio(true);
                    levelImage.fitWidthProperty().bind(this.widthProperty().multiply(0.9));
                    this.getChildren().add(levelImage);
                    break;
                }
            }
            if (hasDome) {
                URL url = getClass().getResource("/img/buildings/dome.png");
                ImageView domeImage = getImageImageViewByURL(url);
                domeImage.setPreserveRatio(true);
                domeImage.fitWidthProperty().bind(this.widthProperty().multiply(0.9));
                this.getChildren().add(domeImage);
            }
        }

        /**
         * Highlights a worker by placing a highlight ImageView on the Tile
         */
        void highlightWorker(Colors color) {
            ImageView workerHighlight = initializeHighlight(color);
            this.getChildren().add(workerHighlight);
        }

        /**
         * Utility method, gets an ImageView from the resources given the url
         * @param url link to ImageView
         * @return accessed ImageView
         */
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
         * Returns an ImageView of the desired highlight
         *
         * @return ImageView of the highlight
         */
        ImageView initializeHighlight(Colors color){
            String colorString;
            if(color == Colors.BLUE)
                colorString = "Blue";
            else if(color == Colors.RED)
                colorString = "Red";
            else
                colorString = "Yellow";
            URL highlightURL = getClass().getResource("/img/common/highlight"+colorString+".png");
            ImageView highlight = getImageImageViewByURL(highlightURL);

            highlight.setId("highlight");
            highlight.setPreserveRatio(true);
            highlight.fitWidthProperty().bind(this.widthProperty().multiply(0.9));

            ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(2), highlight);
            scaleTransition.setByX(-0.3);
            scaleTransition.setByY(-0.3);
            scaleTransition.setCycleCount(ScaleTransition.INDEFINITE);
            scaleTransition.setAutoReverse(true);
            scaleTransition.play();

            return highlight;
        }

        /**
         * Setter for the map grid
         * @param mapGrid map grid to set
         */
        void setMapGrid(GridPane mapGrid) {
            this.mapGrid = mapGrid;
        }
    }
}

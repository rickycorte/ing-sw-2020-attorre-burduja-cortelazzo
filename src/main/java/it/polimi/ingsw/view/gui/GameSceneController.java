package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.game.Vector2;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import javafx.scene.control.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class GameSceneController implements Initializable {

    private boolean my_turn;            //true if it's my turn
    private int state;                  // 0 - waiting for other players/for game to start
    // 1 - placing workers state
    // 2 -
    private Tile[][] tiles;             //bi dimensional array holding a reference to all my Tiles

    private GuiManager guiManager;

    private Vector2[] my_workers;

    private Vector2[] chosenWorkerPos;
    private ArrayList<Vector2> workers_placement;


    private int selectedWoker;
    private Map<Integer, URL> workerImages;

    private Map<Integer, String> idsUsernamesMap;

    private Map<Integer, Integer> idsGodsMap;

    private ActionCommand receivedCommand;

    private int actionID;

    private int currentPlayerID;


    @FXML
    private Button endTurnButton;
    @FXML
    private Button godPowerButton;
    @FXML
    private Button cancellWorkerSelection;

    @FXML
    private Label gameLabel;
    @FXML
    private StackPane stack_id;
    @FXML
    private ImageView map;
    @FXML
    private ImageView player1_god;
    @FXML
    private ImageView player2_god;
    @FXML
    private ImageView myGod;
    @FXML
    private GridPane mapGrid;



    @FXML
    public void initialize() {

    }

    //TODO make sure it works with only 2 players too

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        guiManager = GuiManager.getInstance();
        guiManager.setGameSceneController(this);
        idsUsernamesMap = GuiManager.getInstance().getIDsUsernameMap();
        idsGodsMap = GuiManager.getInstance().getIDsGodsMap();

        workerImages = new HashMap<>();
        if (guiManager.getAllPlayerIds().size() > 0)
            mapPlayers();
        my_workers = new Vector2[2];

        state = 0;
        my_turn = false;
        stack_id.getChildren().add(initializeTiles());

        initializeButtons();
        disableAllButtons();

    }

    /**
     * This method handles the Place Workers command by highlighting the available positions
     * @param cmd Place Workers command
     */
    void onPlaceWorkersCommand(CommandWrapper cmd) {
        if (GuiManager.getInstance().isForMe(cmd)) {
            my_turn = true;
            state = 1; // means it's my turn to place workers
            gameLabel.setText("It's your turn, choose two tiles the map to place your workers");
            workers_placement = new ArrayList<>();
            chosenWorkerPos = new Vector2[2];

            WorkerPlaceCommand workerPlaceCommand = cmd.getCommand(WorkerPlaceCommand.class);
            Vector2[] available_positions = workerPlaceCommand.getPositions();
            highlightAvailableTiles(available_positions);
        } else {
            currentPlayerID = cmd.getCommand(WorkerPlaceCommand.class).getTarget();
            gameLabel.setText("Wait, " + idsUsernamesMap.get(currentPlayerID) + " is placing workers");
        }
    }

    /**
     * This method adds a highlight image on a tile
     * @param availablePositions represents the tiles/positions to highlight
     */
    void highlightAvailableTiles(Vector2[] availablePositions) {
        for (int i = 0; i < availablePositions.length; i++) {
            Tile tileToHighlight = tiles[availablePositions[i].getX()][availablePositions[i].getY()];
            tileToHighlight.highlightTile();
        }
    }

    /**
     * This method removes the highlight image of a tile if it has one
     */
    void unHighlightTiles() {
        for (int col = 0; col < mapGrid.getColumnCount(); col++) {
            for (int row = 0; row < mapGrid.getRowCount(); row++) {
                Tile tileToUnhighlight = tiles[col][row];
                tileToUnhighlight.unhighlight();
            }
        }
    }

    /**
     * Handle mouse clicks on a tile
     * @param mouseEvent user mouse click
     */
    void onTileClick(javafx.scene.input.MouseEvent mouseEvent){
        if(my_turn){
            if(state == 1){ // workers placement phase
                Tile clickedTile = findTileByMouseEvent(mouseEvent);
                if(clickedTile.workerId == -1) {
                    workers_placement.add(clickedTile.pos);

                    clickedTile.putWorker(workers_placement.size() - 1, GuiManager.getInstance().getServerConnection().getClientID());
                    clickedTile.render();
                    if( workers_placement.size() == 2){
                        my_workers = workers_placement.toArray(my_workers);
                        sendMyWorkers(my_workers);
                        disableAllButtons();
                        my_turn = false;
                        state = 0;
                    }
                }
            }else if(state == 2){ // i'm in the move/build phase
                Tile clickedTile = findTileByMouseEvent(mouseEvent);

                if(selectedWoker == -1){ // if I haven't selected a worker yet
                    if ((clickedTile.workerId != -1)
                            && clickedTile.workerOwnerID == GuiManager.getInstance().getServerConnection().getClientID()
                            && receivedCommand.getAvailableWorker().contains(clickedTile.workerId)) {
                            // the worker is mine

                        selectedWoker = clickedTile.workerId;
                        unHighlightWorkers();
                        if( receivedCommand.getActionForWorker(selectedWoker).size() == 1) { // i have one possible action
                            int actionBaseIndex = receivedCommand.getBaseIndexForAction(selectedWoker);
                            actionID = 0;

                            List<Vector2> availableCells = receivedCommand.getPositionsForAction(actionBaseIndex + actionID);
                            Vector2[] cellsToHighlight = new Vector2[availableCells.size()];
                            cellsToHighlight = availableCells.toArray(cellsToHighlight);
                            highlightAvailableTiles(cellsToHighlight);
                            cancellWorkerSelection.setDisable(false);
                        }else if( receivedCommand.getActionForWorker(selectedWoker).size() == 2 ){ // the worker has two possible actions
                            //make the user choose which of the two actions to execute
                            if(isEndTurnAvailable()){
                                endTurnButton.setDisable(false);
                            }
                            List<String> availableActions = receivedCommand.getActionForWorker(selectedWoker);
                            actionID = 0;
                            godPowerButton.setText(availableActions.get(1));
                            godPowerButton.setDisable(false);

                            int actionBaseIndex = receivedCommand.getBaseIndexForAction(selectedWoker);
                            List<Vector2> availableCells = receivedCommand.getPositionsForAction(actionBaseIndex + actionID);
                            Vector2[] cellsToHighlight = new Vector2[availableCells.size()];
                            cellsToHighlight = availableCells.toArray(cellsToHighlight);
                            if(!(availableActions.get(actionID).startsWith("End Turn"))) {
                                highlightAvailableTiles(cellsToHighlight);
                            }
                            //cancellWorkerSelection.setDisable(false);

                        }else {
                            // the worker has more than 2 possible actions or no possible action, should never happen
                        }

                    }else{
                        // the worker is either not mine or there is no worker at all or is not available to be chosen
                    }

                }else { // handle execution, i have already selected a worker
                    cancellWorkerSelection.setDisable(false);
                    int actionBaseIndex = receivedCommand.getBaseIndexForAction(selectedWoker);

                    int[] workerAndAction = new int[2];
                    workerAndAction[0] = selectedWoker;

                    int a = lookForPos(receivedCommand.getPositionsForAction(actionBaseIndex + actionID), clickedTile.pos);

                    int baseIndex = receivedCommand.getBaseIndexForPositions(actionBaseIndex + actionID);
                    Vector2 posToSend = receivedCommand.getTargetPosition(baseIndex + a);

                    workerAndAction[1] = actionID;
                    GuiManager.getInstance().send(new CommandWrapper(CommandType.ACTION_TIME, new ActionCommand(GuiManager.getInstance().getServerConnection().getClientID(), GuiManager.getInstance().getServerConnection().getServerID(), workerAndAction, posToSend)));
                    unHighlightTiles();
                    disableAllButtons();
                    my_turn = false;
                    state = 0;
                }
            }
        }else{
            gameLabel.setText("Wait, it's not your turn yet");
        }
    }

    void initializeButtons(){
        godPowerButton.setText("Use god power");
        godPowerButton.setOnMouseClicked(this :: onGodPowerButtonClick);

        endTurnButton.setText("End Turn");
        endTurnButton.setOnMouseClicked(this :: onEndTurnButtonClick);

        cancellWorkerSelection.setText("Cancel W.Sel");
        cancellWorkerSelection.setOnMouseClicked(this :: onCancellWorkerSelectionClick);

    }

    private void onCancellWorkerSelectionClick(MouseEvent mouseEvent) {
        selectedWoker = -1;
        unHighlightTiles();
        disableAllButtons();
    }

    /**
     * Disables all buttons
     */
    void disableAllButtons(){
        endTurnButton.setDisable(true);
        godPowerButton.setDisable(true);
        cancellWorkerSelection.setDisable(true);
    }

    /**
     * Handle the End Turn button
     * @param mouseEvent user's click on the button
     */
    private void onEndTurnButtonClick(MouseEvent mouseEvent) {
        GuiManager.getInstance().send(new CommandWrapper(CommandType.ACTION_TIME, new ActionCommand(
                GuiManager.getInstance().getServerConnection().getClientID(),
                GuiManager.getInstance().getServerConnection().getServerID(),
                endTurnCommand(),
                receivedCommand.getAvailablePos()[0])));
        disableAllButtons();
    }

    /**
     * Checks if End Turn is among the possible actions
     * @return true / false accordingly
     */
    boolean isEndTurnAvailable(){
        List<String> availableActions = receivedCommand.getActionForWorker(selectedWoker);
        for(String anAction : availableActions){
            if(anAction.startsWith("End Turn"))
                return true;
        }
        return false;
    }

    /**
     * Handle god power button
     * @param mouseEvent user click on the button
     */
    void onGodPowerButtonClick(MouseEvent mouseEvent) {
        actionID = 1;
        int actionBaseIndex = receivedCommand.getBaseIndexForAction(selectedWoker);
        List<Vector2> availableCells = receivedCommand.getPositionsForAction(actionBaseIndex + actionID);
        Vector2[] cellsToHighlight = new Vector2[availableCells.size()];
        cellsToHighlight = availableCells.toArray(cellsToHighlight);
        highlightAvailableTiles(cellsToHighlight);
        disableAllButtons();
    }

    /**
     * This method gets the clicked Tile from the mouse event
     * @param mouseEvent user click
     * @return tile clicked
     */
    Tile findTileByMouseEvent(javafx.scene.input.MouseEvent mouseEvent){
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
     * Gets the index of the chosen position
     * @param positionsForAction List of available positions
     * @param pos   chosen position
     * @return index of the chosen position inside the available positions list
     */
    private int lookForPos(List<Vector2> positionsForAction, Vector2 pos) {
        for (int i = 0; i < positionsForAction.size(); i++) {
            if (positionsForAction.get(i).equals(pos))
                return i;
        }
        return -1;
    }

    /**
     * Sends the chosen positions to place my workers
     * @param w_pos array of positions I chose
     */
    void sendMyWorkers(Vector2[] w_pos) {
        GuiManager.getInstance().send(new CommandWrapper(CommandType.PLACE_WORKERS, new WorkerPlaceCommand(GuiManager.getInstance().getServerConnection().getClientID(), GuiManager.getInstance().getServerConnection().getServerID(), w_pos)));
    }

    /**
     * Handles Action commands
     * @param cmd action command to handle
     */
    void onActionCommand(CommandWrapper cmd) {
        if (GuiManager.getInstance().isForMe(cmd)) {
            if (cmd.getCommand(ActionCommand.class).getActionName()[0].equals("End Turn") && cmd.getCommand(ActionCommand.class).getActionName().length == 1) { // if i got an End Turn action and it's the only option
                ActionCommand endTurn = cmd.getCommand(ActionCommand.class);
                GuiManager.getInstance().send(new CommandWrapper(CommandType.ACTION_TIME, new ActionCommand(
                        GuiManager.getInstance().getServerConnection().getClientID(),
                        GuiManager.getInstance().getServerConnection().getServerID(),
                        endTurnCommand(),
                        endTurn.getAvailablePos()[0])));
                return;
            }
            highlightAvailableWorkers(cmd.getCommand(ActionCommand.class).getAvailableWorker());
            selectedWoker = -1;
            receivedCommand = cmd.getCommand(ActionCommand.class);
            my_turn = true;
            state = 2;
            gameLabel.setText("Click on a Worker then click on a tile");
        } else {
            currentPlayerID = cmd.getCommand(ActionCommand.class).getTarget();
            gameLabel.setText("Wait, it's " + idsUsernamesMap.get(currentPlayerID) + "'s turn");
        }
    }

    /**
     * Utility method in case End Turn is the only available action
     * @return worker id  and N move array
     */
    int[] endTurnCommand(){
        if(selectedWoker == 0){
            return new int[] {0, 0};
        }else{
            return new int[] {1, 0};
        }
    }
    /**
     * Iterates thru all the available workers and highlights them
     * @param availableWorkers workers to highlight
     */
    void highlightAvailableWorkers(List<Integer> availableWorkers){
        for( Integer workerID : availableWorkers ){
            highlightWorker(workerID);
        }
    }

    /**
     * Highlights a worker by placing an ImageView on his Tile
     * @param workerID worker to highlight
     */
    void highlightWorker(int workerID){
        for(int col = 0; col < 5 ; col++){
            for (int row = 0; row < 5; row++) {
                Tile tileToHighlight = tiles[col][row];
                if(tileToHighlight.workerId == workerID && tileToHighlight.workerOwnerID == GuiManager.getInstance().getServerConnection().getClientID()){
                    tileToHighlight.highlightWorker();
                }
            }
        }
    }

    /**
     * Calls the unHighlightWorker method on each Tile
     */
    void unHighlightWorkers() {
        for (int col = 0; col < 5; col++) {
            for (int row = 0; row < 5; row++) {
                Tile tile = tiles[col][row];
                tile.unHighlightWorker();
            }
        }
    }

    /**
     * Handles the update command by redrawing each tile
     * @param cmd update command coming from the server
     */
    void onUpdateCommand(CommandWrapper cmd) {
        UpdateCommand updateCommand = cmd.getCommand(UpdateCommand.class);
        int[] map_workers = updateCommand.getIntData(); // represents map and workers
        //Vector2[] workers_pos = updateCommand.getV2Data(); // represents worker's position
        int[] worker_info = updateCommand.getWorkersInfo(); // represents all the info about workers

        updateLevels(map_workers);
        updateWorkers(worker_info);

        renderTiles();
    }

    /**
     * Method that updates the map with the new levels
     * @param map_workers array representing the new map with its new building levels, see UpdateCommand
     */
    void updateLevels(int[] map_workers) {
        int col = 0;
        int row = 0;
        for (int i = 0; i < 25; i++) {
            Tile tileToUpdate = tiles[row][col];
            if (tileToUpdate.getLevel() != map_workers[i])
                tileToUpdate.buildLevel(map_workers[i]);
            col++;
            if (col == 5) {
                row++;
                col = 0;
            }
        }
    }

    /**
     * This method updates all the worker's info from information coming from the server (update command)
     * @param workers_info an array representing all the worker's info, see UpdateCommand
     */
    void updateWorkers(int[] workers_info) {
        for (int col = 0; col < 5; col++) {
            for (int row = 0; row < 5; row++) {
                tiles[row][col].putWorker(-1, -1);
            }
        }

        for (int i = 0; i < workers_info.length; i = i + 4) {
            int workerOwnerID = workers_info[i];
            int workerID = workers_info[i + 1];
            int workerXpos = workers_info[i + 2];
            int workerYpos = workers_info[i + 3];
            Tile tileToUpdate = tiles[workerXpos][workerYpos];
            tileToUpdate.putWorker(workerID, workerOwnerID);
        }
    }

    /**
     * This method iterates thru all the tiles of the mapGrid and renders them
     */
    void renderTiles() {
        for (int col = 0; col < 5; col++) {
            for (int row = 0; row < 5; row++) {
                Tile tileToRender = tiles[row][col];
                tileToRender.render();
            }
        }
    }

    /**
     * This method initializes the mapGrid pane by placing a Tile on each cell
     * @return mapGrid initialized
     */
    private GridPane initializeTiles() {
        mapGrid = new GridPane();
        mapGrid.setMinSize(map.getFitWidth(), map.getFitHeight());
        double tileW = map.getFitWidth() / 5;
        double tileH = map.getFitHeight() / 5;

        tiles = new Tile[5][5];
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Tile tile = new Tile();
                tile.setOnMouseClicked((e) -> {
                    System.out.println("[GAME SCENE] Tile clicked");
                    onTileClick(e);
                    ///
                });
                //System.out.println("[GAME SCENE] Set on tile col: " + col + " row: " + row);
                //tile.setId(String.valueOf(col) + String.valueOf(row));
                //System.out.println("[GAME SCENE] Tile ID: " + tile.getId());
                tile.pos = new Vector2(row, col);
                //tile.getChildren().add(new Label("row: " + tile.pos.getX() + " col: " + tile.pos.getY()));
                tile.workerImages = workerImages;
                tile.setMinSize(tileW, tileH);
                mapGrid.add(tile, col, row);

                tiles[row][col] = tile;
            }
        }
        mapGrid.setGridLinesVisible(true);
        return mapGrid;
    }

    /**
     * This method maps each client to a specific worker color image URL
     */
    void mapPlayers() {
        List<Integer> clientIds = guiManager.getAllPlayerIds();
        //System.out.printf("player idssssss %s\n", clientIds);
        Collections.sort(clientIds);
        Queue<URL> images = new ArrayDeque<>(Arrays.asList(
                getClass().getResource("/img/workers/worker_blue.png"),
                getClass().getResource("/img/workers/worker_red.png"),
                getClass().getResource("/img/workers/worker_yellow.png")
        ));
        for (Integer client : clientIds) {
            URL url = images.poll();
            workerImages.put(client, url);
        }
    }


    /**
     * This class represents a grid cell of the mapGrid
     * It extends StackPane so that it's easy to place/remove elements to/from a mapGrid cell
     */
    private static class Tile extends StackPane {
        private int workerOwnerID = -1;         //initially has no worker owner id
        private int workerId = -1;              //initially has no worker id
        private int level = 1;                  //initially the Tile has no building on it
        private Vector2 pos;                    //the position of this tile inside the gridMap
        private Map<Integer, URL> workerImages; //map to have a reference of each player's worker color

        /**
         * Updates the level of this Tile
         * @param newLevel new level of the tile
         */
        void buildLevel(int newLevel) {
            this.level = newLevel;
        }

        /**
         * Gets the level of this Tile
         * @return this tile's level
         */
        int getLevel() {
            return this.level;
        }

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
        private void highlightTile() {
            URL url = getClass().getResource("/img/common/highlight.png");
            ImageView highlight = getImageImageViewByURL(url);
            highlight.setId("highlight");
            this.getChildren().add(highlight);
        }

        /**
         * Removes the highlight_worker image from the Tile if it has one
         */
        private void unHighlightWorker() {
            Node toRemove = null;
            ObservableList<Node> imagesOnTile = this.getChildren();
            for (Node node : imagesOnTile) {
                if (node.getId() == ("highlight_worker")) {
                    toRemove = node;
                    break;
                }
                break;
            }
            this.getChildren().remove(toRemove);
        }

        /**
         * Removes the highlight image from the Tile if it has one
         */
        private void unhighlight() {
            Node toRemove = null;
            ObservableList<Node> imagesOnTile = this.getChildren();
            for (Node node : imagesOnTile) {
                if (node.getId() == ("highlight")) {
                    toRemove = node;
                    break;
                }
                break;
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
        }

        /**
         * Render method - Places the corresponding image of a worker
         */
        //TODO add female type worker
        private void placeWorkerImage() {
            int workerOwnerID = this.workerOwnerID;
            int workerID = this.workerId;

            if (workerID == -1) {
                return;
            }
            URL url = workerImages.get(workerOwnerID);
            //System.out.printf("DEbug %d, %d, %s || %s\n", workerID, workerOwnerID, url, workerImages);
            ImageView my_worker = getImageImageViewByURL(url);
            this.getChildren().add(my_worker);
        }

        /**
         * Render method - Places the corresponding image of the Tile level
         */
        private void placeBuildingImage() {
            int levelToPlace = this.level;
            switch (levelToPlace) {
                case 2: {
                    URL url = getClass().getResource("/img/buildings/Level_1.png");
                    ImageView levelImage = getImageImageViewByURL(url);
                    this.getChildren().add(levelImage);
                    break;
                }
                case 4: {
                    URL url = getClass().getResource("/img/buildings/Level_2.png");
                    ImageView levelImage = getImageImageViewByURL(url);
                    this.getChildren().add(levelImage);
                    break;
                }
                case 8: {
                    URL url = getClass().getResource("/img/buildings/Level_3.png");
                    ImageView levelImage = getImageImageViewByURL(url);
                    this.getChildren().add(levelImage);
                    break;
                }
                case 130: {
                    URL url = getClass().getResource("/img/buildings/dome.png");
                    ImageView levelImage = getImageImageViewByURL(url);
                    this.getChildren().add(levelImage);
                    break;
                }
                case 132:{
                    URL url = getClass().getResource("/img/buildings/Level_1.png");
                    ImageView levelImage = getImageImageViewByURL(url);
                    this.getChildren().add(levelImage);

                    url = getClass().getResource("/img/buildings/dome.png");
                    levelImage = getImageImageViewByURL(url);
                    this.getChildren().add(levelImage);
                    break;
                }
                case 136:{
                    URL url = getClass().getResource("/img/buildings/Level_2.png");
                    ImageView levelImage = getImageImageViewByURL(url);
                    this.getChildren().add(levelImage);

                    url = getClass().getResource("/img/buildings/dome.png");
                    levelImage = getImageImageViewByURL(url);
                    this.getChildren().add(levelImage);
                    break;
                }
                case 144: {
                    URL url = getClass().getResource("/img/buildings/Level_3.png");
                    ImageView levelImage = getImageImageViewByURL(url);
                    this.getChildren().add(levelImage);

                    url = getClass().getResource("/img/buildings/dome.png");
                    levelImage = getImageImageViewByURL(url);
                    this.getChildren().add(levelImage);
                    break;
                }

            }
        }

        /**
         * Highlights a worker by placing a highlight ImageView on the Tile
         */
        void highlightWorker() {
            URL url = getClass().getResource("/img/common/highlight_worker.png");
            ImageView levelImage = getImageImageViewByURL(url);
            levelImage.setId("highlight_worker");
            this.getChildren().add(levelImage);
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
    }
}

package it.polimi.ingsw.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main Game and Model class that represents a complete match and serves as the model interface with the outside world
 * Direct calls to internal components should be avoided
 * This class require that the user implements his own "execution control"
 * Game class has no concept of what happens first or later, nor attempts to force a particular flow of execution
 * creating a "forced" flow in this class would end up in a huge mess of spaghetti code
 * Outside elements could have a better idea of time, flow control and execution order
 * For example the controller has a better idea of the current status of the application, has a better idea how to directly ask a user to perform an action and so on
 * Some hints to the used are still provided with game state to suggest the next requested action that game is expecting to proceed to the next step
 */
public final class Game
{
    private static final int MAX_PLAYERS = 3;
    private static final int MIN_PLAYERS = 2;

    /**
     * Enum that represent all the possible phases of a game:
     *   WAIT - wait players before start
     *   GOD_SELECTION - game creator select gods allowed in this game
     *   GOD_PICK - players chose their god from selected ones
     *   FIRST_PLAYER_PICK - game creator chose who starts first (no self, skipped if two), then players place workers
     *   WORKER_PLACE - place the workers starting from first player
     *   GAME - normal game
     *   END - game ended due to finish of game or insufficient player
     */
    public enum GameState{WAIT, GOD_SELECTION, GOD_PICK, FIRST_PLAYER_PICK, WORKER_PLACE, GAME, END}

    private List<Player> players;
    private List<Card> allowed_cards;
    private GameConstraints globalConstraints;
    private Map game_map;
    private Turn current_turn, last_turn;
    private int current_player, first_player;
    private int state_progress;
    private GameState gameState;
    private CardCollection cardCollection;

    //TODO: worker placement

    public Game()
    {
        players = new ArrayList<>();
        allowed_cards = new ArrayList<>();
        globalConstraints = new GameConstraints();
        game_map = new Map(); // TODO: load prev state
        last_turn = null;
        current_turn = null;
        current_player = -1;
        first_player = -1;
        state_progress = 0;
        gameState = GameState.WAIT;
        cardCollection = new CardCollection();
    }


    // **********************************************************************************************
    // Getters & Setters
    // **********************************************************************************************


    /**
     * @return true if game is started and not waiting for more players
     */
    public boolean isStarted() {
        return gameState != GameState.WAIT;
    }

    /**
     * @return true if game is ended
     */
    public boolean isEnded() {
        return gameState == GameState.END;
    }


    /**
     * @return current game state
     */
    public GameState getCurrentState()
    {
        return gameState;
    }

    /**
     * @return the game host aka the challenger who created the match
     */
    public Player getHost(){
        return  players.get(0);
    }


    /**
     * @return current player that needs to perform an action of any kind
     */
    public Player getCurrentPlayer(){
        return players.get(current_player);
    }

    /**
     * @return list of all the players in this match
     */
    public List<Player> getPlayers()
    {
        return new ArrayList<>(players);
    }


    /**
     * @return number of players in this game
     */
    public int playerCount() {
        return players.size();
    }

    /**
     * @return the current map state
     */
    public Map getCurrentMap(){
        return game_map; //TODO: swap to copy constructor
    }


    /**
     * Generate an array of card ids from a card array
     * @param cards list of cards
     * @return array of cards ids
     */
    public int[] getCardIds(List<Card> cards)
    {
        int[] res = new int[cards.size()];
        for (int i =0; i< cards.size(); i++)
            res[i] = cards.get(i).getId();

        return res;
    }

    // **********************************************************************************************
    // Operations
    // **********************************************************************************************

    /**
     * Join a match what is still accepting players
     * Attempting to join a full or started match result in a failed attempt
     * joining during a match is not supported
     * @param sender player that wants to join
     * @return true if joined successfully
     */
    public boolean join(Player sender)
    {
        if(canJoin() && sender != null)
        {
            sender.clear();
            players.add(sender);
            return true;
        }
        return false;
    }


    /**
     * Start a new game with the god selection phase that requires that the host
     * chose 2-3 gods (based on player number)
     * @return true if started correctly
     */
    public boolean start(Player sender) {
        if(canStart() && isPlayerHost(sender)){
            gameState = GameState.GOD_SELECTION;
            state_progress = 0;
            return true;
        }

        return false;
    }


    /**
     * Command Handler that receives host selected gods
     * If selection is done right the game proceed to next phase according to the gamestate struct
     * If cardIDs is not correct this phase is requested again to the external event handler
     * @param sender player who issues this command
     * @param filter allowed cards selected by the host
     * @throws NotAllowedOperationException if the sender is not in this game or it's not the host or the operation is not allowed at the current game state
     */
    public boolean applyGodFilter(Player sender, int[] filter) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isPlayerHost(sender) || gameState !=GameState.GOD_SELECTION)
            throw new NotAllowedOperationException("Only the host can chose allowed gods");

        try
        {
            //TODO: check cardIDs has no duplicates
            allowed_cards = Arrays.asList(cardCollection.getCards(filter));
            cardCollection = null; // we don't need it anymore
            current_player = 1;
            state_progress = 1;
            gameState = GameState.GOD_PICK;
            return true;

        } catch (CardNotExistsException e)
        {
            return false;
        }

    }

    /**
     * Command Handler that receives selected gods from a player and goes to the next one
     * The last players must not call this command because it gets the last god
     * After the pick the function moves the game to the next stage
     * If the godid is not correct this phase is requested again to the external event handler
     * @param sender player who issues this command
     * @param godID id of the god to chose
     * @throws NotAllowedOperationException if sender is not in this game
     */
    public boolean selectGod(Player sender, int godID) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isCurrentPlayer(sender) || gameState != GameState.GOD_PICK)
            throw new NotAllowedOperationException("Wait for your turn to chose a god");

        try {
            sender.setGod(pickGod(godID, allowed_cards));
            nextPlayer();

            if(state_progress == playerCount() && current_player == 0)
            {
                players.get(0).setGod(allowed_cards.get(0)); // autopick god for last player
                if(playerCount() == MIN_PLAYERS)
                {
                    first_player = 1;
                    gameState = GameState.WORKER_PLACE;
                }
                else
                {
                    gameState = GameState.FIRST_PLAYER_PICK;
                }
            }

            return true;
        }
        catch (CardNotExistsException e)
        {
            return false;
        }

    }


    /**
     * Command Handler to receive host decided first player for this game
     * If the selected player is not correct this phase is requested again to the external event handler
     * @param sender player who issues this command
     * @param first_player first player of the game that has the honor to make the first move
     * @throws NotAllowedOperationException if the sender is not in this game nor is the host
     */
    public boolean selectFirstPlayer(Player sender, Player first_player) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isPlayerHost(sender) && gameState != GameState.FIRST_PLAYER_PICK)
            throw new NotAllowedOperationException("Only the host can chose the starting player");

        if(isPlayerInThisMatch(first_player))
        {
            this.first_player = players.indexOf(first_player);
            state_progress = 1;
            current_player = this.first_player;
            gameState = GameState.WORKER_PLACE;
            return true;
        }

        return false;
    }


    /**
     * Place two workers for a player
     * @param sender
     * @param positions
     * @return
     * @throws NotAllowedOperationException
     */
    public boolean placeWorkers(Player sender, Vector2[] positions) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isPlayerHost(sender) && gameState != GameState.WORKER_PLACE)
            throw new NotAllowedOperationException("You can't place a worker now");

        List<Vector2> validPos = game_map.cellWithoutWorkers();

        if(positions.length == 2 && !positions[0].equals(positions[1]) && validPos.contains(positions[0]) && validPos.contains(positions[1]) )
        {
            sender.addWorker(new Worker(sender, positions[0]));
            sender.addWorker(new Worker(sender, positions[1]));
            game_map.setWorkers(sender);

            nextPlayer();
            if(current_player == first_player)
            {
                // move to game finally
                makeTurn(first_player);
            }

            return true;
        }

        return false;
    }

    /**
     * Command handler to select a worker for the current turn
     * @param sender player who issues this command
     * @param target worker id to select
     * @throws NotAllowedOperationException if sender is not in this game or is not the current player
     */
    public void selectWorker(Player sender, int target) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isCurrentPlayer(sender) ||  gameState != GameState.GAME)
            throw  new NotAllowedOperationException("You can't select a worker now");

        current_turn.selectWorker(target);
    }


    /**
     * Command handler to execute an action in the current turn
     * @param sender player who issues this command
     * @param actionId id of the selected action to run
     * @param target target position where the action should run
     * @throws NotAllowedOperationException if player is not in this match or it's not his turn
     */
    public int runAction(Player sender, int actionId, Vector2 target) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isCurrentPlayer(sender) ||  gameState != GameState.GAME)
            throw new NotAllowedOperationException("You can't run an action now");

        int res = current_turn.runAction(actionId, target, game_map, globalConstraints);

        if(res > 0)
            endGame(players.get(current_player));
        else if (res < 0)
            playerLost(players.get(current_player));

        if(current_turn.isEnded())
        {
            nextPlayer();
            makeTurn(current_player);
            return 1;
        }

        return 0;
    }

    /**
     * Get the list of next possible actions
     * @param sender player who issues this command
     * @return list of actions
     */
    String[] getNextActions(Player sender){
        //TODO: calc all possibile operations for every worker if not selected or limit action to a single worker
        return null;
    }


    /**
     * Left the game
     * @param sender player who issues this command
     * @return true if left, false if player is not part of this game and cannot left
     */
    public boolean left(Player sender)
    {
        if(players.indexOf(sender) < 0)
            return false;

        players.remove(sender);
        if(players.size() < MIN_PLAYERS)
        {
            gameState = GameState.END;
        }

        return true;
    }


    // **********************************************************************************************
    // Private
    // **********************************************************************************************

    /**
     * Check if the current game can accept more players
     * You can join only if there is a free slot and  the game is not running
     * joining during a match is not supported
     * @return true if can join
     */
    private boolean canJoin() {
        return !isEnded() && !isStarted() && players.size() < MAX_PLAYERS;
    }

    /**
     * Check if a game can be started
     * A game can start only if minimum player number is met and the game is not already running
     * @return true if start condition is met
     */
    private boolean canStart() {

        return players.size() >= MIN_PLAYERS && !isEnded() && !isStarted();
    }


    /**
     * Check if a player is in the current game or not
     * This function should be called before processing a player command or action of any kind
     * @param target player to check
     * @return true if target is in this game
     */
    private boolean isPlayerInThisMatch(Player target)
    {
        return players.indexOf(target) < 0;
    }

    /**
     * Checks if the target player is the host of the game or not
     * The game host is the first player that joined the game
     * @param target player to check
     * @return true if the player is the host
     */
    private boolean isPlayerHost(Player target)
    {
        return players.get(0).equals(target);
    }

    /**
     * Check if the target player is the current turn owner
     * @param target player to check
     * @return true if target is the current player
     */
    private boolean isCurrentPlayer(Player target)
    {
        return target.equals(players.get(current_player));
    }

    /**
     * Pick a god from a list and remove it
     * @param id god to pick
     * @param  gods list of allowed "pick" gods
     * @return card instance for the selected god id
     * @throws CardNotExistsException if the selected god in not in the list
     */
    private Card pickGod(int id, List<Card> gods) throws CardNotExistsException
    {
        Card ret;
        for(int  i =0; i < gods.size(); i++)
        {
            if(gods.get(i).getId() == id)
            {
                ret = gods.get(i);
                gods.remove(i);
                return  ret;
            }
        }

        throw new CardNotExistsException();
    }

    /**
     * Calculate next player index
     */
    private void nextPlayer(){
        current_player ++;
        state_progress++;
        if(current_player > players.size())
            current_player = 0;
    }




    /**
     * Create and start a turn for the selected player
     * Turn is created only if the game is not in end phase
     * @param player player (players index) that ons the new turn
     */
    private void makeTurn(int player){

        if(gameState == GameState.END) return;

        gameState = GameState.GAME;

        last_turn = current_turn;
        current_turn = new Turn(players.get(player));

        if(!current_turn.canStillMove())
        {
            playerLost(players.get(current_player));
            nextPlayer();
            makeTurn(current_player);
        }
    }

    /**
     * End the current match and inform the winner
     * @param winner player that won the game
     */
    private void endGame(Player winner)
    {
        gameState = GameState.END;
    }

    /**
     * Remove a player that lost the game
     * @param loser player that met a lose condition
     */
    private void playerLost(Player loser)
    {
        if(players.size() == 2)
        {
            nextPlayer(); //if we are two and i lost the winner is the next player... "the other player"
            endGame(players.get(current_player));
        }
        else{
            game_map.removeWorkers(loser);
        }
    }

}

package it.polimi.ingsw.game;

import java.util.*;
import com.google.gson.Gson;
import java.io.*;

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
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 3;
    private static final int WORKERS_PER_PLAYER = 2;

    /**
     * Enum that represent all the possible phases of a game:
     */
    public enum GameState
    {
        /**
         * Wait players before start
         */
        WAIT,
        /**
         * Game creator select gods allowed in this game
         */
        GOD_FILTER,
        /**
         * Players chose their god from selected ones
         */
        GOD_PICK,
        /**
         * Game creator chose who starts first (no self, skipped if two), then players place workers
         */
        FIRST_PLAYER_PICK,
        /**
         * Place the workers starting from first player
         */
        WORKER_PLACE,
        /**
         *Normal game
         */
        GAME,
        /**
         * Game ended due to finish of game or insufficient player
         */
        END
    }

    private List<Player> players;
    private transient List<Card> allowedCards;
    private GameConstraints globalConstraints;
    private Map game_map;
    private transient Turn currentTurn, lastTurn;
    private int currentPlayer, firstPlayer;
    private int stateProgress;
    private GameState gameState;
    private transient CardCollection cardCollection;
    private Player winner;


    public Game()
    {
        players = new ArrayList<>();
        allowedCards = new ArrayList<>();
        globalConstraints = new GameConstraints();
        game_map = new Map();
        lastTurn = null;
        currentTurn = null;
        currentPlayer = 0;
        firstPlayer = 0;
        stateProgress = 0;
        gameState = GameState.WAIT;
        cardCollection = new CardCollection();
        winner = null;
    }


    // **********************************************************************************************
    // Getters & Setters
    // **********************************************************************************************


    /**
     * Check if the game is started
     * @return true if game is started and not waiting for more players
     */
    public boolean isStarted() {
        return gameState != GameState.WAIT;
    }

    /**
     * Check if the game is ended
     * @return true if game is ended
     */
    public boolean isEnded() {
        return gameState == GameState.END;
    }


    /**
     * Return the current game state
     * @return current game state
     */
    public GameState getCurrentState()
    {
        return gameState;
    }

    /**
     * Return the player that is "hosting" this match
     * @return the game host aka the challenger who created the match
     */
    public Player getHost(){
        return  players.get(0);
    }


    /**
     * Return the current player that needs to perform an action
     * @return current player that needs to perform an action of any kind
     */
    public Player getCurrentPlayer(){
        if(playerCount() == 0)
            return null;
        else
            return players.get(currentPlayer);
    }

    /**
     * Return all the players in the match
     * @return list of all the players in this match
     */
    public List<Player> getPlayers()
    {
        return new ArrayList<>(players);
    }


    /**
     * Return the player count
     * @return number of players in this game
     */
    public int playerCount() {
        return players.size();
    }

    /**
     * Return the current map
     * @return the current map state
     */
    public Map getCurrentMap(){
        return game_map;
    }


    /**
     * Return a list of available card ids for this match
     * @return list of available god cards by id
     */
    public int[] getCardIds()
    {
        return cardCollection.getCardIDs();
    }

    /**
     * Return current game winner if there is any
     * Winner can be null if game is not ended
     * if current phase is ended and winner is still null this match was interrupted
     * @return null if not ended or interrupted otherwise winner player
     */
    public Player getWinner() { return winner; }


    /**
     * Return current list of usable cards
     * @return current pickable cards for this match
     */
    public int[] getAllowedCardIDs(){
        return getCardIds(allowedCards);
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
     * @param sender player who issues this command
     * @return true if started correctly
     */
    public boolean start(Player sender) {
        if(canStart() && isPlayerHost(sender)){
            gameState = GameState.GOD_FILTER;
            stateProgress = 0;
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
     * @return true if filter is applied correctly
     * @throws NotAllowedOperationException if the sender is not in this game or it's not the host or the operation is not allowed at the current game state
     */
    public boolean applyGodFilter(Player sender, int[] filter) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isPlayerHost(sender) || filter == null)
            throw new NotAllowedOperationException("Only the host can chose allowed gods");

        try
        {
            if(filter.length != playerCount() || hasDuplicates(filter)) // 20 cards should be fine for base gods ids
                return false;

            allowedCards = new ArrayList<>(Arrays.asList(cardCollection.getCards(filter))); // force a mutable list generation
            currentPlayer = 1;
            stateProgress = 1;
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
     * @return true if god is selected correctly
     * @throws NotAllowedOperationException if sender is not in this game
     */
    public boolean selectGod(Player sender, int godID) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isCurrentPlayer(sender))
            throw new NotAllowedOperationException("Wait for your turn to chose a god");

        try {
            sender.setGod(pickGod(godID, allowedCards));
            nextPlayer();

            if(stateProgress == playerCount() && currentPlayer == 0)
            {
                players.get(0).setGod(allowedCards.get(0)); // autopick god for last player
                allowedCards.clear();
                gameState = GameState.FIRST_PLAYER_PICK;
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
     * @param firstPlayer first player of the game that has the honor to make the first move
     * @return true if first player is selected correctly, false otherwise
     * @throws NotAllowedOperationException if the sender is not in this game nor is the host
     */
    public boolean selectFirstPlayer(Player sender, Player firstPlayer) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isPlayerHost(sender))
            throw new NotAllowedOperationException("Only the host can chose the starting player");

        if(isPlayerInThisMatch(firstPlayer))
        {
            this.firstPlayer = players.indexOf(firstPlayer);
            stateProgress = 1;
            currentPlayer = this.firstPlayer;
            gameState = GameState.WORKER_PLACE;
            return true;
        }

        return false;
    }


    /**
     * Place two workers for a player, this function also checks that the positions are valid and free from other workers
     * When all the players have placed thier workers the first turn i created and GameState is changed to GAME
     * This function is the only way to actually start turn execution.
     * This design choice is done to force players to select workers before starting a new turn
     * @param sender player who issues this command
     * @param positions 2 position where the player wants to place it's workers
     * @return true if workers are placed correctly, false otherwise
     * @throws NotAllowedOperationException if sender is not in the game or is not current player
     */
    public boolean placeWorkers(Player sender, Vector2[] positions) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isCurrentPlayer(sender))
            throw new NotAllowedOperationException("You can't place a worker now");

        if(areValidWorkerPlacements(positions))
        {
            //workers id are relative for the player
            sender.addWorker(new Worker(0, sender, positions[0]));
            sender.addWorker(new Worker(1, sender, positions[1]));
            game_map.setWorkers(sender);

            nextPlayer();
            if(currentPlayer == firstPlayer)
            {
                // move to game finally
                makeTurn(firstPlayer);
            }

            return true;
        }

        return false;
    }


    /**
     * Command handler to execute an action in the current turn
     * When a turn finishes, a new one is created for the next player
     * If a player loses
     * @param sender player who issues this command
     * @param worker worker id to select
     * @param actionId id of the selected action to run
     * @param target target position where the action should run
     * @throws NotAllowedOperationException if player is not in this match or it's not his turn
     * @return 0 if lost, lower than 0 if error in parameters, greater than 0 if run is successful
     */
    public int executeAction(Player sender, int worker, int actionId, Vector2 target) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isCurrentPlayer(sender))
            throw new NotAllowedOperationException("You can't run an action now");

        if(worker < 0 || worker >= WORKERS_PER_PLAYER) // invalid id
            return -1;

        // save if  worker is already selected of not
        boolean shouldResetWorker = currentTurn.getWorker() == null;

        // a worker is selected, we need to force the same worker
        // so action is rejected
        if(!shouldResetWorker && currentTurn.getWorker().getId() != worker)
            return -1;


        try
        {
            //select worker when the first action run succeed
            if(currentTurn.getWorker() == null)
                currentTurn.selectWorker(worker);

            int actionRes = currentTurn.runAction(actionId, target, game_map, globalConstraints);

            if (actionRes > 0) // player won
            {
                endGame(players.get(currentPlayer));
            }
            else if (actionRes < 0) // player lost
            {
                playerLost(players.get(currentPlayer));
                return 0;
            }
            else {
                // turn continue

                if (currentTurn.isEnded())
                {
                    nextPlayer();
                    makeTurn(currentPlayer);
                }
            }

        }
        catch (NotAllowedMoveException | OutOfGraphException e )
        {
            // first action failed, reset worker to let the player chose again
            if(shouldResetWorker)
                currentTurn.resetSelectedWorker();
            return -1;
        }

        return 1;
    }

    /**
     * Get the list of next possible actions for all worker
     * if a worker is selected for current turn, get possible action to continue turn with that worker
     * @param sender player who issues this command
     * @return list of actions (workerID,actionName,possibleVector2), null if none is available
     */
    public List<NextAction> getNextActions(Player sender) {

        ArrayList<NextAction> nextActions = new ArrayList<>();

        if (currentTurn.getWorker() == null)
        {
            for (Worker worker : sender.getWorkers())
            {
                nextActions.addAll(currentTurn.getNextAction(worker, game_map, globalConstraints));
            }
        }
        else
        {
            nextActions = currentTurn.getNextAction(game_map, globalConstraints);
        }

        if(nextActions.size() > 0)
        {
            return nextActions;
        }
        else
        {
            //no more things to do... the player got stuck and cant complete the turn
            playerLost(sender);
            return null;
        }

        //return nextActions;
    }

    /**
     * Left the game
     * @param sender player who issues this command
     * @return true if left, false if player is not part of this game and cannot left
     */
    public boolean left(Player sender)
    {
        if(!isPlayerInThisMatch(sender))
            return false;

        players.remove(sender);

        // kill the game if match is started and someone leave
        // requested by specification
        if(gameState != GameState.WAIT)
            endGame(null);

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
        if(players.size() <= 0)
            return false;
        else
             return players.indexOf(target) >= 0;
    }

    /**
     * Checks if the target player is the host of the game or not
     * The game host is the first player that joined the game
     * @param target player to check
     * @return true if the player is the host
     */
    private boolean isPlayerHost(Player target)
    {
        if(players.size() <= 0)
            return false;
        else
            return players.get(0).equals(target);
    }

    /**
     * Check if the target player is the current turn owner
     * @param target player to check
     * @return true if target is the current player
     */
    private boolean isCurrentPlayer(Player target)
    {
        if(players.size() == 0)
            return false;
        else
            return target.equals(players.get(currentPlayer));
    }

    /**
     * Generate an array of card ids from a card array
     * @param cards list of cards
     * @return array of cards ids
     */
    private int[] getCardIds(List<Card> cards)
    {
        int[] res = new int[cards.size()];
        for (int i =0; i< cards.size(); i++)
            res[i] = cards.get(i).getId();

        return res;
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
        int res = -1;
        for(int  i =0; i < gods.size(); i++)
        {
            if(gods.get(i).getId() == id)
            {
                res = i;
                break;
            }
        }

        if(res >= 0)
        {
            Card t = gods.get(res);
            gods.remove(res);
            return t;
        }

        throw new CardNotExistsException();
    }

    /**
     * Calculate next player index
     */
    private void nextPlayer(){
        currentPlayer++;
        stateProgress++;
        if(currentPlayer >= players.size())
            currentPlayer = 0;
    }

    /**
     * Create and start a turn for the selected player
     * Turn is created only if the game is not in end phase
     * @param player player (players index) that ons the new turn
     */
    private void makeTurn(int player){

        if(gameState == GameState.END) return;

        gameState = GameState.GAME;

        lastTurn = currentTurn;
        Player p = players.get(player);
        if(p.getGod() == null)
            p.setGod(cardCollection.getNoGodCard());

        currentTurn = new Turn(p);

        if(!currentTurn.canStillMove(game_map, globalConstraints))
        {
            playerLost(players.get(currentPlayer));
        }
    }

    /**
     * End the current match and inform the winner
     * @param winner player that won the game
     */
    private void endGame(Player winner)
    {
        this.winner = winner;
        gameState = GameState.END;
    }

    /**
     * Remove a player that lost the game
     * @param loser player that met a lose condition
     */
    private void playerLost(Player loser)
    {
        players.remove(loser);
        // after this line current player is the next one
        // because we removed the loser
        if(currentPlayer >= playerCount())
            currentPlayer = 0;

        if(players.size() < 2)
        {
            endGame(players.get(currentPlayer));
        }
        else {
            game_map.removeWorkers(loser);
            //start new turn if we can still play
            makeTurn(currentPlayer);
        }
    }

    /**
     * Check if the position passed are valid and thus we can place a worker there
     * Valid positions are free from other workers, are not the same and have only WORKERS_PER_PLAYER size
     * @param positions positions to check
     * @return true if positions are valid
     */
    private boolean areValidWorkerPlacements(Vector2[] positions)
    {
        //easy check for invalid size
        if(positions == null || positions.length != WORKERS_PER_PLAYER)
            return false;

        // block same positions
        if(positions[0].equals(positions[1]))
            return false;

        //check that the two position are available in the map
        List<Vector2> validPos = game_map.cellWithoutWorkers();

        if(!validPos.contains(positions[0]) ||  !validPos.contains(positions[1]))
            return false;

        return true;
    }

    /**
     * Check if an array has duplicates
     * @param arr array to check
     * @return true if a duplicate is found
     */
    private boolean hasDuplicates(final int[] arr)
    {
        Set<Integer> ck = new HashSet<>();
        for (int i : arr)
        {
            if (ck.contains(i))
                return true;
            else
                ck.add(i);
        }
        return false;
    }


}

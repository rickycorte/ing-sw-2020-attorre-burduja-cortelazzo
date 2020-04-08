package it.polimi.ingsw.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main Game and Model class
 * This class represent a game from start to finish and it's the only way to interact with the game itself
 * The class interface is the bare minum to execute all the actions from outside, it's required to pass a valid EventHandler
 * to receive game state updates outside of this class
 * Note: you must pass a class that implements IGameUpdateReceiver interface
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
     *   PRE_GAME - game creator chose who starts first (no self, skipped if two), then players place workers
     *   GAME - normal game
     *   END - game ended due to finish of game or insufficient player
     */
    public enum GameState{WAIT, GOD_SELECTION, GOD_PICK, PRE_GAME, GAME, END}

    private List<Player> players;
    private List<Card> allowed_cards;
    private GameConstraints globalConstraints;
    private Map game_map;
    private Turn current_turn, last_turn;
    private int current_player;
    GameState gameState;
    private IGameUpdateReceiver eventHandler;
    CardCollection cardCollection;

    //TODO: worker placement

    public Game(IGameUpdateReceiver eventHandler)
    {
        players = new ArrayList<>();
        allowed_cards = new ArrayList<>();
        globalConstraints = new GameConstraints();
        game_map = new Map(); // TODO: load prev state
        last_turn = null;
        current_turn = null;
        current_player = 0;
        gameState = GameState.WAIT;
        this.eventHandler = eventHandler;
        cardCollection = new CardCollection();
    }

    /**
     * Join a match what is still accepting players
     * Attempting to join a full or started match result in a failed attempt
     * joining during a match is not supported
     * @param sender player that wants to join
     * @return true if joined successfully
     */
    public boolean join(Player sender)
    {
        //TODO: check if already joined
        if(canJoin() && sender != null)
        {
            players.add(sender);
            eventHandler.onPlayerJoin(sender); // inform host that someone joined
            return true;
        }

        return false;
    }


    /**
     * Start a new game with the god selection phase that requires that the host
     * chose 2-3 gods (based on player number)
     * @return true if started correctly
     */
    public boolean start() {
        if(canStart()){
            gameState = GameState.GOD_SELECTION;
            eventHandler.onGodSelectionPhase(players.get(0), cardCollection.getCardIDs(), players.size());
            return true;
        }

        return false;
    }

    /**
     * Get the current player count in the game
     * @return number of players in this game
     */
    public int playerCount() {
        return players.size();
    }

    /**
     * Check if a game is started
     * @return true if game is started and not waiting for more players
     */
    public boolean isStarted() {
        return gameState != GameState.WAIT;
    }

    /**
     * Check if a game is ended
     * @return true if game is ended
     */
    public boolean isEnded() {
        return gameState == GameState.END;
    }

    /**
     * Command Handler that receives host selected gods
     * If selection is done right the game proceed to next phase according to the gamestate struct
     * If cardIDs is not correct this phase is requested again to the external event handler
     * @param sender player who issues this command
     * @param cardIDs allowed cards selected by the host
     * @throws NotAllowedOperationException if the sender is not in this game or it's not the host or the operation is not allowed at the current game state
     */
    public void selectGameGod(Player sender, int[] cardIDs) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isPlayerHost(sender) || gameState !=GameState.GOD_SELECTION)
            throw new NotAllowedOperationException("Only the host can chose allowed gods");

        try
        {
            //TODO: check cardIDs has no duplicates
            allowed_cards = Arrays.asList(cardCollection.getCards(cardIDs));
            cardCollection = null; // we don't need it anymore
            current_player = 1;
            //send first player the possible gods
            eventHandler.onGodPickPhase(players.get(current_player), cardIDs);

            gameState = GameState.GOD_PICK;

        } catch (CardNotExistsException e)
        {
            //TODO: send explicit error to player?
            //TODO: repeat chose
            eventHandler.onGodSelectionPhase(players.get(0), cardCollection.getCardIDs(), players.size());
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
    public void selectPlayerGod(Player sender, int godID) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isCurrentPlayer(sender) || gameState != GameState.GOD_PICK)
            throw new NotAllowedOperationException("Wait for your turn to chose a god");

        try {
            sender.setGod(pickGod(godID, allowed_cards));
            nextPlayer();

            if(current_player == 0) {
                players.get(0).setGod(allowed_cards.get(0));
                runPreGame();
            } else {
                eventHandler.onGodPickPhase(players.get(current_player), getCardIds(allowed_cards));
            }

        }
        catch (CardNotExistsException e)
        {
            //TODO: send explicit error?
            eventHandler.onGodPickPhase(players.get(current_player), getCardIds(allowed_cards));
        }

    }

    /**
     * Command Handler to receive host decided first player for this game
     * If the selected player is not correct this phase is requested again to the external event handler
     * @param sender player who issues this command
     * @param first_player first player of the game that has the honor to make the first move
     * @throws NotAllowedOperationException if the sender is not in this game nor is the host
     */
    public void selectFirstPlayer(Player sender, Player first_player) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isPlayerHost(sender) && gameState != GameState.PRE_GAME)
            throw new NotAllowedOperationException("Only the host can chose the starting player");

        if(isPlayerInThisMatch(first_player))
            makeTurn(players.indexOf(first_player));
        else //retry chose
            runPreGame();
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
    public void runAction(Player sender, int actionId, Vector2 target) throws NotAllowedOperationException
    {
        if(!isPlayerInThisMatch(sender) || !isCurrentPlayer(sender) ||  gameState != GameState.GAME)
            throw new NotAllowedOperationException("You can't run an action now");

        int res = current_turn.runAction(actionId, target, game_map, globalConstraints);

        eventHandler.onMapUpdate(game_map); //TODO: make copy constructor for map

        if(res > 0)
            endGame(players.get(current_player));
        else if (res < 0)
            playerLost(players.get(current_player));

        if(current_turn.isEnded())
        {
            nextPlayer();
            makeTurn(current_player);
        }
    }

    /**
     * Get the list of next possible actions
     * @param sender player who issues this command
     * @return list of actions
     */
    String[] getNextActions(Player sender){
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
            eventHandler.onGameEnd(players.get(0));
            gameState = GameState.END;
        }

        return true;
    }


    // **********************************************************************************************
    // private

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
        if(current_player > players.size())
            current_player = 0;
    }


    /**
     * Run pregame ("starter" chose time) or skip it if there are only two players
     */
    private void runPreGame()
    {
        gameState = GameState.PRE_GAME;
        if(players.size() > 2)
        {
            Player[] others = new Player[players.size() - 1];
            for(int i = 1; i < players.size(); i++)
                others[i-1] = players.get(i);

            //request the host to chose the first player
            eventHandler.onFirstPlayerChose(players.get(0), others);
        } else {
           //   with 2 players no pregame to select the first player is needed
            makeTurn(1);
        }
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

        eventHandler.onPlayerTurn(players.get(player));
    }

    /**
     * End the current match and inform the winner
     * @param winner player that won the game
     */
    private void endGame(Player winner)
    {
        gameState = GameState.END;
        eventHandler.onGameEnd(winner);
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
            //TODO: cleanup player workers from map

        }

    }

}

package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class GameTest
{

    Game game;
    Player p1, p2, p3, p4;

    @BeforeEach
    void setUp()
    {
        game = new Game();
        p1 = new Player(0, "kazuma");
        p2 = new Player(1, "Raccoon");
        p3 = new Player(2, "Yun Yun");
        p4 = new Player(3, "Chomusuke");
    }

    @Test
    void shouldBeEmptyAndWaiting(){
        assertEquals(0, game.playerCount());
        assertFalse(game.isStarted());
        assertFalse(game.isEnded());
    }

    @Test
    void shouldJoin() {
        // 1player
        assertTrue(game.join(p1));
        assertEquals(1, game.playerCount());
        assertTrue(game.getPlayers().contains(p1));

        //check if player is reset
        assertNull(p1.getGod());
        assertEquals(0, p1.getWorkers().size());

        //2 player
        assertTrue(game.join(p2));
        assertEquals(2, game.playerCount());
        assertTrue(game.getPlayers().contains(p2));

        //3 player
        assertTrue(game.join(p3));
        assertEquals(3, game.playerCount());
        assertTrue(game.getPlayers().contains(p3));
    }

    @Test
    void shouldNotJoin() {
        game.join(p1);
        game.join(p2);

        //null player
        assertFalse(game.join(null));
        assertEquals(2, game.playerCount());
        assertFalse(game.getPlayers().contains(null));

        // game full
        game.join(p3);

        assertFalse(game.join(p4));
        assertFalse(game.getPlayers().contains(p4));

        //game started
        game = new Game(); // reset game
        game.join(p1);
        game.join(p2);

        game.start(p1);

        assertFalse(game.join(p3));
        assertEquals(2, game.playerCount());
        assertFalse(game.getPlayers().contains(p3));

        //TODO: check for game ended
    }

    @Test
    void shouldLeft(){
        game.join(p1);
        game.join(p2);
        assertTrue(game.left(p2));
        assertEquals(1,game.playerCount());
    }

    @Test
    void shouldNotLeft() {
        game.join(p1);
        game.join(p2);
        assertFalse(game.left(p3));
        assertFalse(game.left(null));
        assertEquals(2, game.playerCount());
    }


    @Test
    void shouldFirstPlayerBeTheHost()
    {
        game.join(p1);
        game.join(p2);
        game.join(p3);
        assertEquals(p1, game.getHost());
        assertNotEquals(p2, game.getHost());
        assertNotEquals(p3, game.getHost());

        //host role can be "moved" from first to second player
        game.left(p1);
        assertNotEquals(p1, game.getHost());
        assertEquals(p2, game.getHost());
        assertNotEquals(p3, game.getHost());
    }

    @Test
    void shouldStartGame() {
        //2 player start
        game.join(p1);
        game.join(p2);
        assertTrue(game.start(p1));
        assertTrue(game.isStarted());

        // 3 players
        game = new Game();
        game.join(p1);
        game.join(p2);
        game.join(p3);
        assertTrue(game.start(p1));
        assertTrue(game.isStarted());
        assertEquals(Game.GameState.GOD_FILTER, game.getCurrentState());
    }

    @Test
    void shouldNotStartGame()
    {
        //empty game
        assertFalse(game.start(null));
        assertEquals(Game.GameState.WAIT, game.getCurrentState());
        // 1 player game
        game.join(p1);
        assertFalse(game.start(p1));
        assertEquals(Game.GameState.WAIT, game.getCurrentState());
        //started by not host
        game.join(p2);
        assertFalse(game.start(p2));
        assertEquals(Game.GameState.WAIT, game.getCurrentState());
        //TODO: check during gameplay
    }


    @Test
    void shouldApplyGodFiler()
    {
        try
        {
            game.join(p1);
            game.join(p2);
            int[] ids = new int[]{1,3};
            assertTrue(game.applyGodFilter(p1, ids));
            assertEquals(Game.GameState.GOD_PICK, game.getCurrentState());

            assertNotEquals(game.getHost(), game.getCurrentPlayer());
            assertArrayEquals(ids, game.getAllowedCardIDs());

        }catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception thrown");
        }
    }


    @Test
    void shouldNotApplyGodFilter()
    {
        final int[] ids = new int[]{1,-199};
        Game.GameState prevState = game.getCurrentState();
        //empty game
        assertThrows(NotAllowedOperationException.class, ()-> {game.applyGodFilter(null,ids);});
        assertEquals(prevState, game.getCurrentState());

        assertThrows(NotAllowedOperationException.class, ()-> {game.applyGodFilter(p1,ids);});
        assertEquals(prevState, game.getCurrentState());

        //not in this match
        game.join(p1);
        assertThrows(NotAllowedOperationException.class, ()-> {game.applyGodFilter(p2,ids);});
        assertEquals(prevState, game.getCurrentState());

        //not host
        game.join(p2);
        assertThrows(NotAllowedOperationException.class, ()-> {game.applyGodFilter(p2,ids);});
        assertEquals(prevState, game.getCurrentState());

        //null cards
        assertThrows(NotAllowedOperationException.class, ()-> {game.applyGodFilter(p1,null);});
        assertEquals(prevState, game.getCurrentState());

        try
        {
            //no cards
            assertFalse(game.applyGodFilter(p1, new int[]{}));
            assertEquals(prevState, game.getCurrentState());

            //wrong id
            assertFalse(game.applyGodFilter(p1, ids));
            assertEquals(prevState, game.getCurrentState());

            //wrong number
            assertFalse(game.applyGodFilter(p1, new int[]{1}));
            assertEquals(prevState, game.getCurrentState());

            // duplicates
            assertFalse(game.applyGodFilter(p1, new int[]{1,1}));
            assertEquals(prevState, game.getCurrentState());

        }catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception thrown");
        }
    }


    @Test
    void shouldPickGod()
    {
        // 2 player pick
        game.join(p1);
        game.join(p2);
        try
        {
            game.applyGodFilter(p1, new int[]{1,2});
            assertEquals(p2, game.getCurrentPlayer());
            assertTrue(game.selectGod(game.getCurrentPlayer(), 2));
            assertEquals(1, p1.getGod().getId());
            assertEquals(2, p2.getGod().getId()); // autoselect last
            assertEquals(Game.GameState.FIRST_PLAYER_PICK, game.getCurrentState());
            assertTrue(game.getAllowedCardIDs().length == 0);
        }catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }

        //3 players
        game = new Game();
        game.join(p1);
        game.join(p2);
        game.join(p3);
        try
        {
            Player old;

            game.applyGodFilter(p1, new int[]{1,2,3});
            old = game.getCurrentPlayer();
            assertNotEquals(p1, game.getCurrentPlayer());

            assertTrue(game.selectGod(game.getCurrentPlayer(), 2));
            assertEquals(2, p2.getGod().getId());

            assertEquals(2, game.getAllowedCardIDs().length); // removed god used
            // check is third player and not host (host is always last)
            assertNotEquals(old, game.getCurrentPlayer());
            assertNotEquals(game.getHost(), game.getCurrentPlayer());

            //pick second and autopick last
            assertTrue(game.selectGod(game.getCurrentPlayer(), 3));
            assertEquals(3, p3.getGod().getId());
            assertEquals(1, p1.getGod().getId());

            assertEquals(Game.GameState.FIRST_PLAYER_PICK, game.getCurrentState());
            assertEquals(0, game.getAllowedCardIDs().length);

        }catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldNotPickGod()
    {

        // player not in the match
        assertThrows(NotAllowedOperationException.class, ()->{ game.selectGod(null, 1); });
        assertThrows(NotAllowedOperationException.class, ()->{ game.selectGod(p1, 1); });

        game.join(p1);
        game.join(p2);

        // not current player
        assertThrows(NotAllowedOperationException.class, ()->{ game.selectGod(p2, 1); });

        // no filter applied or no more gods
        try {

            assertFalse(game.selectGod(p1, -100));
        }catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }

    }

    @Test
    void shouldSelectFirstPlayer()
    {
        game.join(p1);
        game.join(p2);
        game.join(p3);

        try
        {
            // random player
            assertTrue(game.selectFirstPlayer(p1, p3));
            assertEquals(p3, game.getCurrentPlayer());
            assertEquals(Game.GameState.WORKER_PLACE, game.getCurrentState());

            //first player as host
            assertTrue(game.selectFirstPlayer(game.getHost(), game.getHost()));
            assertEquals(game.getHost(), game.getCurrentPlayer());
            assertEquals(Game.GameState.WORKER_PLACE, game.getCurrentState());

        }catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldNotSelectFirstPlayer()
    {
        game.join(p1);
        game.join(p2);
        // null sender
        assertThrows(NotAllowedOperationException.class, () -> {game.selectFirstPlayer(null, null);});
        // not host
        assertThrows(NotAllowedOperationException.class, () -> {game.selectFirstPlayer(p2, p2);});

        try {

            // null player
            assertFalse(game.selectFirstPlayer(game.getHost(), null));
            //player not in game
            assertFalse(game.selectFirstPlayer(game.getHost(), p4));

        } catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldPlaceWorkers()
    {
        game.join(p1);
        game.join(p2);
        try{

            // p1 place
            Vector2[] pos = new Vector2[]{new Vector2(0,0), new Vector2(1,1)};
            assertTrue(game.placeWorkers(p1, pos));
            assertEquals(2, p1.getWorkers().size());
            assertEquals(pos[0], p1.getWorkers().get(0).getPosition());
            assertEquals(pos[1], p1.getWorkers().get(1).getPosition());
            assertEquals(p2, game.getCurrentPlayer());

            // check if map has placed workers
            assertFalse(game.getCurrentMap().isCellEmpty(pos[0]));
            assertFalse(game.getCurrentMap().isCellEmpty(pos[1]));

            //p2 place (all the checks are done above)
            pos = new Vector2[]{new Vector2(2,2), new Vector2(3,3)};
            assertTrue(game.placeWorkers(p2, pos));

            // check if map has placed workers
            assertFalse(game.getCurrentMap().isCellEmpty(pos[0]));
            assertFalse(game.getCurrentMap().isCellEmpty(pos[1]));

            //check turn start with p1
            assertEquals(Game.GameState.GAME, game.getCurrentState());
            assertEquals(p1, game.getCurrentPlayer());

        }catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldNotPlaceWorkers()
    {

        // null player
        assertThrows(NotAllowedOperationException.class, ()->{game.placeWorkers(null, null);});
        // not a player
        assertThrows(NotAllowedOperationException.class, ()->{game.placeWorkers(p1, null);});

        game.join(p1);
        game.join(p2);
        game.join(p4);

        // not current player
        assertThrows(NotAllowedOperationException.class, ()->{game.placeWorkers(p4, null);});

        try {

            // null pos
            assertFalse(game.placeWorkers(p1, null));

            // invalid size
            Vector2[] pos = new Vector2[]{new Vector2(0,0)};
            assertFalse(game.placeWorkers(p1, pos));
            assertEquals(0, p1.getWorkers().size());

            // same position repeated
            pos = new Vector2[]{new Vector2(0,0), new Vector2(0,0)};
            assertFalse(game.placeWorkers(p1, pos));
            assertEquals(0, p1.getWorkers().size());

            //wrong position[0]
            pos = new Vector2[]{new Vector2(-1,+29), new Vector2(0,0)};
            assertFalse(game.placeWorkers(p1, pos));
            assertEquals(0, p1.getWorkers().size());

            //wrong position[1]
            pos = new Vector2[]{new Vector2(0,0), new Vector2(-1,+29)};
            assertFalse(game.placeWorkers(p1, pos));
            assertEquals(0, p1.getWorkers().size());

            //used cells
            pos = new Vector2[]{new Vector2(0,0), new Vector2(1,1)};
            game.placeWorkers(p1, pos);

            assertFalse(game.placeWorkers(p2, pos));
            assertFalse(game.placeWorkers(p2, pos));
            assertEquals(0, p2.getWorkers().size());

        }catch(NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }

    }


    void prepareGameForAction()
    {
        game.join(p1);
        game.join(p2);

        try
        {
            game.placeWorkers(p1, new Vector2[]{new Vector2(0,0), new Vector2(1,1)});
            game.placeWorkers(p2, new Vector2[]{new Vector2(2,2), new Vector2(3,3)});
        }catch (NotAllowedOperationException ignored)
        {

        }

    }

    @Test
    void shouldRunAction()
    {
        // we assume the default turn (move - build) is used
        prepareGameForAction();
        try{
            // move action (we don't test actions here, just the turn flow)
            assertTrue(game.executeAction(p1,0, 0, new Vector2(0,1)) > 0);
            assertEquals(Game.GameState.GAME,game.getCurrentState());
            assertEquals(p1, game.getCurrentPlayer());

            //build action (same worker)
            assertTrue(game.executeAction(p1,0, 0, new Vector2(0,0)) > 0);
            assertEquals(Game.GameState.GAME, game.getCurrentState());
            //new turn
            assertNotEquals(p1, game.getCurrentPlayer());
        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }


    @Test
    void shouldNotRunAction()
    {
        // we assume the default turn (move - build) is used
        prepareGameForAction();

        // not current player
        assertThrows(NotAllowedOperationException.class, ()->{game.executeAction(p2, 0,0, null);});

        // not in game
        assertThrows(NotAllowedOperationException.class, ()->{game.executeAction(p3, 0,0, null);});

        try{

            // wrong worker id (neg)
            assertTrue(game.executeAction(p1,-1, 6, null) < 0);
            assertEquals(p1, game.getCurrentPlayer());

            // wrong worker id (sep)
            assertTrue(game.executeAction(p1,2, 6, null) < 0);
            assertEquals(p1, game.getCurrentPlayer());

            // out of range action
            assertTrue(game.executeAction(p1,0, 10, null) < 0);
            assertTrue(game.executeAction(p1,0, -1, null) < 0);

            // null position
            assertTrue(game.executeAction(p1,0, 0, null) < 0);

            // wrong position
            assertTrue(game.executeAction(p1,0, 0, new Vector2(-1,-1)) < 0);

            // wrong worker after first action
            assertTrue(game.executeAction(p1,1, 0, new Vector2(0,1)) > 0);
            assertTrue(game.executeAction(p1,1, 0, new Vector2(0,0)) < 0);
            assertEquals(Game.GameState.GAME, game.getCurrentState());
            assertEquals(p1, game.getCurrentPlayer());

        }catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void jsonTest(){
        CardCollection cardCollection = new CardCollection();
        Player player1 = new Player(0,"firstPlayer");
        Player player2 = new Player(1,"secondPlayer");

        game.join(player1);
        game.join(player2);

        //player1 has a worker
        Vector2 pos = new Vector2(0,0);
        Worker w = new Worker(player1);
        w.setPosition(pos);

        player1.addWorker(w);

        //player2 has a worker
        Vector2 pos1 = new Vector2(1,1);
        Worker w1 = new Worker(player2);
        w1.setPosition(pos1);

        player2.addWorker(w1);

        try {
            Card card1 = cardCollection.getCard(1);
            Card card2 = cardCollection.getCard(2);
            player1.setGod(card1);
            player2.setGod(card2);
        } catch (CardNotExistsException e) {
            e.printStackTrace();
        }

        game.getCurrentMap().setWorkers(player1);
        game.getCurrentMap().setWorkers(player2);

        game.gameJsonToFile("game.txt");

        Game newGame = new Game();
        newGame = newGame.gameJsonFromFile("game.txt");

        assertEquals(game.getPlayers().size(),newGame.getPlayers().size());
        assertEquals(game.getPlayers().get(0).getId(),newGame.getPlayers().get(0).getId());
        assertEquals(game.getPlayers().get(1).getId(),newGame.getPlayers().get(1).getId());
        assertEquals(game.getPlayers().get(0).getGod().getId(),newGame.getPlayers().get(0).getGod().getId());
        assertEquals(game.getPlayers().get(1).getGod().getId(),newGame.getPlayers().get(1).getGod().getId());

        assertEquals(game.getPlayers().get(0).getId(),newGame.getPlayers().get(0).getId());
        assertEquals(game.getPlayers().get(1).getId(),newGame.getPlayers().get(1).getId());

        assertTrue(game.getCurrentMap().getWorkers().get(0).equals(newGame.getCurrentMap().getWorkers().get(0)));
        assertTrue(game.getCurrentMap().getWorkers().get(1).equals(newGame.getCurrentMap().getWorkers().get(1)));

        // check graph is assigned correctly based on god's player identifier
        // TODO : correct graph assigned
    }
}
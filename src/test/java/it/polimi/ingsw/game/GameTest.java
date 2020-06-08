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
    void shouldKeepWaitingIfLobbyLosesPlayers()
    {
        game.join(p1);
        game.join(p2);
        game.join(p3);

        assertEquals(Game.GameState.WAIT, game.getCurrentState());
        assertEquals(p1, game.getCurrentPlayer());

        game.left(p2);

        assertEquals(Game.GameState.WAIT, game.getCurrentState());
        assertEquals(p1, game.getCurrentPlayer());

        game.left(p3);
        assertEquals(Game.GameState.WAIT, game.getCurrentState());
        assertEquals(p1, game.getCurrentPlayer());

        game.left(p1);
        assertEquals(Game.GameState.WAIT, game.getCurrentState());
        assertEquals(null, game.getCurrentPlayer());
    }

    @Test
    void shouldNotJoinIfGameStarted() {
        game.join(p1);
        game.join(p2);

        game.start(p1);

        assertFalse(game.join(p3));
        assertEquals(2, game.playerCount());
        assertFalse(game.getPlayers().contains(p3));
    }

    @Test
    void shouldNotJoinIfFullGame()
    {
        game.join(p1);
        game.join(p2);
        game.join(p3);

        assertFalse(game.join(p4));
        assertFalse(game.getPlayers().contains(p4));
    }

    @Test
    void shouldNotJoinIfNullPlayer()
    {
        assertFalse(game.join(null));
        assertEquals(0, game.playerCount());
        assertFalse(game.getPlayers().contains(null));
    }

    @Test
    void shouldLeft(){
        game.join(p1);
        game.join(p2);
        assertTrue(game.left(p2));
        assertEquals(1,game.playerCount());
    }

    @Test
    void shouldNotLeftIfNullPlayer() {
        game.join(p1);
        game.join(p2);
        assertFalse(game.left(null));
        assertEquals(2, game.playerCount());
    }

    @Test
    void shouldNotLeftIfNeverJoined()
    {
        game.join(p1);
        game.join(p2);
        assertFalse(game.left(p3));
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
    void shouldStartGameWithTwoPlayers() {
        //2 player start
        game.join(p1);
        game.join(p2);
        assertTrue(game.start(p1));
        assertTrue(game.isStarted());
    }

    @Test
    void shouldStartGameWithThreePlayers()
    {
        game.join(p1);
        game.join(p2);
        game.join(p3);
        assertTrue(game.start(p1));
        assertTrue(game.isStarted());
        assertEquals(Game.GameState.GOD_FILTER, game.getCurrentState());
    }

    @Test
    void shouldNotStartGameIfNotHost()
    {
        game.join(p1);
        game.join(p2);
        assertFalse(game.start(p2));
        assertEquals(Game.GameState.WAIT, game.getCurrentState());
    }

    @Test
    void shouldNotStartGameIfOnlyOnePlayer()
    {
        game.join(p1);
        assertFalse(game.start(p1));
        assertEquals(Game.GameState.WAIT, game.getCurrentState());
    }

    @Test
    void shouldNotStartGameIfEmpty()
    {
        assertFalse(game.start(null));
        assertEquals(Game.GameState.WAIT, game.getCurrentState());
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

        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception thrown");
        }
    }


    @Test
    void shouldNotApplyGodFilterIfEmptyGame()
    {
        final int[] ids = new int[]{1,2};
        Game.GameState prevState = game.getCurrentState();

        assertThrows(NotAllowedOperationException.class, ()-> {game.applyGodFilter(p1,ids);});
        assertEquals(prevState, game.getCurrentState());
    }

    @Test
    void shouldNotApplyGodFilterIfNullSender()
    {
        game.join(p1);
        final int[] ids = new int[]{1,2};
        Game.GameState prevState = game.getCurrentState();
        //empty game
        assertThrows(NotAllowedOperationException.class, ()-> {game.applyGodFilter(null,ids);});
        assertEquals(prevState, game.getCurrentState());
    }

    @Test
    void shouldNotApplyGodFilterIfNotInTheGame()
    {
        final int[] ids = new int[]{1,2};
        Game.GameState prevState = game.getCurrentState();
        game.join(p1);

        assertThrows(NotAllowedOperationException.class, ()-> {game.applyGodFilter(p2,ids);});
        assertEquals(prevState, game.getCurrentState());
    }

    @Test
    void shouldNotApplyGodFilterIfNotHost()
    {
        final int[] ids = new int[]{1,2};
        Game.GameState prevState = game.getCurrentState();
        game.join(p1);
        game.join(p2);

        assertThrows(NotAllowedOperationException.class, ()-> {game.applyGodFilter(p2,ids);});
        assertEquals(prevState, game.getCurrentState());
    }

    @Test
    void shouldNotApplyGodFilterIfNullCards()
    {
        Game.GameState prevState = game.getCurrentState();

        assertThrows(NotAllowedOperationException.class, ()-> {game.applyGodFilter(p1,null);});
        assertEquals(prevState, game.getCurrentState());
    }

    @Test
    void shouldNotApplyGodFilterIfCardListSizeNotMatchPlayerCount()
    {
        Game.GameState prevState = game.getCurrentState();
        game.join(p1);
        game.join(p2);

        try
        {
            // 0 cards
            assertFalse(game.applyGodFilter(p1, new int[]{}));
            assertEquals(prevState, game.getCurrentState());

            // 1 cards
            assertFalse(game.applyGodFilter(p1, new int[]{1}));
            assertEquals(prevState, game.getCurrentState());

            // 3 cards
            assertFalse(game.applyGodFilter(p1, new int[]{1,2,3}));
            assertEquals(prevState, game.getCurrentState());

        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception thrown");
        }

    }

    @Test
    void shouldNotApplyGodFilterWithNotExistentCards()
    {
        Game.GameState prevState = game.getCurrentState();
        game.join(p1);
        game.join(p2);

        try
        {
            assertFalse(game.applyGodFilter(p1, new int[]{-451,821}));
            assertEquals(prevState, game.getCurrentState());

        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception thrown");
        }
    }

    @Test
    void shouldNotApplyGodFilterIfDuplicateCardID()
    {
        Game.GameState prevState = game.getCurrentState();
        game.join(p1);
        game.join(p2);

        try
        {
            // duplicates
            assertFalse(game.applyGodFilter(p1, new int[]{1,1}));
            assertEquals(prevState, game.getCurrentState());

        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception thrown");
        }
    }


    @Test
    void shouldPickGodWithTwoPlayers()
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
        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldPickGodWithThreePlayers()
    {
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

        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldNotPickGodIfNullPlayer()
    {
        assertThrows(NotAllowedOperationException.class, ()->{ game.selectGod(null, 1); });
    }

    @Test
    void shouldNotPickGodIfNotInTheMatch()
    {
        assertThrows(NotAllowedOperationException.class, ()->{ game.selectGod(p1, 1); });
    }

    @Test
    void shouldNotPickGodIfNotCurrentPlayer()
    {
        game.join(p1);
        game.join(p2);

        // not current player
        assertThrows(NotAllowedOperationException.class, ()->{ game.selectGod(p2, 1); });
    }

    @Test
    void shouldNotPickGodIfNoFiltersWasApplied()
    {
        game.join(p1);
        game.join(p2);

        try
        {
            assertFalse(game.selectGod(p1, -100));
        }
        catch (NotAllowedOperationException e)
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

            // the host is also a valid player, make sure it can be selected
            assertTrue(game.selectFirstPlayer(game.getHost(), game.getHost()));
            assertEquals(game.getHost(), game.getCurrentPlayer());
            assertEquals(Game.GameState.WORKER_PLACE, game.getCurrentState());

        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldNotSelectFirstPlayerIfNullSender()
    {
        // null sender
        assertThrows(NotAllowedOperationException.class, () -> {game.selectFirstPlayer(null, null);});
    }

    @Test
    void shouldNotSelectFirstPlayerIfSenderIsNotHost()
    {
        game.join(p1);
        game.join(p2);

        // not host
        assertThrows(NotAllowedOperationException.class, () -> {game.selectFirstPlayer(p2, p2);});
    }

    @Test
    void shouldNotSelectFirstPlayerIfNull()
    {
        game.join(p1);
        game.join(p2);

        try
        {
            assertFalse(game.selectFirstPlayer(game.getHost(), null));
        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldNotSelectFirstPlayerIfNotInGame()
    {
        game.join(p1);
        game.join(p2);

        try
        {
            assertFalse(game.selectFirstPlayer(game.getHost(), p4));

        }
        catch (NotAllowedOperationException e)
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

        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }


    @Test
    void shouldNotPlaceWorkersIfNullSender()
    {
        assertThrows(NotAllowedOperationException.class, ()->{game.placeWorkers(null, null);});
    }

    @Test
    void shouldNotPlaceWorkersIfNotInTheGame()
    {
        assertThrows(NotAllowedOperationException.class, ()->{game.placeWorkers(p1, null);});
    }

    @Test
    void shouldNotPlaceWorkersIfNotCurrentPlayer()
    {
        game.join(p1);
        game.join(p2);
        assertThrows(NotAllowedOperationException.class, ()->{game.placeWorkers(p2, null);});
    }

    @Test
    void shouldNotPlaceWorkersIfNullPositions()
    {
        game.join(p1);
        game.join(p2);
        try
        {
            assertFalse(game.placeWorkers(p1, null));
        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldNotPlaceWorkersIfPositionsSizeIsNotTwo()
    {
        game.join(p1);
        game.join(p2);
        try
        {
            //size 0
            Vector2[] pos = new Vector2[]{};
            assertFalse(game.placeWorkers(p1, pos));
            assertEquals(0, p1.getWorkers().size());

            // size 1
            pos = new Vector2[]{new Vector2(0,0)};
            assertFalse(game.placeWorkers(p1, pos));
            assertEquals(0, p1.getWorkers().size());

            //size 3
            // size 1
            pos = new Vector2[]{new Vector2(0,0), new Vector2(1,1), new Vector2(3,3)};
            assertFalse(game.placeWorkers(p1, pos));
            assertEquals(0, p1.getWorkers().size());
        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldNotPlaceWorkersIfDuplicatePositions()
    {
        game.join(p1);
        game.join(p2);
        try
        {
            // same position repeated
            var pos = new Vector2[]{new Vector2(0,0), new Vector2(0,0)};
            assertFalse(game.placeWorkers(p1, pos));
            assertEquals(0, p1.getWorkers().size());
        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }


    @Test
    void shouldNotPlaceWorkersIfOnePositionIsInvalid()
    {
        game.join(p1);
        game.join(p2);
        try
        {
            //wrong position[0]
            var pos = new Vector2[]{new Vector2(-1,+29), new Vector2(0,0)};
            assertFalse(game.placeWorkers(p1, pos));
            assertEquals(0, p1.getWorkers().size());

            //wrong position[1]
            pos = new Vector2[]{new Vector2(0,0), new Vector2(-1,+29)};
            assertFalse(game.placeWorkers(p1, pos));
            assertEquals(0, p1.getWorkers().size());
        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldNotPlaceWorkersInAlreadyUsedCells()
    {
        game.join(p1);
        game.join(p2);

        try
        {
            var pos = new Vector2[]{new Vector2(0,0), new Vector2(1,1)};
            game.placeWorkers(p1, pos);

            assertFalse(game.placeWorkers(p2, pos));
            assertFalse(game.placeWorkers(p2, pos));
            assertEquals(0, p2.getWorkers().size());
        }
        catch(NotAllowedOperationException e)
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
    void shouldNotRunActionIfNotCurrentPlayer()
    {
        prepareGameForAction();

        // not current player
        assertThrows(NotAllowedOperationException.class, ()->{game.executeAction(p2, 0,0, null);});
    }


    @Test
    void shouldNotRunActionIfNotInTheGame()
    {
        prepareGameForAction();

        // not in game
        assertThrows(NotAllowedOperationException.class, ()->{game.executeAction(p3, 0,0, null);});
    }

    @Test
    void  shouldNotRunActionIfWrongWorkerID()
    {
        prepareGameForAction();
        try
        {
            // wrong worker id (neg)
            assertTrue(game.executeAction(p1,-1, 6, null) < 0);
            assertEquals(p1, game.getCurrentPlayer());

            // wrong worker id (sep)
            assertTrue(game.executeAction(p1,2, 6, null) < 0);
            assertEquals(p1, game.getCurrentPlayer());
        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void  shouldNotRunActionIfWrongActionID()
    {
        prepareGameForAction();
        try
        {
            // out of range action
            assertTrue(game.executeAction(p1,0, 10, null) < 0);
            assertTrue(game.executeAction(p1,0, -1, null) < 0);
        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldNotRunActionIfWrongPosition()
    {
        prepareGameForAction();
        try
        {
            // null position
            assertTrue(game.executeAction(p1,0, 0, null) < 0);

            // wrong position
            assertTrue(game.executeAction(p1,0, 0, new Vector2(-1,-1)) < 0);
        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldNotRunSecondActionWithDifferentWorker()
    {
        // we assume the default turn (move - build) is used
        prepareGameForAction();

        try
        {
            // wrong worker after first action
            assertTrue(game.executeAction(p1,1, 0, new Vector2(0,1)) > 0);
            assertTrue(game.executeAction(p1,1, 0, new Vector2(0,0)) < 0);
            assertEquals(Game.GameState.GAME, game.getCurrentState());
            assertEquals(p1, game.getCurrentPlayer());

        }
        catch (NotAllowedOperationException e)
        {
            fail("Unexpected exception");
        }
    }

    @Test
    void shouldReturnNullWinnerIfNotStarted()
    {
        assertNull(game.getWinner());
    }

    @Test
    void shouldNotEndIfWaiting()
    {
        game.join(p1);
        game.join(p2);
        // left when started
        game.left(p2);
        assertEquals(Game.GameState.WAIT, game.getCurrentState());
    }

    @Test
    void shouldReturnNullWinnerWhenGameIsInterrupt()
    {
        game.join(p1);
        game.join(p2);
        game.start(p1);
        // left when started
        game.left(p2);
        assertNull(game.getWinner());
        assertEquals(Game.GameState.END, game.getCurrentState());
    }

    @Test
    void shouldEndGameIfPlayerWin() throws NotAllowedOperationException
    {
        game.join(p1);
        game.join(p2);
        game.start(p1);
        game.applyGodFilter(p1, new int[]{1,2});
        game.selectGod(p2, 1);
        game.selectFirstPlayer(p1, p1);
        game.placeWorkers(p1, new Vector2[]{new Vector2(0,0), new Vector2(0,2)});
        game.placeWorkers(p2, new Vector2[]{new Vector2(1,0), new Vector2(1,2)});

        // game is ready to be played
        // but we cheat a bit
        // build level 2 under worker
        game.getCurrentMap().build(new Vector2(0,0));
        game.getCurrentMap().build(new Vector2(0,0));

        // build 3 next to worker
        game.getCurrentMap().build(new Vector2(0,1));
        game.getCurrentMap().build(new Vector2(0,1));
        game.getCurrentMap().build(new Vector2(0,1));

        // move worker from 2 to 3 -> we should win
        assertTrue(game.executeAction(p1, 0, 0, new Vector2(0,1)) > 0);
        assertEquals(p1, game.getWinner());

    }

    @Test
    void shouldContinueGameIfOneLoseInThreePlayers()  throws NotAllowedOperationException
    {
        game.join(p1);
        game.join(p2);
        game.join(p3);
        game.start(p1);
        game.applyGodFilter(p1, new int[]{1,2,3});
        game.selectGod(p2, 1);
        game.selectGod(p3, 2);
        game.selectFirstPlayer(p1, p1);
        game.placeWorkers(p1, new Vector2[]{new Vector2(0,0), new Vector2(0,1)});
        game.placeWorkers(p2, new Vector2[]{new Vector2(1,0), new Vector2(1,1)});
        game.placeWorkers(p3, new Vector2[]{new Vector2(2,0), new Vector2(2,1)});

        //game is ready
        //we cheat a bit and lock player 2 with a building
        //lock horizontal
        game.getCurrentMap().build(new Vector2(0,2));
        game.getCurrentMap().build(new Vector2(0,2));
        //lock diagonal
        game.getCurrentMap().build(new Vector2(1,2));
        game.getCurrentMap().build(new Vector2(1,2));
        // map:
        // w1 | w2 | X
        // A1 | A2 | X

        assertEquals(0, game.executeAction(p1,1,0, new Vector2(0,0)));
        assertEquals(Game.GameState.GAME, game.getCurrentState());
        assertEquals(p2, game.getCurrentPlayer());
        //ensure cells are freed when a player loses
        assertTrue(game.getCurrentMap().isCellEmpty(new Vector2(0,0)));
        assertTrue(game.getCurrentMap().isCellEmpty(new Vector2(0,1)));
    }

    @Test
    void shouldLoseAndWhenThereAreNoNextActions() throws NotAllowedOperationException
    {
        game.join(p1);
        game.join(p2);
        game.join(p3);
        game.start(p1);
        game.applyGodFilter(p1, new int[]{1,2,3});
        game.selectGod(p2, 1);
        game.selectGod(p3, 2);
        game.selectFirstPlayer(p1, p1);
        game.placeWorkers(p1, new Vector2[]{new Vector2(0,0), new Vector2(0,1)});
        game.placeWorkers(p2, new Vector2[]{new Vector2(1,0), new Vector2(1,1)});
        game.placeWorkers(p3, new Vector2[]{new Vector2(2,0), new Vector2(2,1)});

        //game is ready
        //we cheat a bit and lock player 2 with a building
        //lock horizontal
        game.getCurrentMap().build(new Vector2(0,2));
        game.getCurrentMap().build(new Vector2(0,2));
        //lock diagonal
        game.getCurrentMap().build(new Vector2(1,2));
        game.getCurrentMap().build(new Vector2(1,2));
        // map:
        // w1 | w2 | X
        // A1 | A2 | X

        assertNull(game.getNextActions());
        assertEquals(Game.GameState.GAME, game.getCurrentState());
        assertEquals(p2, game.getCurrentPlayer());
        //ensure cells are freed when a player loses
        assertTrue(game.getCurrentMap().isCellEmpty(new Vector2(0,0)));
        assertTrue(game.getCurrentMap().isCellEmpty(new Vector2(0,1)));
    }


    @Test
    void shouldWinIfOtherPlayerHasNoMoreMoves() throws NotAllowedOperationException
    {
        game.join(p1);
        game.join(p2);
        game.start(p1);
        game.applyGodFilter(p1, new int[]{1,2});
        game.selectGod(p2, 1);
        game.selectFirstPlayer(p1, p1);
        game.placeWorkers(p1, new Vector2[]{new Vector2(0,0), new Vector2(0,1)});
        game.placeWorkers(p2, new Vector2[]{new Vector2(1,0), new Vector2(1,1)});

        //game is ready
        //we cheat a bit and lock player 2 with a building
        //lock horizontal
        game.getCurrentMap().build(new Vector2(0,2));
        game.getCurrentMap().build(new Vector2(0,2));
        //lock diagonal
        game.getCurrentMap().build(new Vector2(1,2));
        game.getCurrentMap().build(new Vector2(1,2));
        // map:
        // w1 | w2 | X
        // A1 | A2 | X

        assertEquals(0, game.executeAction(p1,1,0, new Vector2(0,0)));
        assertEquals(Game.GameState.END, game.getCurrentState());
        assertEquals(p2, game.getCurrentPlayer());
        assertEquals(p2, game.getWinner());
    }

    @Test
    void shouldRemovePlayerIfHasNoMoreActionsOnTurnCreation() throws NotAllowedOperationException
    {
        game.join(p1);
        game.join(p2);
        game.start(p1);
        game.applyGodFilter(p1, new int[]{1,2});
        game.selectGod(p2, 1);
        game.selectFirstPlayer(p1, p1);
        game.placeWorkers(p1, new Vector2[]{new Vector2(0,0), new Vector2(0,1)});

        //we cheat a bit and lock player 2 with a building
        //lock horizontal
        game.getCurrentMap().build(new Vector2(0,2));
        game.getCurrentMap().build(new Vector2(0,2));
        //lock diagonal
        game.getCurrentMap().build(new Vector2(1,2));
        game.getCurrentMap().build(new Vector2(1,2));
        // map:
        // w1 | w2 | X
        // A1 | A2 | X

        // by blocking before the player before the turn is created we can trigger the "no more moves at turn creation"

        game.placeWorkers(p2, new Vector2[]{new Vector2(1,0), new Vector2(1,1)});

        //turn should have been created ad gave error

        assertEquals(Game.GameState.END, game.getCurrentState());
        assertEquals(p2, game.getCurrentPlayer());
        assertEquals(p2, game.getWinner());
    }

}
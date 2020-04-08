package it.polimi.ingsw.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class GameTest
{

    ReceiverHandler handler;
    Game game;
    Player p1, p2, p3;

    @BeforeEach
    void setUp()
    {
        handler = new ReceiverHandler();
        game = new Game(handler);
        p1 = new Player(0, "kazuma");
        p2 = new Player(1, "Raccoon");
        p3 = new Player(2, "Yun Yun");
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
        assertEquals(p1, handler.lastPlayer);

        //2 player
        assertTrue(game.join(p2));
        assertEquals(2, game.playerCount());
        assertEquals(p2, handler.lastPlayer);

        //3 player
        assertTrue(game.join(p3));
        assertEquals(3, game.playerCount());
        assertEquals(p3, handler.lastPlayer);
    }

    @Test
    void shouldNotJoin() {
        game.join(p1);
        game.join(p2);
        //null player


        // game full
        game.join(p3);

        assertFalse(game.join(new Player( 19, "Nope :'L")));
        assertEquals(p3, handler.lastPlayer);

        //game started
        game = new Game(handler); // reset game
        game.join(p1);
        game.join(p2);

        game.start();

        assertFalse(game.join(p3));
        assertEquals(2, game.playerCount());
        assertEquals(p2, handler.lastPlayer);

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
    void shouldStartGame() {
        //2 player start
        game.join(p1);
        game.join(p2);
        assertTrue(game.start());
        assertTrue(game.isStarted());

        // 3 players
        game = new Game(handler);
        game.join(p1);
        game.join(p2);
        game.join(p3);
        assertTrue(game.start());
        assertTrue(game.isStarted());
    }

    @Test
    void shouldNotStartGame()
    {
        //empty game
        assertFalse(game.start());
        // 1 player game
        game.join(p1);
        assertFalse(game.start());

        //TODO: check during gameplay
    }

}
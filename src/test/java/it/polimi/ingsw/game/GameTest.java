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
    }

    @Test
    void shouldNotStartGame()
    {
        //empty game
        assertFalse(game.start(null));
        // 1 player game
        game.join(p1);
        assertFalse(game.start(p1));
        //started by not host
        game.join(p2);
        assertFalse(game.start(p2));

        //TODO: check during gameplay
    }

}
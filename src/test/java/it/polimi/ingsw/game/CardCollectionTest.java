package it.polimi.ingsw.game;

import it.polimi.ingsw.game.Card;
import it.polimi.ingsw.game.CardCollection;
import it.polimi.ingsw.game.CardNotExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class CardCollectionTest {

    CardCollection coll;

    @BeforeEach
    void init()
    {
        coll = new CardCollection();
    }

    @Test
    public void shouldHasThreeOrMoreCards(){
        // min is 3 cards, 1 for every player
        assertTrue(coll.getCardIDs().length >= 3);
        assertEquals(coll.size(), coll.getCardIDs().length);
    }

    @Test
    public void shouldReturnCorrectCard(){

        int[] ids = coll.getCardIDs();
        for(int id : ids){

            try {
                Card c = coll.getCard(id);
                assertEquals(id, c.getId());
                assertNotEquals(null, c.getName());
                assertNotEquals("", c.getName());
                assertNotEquals(null, c.getGraph());
            } catch (CardNotExistsException e)
            {
                fail("getCardsID returned an invalid id");
            }
        }
    }

    @Test
    void shouldHaveNoDuplicateId() {
        int[] ids = coll.getCardIDs();

        boolean[] bitmap = new boolean[ids.length +1];

        for (int id : ids) {
            bitmap[id] ^= true;
            assertEquals(false, !bitmap[id]); // if false
        }
    }

    @Test
    void shouldReturnSelectedCards(){

        int sz = coll.size();
        int[] ids = new int[sz/2];
        Random r = new Random();
        // generate half ids size with random ids
        for(int i = 0; i < sz/2; i++)
        {
            ids[i] = coll.getCardIDs()[Math.abs(r.nextInt() % sz)];
        }

        try
        {
            Card[] cards = coll.getCards(ids);
            for(int i = 0; i < cards.length; i++ )
            {
                assertEquals(ids[i], cards[i].getId());
            }
        } catch (CardNotExistsException e)
        {
            fail("getCardsIDs returned an invalid id");
        }
    }

    @Test
    void shouldThowOnWrongID(){
        //single card
        assertThrows(CardNotExistsException.class, ()-> { coll.getCard(-100); });
        assertThrows(CardNotExistsException.class, ()-> { coll.getCard(1000000); });

        // multiple cards
        int[] ids = new int[2];
        ids[0] = -100;
        ids[1] = 1000000;
        assertThrows(CardNotExistsException.class, ()-> { coll.getCards(ids); });

        ids[0] = 1000000;
        ids[1] = -100;
        assertThrows(CardNotExistsException.class, ()-> { coll.getCards(ids); });
    }


}
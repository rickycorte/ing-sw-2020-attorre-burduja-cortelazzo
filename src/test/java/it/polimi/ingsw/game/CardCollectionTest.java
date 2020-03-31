package it.polimi.ingsw.game;

import it.polimi.ingsw.game.Card;
import it.polimi.ingsw.game.CardCollection;
import it.polimi.ingsw.game.CardNotExistsException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CardCollectionTest {

    @Test
    public void constructorTest ()
    {
        CardCollection myCollection = new CardCollection();
        assertNotNull(myCollection);

    }

    @Test
    public void getRandomTest(){
        int n = 3;
        Card card1 = new Card(1, "one", null);
        Card card2 = new Card(2, "two", null);
        Card card3 = new Card(3, "thr", null);
        Card card4 = new Card(4, "fou", null);
        Card card5 = new Card(5, "fiv", null);
        CardCollection myCollection = new CardCollection();
        myCollection.addCard(card1);
        myCollection.addCard(card2);
        myCollection.addCard(card3);
        myCollection.addCard(card4);
        myCollection.addCard(card5);
        List<Card> my_list = myCollection.getRandom(n);
        assertEquals(n, my_list.size());
    }

    @Test
    public void selectCardTest() throws CardNotExistsException {
        Card card1 = new Card(1, "one", null);
        Card card2 = new Card(2, "two", null);
        Card card3 = new Card(3, "thr", null);
        Card card4 = new Card(4, "fou", null);
        Card card5 = new Card(5, "fiv", null);
        CardCollection myCollection = new CardCollection();
        myCollection.addCard(card1);
        myCollection.addCard(card2);
        myCollection.addCard(card3);
        myCollection.addCard(card4);
        myCollection.addCard(card5);

        Card myCard = myCollection.select("fou");
        assertEquals("fou",myCard.getName());

    }
}
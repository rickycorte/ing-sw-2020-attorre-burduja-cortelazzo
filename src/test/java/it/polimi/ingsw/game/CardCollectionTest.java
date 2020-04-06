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



    //TODO: make card tests again :L

}
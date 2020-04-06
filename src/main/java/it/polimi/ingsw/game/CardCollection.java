package it.polimi.ingsw.game;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class holds the collection of currently available gods
 * This class should be instantiated for every new game to ensure isolation between cards
 */
public class CardCollection {
    private List<Card> cardCollection;


    /**
     * Initialize and load all available gods
     * The constructor is private to force access from singleton pattern
     * There is no need to generate a new instance of this class every time
     */
    public CardCollection(){
        // create new cards here
        cardCollection = new ArrayList<>();
    }


    /**
     * Get an array of card ids that are unique for every card
     * @return array of card ids
     */
    public int[] getCardIDs(){
        int[] ids = new int[cardCollection.size()];

        for(int i=0; i< cardCollection.size(); i++)
        {
            ids[i] = cardCollection.get(i).getId();
        }
        return ids;
    }


    /**
     * Return a card from its id, throw an exception if no card is found
     * It's returned a copy because this guarantee the isolation of a single card execution
     * @param id card id to search
     * @return card with corresponding id
     * @throws CardNotExistsException if no card is found
     */
    public Card getCard(int id) throws CardNotExistsException {
        for(int i = 0; i < cardCollection.size(); i++)
            if(cardCollection.get(i).getId() == id) return cardCollection.get(i);

            throw new CardNotExistsException();
    }

    /**
     * Return an array of cards based on ids passed as parameters
     * If one of the cards is not found an exception is thrown
     * @param ids card ids to search
     * @return card list
     * @throws CardNotExistsException if one of the ids doesn't match a card
     */
    public Card[] getCards(int[] ids) throws CardNotExistsException
    {
        Card[] cards = new Card[ids.length];
        for(int i =0; i < ids.length; i++)
            cards[i] = getCard(ids[i]);
        return cards;
    }
}

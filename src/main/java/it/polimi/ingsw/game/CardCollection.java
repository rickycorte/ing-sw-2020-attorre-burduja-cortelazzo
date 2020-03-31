package it.polimi.ingsw.game;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class CardCollection {
    List<Card> cardCollection;



    public CardCollection(){
        cardCollection = new ArrayList<>();
    }

    public List<Card> getCollection(){
        return cardCollection;
    }
    public void addCard(Card my_card){
        cardCollection.add(my_card);
    }

    // get n-number of random cards from card collection
    public List<Card> getRandom(int n){
        List<Card> myList = new ArrayList<>();
        Random r = new Random();
        for(int i = 0; i < n; i++){
            int random = r.nextInt(cardCollection.size());
            myList.add(cardCollection.get(random));
        }

        return myList;
    }

    //returns the card given the name
    public Card select (String c_name) throws CardNotExistsException{
        for(Card n : cardCollection){
            if((n.name).equals(c_name))
                return n;
        }
        throw new CardNotExistsException();
    }
}

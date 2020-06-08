package it.polimi.ingsw.view;

import java.util.ArrayList;
import java.util.List;

public class CardCollection {
    private List<Card> cardCollection;

    public CardCollection(){
        cardCollection = new ArrayList<>();

        cardCollection.add(new Card(
                1,
                "Apollo",
                "God Of Music",
                "Your Move: Your Worker may move into an opponent Worker’s space by forcing their Worker to the space yours just vacated."));

        cardCollection.add(new Card(
                2,
                "Artemis",
                "Goddess of the Hunt",
                "Your Move: Your Worker may move one additional time, but not back to its initial space. "));

        cardCollection.add(new Card(
                3,
                "Athena",
                "Goddess of Wisdom",
                "Opponent’s Turn: If one of your Workers moved up on your last turn, opponent Workers cannot move up this turn. "));

        cardCollection.add(new Card(
                4,
                "Atlas",
                "Titan Shouldering the Heavens",
                "Your Build: Your Worker may build a dome at any level."));

        cardCollection.add(new Card(
                5,
                "Demeter",
                "Goddess of the Harvest",
                "Your Build: Your Worker may build one additional time, but not on the same space."));

        cardCollection.add(new Card(
                6,
                "Hephaestus",
                "God of Blacksmiths",
                "Your Build: Your Worker may build one additional block (not dome) on top of your first block."));

        cardCollection.add(new Card(
                8,
                "Minotaur",
                "Bull-headed Monster ",
                "Your Move: Your Worker may move into an opponent Worker’s space, if their Worker can be forced one space straight backwards to an unoccupied space at any level."));

        cardCollection.add(new Card(
                9,
                "Pan",
                "God of the Wild",
                "Win Condition: You also win if your Worker moves down two or more levels."));

        cardCollection.add(new Card(
                10,
                "Prometheus",
                "Titan Benefactor of Mankind",
                "Your Turn: If your Worker does not move up, it may build both before and after moving."));
    }


    public List<Integer> getAvailableIds(){
        List<Integer> ids = new ArrayList<>();
        for(Card card : cardCollection){
            ids.add(card.getId());
        }
        return ids;
    }

    public Card getCard(int id) {
        for(Card  card : cardCollection){
            if(card.getId() == id)
                return card;
        }
        return null;
    }

    int getSize() { return cardCollection.size(); }
}

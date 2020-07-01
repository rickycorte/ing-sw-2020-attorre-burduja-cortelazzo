package it.polimi.ingsw.game;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the collection of currently available gods
 * This class should be instantiated for every new game to ensure isolation between cards
 */
public class CardCollection {
    final private List<Card> cardCollection;


    /**
     * Initialize and load all available gods
     * The constructor is private to force access from singleton pattern
     * There is no need to generate a new instance of this class every time
     */
    public CardCollection(){
        // create new cards here
        cardCollection = new ArrayList<>();

        // Apollo
        cardCollection.add(new Card(1, "Apollo", BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction(GameConstraints.Constraint.CAN_SWAP_CONSTRAINT))
                        .setNext(new BuildAction())
                        .setNext(new EndTurnAction())
                        .getRoot()
        )));
        // Artemis
        cardCollection.add(new Card(2, "Artemis", BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction())
                        .addBranch(BehaviourNode.makeRootNode(new BuildAction())
                                .setNext(new EndTurnAction())
                                .getRoot())
                        .addBranch(BehaviourNode.makeRootNode(new MoveAgainAction(GameConstraints.Constraint.BLOCK_SAME_CELL_MOVE))
                                .setNext(new BuildAction())
                                .setNext(new EndTurnAction())
                                .getRoot())
        )));

        // Athena
        var athena = new Card(3, "Athena", BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction(GameConstraints.Constraint.SET_BLOCK_MOVE_UP))
                        .setNext(new BuildAction())
                        .setNext(new EndTurnAction())
                        .getRoot()
        ));

        athena.setConstraintToClear(GameConstraints.Constraint.BLOCK_MOVE_UP);

        cardCollection.add(athena);

        // Atlas
        cardCollection.add(new Card(4, "Atlas", BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction())
                        .addBranch(BehaviourNode.makeRootNode(new BuildAction())
                                .setNext(new EndTurnAction())
                                .getRoot())
                        .addBranch(BehaviourNode.makeRootNode(new BuildDomeAction())
                                .setNext(new EndTurnAction())
                                .getRoot())
        )));
        // Demeter
        cardCollection.add(new Card(5, "Demeter", BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction()).setNext(new BuildAction())
                        .addBranch(new EndTurnAction())
                        .addBranch(BehaviourNode.makeRootNode(new BuildAgainAction(GameConstraints.Constraint.BLOCK_SAME_CELL_BUILD))
                                .setNext(new EndTurnAction())
                                .getRoot())
                        .getRoot()
        )));
        // Hephaestus
        cardCollection.add(new Card(6, "Hephaestus", BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction()).setNext(new BuildAction())
                        .addBranch(new EndTurnAction())
                        .addBranch(BehaviourNode.makeRootNode(new BuildAgainAction(GameConstraints.Constraint.BLOCK_DIFF_CELL_BUILD, GameConstraints.Constraint.BLOCK_DOME_BUILD))
                                .setNext(new EndTurnAction())
                                .getRoot())
                        .getRoot()
        )));
        // Minotaur
        cardCollection.add(new Card(8, "Minotaur", BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction(GameConstraints.Constraint.CAN_PUSH_CONSTRAINT))
                        .setNext(new BuildAction())
                        .setNext(new EndTurnAction())
                        .getRoot()
        )));
        // Pan
        cardCollection.add(new Card(9, "Pan", BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction(GameConstraints.Constraint.WIN_BY_GOING_DOWN))
                        .setNext(new BuildAction())
                        .setNext(new EndTurnAction())
                        .getRoot()
        )));
        // Prometheus
        cardCollection.add(new Card(10, "Prometheus ", BehaviourGraph.makeEmptyGraph().appendSubGraph(
                BehaviourNode.makeRootNode(new MoveAction())
                        .setNext(new BuildAction())
                        .setNext(new EndTurnAction())
                        .getRoot()
        ).appendSubGraph(
                BehaviourNode.makeRootNode(new BuildAction())
                        .setNext(new MoveAction(GameConstraints.Constraint.BLOCK_MOVE_UP))
                        .setNext(new BuildAction())
                        .setNext(new EndTurnAction())
                        .getRoot()
        )));

    }

    /**
     * Return the number of cards in the collection
     * @return number of cards in this collection
     */
    public int size()
    {
        return cardCollection.size();
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


    /**
     * Return the default turn made of a Move and a Build
     * This can be used to make matches without workers
     * @return the default god game turn behaviour without any god
     */
    public Card getNoGodCard()
    {
        return new Card(177013, "No God",
                BehaviourGraph.makeEmptyGraph().appendSubGraph(
                        BehaviourNode.makeRootNode(new MoveAction()).setNext(new BuildAction()).getRoot()
                ));
    }

}

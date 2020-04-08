package it.polimi.ingsw.game;

/**
 * Interface used to define observer callbacks for the Game class
 */
public interface IGameUpdateReceiver
{
    void onPlayerJoin(Player player);

    void onGodSelectionPhase(Player host, int[] cardIDs, int cardCount);

    void onGodPickPhase(Player target, int[] cards);

    void onFirstPlayerChose(Player host, Player[] others);

    void onPlayerTurn(Player target);

    void onMapUpdate(Map map);

    void onGameEnd(Player winner);
}

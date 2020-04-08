package it.polimi.ingsw.game;

/**
 * This class is designed to be used only in tests
 */
class ReceiverHandler implements IGameUpdateReceiver {

    public Player lastPlayer;

    @Override
    public void onPlayerJoin(Player player)
    {
        lastPlayer = player;
    }

    @Override
    public void onGodSelectionPhase(Player host, int[] cardIDs, int cardCount)
    {

    }

    @Override
    public void onGodPickPhase(Player target, int[] cards)
    {

    }

    @Override
    public void onFirstPlayerChose(Player host, Player[] others)
    {

    }

    @Override
    public void onPlayerTurn(Player target)
    {

    }

    @Override
    public void onMapUpdate(Map map)
    {

    }

    @Override
    public void onGameEnd(Player winner)
    {

    }
}
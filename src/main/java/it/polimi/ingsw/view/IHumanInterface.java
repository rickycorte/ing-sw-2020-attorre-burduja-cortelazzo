package it.polimi.ingsw.view;

import it.polimi.ingsw.network.ICommandReceiver;

/**
 * This interface extends ICommandReceiver to add a start function to enable UIs to start their job
 */
public interface IHumanInterface extends ICommandReceiver
{
    /**
     * Start the human interface
     */
    void start();
}

package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.Command;
import it.polimi.ingsw.controller.CommandWrapper;

public interface ICommandReceiver
{
    /**
     * Callback to a join operation
     * Server: this callback is called for every player that tries to join
     * Client: this callback is called when you join a match
     * @param cmd join command
     */
    void onConnect(CommandWrapper cmd);

    /**
     * Callback for a disconnect operation
     * Server: this callback is called for every player that tries to left
     * Client: this callback is called when you left a match
     * @param cmd disconnect command
     */
    void onDisconnect(CommandWrapper cmd);

    /**
     * Callback called when a command is received (no join/left that have dedicated handlers)
     * @param cmd command to process
     */
    void onCommand(CommandWrapper cmd);
}

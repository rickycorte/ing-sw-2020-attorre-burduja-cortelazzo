package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.Command;

public interface ICommandReceiver
{
    /**
     * Callback to a join operation
     * Server: this callback is called for every player that tries to join
     * Client: this callback is called when you join a match
     * @param id id of the player
     * @param username username of the player
     */
    void onConnect(int id, String username);

    /**
     * Callback for a disconnect operation
     * Server: this callback is called for every player that tries to left
     * Client: this callback is called when you left a match
     * @param id id of the player
     */
    void onDisconnect(int id);

    /**
     * Callback called when a command is received (no join/left that have dedicated handlers)
     * @param cmd command to process
     */
    void onCommand(Command cmd);
}

package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.CommandWrapper;

/**
 * Interface for a network adapter
 */
public interface INetworkAdapter
{

    /* server */

    /**
     * Start a server
     * @param port port where the server should listen
     */
    void StartServer(int port);

    /**
     * Stop the current running server
     */
    void StopServer();

    /* client */

    /**
     * Connect to a running server
     * @param ip server ip
     * @param port server port
     * @param username username to use
     */
    void Connect(String ip, int port, String username);

    /**
     * Disconnect from a server
     */
    void Disconnect();

    /* common */

    /**
     * Add a receiver for packets sent over the network
     * @param receiver packet receiver to add
     */
    void AddReceiver(ICommandReceiver receiver);


    /**
     * gets ICommandReceiver
     * @return receiver
     */
    ICommandReceiver getReceiver();

    /**
     * Remove a receiver for packers
     */
    void RemoveReceiver();

    /**
     * Send a packet to all clients, only command's target can handle the command
     * @param packet packet to send
     */
    void Send(CommandWrapper packet);

    int getServerID();

    int getBroadCastID();
}

package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.CommandWrapper;

/**
 * Interface for a network adapter
 */
public interface INetworkAdapter
{

    void startServer();

    /* server */

    /**
     * Start a server
     * @param port port where the server should listen
     */
    void startServer(int port);

    /**
     * Stop the current running server
     */
    void stopServer();

    /* clientDELETE */

    /**
     * Connect to a running server
     * @param ip server ip
     * @param port server port
     * @param username username to use
     * @return true on success
     */
    boolean connect(String ip, int port, String username);

    /**
     * Disconnect from a server
     */
    void disconnect();

    /* common */

    /**
     * Add a receiver for packets sent over the network
     * @param receiver packet receiver to add
     */
    void setReceiver(ICommandReceiver receiver);


    /**
     * Remove a receiver for packers
     */
    void removeReceiver();

    /**
     * Send a packet to all clients, only command's target can handle the command
     * @param packet packet to send
     */
    void send(CommandWrapper packet);

    /**
     * Return default server ID
     * @return server ID
     */
    int getServerID();

    /**
     * Return default broadcast ID
     * @return broadcast ID
     */
    int getBroadCastID();

    /**
     * Return default server port
     * @return default server port
     */
    int getDefaultPort();
}

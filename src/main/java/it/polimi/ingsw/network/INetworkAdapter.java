package it.polimi.ingsw.network;

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
     * Remove a receiver for packers
     * @param receiver packet receiver to remove
     */
    void RemoveReceiver(ICommandReceiver receiver);

    /**
     * Send a packet to a specific client
     * @param id client id
     * @param packet packet to send
     */
    void Send(int id, INetworkSerializable packet);

    /**
     * Send a packet to all the clients
     * @param packet packet to send
     */
    void SendBroadcast(INetworkSerializable packet);
}

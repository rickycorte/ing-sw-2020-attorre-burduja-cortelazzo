package it.polimi.ingsw.network;

/**
 * Interface for a network adapter.
 * This is the complete and full version of the adapter used in the application, for a reduces interface to only send/receive messages see parent interface {@link INetworkForwarder}
 */
public interface INetworkAdapter extends INetworkForwarder
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

    /* client */

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

    int getClientID();
}

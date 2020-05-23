package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.network.client.Client;
import it.polimi.ingsw.network.server.Server;

/**
 * TCP implementation on INetworkAdapter
 * use this class with the interface e not directly to be able to swap adapter without rewriting a line of code
 *
 * Note that this class is used as both server and client, but only a client or server per instance is allowed.
 * To create multiple servers/client in the same program use multiple instances of this class
 */
public class TPCNetwork implements INetworkAdapter
{


    boolean isRunning = false;
    Server server;
    Client client;

    ICommandReceiver receiver;

    /**
     * Start the server in background with the default listen port
     */
    @Override
    public void startServer()
    {
        startServer(Server.DEFAULT_SERVER_PORT);
    }

    /**
     * Start the server in background with a specified port
     * @param port port where the server should listen
     */
    @Override
    public void startServer(int port)
    {
        if(isRunning) return;

        if(port < 1024 || port > 65535) port = Server.DEFAULT_SERVER_PORT;
        isRunning = true;

        server = new Server(port);
        if(receiver != null)
            server.setReceiver(receiver);
        server.startInBackground();
    }

    /**
     * Stop the current running server
     */
    @Override
    public void stopServer()
    {
        if(isRunning && server != null)
        {
            server.stop();
            server = null;
        }
        isRunning = false;
    }

    /**
     * Connect to a server in background
     * @param ip server ip
     * @param port server port
     * @param username username to use
     * @return true is success
     */
    @Override
    public boolean connect(String ip, int port, String username)
    {
        if(isRunning) return false;

        isRunning = true;
        if(client == null)
        {
            client = new Client();
            if(receiver != null)
                client.setReceiver(receiver);
            return client.connect(ip, port, username);
        }

        return false;
    }

    /**
     * Disconnect from the current server
     */
    @Override
    public void disconnect()
    {

        if (client != null)
        {
            client.disconnect();
            client = null;
        }

        isRunning = false;
    }

    /**
     * Set receiver for commands
     * @param receiver packet receiver to add
     */
    @Override
    public void setReceiver(ICommandReceiver receiver)
    {
        this.receiver = receiver;
        if(server != null)
            server.setReceiver(receiver);
        else if( client != null)
            client.setReceiver(receiver);

    }

    /**
     * Remove receiver for commands
     */
    @Override
    public void removeReceiver()
    {
        receiver = null;
        if(server != null)
            server.removeReceiver();
        else if( client != null)
            client.removeReceiver();
    }

    /**
     * Send a command to connected sockets
     * @param packet packet to send
     */
    @Override
    public void send(CommandWrapper packet)
    {
        if(server != null)
            server.send(packet);
        else if(client != null)
            client.send(packet);

    }

    /**
     * Get the default server ID that can be used to target a command to the server
     * @return default server ID
     */
    @Override
    public int getServerID()
    {
        return Server.SERVER_ID;
    }

    /**
     * Get the default broadcast ID for commands
     * @return default broadcast ID
     */
    @Override
    public int getBroadCastID()
    {
        return Server.BROADCAST_ID;
    }

    /**
     * Get the default server port
     * @return default server port
     */
    @Override
    public int getDefaultPort()
    {
        return Server.DEFAULT_SERVER_PORT;
    }
}

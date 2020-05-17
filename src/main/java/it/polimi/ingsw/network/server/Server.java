package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.network.ICommandReceiver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * This class is the main server which starts a socket
 * Handles the clients
 */
public class Server {

    static final public int SERVER_ID = -1111;
    static final public int BROADCAST_ID = -2222;

    ServerSocket listener;
    private ExecutorService pool;                    //pool to execute a different thread
    private int port;
    private Map<Integer, ClientHandler> syncClientHandlerMap;  //Hash map to map a client_id to a ClientHandler
    private ICommandReceiver controller;                    //Controller to handle commands

    private int last_client_id;

    private boolean shouldStop;


    /**
     * Server constructor
     * @param port where the server should bind
     */
    public Server(int port)
    {
        shouldStop = false;
        this.port = port;
        syncClientHandlerMap = Collections.synchronizedMap(new HashMap<>()); // sync operations NOT iteration thank you java for half made things :L
        pool = Executors.newCachedThreadPool();
    }


    /**
     * Get should stop background accept loop
     * This function is used by the loop to check is should stop in synchronized way
     * @return
     */
    private synchronized boolean getShouldStop()
    {
        return shouldStop;
    }


    /**
     * Set should stop to true to stop the accept loop in a synchronized way to avoid race conditions
     * and gracefully stop background accept thread
     * @param shouldStop set to true to stop accept loop
     */
    private synchronized void setShouldStop(boolean shouldStop)
    {
        this.shouldStop = shouldStop;
    }


    /**
     * Start the server in background leaving the control of the current thread to the caller
     */
    public void startInBackground()
    {
        new Thread(this::start).start();
    }

    /**
     * Start the server
     * This function hooks to the calling thread and "grab" its control to create the accept loop for incoming connections
     */
    public void start()
    {
        System.out.println("[SERVER] Starting server on port " + port + " ...");
        try
        {
            listener = new ServerSocket(port);

            System.out.println("[SERVER] Waiting for connections...");
            runServerLoop(listener);
        }
        catch (IOException e)
        {
            System.out.println("[SERVER] Unable to bind to port: "+ port);
        }
        catch (Throwable e)
        {
            System.out.println("[SERVER] Something when wrong in server loop: "+e.getMessage());
        }
    }


    /**
     * Represents the behaviour of the server
     * Waits on clients to connect and handles each connection in a different thread
     */
    private void runServerLoop(ServerSocket listener) {

        while (!getShouldStop())
        {
            Socket client_socket;
            try
            {
                last_client_id++; // increment client id

                // accept connection
                client_socket = listener.accept();

                // run connection handler in background
                ClientHandler clientHandler = new ClientHandler(client_socket, last_client_id, this);
                pool.execute(clientHandler);

                // track connected clients
                syncClientHandlerMap.put(last_client_id, clientHandler);

                System.out.println("[SERVER] New connection, assigned id: " + last_client_id);
            }
            catch (IOException e)
            {
                System.out.println("[SERVER] Could not accept connection: "+e.getMessage());
            }
        }
    }

    //Methods used to pass to handle interactions between client handlers and controller

    /**
     * Handle join type commands
     * @param cmd join type command
     */
    public synchronized void onConnectEvent(CommandWrapper cmd){
        System.out.println("[SERVER] Received " + cmd.getType().name()+ " command");
        if(controller != null)
            controller.onConnect(cmd);
        else
            System.out.println("[SERVER] Missing controller handler");
    }

    /**
     * Handle a leaving client
     * @param client_handler leaving
     */
    public synchronized void onDisconnectEvent(ClientHandler client_handler){
        CommandWrapper leave_command = new CommandWrapper(CommandType.LEAVE, new LeaveCommand(client_handler.getId(), SERVER_ID));
        leave_command.Serialize();
        if(controller != null)
            controller.onDisconnect(leave_command);
        else
            System.out.println("[SERVER] Missing controller handler");

        //remove both id and client handler from the mapped ones
        syncClientHandlerMap.remove(client_handler.getId());
    }

    /**
     * Handle all other commands
     * @param cmd command
     */
    public synchronized void onCommandEvent(CommandWrapper cmd){
        System.out.println("[SERVER] Received " + cmd.getType().name()+ " command");
        if(controller != null)
            controller.onCommand(cmd);
        else
            System.out.println("[SERVER] Missing controller handler");
    }



    /**
     * Send a command to all clients, only command's target can handle it
     * @param cmd represents the message to send
     */
    public void send(CommandWrapper cmd) {
        System.out.println("[SERVER] Sending " + cmd.getType().name() + " command");

        //explicit lock for iteration
        synchronized (syncClientHandlerMap)
        {
            syncClientHandlerMap.forEach( (id, clientHandler) -> {
                syncClientHandlerMap.get(id).sendMessage(cmd);
            } );
        }
    }

    /**
     * Method used to close the server, closes the socket
     */
    public void stop()
    {
        try
        {
            if(listener != null)
            {
                setShouldStop(true);
                listener.close();
            }
        }
        catch (IOException e)
        {
            System.out.println("[SERVER] [FATAL] Error closing server: "+e.getMessage());
            System.exit(1);
        }
        System.out.println("[SERVER] Server closed");
    }


    /**
     * Adds a receiver to this server
     * @param receiver packet receiver to add
     */
    public synchronized void setReceiver(ICommandReceiver receiver)
    {
        if(receiver != null)
            this.controller = receiver;
    }

    /**
     * Removes this server's receiver
     */
    public synchronized void removeReceiver() {
        this.controller = null;
    }


}

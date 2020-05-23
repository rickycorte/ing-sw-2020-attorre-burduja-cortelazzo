package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.network.ICommandReceiver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * This class is the main server which starts a socket
 * Handles the clients
 */
public class Server {

    static final public int SERVER_ID = -1111;
    static final public int BROADCAST_ID = -2222;
    static final public int DEFAULT_SERVER_PORT = 0xDED; // aka 3565

    ServerSocket listener;
    private ExecutorService pool;                    //pool to execute a different thread
    private int port;
    private final Map<Integer, ClientHandler> syncClientHandlerMap;  //Hash map to map a client_id to a ClientHandler
    private ICommandReceiver receiver;                    //Controller to handle commands

    private int last_client_id;

    private AtomicBoolean shouldStop;

    private final Object lckController;


    /**
     * Server constructor
     * @param port where the server should bind
     */
    public Server(int port)
    {
        shouldStop = new AtomicBoolean(false);
        this.port = port;
        syncClientHandlerMap = Collections.synchronizedMap(new HashMap<>()); // sync operations NOT iteration thank you java for half made things :L
        pool = Executors.newCachedThreadPool();
        lckController = new Object();
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

        while (!shouldStop.get())
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
     * Remove a client
     * @param id
     */
    public void removeClient(int id)
    {
        syncClientHandlerMap.remove(id);
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

    public void send(int target, CommandWrapper cmd)
    {
        System.out.println("[SERVER] Sending " + cmd.getType().name() + " command to " + target);
        syncClientHandlerMap.get(target).sendMessage(cmd);
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
                shouldStop.set(true);
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
     * (Thread safe) Adds a receiver to this server
     * @param receiver packet receiver to add
     */
    public void setReceiver(ICommandReceiver receiver)
    {
        synchronized (lckController)
        {
            if (receiver != null)
                this.receiver = receiver;
        }
    }

    /**
     * (Thread safe) Removes this server's receiver
     */
    public void removeReceiver() {
        synchronized (lckController)
        {
            this.receiver = null;
        }
    }

    /**
     * (Thread safe) Return the current receiver in use by the server (can be null)
     * @return receiver handler for commands
     */
    public ICommandReceiver getReceiver()
    {
        synchronized (lckController)
        {
            return receiver;
        }
    }


}

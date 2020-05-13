package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;

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
public class Server implements INetworkAdapter {
    private static int server_id;                           //server id
    //private static int port;                                //the port on which the server will listen
    private ServerSocket s_socket;                          //the socket on which the server will read/write
    private static ExecutorService pool;                    //pool to execute a different thread
    private final Object outLock;                           //lock for out stream
    private final Object mapLock;                           //lock for clientHandlerMao
    private Map<Integer, Client_Handler> clientHandlerMap;  //Hash map to map a client_id to a ClientHandler
    private ArrayList<String> inGameUsernames;                           //list containing usernames of all connected clients
    private ICommandReceiver controller;                    //Controller to handle commands


    /**
     * Server constructor
     */
    public Server(){
        server_id = -1111;
        mapLock = new Object();
        outLock = new Object();

        inGameUsernames = new ArrayList<>();

        synchronized (mapLock) {
            clientHandlerMap = new HashMap<>();
        }

        pool = Executors.newCachedThreadPool();

    }

    @Override
    public void StartServer(int port) {
        System.out.println("Welcome to the Santorini server!");
        System.out.println("[SERVER] Starting...");
        //this.port = port;

        ServerSocket listener = null;
        try {
            listener = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("[SERVER] ERR! Couldn't start ServerSocket");
        }

        System.out.println("[SERVER] Started!");
        System.out.println("[SERVER] Listening on: " + port + " port");

        if (listener != null) {
            try {
                acceptConnections(listener);
            } catch (Throwable e) {
                System.out.println("[SERVER] ERR! couldn't start accepting connections");
            } finally {
                System.out.println("[SERVER] Shutting down...");
                StopServer();
            }
        } else {
            System.out.println("Error in setting the listener");
            StopServer();
        }
    }

    /**
     * Represents the behaviour of the server
     * Waits on clients to connect and handles each connection in a different thread
     */
    private void acceptConnections(ServerSocket listener) {
        int id = -1;
        System.out.println("[SERVER] Waiting for clients...");
        while (true) {
            Socket client_socket;
            try {
                client_socket = listener.accept();
                id++;
                Client_Handler clientHandler = new Client_Handler(client_socket, id, this, outLock);
                synchronized (mapLock) {
                    clientHandlerMap.put(id, clientHandler);
                }
                pool.execute(clientHandler);
                System.out.println("[SERVER] Connected to client no: " + id);
            } catch (IOException e) {
                System.out.println("[SERVER] ERR! couldn't accept");
            }
        }
    }

    //Methods used to pass to handle interactions between client handlers and controller

    /**
     * Handle join type commands
     * @param cmd join type command
     */
    void onConnect(CommandWrapper cmd){
        System.out.println("[SERVER] Received " + cmd.getType().name()+ " command");
        controller.onConnect(cmd);
    }

    /**
     * Handle a leaving client
     * @param client_handler leaving
     */
    void onDisconnect(Client_Handler client_handler){
        CommandWrapper leave_command = new CommandWrapper(CommandType.LEAVE, new LeaveCommand(client_handler.getId(), -1111));
        leave_command.Serialize();
        controller.onDisconnect(leave_command);

        //remove both id and client handler from the mapped ones
        synchronized (mapLock) {
            clientHandlerMap.remove(client_handler.getId());
        }
    }

    /**
     * Handle all other commands
     * @param cmd command
     */
    void onCommand(CommandWrapper cmd){
        System.out.println("[SERVER] Received " + cmd.getType().name()+ " command");
        controller.onCommand(cmd);
    }


    //INetworkAdapter method implementations

    /**
     * Send a command to all the clients
     * @param cmd command to send
     */
    @Override
    public void SendBroadcast(CommandWrapper cmd) {
        System.out.println("[SERVER] Sending " + cmd.getType().name() + " command on broadcast");
        synchronized (mapLock) {
            clientHandlerMap.forEach((id, clientHandler) -> {
                clientHandlerMap.get(id).sendMessage(cmd);
            });
        }
    }
    /**
     * Send a command to a client
     * @param id client_id
     * @param cmd represents the message to send
     */
    @Override
    public void Send(int id, CommandWrapper cmd) {
        synchronized (mapLock) {
            System.out.println("[SERVER] Sending " + cmd.getType().name() + " command to id: " + id);
            clientHandlerMap.get(id).sendMessage(cmd);
        }
    }

    /**
     * Method used to close the server, closes the socket
     */
    @Override
    public void StopServer(){
        try {
            s_socket.close();
        }catch (IOException e){
            System.out.println("[SERVER] Shutting down...");
            System.exit(0);
        }
    }

    @Override
    public void Connect(String ip, int port, String username) {
        //do nothing here, client method
    }

    @Override
    public void Disconnect() {
        //do nothing here, client method
    }

    /**
     * Adds a receiver to this server
     * @param receiver packet receiver to add
     */
    @Override
    public void AddReceiver(ICommandReceiver receiver) {
        this.controller = receiver;
    }

    /**
     * Return this server's command receiver
     * @return this server's command receiver
     */
    @Override
    public ICommandReceiver getReceiver() {
        return this.controller;
    }

    /**
     * Removes this server's receiver
     */
    @Override
    public void RemoveReceiver() {
        this.controller = null;
    }

    /**
     * returns this server's id
     * @return server's id
     */
    @Override
    public int getServerID() {
        return server_id;
    }

    /**
     * gets broadcast id
     * @return broadcast id
     */
    @Override
    public int getBroadCastID() {
        return -2222;
    }



}

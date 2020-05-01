package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.network.INetworkSerializable;

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
    private static int server_id = -1111;
    private static int port;                                //the port on which the server will listen
    private ServerSocket s_socket;                          //the socket on which the server will read/write
    private static ExecutorService pool;                    //max num of connected clients
    private Timer timer;
    private Map<Integer, Client_Handler> clientHandlerMap;   //Hash map to map a client_id to a ClientHandler
    private Controller controller;                          //Controller to handle commands



    /**
     * Server constructor
     */
    Server(){
        clientHandlerMap = new HashMap<>();
        this.pool = Executors.newCachedThreadPool();
        this.timer = new Timer();
        this.controller = new Controller();

    }

    /**
     * Method used to start a connection to a given port
     * @param port int representing the exact port
     */
    public void StartServer(int port) {
        System.out.println("Welcome to the Santorini server!");
        System.out.println("[SERVER] Starting...");
        Server.port = port;
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(port);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("[SERVER] Started!");
        System.out.println("[SERVER] Listening on: " + port + " port");

        try {
            behaviour(listener);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.out.println("[SERVER] Shutting down...");
            StopServer();
        }

    }

    /**
     * Represents the behaviour of a server
     * Waits on clients to connect and handles each connection in a different thread
     */
    private void behaviour(ServerSocket listener) {
        int id = 0;
        System.out.println("[SERVER] Waiting for clients...");
        while (true) {
            Socket client_socket = null;
            try {
                client_socket = listener.accept();
                id++;
                Client_Handler clientHandler = new Client_Handler(client_socket, id, controller);
                clientHandlerMap.put(id, clientHandler);
                pool.execute(clientHandler);
                System.out.println("[SERVER] Connected to client");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method used to send a message to a client
     * @param id represents the client_id
     * @param packet represents the message to send
     */

    public void Send(int id, INetworkSerializable packet) {
        clientHandlerMap.get(id).sendMessage(packet);
    }


    /**
     * Method used to send a message to all the clients
     * @param packet represents the message to send
     */
    public void SendBroadcast(INetworkSerializable packet) {
        clientHandlerMap.forEach((id, clientHandler) -> {
            clientHandlerMap.get(id).sendMessage(packet);
        });
    }
    /**
     * Method used to close the server
     */
    public void StopServer(){
        try {
            s_socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    int getServer_id(){
        return server_id;
    }
}

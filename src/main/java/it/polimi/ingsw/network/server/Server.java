package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.BaseCommand;
import it.polimi.ingsw.controller.CommandType;
import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.controller.Controller;
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
    private static int server_id = -1111;
    private static int port;                                //the port on which the server will listen
    private ServerSocket s_socket;                          //the socket on which the server will read/write
    private static ExecutorService pool;                    //max num of connected clients
    private final Object mapLock;
    private Map<Integer, Client_Handler> clientHandlerMap;  //Hash map to map a client_id to a ClientHandler
    private ICommandReceiver controller;                    //Controller to handle commands
    private final Object outLock;




    /**
     * Server constructor
     */
    public Server(){
        this.mapLock = new Object();
        this.outLock = new Object();
        synchronized (mapLock) {
            clientHandlerMap = new HashMap<>();
        }
        this.pool = Executors.newCachedThreadPool();
        controller = new Controller(this);

    }

    public void StartServer(int port) {
        System.out.println("Welcome to the Santorini server!");
        System.out.println("[SERVER] Starting...");
        Server.port = port;

        ServerSocket listener = null;
        try {
            listener = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("ERR! Couldn't start the server");
        }

        System.out.println("[SERVER] Started!");
        System.out.println("[SERVER] Listening on: " + port + " port");

        if (listener != null) {
            try {
                behaviour(listener);
            } catch (Throwable e) {
                System.out.println("[SERVER] err behaviour(listener)");
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
     * Represents the behaviour of a server
     * Waits on clients to connect and handles each connection in a different thread
     */
    private void behaviour(ServerSocket listener) {
        int id = 0;
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
                System.out.println("[SERVER] Connected to client");
            } catch (IOException e) {
                System.out.println("[SERVER] err in accepting");
                //e.printStackTrace();
            }
        }
    }

    void handleJoin(CommandWrapper cmd){
        controller.onConnect(cmd);
    }

    void onCommand(CommandWrapper cmd){
        controller.onCommand(cmd);
    }

    void onDisconnect(Client_Handler ch){

        CommandWrapper leave_command = new CommandWrapper(CommandType.LEAVE, new BaseCommand(ch.getId(), -1111));
        controller.onDisconnect(leave_command);

        //remove both id and client handler from the mapped ones
        synchronized (mapLock) {
            clientHandlerMap.remove(ch.getId());
        }

    }

    /**
     * Method used to send a message to a client
     * @param id represents the client_id
     * @param packet represents the message to send
     */

    public void Send(int id, CommandWrapper packet) {
        synchronized (mapLock) {
            clientHandlerMap.get(id).sendMessage(packet);
        }
    }


    /**
     * Method used to send a message to all the clients
     * @param packet represents the message to send
     */
    public void SendBroadcast(CommandWrapper packet) {
        synchronized (mapLock) {
            clientHandlerMap.forEach((id, clientHandler) -> {
                clientHandlerMap.get(id).sendMessage(packet);
            });
        }
    }

    @Override
    public int getServerID() {
        return 0;
    }

    @Override
    public int getBroadCastID() {
        return 0;
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

    @Override
    public void Connect(String ip, int port, String username) {

    }

    @Override
    public void Disconnect() {

    }

    @Override
    public void AddReceiver(ICommandReceiver receiver) {
        this.controller = receiver;

    }

    @Override
    public ICommandReceiver getReceiver() {
        return this.controller;
    }

    @Override
    public void RemoveReceiver(ICommandReceiver receiver) {
        this.controller = null;

    }

    int getServer_id(){
        return server_id;
    }

    ICommandReceiver getController(){
        return this.controller;
    }


}

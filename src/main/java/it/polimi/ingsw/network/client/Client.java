package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.Command;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.INetworkSerializable;

import java.io.*;
import java.net.Socket;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 16000;


    /**
     * Method used to create a connection between a server and a client
     * Starts a ServerConnection class in a new thread taht will listen for incoming messages
     * @param server_ip server's address
     * @param port      server's listening port
     */
    public static void connect(String server_ip, int port) {
        System.out.println("[CLIENT] Connecting...");
        Socket s_socket;
        try {
            s_socket = new Socket(SERVER_IP, PORT);
            System.out.println("[CLIENT] Connection to server established");
            ServerConnection serverConnection = new ServerConnection(s_socket, SERVER_IP);

            new Thread(serverConnection).start();

        } catch (IOException e) {
            System.out.println("[CLIENT] error in connect method");
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        System.out.println("[CLIENT] Welcome to the Santorini client!");
        //TODO should ask the server ip and the port you want to connect to
        connect(SERVER_IP, PORT);

    }



}




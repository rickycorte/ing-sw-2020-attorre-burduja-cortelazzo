package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.Command;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;

import java.io.*;
import java.net.Socket;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 16000;
    private static ServerConnection serverConnection;
    //public String username;
    //private BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));



    /**
     * Method used to create a connection between a server and a client
     * Starts a ServerConnection class in a new thread taht will listen for incoming messages
     * @param server_ip server's address
     * @param port      server's listening port
     */
    public void connect(String server_ip, int port) {
        System.out.println("[CLIENT] Connecting...");
        Socket s_socket;
        try {
            s_socket = new Socket(SERVER_IP, PORT);
            System.out.println("[CLIENT] Connection to server established");
            serverConnection = new ServerConnection(s_socket, SERVER_IP);
            //username = askUsername();
            new Thread(serverConnection).start();

        } catch (IOException e) {
            System.out.println("[CLIENT] error in connect method");
            e.printStackTrace();
        }
    }
    public ServerConnection getServerConnection(){
        return serverConnection;
    }
    /*

    public String askUsername() throws IOException {
        System.out.println("[CLIENT] Username?");
        return keyboard.readLine();

    }

     */




}




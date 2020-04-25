package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.Command;
import it.polimi.ingsw.network.ICommandReceiver;

import java.io.*;
import java.net.Socket;

public class Client{
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 9090;


    /**
     * Method used to create a connection between a server and a client
     * @param server_ip server's address
     * @param port server's listening port
     * @return the socket to communicate with the server
     */
    public static Socket connect(String server_ip, int port) {
        System.out.println("[CLIENT] Connecting...");
        Socket s_socket = null;
        try {
            s_socket = new Socket(SERVER_IP, PORT);
            System.out.println("[CLIENT] Connected successfully!");
            ServerConnection serverConnection = new ServerConnection(s_socket, SERVER_IP);
            new Thread(serverConnection).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s_socket;
    }



    public static void closeConnection(Socket socket){
        try {
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("[CLIENT] Welcome to Santorini client!");
        Socket s_socket = connect(SERVER_IP,PORT);

        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); // buffer for keyboard user input
        PrintWriter out = new PrintWriter(s_socket.getOutputStream(), true);


        while (true) {
            System.out.println("> ");
            String command = keyboard.readLine();
            if (command.equals("quit")) break; //if user types "quit", client will shut down
            out.println(command);              // else it will send the message to the server
        }
        closeConnection(s_socket);
        System.exit(0);


    }



}

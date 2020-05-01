package it.polimi.ingsw.network.client;


import it.polimi.ingsw.controller.Command;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.INetworkSerializable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Class for the client to interact with the server
 *
 */

public class ServerConnection implements Runnable {
    private String host;   //server's ip address
    private Socket s_socket;
    private int port;
    private BufferedReader in;
    private PrintWriter out;
    BufferedReader keyboard;
    INetworkSerializable ins;

    /**
     * ServerConnection constructor
     * @param server_socket server's socket to connect to
     */
    ServerConnection(Socket server_socket, String host){
        try {
            this.host = host;
            s_socket = server_socket;
            this.port = server_socket.getPort();
            in = new BufferedReader(new InputStreamReader(s_socket.getInputStream()));   // used to receive messages
            out = new PrintWriter(s_socket.getOutputStream(), true);            //used to send messages
            keyboard = new BufferedReader(new InputStreamReader(System.in));            //handle user input

        }catch (IOException e){
            System.out.println("unhandled exception in ServerConnection constructor");
        }
    }

    /**
     * This is the entry point of the ServerConnection class
     * this method receives the id, sends a join command and begins the read loop
     */
    @Override
    public void run() {

        String serverResponse = null; // this is the client_id given to me by the server
        serverResponse = receive();
        serverResponse = receive();
        System.out.println(serverResponse);
        int id = Integer.parseInt(serverResponse);
        System.out.println("Choose an username\n");
        String username = null;
        try {
            username = keyboard.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Command cmd = createJOINpacket(id, username);
        //send(ins.Serialize(cmd));

        readLoop();

    }

    /**
     * Creates a JOIN command
     * @param client_id sender id
     * @param client_username sender username
     * @return the newly created command
     */
    private Command createJOINpacket(int client_id, String client_username) {
        //return new Command((Command.CType.JOIN).toInt(), true, client_id, -1111, client_username);
        return null;
    }

    /**
     * This method represents the main behaviour of this class
     * it listens for server's response and displays it
     */

    public void readLoop(){
        //TODO implement timer so it doesnt run at crazy speeds
        String serverResponse = null;
        while (true) {
            serverResponse = receive();
            if (serverResponse == null) break;
            System.out.println(serverResponse);
        }
        close();

    }

    /**
     * Method used to send a message to the server
     * @param message to send
     */
    public void send(String message) {
        out.println(message);
    }

    /**
     * Method to receive string messages from the server
     * @return received message
     */
    public String receive(){
        try {
            return in.readLine();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method used to close the connection to the server
     */
    public void close(){
        try {
            in.close();
            out.close();
            s_socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }



}

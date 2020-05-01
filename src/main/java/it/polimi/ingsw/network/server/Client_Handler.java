
package it.polimi.ingsw.network.server;


import it.polimi.ingsw.controller.Command;
import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.INetworkSerializable;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Class for the server to handle each client
 *
 */
public class Client_Handler implements Runnable {
    private Socket c_socket;            //client_socket used to communicate with the client
    private int id;                     //client_id
    private BufferedReader in;
    private PrintWriter out;
    private Controller controller;      //controller to handle all the commands
    INetworkSerializable ins;           //interface used to deserialize messages from the client


    Client_Handler(Socket clientSocket, int client_id, Controller controller) {
        try {
            this.c_socket = clientSocket;
            this.in = new BufferedReader(new InputStreamReader(c_socket.getInputStream()));
            this.out = new PrintWriter(c_socket.getOutputStream(), true);
            this.controller = controller;
            this.id = client_id;
        } catch (IOException e) {
            System.out.println("unhandled exception in clientHandler's constructor");
        }
    }


    /**
     * This method is the entry point for the ClientHandler class
     * It will 1. ask the client for a username
     *         2. inform the controller of the connection
     *         3. enter a read loop
     */
    @Override
    public void run() {
        out.println("[SERVER] Your client_id is...");
        out.println(id);
        readLoop();
    }

    /**
     * With this method the clientHandler will 1.read messages from the client
     *                                         2. deserialize them
     *                                         3. pass the commands to the controller
     */
    void readLoop() {
        String message;
        do {
            message = receive();
            if (message != null){
                //Command cmd = ins.Deserialize(message);
                //controller.onCommand(cmd);

            }
        } while (message != null);
    }


    /**
     * Method used to receive a message from a client
     * @return the received message
     */
    public String receive() {
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method used to send a message to the client
     * @param packet is the message to send
     */
    public void sendMessage(INetworkSerializable packet){
        //out.println(ins.Serialize(packet));
        out.flush();
    }




}
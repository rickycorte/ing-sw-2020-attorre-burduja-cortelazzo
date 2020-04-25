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
public class ClientHandler implements Runnable, ICommandReceiver {
    private Socket c_socket;
    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<ClientHandler> clients;
    private Controller controller;



    void read() {
        String message;
        do {
            message = receive();
            if (message != null) handleMessage(message);
        } while (message != null);
    }

    /**
     * Handle messages from Client to Server
     * @param message to handle
     */
    void handleMessage(String message){
        //deserialize and convert into a Command
        //send the command to the controller with onCommand(cmd)
    }

    public ClientHandler(Socket clientSocket, ArrayList<ClientHandler> clients){
        try {
            this.c_socket = clientSocket;
            this.clients = clients; //the list of all the clients so that i can broadcast a message to all clients
            in = new BufferedReader(new InputStreamReader(c_socket.getInputStream()));
            out = new PrintWriter(c_socket.getOutputStream(), true);
            controller = new Controller();
        }catch (IOException e){
            System.out.println("unhandled exception in clientHandler");
        }

    }

    @Override
    public void run() {
        out.println("your id?");
        int id = Integer.parseInt(receive());
        out.println("your username");
        String username = receive();
        controller.onConnect(id, username);
        read();

    }
    public String receive() {
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendMessage(String message){
        out.println(message);
        out.flush();
    }

    private void sendMessageToAll(String msg) {
        for(ClientHandler aClient: clients){
            aClient.out.println("Broadcast msg: "+ msg);
        }
    }

    @Override
    public void onConnect(int id, String username) {
        controller.onConnect(id,username);
    }

    @Override
    public void onDisconnect(int id) {
        controller.onDisconnect(id);

    }

    @Override
    public void onCommand(Command cmd) {
        //controller.onCommand(cmd);

    }
}

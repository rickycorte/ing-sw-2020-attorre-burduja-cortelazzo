
package it.polimi.ingsw.network.server;


import com.google.gson.Gson;
import it.polimi.ingsw.controller.CommandType;
import it.polimi.ingsw.controller.CommandWrapper;

import java.io.*;
import java.net.Socket;
import java.util.Timer;

/**
 * Class for the server to handle each client
 *  This represents a connection to a Client
 */
public class Client_Handler implements Runnable {

    private Server server;              //server will handling the commands commands
    private Socket c_socket;            //client_socket used to communicate with the client
    private int id;                     //client_id
    private boolean connected;          //connection flag

    private BufferedReader in;          //in stream
    private PrintWriter out;            //out stream

    private Timer disconnectTimer;      //9 seconds timer - receiving a Base command resets the timer
    private final Object outLock;       //out stream lock


    /**
     * Constructs a Client Handler that will read from / write to a client
     * @param clientSocket socket to a client
     * @param client_id unique identifier for a client
     * @param server server to handle the messages
     * @param outLock out stream lock to synchronize to
     */
    Client_Handler(Socket clientSocket, int client_id, Server server, Object outLock) {
        this.connected = true;
        this.outLock = outLock;
        this.server = server;
        this.c_socket = clientSocket;

        try {
            this.in = new BufferedReader(new InputStreamReader(c_socket.getInputStream()));
            this.out = new PrintWriter(c_socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("[CLIENT_HANDLER] Couldn't create a read/write stream");
        }
        this.id = client_id;
        this.disconnectTimer = new Timer();
    }

    /**
     * This method is the entry point for the Client_Handler class
     * It will receive incoming messages from the client and pass them to the server
     * We assume the Base command as a ping
     */
    @Override
    public void run() {
        out.println(id);
        disconnectTimer.schedule(new DisconnectTask(this), 9000); // schedule a disconnection task in 9 seconds
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String serialized_command = in.readLine();
                if (serialized_command != null) {
                    CommandWrapper command = Deserialize(serialized_command);
                    //System.out.println(serialized_command);

                    if (command.getType() == CommandType.BASE) {
                        //System.out.println("[CLIENT_HANDLER] Disconnection timer reset");
                        rescheduleTimer();
                    } else if (command.getType() == CommandType.JOIN) {
                        server.onConnect(command);
                    } else if (command.getType() == CommandType.LEAVE) {
                        server.onDisconnect(this);
                    } else {
                        server.onCommand(command);
                    }
                }
            } catch (IOException e) {
                System.out.println("[CLIENT_HANDLER] Client no: " + this.id + " has disconnected");
                disconnect();
            }
        }
    }


    /**
     * Reschedules the disconnection timer
     */
    private void rescheduleTimer(){
        disconnectTimer.cancel();
        disconnectTimer = new Timer();
        disconnectTimer.schedule(new DisconnectTask(this), 9000);
    }

    /**
     * This method closes the socket to the client, informs the server and stops the current thread
     */
    void disconnect() {
        if (connected) {
            try {
                if (c_socket.isConnected()) {
                    c_socket.close();
                }
            } catch (IOException e) {
                System.out.println("[CLIENT_HANDLER] ERR! While trying to close the connection");
            }
            connected = false;
            server.onDisconnect(this);
            Thread.currentThread().interrupt();

        }
    }

    /**
     * Method used to send a message to the client
     * @param packet is the message to send
     */
    public void sendMessage(CommandWrapper packet){
        if(connected) {
            synchronized (outLock) {
                out.println(packet.Serialize());
                out.flush();
            }
        }
    }

    /**
     * Method used to deserialize messages send over the network
     * @param message send over the network
     * @return deserialized command wrapper
     */
    public CommandWrapper Deserialize(String message){
        Gson gson = new Gson();
        return gson.fromJson(message, CommandWrapper.class);
    }

    /**
     * gets this client's id
     * @return client's id
     */
    public int getId(){
        return this.id;
    }

    void checkDisconnect(){
        if(this.c_socket.isOutputShutdown())
            System.out.println("[CLIENTHANDLER] Client "+ id +" is not connected anymore");

    }
}
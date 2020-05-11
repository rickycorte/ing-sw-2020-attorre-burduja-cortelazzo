
package it.polimi.ingsw.network.server;


import com.google.gson.Gson;
import it.polimi.ingsw.controller.CommandType;
import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.network.INetworkSerializable;

import java.io.*;
import java.net.Socket;
import java.util.Timer;

/**
 * Class for the server to handle each client
 *  This represents a connection to a Client
 */
public class Client_Handler implements Runnable {

    private boolean connected;

    private Server server;              //server will handle the commands
    private Socket c_socket;            //client_socket used to communicate with the client
    private int id;                     //client_id

    private BufferedReader in;
    private PrintWriter out;

    private Timer disconnectTimer;      //disconnection timer in case i dont receive a ping message in 9 seconds



    /**
     * Constructs a Client Handler that will read from / write to a client
     *
     * @param clientSocket socket to a client
     * @param client_id unique identifier for a client
     * @param server server to handle the messages
     */
    Client_Handler(Socket clientSocket, int client_id, Server server) {
        this.connected = true;

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
     * It will receive incoming messages from the Client
     * we assume the base command as a ping, so when i receive a base command i reschedule the disconnection timer
     * else i pass it to the server
     */
    @Override
    public void run() {
        out.println(id);

        while (!Thread.currentThread().isInterrupted()) {

            try {
                String serialized_command = in.readLine();

                if (serialized_command != null) {
                    CommandWrapper command = Deserialize(serialized_command);

                    if (command.getType() == CommandType.BASE) {
                        //System.out.println("disconnection timer reset");
                        disconnectTimer.cancel();
                        disconnectTimer = new Timer();
                        disconnectTimer.schedule(new DisconnectTask(this), 9000); // schedule a disconnection task in 9 seconds
                    } else if (command.getType() == CommandType.JOIN) {
                        server.handleJoin(command);
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
     * This method closes the socket to the client, informs the server and stops the current thread
     */
    void disconnect() {
        if (connected) {
            try {
                if (c_socket.isConnected()) {
                    c_socket.close();
                }
            } catch (IOException e) {
                System.out.println("[CLIENT_HANDLER] Error while trying to close the connection");
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
    public void sendMessage(INetworkSerializable packet){
        if(connected) {
            out.println(packet.Serialize());
            out.flush();
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
     * @return connection status
     */
    public boolean isConnected(){
        return connected;
    }


    void checkDisconnect(){
        if(this.c_socket.isOutputShutdown())
            System.out.println("[CLIENTHANDLER] Client "+ id +" is not connected anymore");

    }

    public int getId(){
        return this.id;
    }





}
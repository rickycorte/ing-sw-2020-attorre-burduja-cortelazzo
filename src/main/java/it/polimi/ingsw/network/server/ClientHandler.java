
package it.polimi.ingsw.network.server;


import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import it.polimi.ingsw.controller.CommandWrapper;

import java.io.*;
import java.net.Socket;
import java.util.Timer;

/**
 * Class for the server to handle each client
 *  This represents a connection to a Client
 */
public class ClientHandler implements Runnable {

    private final int DISCONNECT_TIMER = 9000;

    private Server server;              //server will handling the commands commands
    private Socket c_socket;            //client_socket used to communicate with the client
    private int id;                     //client_id
    private boolean connected;          //connection flag

    private BufferedReader in;          //in stream
    private PrintWriter out;            //out stream

    private Timer disconnectTimer;      //9 seconds timer - receiving a Base command resets the timer


    /**
     * Constructs a Client Handler that will read from / write to a client
     * @param clientSocket socket to a client
     * @param client_id unique identifier for a client
     * @param server server to handle the messages
     */
    ClientHandler(Socket clientSocket, int client_id, Server server) {
        this.connected = true;
        this.server = server;
        this.c_socket = clientSocket;
        this.id = client_id;
        this.disconnectTimer = new Timer();

        try
        {
            this.in = new BufferedReader(new InputStreamReader(c_socket.getInputStream()));
            this.out = new PrintWriter(c_socket.getOutputStream(), true);
        }
        catch (IOException e)
        {
            System.out.println("[CLIENT_HANDLER] Couldn't create a read/write stream");
        }
    }

    /**
     * This method is the entry point for the ClientHandler class
     * It will receive incoming messages from the client and pass them to the server
     * We assume the Base command as a ping
     */
    @Override
    public void run() {
        out.println(id);
        disconnectTimer.schedule(new DisconnectTask(this), 9000); // schedule a disconnection task in 9 seconds
        while (!Thread.currentThread().isInterrupted() && connected)
        {
            try {
                String serialized_command = in.readLine();
                if (serialized_command != null)
                {
                    handleCommand(serialized_command);
                }
            }
            catch (IOException e)
            {
                System.out.println("[CLIENT_HANDLER] Client " + 1 + " error: "+e.getMessage());
                disconnect();
            }
        }
    }


    /**
     * Deserialize command, and if valid run it on the server
     * @param data data to deserialize
     */
    private void handleCommand(String data)
    {
        CommandWrapper command = Deserialize(data);
        //System.out.println("[CLIENT_HANDLER "+id+"] Got: "+data);
        if(command == null) return; // broken command found

        switch (command.getType())
        {
            case BASE:
                rescheduleTimer();
                break;
            case JOIN:
                server.onConnectEvent(command);
                break;
            case LEAVE:
                server.onDisconnectEvent(this);
                break;
            default:
                server.onCommandEvent(command);
        }
    }

    /**
     * Reschedules the disconnection timer
     */
    private void rescheduleTimer(){
        //System.out.println("[CLIENT_HANDLER] Ping Ok");
        disconnectTimer.cancel();
        disconnectTimer = new Timer();
        disconnectTimer.schedule(new DisconnectTask(this), DISCONNECT_TIMER);
    }

    /**
     * This method closes the socket to the client, informs the server and stops the current thread
     */
    public void disconnect() {
        if (connected)
        {
            System.out.println("[CLIENT_HANDLER] Disconnecting client "+ id);
            try
            {
                if (c_socket.isConnected())
                {
                    c_socket.close();
                }
            }
            catch (IOException e)
            {
                System.out.println("[CLIENT_HANDLER] Error while trying to close the connection");
            }
            connected = false;
            server.onDisconnectEvent(this);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Method used to send a message to the client
     * @param packet is the message to send
     */
    public void sendMessage(CommandWrapper packet){
        if(connected)
        {
            System.out.println("[CLIENT_HANDLER] Sending to "+ id+": " + packet.Serialize());
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
        try
        {
            Gson gson = new Gson();
            return gson.fromJson(message, CommandWrapper.class);
        }
        catch (JsonParseException e)
        {
            System.out.println("[CLIENT_HANDLER] Broken command received");
            return null;
        }
    }

    /**
     * gets this client's id
     * @return client's id
     */
    public int getId(){
        return this.id;
    }
}
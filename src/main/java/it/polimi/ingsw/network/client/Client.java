package it.polimi.ingsw.network.client;


import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class represent a network client and holds all the logic
 * to read/write messages to the server.
 * This class also provides a ping server that is enabled automatically to keep
 * the connection alive with the server
 */

public class Client {

    private int id;                                 //this client's id

    private Socket s_socket;                        //server socket used to read/write

    private BufferedReader in;                      //used to receive messages from server
    private PrintWriter out;                        //used to send messages to the server

    private Timer pingTimer;                        //ping timer used to inform the server i'm still alive
    private ICommandReceiver commandReceiver;       //representing the cli/gui based on client's choice

    boolean alreadyDisconnected;

    final AtomicBoolean shouldStop;


    /**
     * Create a new client
     */
    public Client()
    {
        id = -1;
        pingTimer = new Timer();
        shouldStop = new AtomicBoolean(false);
    }


    /**
     * Connect to a remote server, if connections is created send a join command and start
     * socket read loop in background
     * After joining the control over the calling thread is returned to the caller
     * @param ip server ip
     * @param port server port
     * @param username username used to join
     * @return true if joined, false if some error happened
     */
    public boolean connect(String ip, int port, String username)
    {
        System.out.println("Connecting...");

        try {
            s_socket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(s_socket.getInputStream()));
            out = new PrintWriter(s_socket.getOutputStream(), true);
            alreadyDisconnected = false;

            id = Integer.parseInt(in.readLine()); //client_id given to me by the server
            setUPTimer();

            System.out.println("Connected to server");

            //send proper login request
            send(JoinCommand.makeRequest(id, Server.SERVER_ID, username));

            new Thread(this::runReadLoop).start(); // run connection loop

        }
        catch (IOException e) {
            System.out.println("Can't connect to server: "+ e.getMessage());
            return false;
        }

        return true;
    }


    /**
     * Sets up a task to send ping messages to server every 3 seconds after 1 second delay
     */
    private void setUPTimer(){
        pingTimer.cancel();
        pingTimer = new Timer();
        pingTimer.scheduleAtFixedRate(new PingTask(this), 1000, 3000);
    }

    /**
     * Goes on a read loop, will receive messages from the server and pass them to the ICommandReceiver
     */
    private void runReadLoop() {
        String serverResponse;

        while (!shouldStop.get())
        {
            try
            {
                serverResponse = in.readLine();
                if (serverResponse == null) break;

                //System.out.println("[CLIENT] Got command "+ serverResponse);

                CommandWrapper cmd = deserialize(serverResponse);
                if(commandReceiver == null)
                {
                    System.out.println("[CLIENT] Missing command receiver, nothing will happen");
                    continue;
                }

                switch (cmd.getType())
                {
                    case JOIN:
                        commandReceiver.onConnect(cmd);
                        break;
                    case LEAVE:
                        commandReceiver.onDisconnect(cmd);
                        break;
                    default:
                        commandReceiver.onCommand(cmd);
                }
            }
            catch (IOException e)
            {
                System.out.println("[CLIENT] Couldn't read from the socket, closing connection");
                shutdownConnection();
                alreadyDisconnected = true;
                commandReceiver.onDisconnect(null);
            }
        }
    }


    /**
     * Gets client's id
     * @return client's id
     */
    public int getId(){
        return id;
    }

    /**
     * Deserializes a message received from the server
     * @param message received from the server
     * @return deserialized message to ClientWrapper
     */
    private CommandWrapper deserialize(String message)
     {
        try
        {
            Gson gson = new Gson();
            return gson.fromJson(message, CommandWrapper.class);
        }
        catch (JsonParseException e)
        {
            System.out.println("[CLIENT] Broken command received");
            return null;
        }
    }


    /**
     * Close sockets and stop ping, this does not send anything to the server
     * but kill everything locally
     */
    private void shutdownConnection()
    {
        try
        {
            shouldStop.set(true);
            in.close();
            out.close();
            s_socket.close();
            pingTimer.cancel();
            //System.out.println("[CLIENT] Disconnected");
        }
        catch (IOException e)
        {
            System.out.println("[CLIENT] Error closing the connection");
        }
    }


    /**
     * Disconnect for the current server
     * If not connected nothing happens
     */
    public void disconnect()
    {
        if(!alreadyDisconnected)
            //send leave message
            send(LeaveCommand.makeRequest(id, Server.SERVER_ID));

        // close everything
        shutdownConnection();

    }

    /**
     * Set receiver for commands
     * @param receiver command receiver
     */
    public void setReceiver(ICommandReceiver receiver) {
        this.commandReceiver = receiver;

    }


    /**
     * Remove the command receiver
     */
    public void removeReceiver() {
        this.commandReceiver = null;
    }


    /**
     * Send a command to the server
     * @param packet command to send
     */
    public void send(CommandWrapper packet) {
        if(out == null) return;

        synchronized (out)
        {
            String str = packet.Serialize();
            //if (packet.getType() != CommandType.BASE)
                //System.out.println("[CLIENT] Sending "+ str);

            out.println(str);
            out.flush();
            if(out.checkError())
            {
                shutdownConnection();
                commandReceiver.onDisconnect(null);
            }
        }
    }

}

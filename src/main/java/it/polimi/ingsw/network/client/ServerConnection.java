package it.polimi.ingsw.network.client;


import com.google.gson.Gson;
import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.view.cli.Cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;

/**
 * Class for the client to interact with the server
 *
 */

public class ServerConnection implements Runnable, INetworkAdapter {

    private int id;                                 //this client's id
    private String username;                        //this client's username

    private String host;                            //server's ip address
    private Socket s_socket;                        //server socket used to read/write
    private int port;                               //port on which to connect to

    private BufferedReader in;                      //used to receive messages from server
    private PrintWriter out;                        //used to send messages to the server

    BufferedReader keyboard;                        //used to interract with the client

    private Timer pingTimer;                        //ping timer used to inform the server i'm still alive
    private ICommandReceiver commandReceiver;       //representing the cli/gui based on client's choice

    /**
     * ServerConnection constructor
     * @param server_socket server's socket to connect to
     */
    ServerConnection(Socket server_socket, String host) {
        this.host = host;
        s_socket = server_socket;
        this.port = server_socket.getPort();
        pingTimer = new Timer();
        this.username = null;
        try {
            in = new BufferedReader(new InputStreamReader(s_socket.getInputStream()));
            out = new PrintWriter(s_socket.getOutputStream(), true);
            keyboard = new BufferedReader(new InputStreamReader(System.in));
        } catch (IOException e) {
            System.out.println("[SERVER_CONNECTION] Couldn't create read/write streams");
        }
    }

    /**
     * This is the entry point of the ServerConnection class
     * this method receives the id, starts a ping task at fixed rate, asks for a graphic interface and enters a readLoop
     */
    @Override
    public void run() {
        String serverResponse = null;

        try {
            serverResponse = in.readLine(); //client_id given to me by the server
        } catch (IOException e) {
            Disconnect();
        }
        System.out.println("[SERVER_CONNECTION] My client id is: " + serverResponse);
        int id = Integer.parseInt(serverResponse);
        setId(id);

        setUPTimer();

        System.out.println("[SERVER_CONNECTION] Enter username");
        try {
            username = keyboard.readLine();
        } catch (IOException e) {
            System.out.println("[SERVER_CONNECTION] ERR! Couldn't read keyboard input");
        }

        int interface_choice = 0;
        try {

            do {
                System.out.println("Choose an interface\n");
                System.out.println(("Type 1 for CLI, 2 for GUI\n"));
                interface_choice = Integer.parseInt(keyboard.readLine());
            } while (interface_choice != 1 && interface_choice != 2);
        } catch (IOException e) {
            System.out.println("[SERVER_CONNECTION] ERR! wrong input");
        }
        if (interface_choice == 1) {
            commandReceiver = new Cli(this);
        } else {
            //Start the GUI with this adapter
        }
        readLoop();
    }

    /**
     * Sets up a task to send ping messages to server every 3 seconds after 1 second delay
     */
    void setUPTimer(){
        pingTimer.cancel();
        pingTimer = new Timer();
        pingTimer.scheduleAtFixedRate(new PingTask(this), 1000, 3000);
    }

    /**
     * Goes on a read loop, will receive messages from the server and pass them to the ICommandReceiver
     */
    public void readLoop() {
        String serverResponse = null;
        Send(new CommandWrapper(CommandType.JOIN, new JoinCommand(this.id, getServerID(), username, true)));

        boolean condition = true;
        while (condition) {
            try {
                serverResponse = in.readLine();
                if (serverResponse == null) break;
                CommandWrapper cmd = Deserialize(serverResponse);
                //System.out.println(serverResponse);
                //System.out.println("[SERVER_CONNECTION] Received a message");
                if(itsForMe(cmd)) {
                    //System.out.println("[SERVER_CONNECTION] Message was directed to me");
                    if (cmd.getType() == CommandType.JOIN)
                        commandReceiver.onConnect(cmd);
                    else if (cmd.getType() == CommandType.LEAVE)
                        commandReceiver.onDisconnect(cmd);
                    else
                        commandReceiver.onCommand(cmd);
                }else {
                    //System.out.println("[SERVER_CONNECTION] But it wasn't for me");
                }
            } catch (IOException e) {
                System.out.println("[SERVER_CONNECTION] Couldn't read from the socket, closing connection");
                Disconnect();
            }
        }
    }


    /**
     * Checks if the incoming message it's for me
     * @param commandWrapper received message
     * @return true if it's for me or broadcast
     */
    private boolean itsForMe(CommandWrapper commandWrapper){
        int command_target = getCommandTarget(commandWrapper);
        int my_id = getId();
        int broadcast_id = getBroadCastID();

        if(command_target == my_id || command_target == broadcast_id) return true;
        return false;
    }

    /**
     * Gets command's target id
     * @param cmd command
     * @return command's target id
     */
    private int getCommandTarget(CommandWrapper cmd){
        return cmd.getCommand(BaseCommand.class).getTarget();
    }

    /**
     * Setts the id for this connection
     * @param client_id client's id given to me by the server
     */
    void setId(int client_id){
        this.id = client_id;
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
    public CommandWrapper Deserialize(String message){
        Gson gson = new Gson();
        return gson.fromJson(message, CommandWrapper.class);
    }

    //Methods that implement INetworkAdapter

    @Override
    public void StartServer(int port) {
        //do nothing here, Server method
    }

    @Override
    public void StopServer() {
        //do nothing here, Server method
    }

    @Override
    public void Connect(String ip, int port, String username) {

    }

    @Override
    public void Disconnect() {
        try {
            in.close();
            out.close();
            s_socket.close();
            System.out.println("[SERVER_CONNECTION] Connection was closed");
        }catch (IOException e){
            System.out.println("[SERVER_CONNECTION] Error in closing the connection");
        }

    }

    @Override
    public void AddReceiver(ICommandReceiver receiver) {
        this.commandReceiver = receiver;

    }

    @Override
    public ICommandReceiver getReceiver() {
        return this.commandReceiver;
    }

    @Override
    public void RemoveReceiver() {
        this.commandReceiver = null;
    }


    @Override
    public void Send(CommandWrapper packet) {
        if(out != null) {
            if(packet.getType() != CommandType.BASE) System.out.println("[SERVER_CONNECTION] Sending " + packet.getType().name() + " command");
            out.println(packet.Serialize());
            out.flush();
        }
    }

    @Override
    public int getServerID() {
        return -1111;
    }

    @Override
    public int getBroadCastID() {
        return -2222;
    }
}

package it.polimi.ingsw.network.client;


import com.google.gson.Gson;
import it.polimi.ingsw.controller.Command;
import it.polimi.ingsw.controller.CommandType;
import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.INetworkSerializable;
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
    private String host;   //server's ip address
    private int id;
    private Socket s_socket;
    private int port;
    private BufferedReader in;
    private PrintWriter out;
    BufferedReader keyboard;
    private Timer pingTimer;
    private Cli cli;
    //private Gui gui;

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
            keyboard = new BufferedReader(new InputStreamReader(System.in));               //handle user input
            pingTimer = new Timer();
        }catch (IOException e){
            System.out.println("unhandled exception in ServerConnection constructor");
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
            serverResponse = in.readLine(); // this is the client_id given to me by the server
        } catch (IOException e) {
            close();
        }
        System.out.println("My client_id is "+ serverResponse);
        int id = Integer.parseInt(serverResponse);
        setId(id);
        pingTimer.cancel();
        pingTimer = new Timer();
        pingTimer.scheduleAtFixedRate(new PingTask(this), 1000, 3000);
        int interface_choice = 0;
        try {
            do {
                System.out.println("Choose an interface\n");
                System.out.println(("Type 1 for CLI, 2 for GUI\n"));
                interface_choice = Integer.parseInt(keyboard.readLine());
            } while (interface_choice != 1 && interface_choice != 2);

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(interface_choice == 1){
            this.cli = new Cli(this);
        }
        else{
            //Start the GUI with this adapter
        }
        readLoop();
    }


    /**
     * This method is a client read loop
     * it will receive messages from the server and pass them to the graphic interface
     */
    public void readLoop(){
        String serverResponse = null;
        boolean condition = true;
        while (condition) {
            try {
                serverResponse = in.readLine();
                if (serverResponse == null) break;
                CommandWrapper cmd = Deserialize(serverResponse);
                if(cli != null){
                    cli.onCommand(cmd);
                }else{
                    //gui.onCommand(cmd);
                }

            }catch (IOException e){
                System.out.println("[SERVER_CONNECTION] Couldn't read from the socket, closing connection");
                close();
            }
        }

    }

    /**
     * Method used to send a message to the server
     * @param message to send
     */
    public void send(String message) {
        if(out != null) {
            out.println(message);
            out.flush();
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
            System.out.println("connection was closed");
        }catch (IOException e){
            System.out.println("[SERVERCONNECTION] error in closing the connection");
        }
    }


    void setId(int client_id){
        this.id = client_id;
    }

    public int getId(){
        return id;
    }

    public CommandWrapper Deserialize(String message){
        Gson gson = new Gson();
        return gson.fromJson(message, CommandWrapper.class);
    }


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

    }

    @Override
    public void AddReceiver(ICommandReceiver receiver) {

    }

    @Override
    public void RemoveReceiver(ICommandReceiver receiver) {

    }

    @Override
    public void Send(int id, INetworkSerializable packet) {
        out.println(packet.Serialize());
        out.flush();
    }

    @Override
    public void SendBroadcast(INetworkSerializable packet) {

    }
}

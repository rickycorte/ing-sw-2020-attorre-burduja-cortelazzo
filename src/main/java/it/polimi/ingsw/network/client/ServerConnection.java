package it.polimi.ingsw.network.client;


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
    private String host;
    private Socket s_socket;
    private int port;
    private BufferedReader in;
    private PrintWriter out;
    BufferedReader keyboard;


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
     * Waits for the server to say something and then displays it
     */
    @Override
    public void run() {
        String serverResponse = null;

        while (true) {
            serverResponse = receive();
            if (serverResponse == null) break;
            System.out.println("Server says: " + serverResponse);
        }
        close();

    }



    /**
     * Method used to send a message
     * @param message to send
     */
    public void send(String message) {
        out.println(message);
    }

    /**
     * Method to receive string messages
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

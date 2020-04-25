package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.Command;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.INetworkSerializable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.*;


/**
 * This class is the main server which starts a socket
 * Handles the clients
 */
public class Server {
    private static int port;
    private ServerSocket s_socket;
    private static ArrayList<ClientHandler> clients;
    private static ExecutorService pool; //max num of connected clients

    /**
     * Server constructor
     * @param port to which the clients should connect
     * @param n_players max number of clients that can connect
     */
    private Server(int port, int n_players){
        this.port = port;
        this.clients = new ArrayList<>();
        pool = Executors.newFixedThreadPool(n_players);


    }


    /**
     * Method used to start a connection to a given port
     * @param port int representing the exact port
     * @return ServerSocket used to communicate with the client
     */
    public static ServerSocket startServer(int port){
        System.out.println("[SERVER] Starting...");
        ServerSocket listener = null;
        try{
            listener = new ServerSocket(port);

        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("[SERVER] Started!");
        System.out.println("[SERVER] Listening on: " + port +" port");
        return listener;

    }

    /**
     * Method used to close the server
     */
    public void stopServer(){
        try {
            s_socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     * Represents the behaviour of a server
     * Waits on clients to connect and handles each connection in a diff thread
     */
    private void serverBehaviour() {
        int id = 0;
        s_socket = startServer(port);
        System.out.println("[SERVER] Waiting for clients...");
        while (true) {
            Socket c_socket = null;
            try {

                c_socket = s_socket.accept();
                ClientHandler clientHandler = new ClientHandler(c_socket, clients);
                clients.add(clientHandler);
                pool.execute(clientHandler); //invoke clientHandler run method in a new thread
                System.out.println("[SERVER] Connected to client");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(s_socket.isClosed()) break;
        }
    }




    public static void main(String[] args) {
        System.out.println("Welcome to the Santorini server!");

        System.out.println("Choose a port\n");
        Scanner input = new Scanner(System.in);
        int port = Integer.parseInt(input.next());

        System.out.println("For how many Players?");
        int max_n_players = Integer.parseInt(input.next());
        //TODO there can be only 2 or 3 players

        Server server = new Server(port, max_n_players);
        try {
            server.serverBehaviour();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.out.println("[SERVER] Shutting down...");
            server.stopServer();
        }
    }



}

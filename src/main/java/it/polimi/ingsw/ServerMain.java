package it.polimi.ingsw;


import it.polimi.ingsw.network.server.Server;

public class ServerMain {

    private static Server my_server;


    public static void main(String[] args) {
        //Network my_network = new Network();
        my_server = new Server();
        //Controller my_controller = new Controller(my_server);
        //my_server.AddReceiver(my_controller);
        my_server.StartServer(16000);


    }

}

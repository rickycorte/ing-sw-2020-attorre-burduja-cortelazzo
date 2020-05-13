package it.polimi.ingsw;


import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.server.Server;

public class ServerMain {

    public static void main(String[] args) {
        Server my_server = new Server();
        ICommandReceiver my_controller = new Controller(my_server);
        my_server.AddReceiver(my_controller);
        my_server.StartServer(16000);


    }

}

package it.polimi.ingsw;


import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.TPCNetwork;

import java.util.Scanner;

/**
 * Main class that starts the server
 */
public class ServerMain {

    public static void main(String[] args)
    {

        INetworkAdapter tcpNetwork = new TPCNetwork();
        ICommandReceiver my_controller = new Controller(tcpNetwork);

        tcpNetwork.setReceiver(my_controller);

        tcpNetwork.startServer();
        String input;
        Scanner scn = new Scanner(System.in);
        System.out.println("Type 'q' to kill the server!");
        do
        {
            input = scn.nextLine();
            if(input.equals("q"))
            {
                tcpNetwork.stopServer();
                break;
            }
        } while (true);

        System.exit(0); // kill everything
    }

}

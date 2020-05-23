package it.polimi.ingsw;


import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.TPCNetwork;
import it.polimi.ingsw.network.matchmaking.VirtualMatchmaker;

import java.util.Scanner;

/**
 * Main class that starts the server
 */
public class ServerMain {

    public static void main(String[] args)
    {
        VirtualMatchmaker matchmaker = new VirtualMatchmaker();
        matchmaker.start();

        String input;
        Scanner scn = new Scanner(System.in);
        System.out.println("Type 'q' to kill the server!");
        do
        {
            input = scn.nextLine();
            if(input.equals("q"))
            {
                matchmaker.stop();
                break;
            }
        } while (true);

        System.exit(0); // kill everything
    }

}

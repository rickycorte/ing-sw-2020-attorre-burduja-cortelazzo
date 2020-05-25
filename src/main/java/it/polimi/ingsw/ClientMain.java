package it.polimi.ingsw;

import it.polimi.ingsw.view.gui.GuiInterface;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.TPCNetwork;
import it.polimi.ingsw.view.IHumanInterface;
import it.polimi.ingsw.view.cli.Cli;

import java.util.Scanner;

/**
 * Main class that starts Client application
 */
public class ClientMain {

    public static void main(String[] args)
    {

        INetworkAdapter tcpNetwork = new TPCNetwork();
        IHumanInterface hud;
        int choice = 1;
        Scanner scn = new Scanner(System.in);

        do{
            System.out.println("Type 1 for CLI and 2 for GUI:");
            choice = scn.nextInt();
        }while ( choice < 1 || choice > 2);

        //create gui/cli
        if(choice == 1)
        {
          hud = new Cli(tcpNetwork);
        }
        else
        {
            hud = new GuiInterface(tcpNetwork);
        }

        tcpNetwork.setReceiver(hud);

        hud.start();

        System.exit(0); // kill everything
    }
}

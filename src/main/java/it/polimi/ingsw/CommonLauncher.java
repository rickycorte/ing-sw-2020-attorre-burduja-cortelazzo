package it.polimi.ingsw;

import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.TPCNetwork;
import it.polimi.ingsw.network.matchmaking.VirtualMatchmaker;
import it.polimi.ingsw.network.server.Server;
import it.polimi.ingsw.view.IHumanInterface;
import it.polimi.ingsw.view.cli.Cli;
import it.polimi.ingsw.view.gui.GuiInterface;

public class CommonLauncher
{


    public static void launchClient(boolean isGui)
    {

        INetworkAdapter tcpNetwork = new TPCNetwork();
        IHumanInterface hud;

        //create gui/cli
        if(isGui)
        {
            hud = new GuiInterface(tcpNetwork);
        }
        else
        {
            hud = new Cli(tcpNetwork);
        }

        tcpNetwork.setReceiver(hud);

        hud.start();
    }


    public static void launchServer(boolean hasCustomPort, int port)
    {
        VirtualMatchmaker matchmaker = new VirtualMatchmaker();
        if (hasCustomPort)
        {
            matchmaker.startSync(port);
        }
        else
        {
            matchmaker.startSync();
        }
    }



    public static void main(String[] args)
    {

        //by default start gui
        if(args.length == 0)
        {
            launchClient(true);
            System.exit(0);
        }
        else if(args.length == 1)
        {
            //start server
            if(args[0].equals("-s"))
            {
                launchServer(false, -1);
                System.exit(0);
            }
            //start cli
            if(args[0].equals("-cli"))
            {
                launchClient(false);
                System.exit(0);
            }
        }
        else if(args.length == 2)
        {
            //start server with port
            if(args[0].equals("-s"))
            {
                try
                {
                    int port = Integer.parseInt(args[1]);
                    launchServer(true, port);
                    System.exit(0);
                }
                catch (Exception e)
                {
                    System.out.println("Error: "+e.getMessage());
                }
            }
        }

        System.out.println("Use:\n" +
                "-s [<port>]     To start server [with a custom port]\n" +
                "-cli            To start a client with cli interface\n\n" +
                "If no parameter is specified a GUI client is started");
    }

}

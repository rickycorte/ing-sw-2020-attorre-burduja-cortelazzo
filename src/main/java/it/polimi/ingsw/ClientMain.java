package it.polimi.ingsw;

import it.polimi.ingsw.network.client.*;
import it.polimi.ingsw.view.cli.Cli;

public class ClientMain {

    public static void main(String[] args) {
        Client client = new Client();
        client.connect("127.0.0.1", 16000);
    }
}

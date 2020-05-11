package it.polimi.ingsw.network.server;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Task that disconnects the client
 */
public class DisconnectTask extends TimerTask {

    private Client_Handler client_handler;

    DisconnectTask(Client_Handler client_handler){
        super();
        this.client_handler = client_handler;
    }
    @Override
    public void run() {
        client_handler.disconnect();

    }
}

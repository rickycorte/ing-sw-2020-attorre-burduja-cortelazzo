package it.polimi.ingsw.network.server;

import java.util.TimerTask;


/**
 * Task that disconnects the client
 */
public class DisconnectTask extends TimerTask {

    private ClientHandler client_handler;

    DisconnectTask(ClientHandler client_handler){
        super();
        this.client_handler = client_handler;
    }
    
    @Override
    public void run() {
        System.out.println("[TIMER] Timeout reached for client " + client_handler.getId());
        client_handler.disconnect();
    }
}

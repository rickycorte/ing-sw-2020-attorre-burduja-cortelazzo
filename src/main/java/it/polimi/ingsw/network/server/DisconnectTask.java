package it.polimi.ingsw.network.server;

import java.util.TimerTask;


/**
 * Task that disconnects the client
 */
public class DisconnectTask extends TimerTask {

    private ClientHandler client_handler;

    /**
     * Create a new disconnect timer task that should be called when a player disconnects by timeout
     * @param clientHandler player handler
     */
    DisconnectTask(ClientHandler clientHandler){
        super();
        this.client_handler = clientHandler;
    }

    /**
     * This function is called when the timeout expire and handle the disconnection
     */
    @Override
    public void run() {
        System.out.println("[TIMER] Timeout reached for client " + client_handler.getId());
        client_handler.sendClientTimeout();
    }
}

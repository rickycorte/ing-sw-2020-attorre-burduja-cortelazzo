package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.CommandType;
import it.polimi.ingsw.controller.CommandWrapper;

import java.util.TimerTask;


/**
 * Thread that sends a base command
 */
public class PingTask extends TimerTask {

    private ServerConnection serverConnection;

    PingTask(ServerConnection serverConnection){
        super();
        this.serverConnection = serverConnection;
    }


    /**
     * This method sends the Base command (is interpreted as a ping message)
     */
    @Override
    public void run() {
        serverConnection.send(new CommandWrapper(CommandType.BASE, null).Serialize());
    }
}

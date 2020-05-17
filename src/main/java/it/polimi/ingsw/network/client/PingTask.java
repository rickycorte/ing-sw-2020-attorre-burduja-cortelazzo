package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.CommandType;
import it.polimi.ingsw.controller.CommandWrapper;

import java.util.TimerTask;


/**
 * Thread that sends a base command
 */
public class PingTask extends TimerTask {

    private Client client;

    PingTask(Client client){
        super();
        this.client = client;
    }


    /**
     * This method sends the Base command (is interpreted as a ping message)
     */
    @Override
    public void run() {
        client.send(new CommandWrapper(CommandType.BASE, null));
    }
}

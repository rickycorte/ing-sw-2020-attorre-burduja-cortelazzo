package it.polimi.ingsw.controller.compact;

import it.polimi.ingsw.game.Vector2;
import it.polimi.ingsw.game.Worker;

/**
 * This is a data only class that represent a worker with its essential data
 * and its designed to be sent over the network
 */
public class CompactWorker
{
    int ownerID;
    int workerID;
    Vector2 position;

    public CompactWorker(Worker worker)
    {
        ownerID = worker.getOwner().getId();
        workerID = worker.getId();
        position = worker.getPosition();
    }

    /**
     * Get the client id of the owner of this worker
     * @return owner id
     */
    public int getOwnerID()
    {
        return ownerID;
    }

    /**
     * Get the worker id
     * @return worker id
     */
    public int getWorkerID()
    {
        return workerID;
    }

    /**
     * Get worker position
     * @return worker position
     */
    public Vector2 getPosition()
    {
        return position;
    }
}

package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * Command used to request the placement of workers in the map
 * (Server)
 * This command is used to ask a player to select position where they want to place workers from the provided list
 * (Client)
 * Reply to the server with the selected positions where workers should be placed
 */
public class WorkerPlaceCommand extends BaseCommand {
    private Vector2[] positions;


    /**
     * (Server) Create a reply for the server with the positions selected
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param cellWithoutWorker allowed position where warkers can be placed
     */
    public WorkerPlaceCommand(int sender, int target, List<Vector2> cellWithoutWorker) {
        super( sender, target);
        this.positions = cellWithoutWorker.toArray(new Vector2[0]);
    }

    /**
     * (Client) Create a command to inform a client that can place workers
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param positions positions where you want to place the workers
     */
    public WorkerPlaceCommand(int sender, int target, Vector2[] positions) {
        super(sender, target);
        this.positions = positions;
    }


    /**
     * Return the positions stored in the command
     * @return array of positions
     */
    public Vector2[] getPositions() {
        return positions;
    }


    /**
     * @deprecated use {@link #getPositions()}.size()
     */
    @Deprecated
    public List<Integer> getPositionsIndexes(){
        List<Integer> availableIndexes = new ArrayList<>();
        for(int i = 0; i<positions.length; i++){
            availableIndexes.add(i);
        }
        return availableIndexes;
    }

    /**
     * Create a new Worker place command with the supplied positions
     * This functions works both on client and server because they share the same type of parameter
     * so the is no need to have different functions
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param positions allowed position where workers can be placed
     * @return wrapper command ready to be sent over the network
     */
    public static CommandWrapper makeWrapped(int sender, int target, Vector2[] positions)
    {
        return new CommandWrapper(CommandType.PLACE_WORKERS, new WorkerPlaceCommand(sender,target, positions));
    }

    /**
     * Create a new Worker place command with the supplied positions
     * This functions works both on client and server because they share the same type of parameter
     * so the is no need to have different functions
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param positions allowed position where workers can be placed
     * @return wrapper command ready to be sent over the network
     */
    public static CommandWrapper makeWrapped(int sender, int target, List<Vector2> positions)
    {
        return new CommandWrapper(CommandType.PLACE_WORKERS, new WorkerPlaceCommand(sender,target, positions));
    }


}

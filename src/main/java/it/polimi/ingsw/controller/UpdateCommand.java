package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.compact.CompactMap;
import it.polimi.ingsw.game.Map;
import it.polimi.ingsw.game.Vector2;

/**
 * This command is used only as an information method
 * that updates the clients with a new state of the game map is calculated
 * (Server)
 * Send a new map state to the clients
 * (Client)
 * A new map state is received and should be processed
 */
public class UpdateCommand extends BaseCommand {
    private CompactMap map;

    /**
     * (Server) Create a new map update to send to the clients
     * @param sender sender id
     * @param target receiver if of the command (should be broadcast)
     * @param map new map state
     */
    public UpdateCommand(int sender, int target, Map map){
        super(sender,target);
        this.map = new CompactMap(map);
    }


    /**
     * Get the new map sent by th server
     * @return new map state
     */
    public CompactMap getUpdatedMap()
    {
        return map;
    }

    /**
     * (Server) Create a new map update to send to the clients
     * @param sender sender id
     * @param target receiver if of the command (should be broadcast)
     * @param map new map state
     * @return wrapped command ready to be sent over the network
     */
    public static CommandWrapper makeWrapped(int sender, int target, Map map)
    {
        return new CommandWrapper(CommandType.UPDATE, new UpdateCommand(sender, target, map));
    }

}

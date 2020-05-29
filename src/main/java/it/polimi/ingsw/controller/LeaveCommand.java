package it.polimi.ingsw.controller;

import it.polimi.ingsw.network.server.Server;

/**
 * Command used by the client to inform the server that it wants to leave the current match
 * (Server)
 * Never sent
 * (Client)
 * Inform the server that the client wants to disconnect
 */
public class LeaveCommand extends BaseCommand
{

    public LeaveCommand(int sender, int target)
    {
        //FROM CLIENT
        super(sender, target);
    }

    /**
     * (Client) Create a new leave command to send to the server wrapped inside a CommandWrapper
     * By default the target is set to server id
     * @param sender sender of the command (current client id)
     * @param target target id
     * @return wrapped leave command
     */
    public static CommandWrapper makeRequest(int sender, int target)
    {
        return new CommandWrapper(CommandType.LEAVE, new LeaveCommand(sender, target));
    }
}

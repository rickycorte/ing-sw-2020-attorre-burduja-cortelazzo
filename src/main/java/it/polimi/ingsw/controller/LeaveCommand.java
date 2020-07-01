package it.polimi.ingsw.controller;

import it.polimi.ingsw.network.server.Server;

/**
 * Command used by the client to inform the server that it wants to leave the current match
 * (Server)
 * Informs remaining players of a disconnect
 * (Client)
 * Inform the server that the client wants to disconnect
 */
public class LeaveCommand extends BaseCommand
{
    int numberRemainingPlayers;
    int leftPlayerID;
    int newHostPlayerID;

    /**
     * (Client) Request the server to quit the current match
     * @param sender sender of the command (current client id)
     * @param target target id (server id)
     */
    public LeaveCommand(int sender, int target)
    {
        //FROM CLIENT
        super(sender, target);
        //numberRemainingPlayers = -1;
    }

    /**
     * (Server) Inform clients in a lobby that someone left and there could be a new host
     * @param sender sender of the message
     * @param target target of the message
     * @param leftID id of the player that left the game
     * @param newHostID id of the player that became the new host
     * @param remainingPlayers number of players that are still waiting in the lobby
     */
    public LeaveCommand (int sender, int target,int leftID, int newHostID , int remainingPlayers){
        //FROM SERVER
        super(sender,target);
        leftPlayerID = leftID;
        newHostPlayerID = newHostID;
        numberRemainingPlayers = remainingPlayers;
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

    /**
     * (Server - LOBBY ONLY) Create a leave command that informs the remaining players of a disconnection
     * @param sender sender id (server)
     * @param target target id (broadcast)
     * @param leftID id of the player who left the lobby
     * @param newHostID player id of the new host of the lobby (can also be the old one)
     * @param numberRemainingPlayers number of remaining players in the game
     * @return wrapped leave command
     */
    public static CommandWrapper makeReply(int sender, int target,int leftID, int newHostID ,int numberRemainingPlayers){
        return new CommandWrapper(CommandType.LEAVE, new LeaveCommand(sender, target, leftID, newHostID ,  numberRemainingPlayers));
    }

    /**
     * Gets the number or remaining players in the game
     * @return number of remaining players
     */
    public int getNumberRemainingPlayers(){
        return numberRemainingPlayers;
    }

    /**
     * Gets the ID of the player that has disconnected
     * @return ID of the player that has disconnected
     */
    public int getLeftPlayerID(){
        return leftPlayerID;
    }

    /**
     * Gets the ID of the new host player
     * @return ID of the new host player
     */
    public int getNewHostPlayerID(){
        return newHostPlayerID;
    }
}

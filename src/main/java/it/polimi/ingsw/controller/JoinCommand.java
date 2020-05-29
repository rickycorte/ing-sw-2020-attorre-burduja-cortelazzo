package it.polimi.ingsw.controller;

import it.polimi.ingsw.network.server.Server;

/**
 * Command used to join a game
 * (Server)
 * Reply to the client to let it know if it has joined a match or its request was rejected
 * (Client)
 * Request the server to connect and join a game
 */
public class JoinCommand extends BaseCommand {
    private String username;
    private boolean isJoin;
    private boolean validUsername;         //Flag in case username is not valid
    private int hostPlayerID;

    /**
     * (Server) Send to the client a reply
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param isSuccess set to true if login is successful
     * @param validUsername set to true if username is valid
     * @param hostPlayerID id of the game host
     */
    public JoinCommand(int sender, int target, boolean isSuccess, boolean validUsername, int hostPlayerID){
        //UPDATE FROM SERVER
        super(sender,target);
        this.username = null;
        this.isJoin = isSuccess;
        this.validUsername = validUsername;
        this.hostPlayerID = hostPlayerID;
    }

    /**
     * (Client) Send a join request to the server
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param username username of the player
     * @param isJoin set to true to mark this command as a join request
     */
    public JoinCommand(int sender, int target, String username, boolean isJoin) {
        //REQUEST FROM CLIENT
        super(sender, target);
        this.username = username;
        this.isJoin = isJoin;
        this.validUsername = true;
    }

    /**
     * Return stored username
     * @return player username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Return true is this is a join request
     * @return true is join request
     */
    public boolean isJoin() {
        return isJoin;
    }

    /**
     * Return true if the stored username is valid
     * @return true if username is valid
     */
    public boolean isValidUsername(){ return validUsername; }

    /**
     * Return the host if of the joined match
     * @return host id
     */
    public int getHostPlayerID() { return hostPlayerID; }


    /**
     * (Client) Create a join request to send to the server wrapped into a CommandWrapper
     * @param sender sender id (current client id)
     * @param target target id
     * @param username username to use to login
     * @return wrapped join command ready to send over the network
     */
    public static CommandWrapper makeRequest(int sender, int target , String username)
    {
        return new CommandWrapper(CommandType.JOIN, new JoinCommand(sender, target, username, true));
    }


    /**
     * (Server) Create a join reply (successfull/rejected login) to send to the client
     * The command also include information about the owner of the joined game
     * @param sender sender id
     * @param target client id that should receive this command
     * @param isSuccess true if join is successful
     * @param isValidUsername true is username is valid
     * @param matchOwnerID client id of the owner of the match joined by target client
     * @return wrapped join command ready to be sent over network
     */
    public static CommandWrapper makeReply(int sender, int target, boolean isSuccess, boolean isValidUsername ,int matchOwnerID)
    {
        return new CommandWrapper(CommandType.JOIN, new JoinCommand(sender, target, isSuccess, isValidUsername, matchOwnerID));
    }

    /**
     * (Server) Create a successful join reply to send to the client
     * The command also include information about the owner of the joined game
     * @param sender sender id
     * @param target client id that should receive this command
     * @param matchOwnerID client id of the owner of the match joined by target client
     * @return wrapped join command ready to be sent over network
     */
    public static CommandWrapper makeReplyOk(int sender, int target ,int matchOwnerID)
    {
        return makeReply(sender, target, true, true, matchOwnerID);
    }

    /**
     * (Server) Create a rejected join reply to send to the client
     * The command also include information about the owner of the joined game
     * @param sender sender id
     * @param target client id that should receive this command
     * @param isValidUsername true if username is invalid
     * @return wrapped join command ready to be sent over network
     */
    public static CommandWrapper makeReplyFail(int sender, int target, boolean isValidUsername)
    {
        return makeReply(sender, target, false, isValidUsername, -1);
    }


}

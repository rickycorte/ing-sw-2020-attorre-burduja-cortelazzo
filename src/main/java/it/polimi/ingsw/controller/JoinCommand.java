package it.polimi.ingsw.controller;

/**
 * Command used to join a game
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
}

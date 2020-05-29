package it.polimi.ingsw.controller;

/**
 * Command used to select gods
 * (Server)
 * Request a client to pick a god from the provided list
 * (Client)
 * Send the server the god id that should be picked
 */
public class PickGodCommand extends BaseCommand {
    private int[] godsID;


    /**
     * (Server) Send a list of available gods to chose from
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param godsID array of available gods
     */
    public PickGodCommand(int sender, int target,int[] godsID) {
        super(sender, target);
        this.godsID = godsID;
    }


    /**
     * (Client) Inform the server about the selected god
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param godID selected god
     */
    public PickGodCommand(int sender, int target,int godID) {
        super(sender, target);
        this.godsID = new int[]{godID};
    }

    /**
     * Store array of god ids
     * @return god ids array
     * @deprecated use {@link #getAllowedGodsIDS()}
     */
    @Deprecated
    public int[] getGodID() {
        return godsID;
    }

    /**
     * Return stored array of allowed god ids
     * @return array of allowed god ids
     */
    public int[] getAllowedGodsIDS()
    {
        return godsID;
    }

    /**
     * Return picked god id (equivalent to {@link #getAllowedGodsIDS()}[0])
     * @return picked god id
     */
    public int getPickedGodID()
    {
        return godsID[0];
    }

    /**
     * (Server) Send a list of available gods to chose from
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param godsID array of available gods
     * @return wrapped command ready to be sent over the network
     */
    public static CommandWrapper makeRequest(int sender, int target,int[] godsID)
    {
        return new CommandWrapper(CommandType.PICK_GOD, new PickGodCommand(sender, target, godsID));
    }

    /**
     * (Client) Inform the server about the selected god
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param pickedGod selected god
     * @return wrapped command ready to be sent over the network
     */
    public static CommandWrapper makeReply(int sender, int target,int pickedGod)
    {
        return new CommandWrapper(CommandType.PICK_GOD, new PickGodCommand(sender, target, pickedGod));
    }

}

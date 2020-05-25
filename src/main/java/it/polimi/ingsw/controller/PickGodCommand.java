package it.polimi.ingsw.controller;

/**
 * Command used to select gods
 */
public class PickGodCommand extends BaseCommand {
    private int[] godsID;


    /**
     * (Sever) Send a list of available gods to chose from
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
     */
    public int[] getGodID() {
        return godsID;
    }

}

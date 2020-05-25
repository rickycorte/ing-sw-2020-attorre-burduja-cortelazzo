package it.polimi.ingsw.controller;

/**
 * Command used to set available gods for a match
 */
public class FilterGodCommand extends BaseCommand {
    private int[] godsID;


    /**
     * (Server) Request the host to chose gods to use in the match
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     */
    public FilterGodCommand(int sender, int target) {
        super(sender, target);
        this.godsID = null;
    }


    /**
     * (Client) Send the god ids allowed for a match
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param godsID array of selected god ids
     */
    public FilterGodCommand(int sender, int target,int[] godsID) {
        super(sender, target);
        this.godsID = godsID;
    }

    /**
     * Return store god ids array
     * @return god ids array
     */
    public int[] getGodID() {
        return godsID;
    }

}

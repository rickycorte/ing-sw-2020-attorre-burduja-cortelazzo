package it.polimi.ingsw.controller;

/**
 * Command used to set available gods for a match
 * (Server)
 * Request a client to chose allowed gods for the match
 * (Client)
 * Reply yo the server with the list of allowed god ids
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
     * @deprecated use {@link #getGodFilter()} instead.
     */
    @Deprecated
    public int[] getGodID() {
        return godsID;
    }


    /**
     * Return store god ids array
     * @return god ids array
     */
    public int[] getGodFilter()
    {
        return godsID;
    }


    /**
     * (Server) Request the host to chose gods to use in the match
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @return wrapped command ready to be sent over the network
     */
    public static CommandWrapper makeRequest(int sender, int target)
    {
        return new CommandWrapper(CommandType.FILTER_GODS, new FilterGodCommand(sender, target));
    }

    /**
     * (Client) Send the god ids allowed for a match
     * @param sender sender id of who is issuing this command
     * @param target receiver of the command
     * @param filter array of selected god ids
     * @return wrapped command ready to be sent over the network
     */
    public static CommandWrapper makeReply(int sender, int target,int[] filter)
    {
        return new CommandWrapper(CommandType.FILTER_GODS, new FilterGodCommand(sender, target, filter));
    }

}

package it.polimi.ingsw.controller;

/**
 * Enum of all possible commands handled by server and client
 */
public enum CommandType
{
    /**
     * Ping Command
     */
    BASE(1),
    /**
     * Join request/reply
     */
    JOIN(2),
    /**
     * Map state update sent to clients
     */
    UPDATE(3),
    /**
     * Match start request/update
     */
    START(4),
    /**
     * Request/reply sent to/from the host to chose allowed gods in a match
     */
    FILTER_GODS(5),
    /**
     * Request/reply to select an allowed god in the match
     */
    PICK_GOD(6),
    /**
     * Request/reply to/from the host to chose the first player
     */
    SELECT_FIRST_PLAYER(7),
    /**
     * Request/reply to chose where the workers should be placed
     */
    PLACE_WORKERS(8),
    /**
     * Request/reply to chose what action to do in a turn
     */
    ACTION_TIME(9),
    /**
     * Update send to clients when a match ends
     */
    END_GAME(10),
    /**
     * Update sent to the server to inform that a player whats to leave
     */
    LEAVE(11);

    private int val;

    private CommandType(int val) {
        this.val = val;
    }

    /**
     * Convert a constraint into a integer
     * @return integer representation of a constraint
     */
    public int toInt(){
        return val;
    }

    /**
     * Convert an integer to enum value
     * @param val integer value to convert
     * @return enum type associated to that integer, null if nothing is found
     */
    public static CommandType fromInt(int val)
    {
        for(CommandType t : values())
        {
            if(t.toInt() == val)
                return t;
        }
        return  null;
    }
}
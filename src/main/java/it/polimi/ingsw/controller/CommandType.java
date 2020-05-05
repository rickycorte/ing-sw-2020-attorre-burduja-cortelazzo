package it.polimi.ingsw.controller;

public enum CommandType
{
    JOIN(1),
    LEAVE(2),
    START(3),
    FILTER_GODS(4),
    PICK_GOD(5),
    SELECT_FIRST_PLAYER(6),
    PLACE_WORKERS(7),
    ACTION_TIME(8),
    LOSER(9),
    WINNER(10),
    ACK_JOIN(11),
    UPDATE(12);


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
package it.polimi.ingsw.controller;

public enum CommandType
{
    BASE(1),
    JOIN(2),
    UPDATE(3),
    START(4),
    FILTER_GODS(5),
    PICK_GOD(6),
    SELECT_FIRST_PLAYER(7),
    PLACE_WORKERS(8),
    ACTION_TIME(9),
    END_GAME(10);

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
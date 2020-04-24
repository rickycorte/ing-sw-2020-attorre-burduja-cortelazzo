package it.polimi.ingsw.controller;


import it.polimi.ingsw.game.NextAction;
import it.polimi.ingsw.game.Vector2;
import it.polimi.ingsw.network.INetworkSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * A command sent/received from network
 * Can be serialized in json
 */
public class Command  implements INetworkSerializable
{
    /**
     * Possible types of messages exchanged by server and client
     */
    public enum CType
    {
        JOIN(1),
        LEAVE(2),
        START(3),
        FILTER_GODS(4),
        PICK_GOD(5),
        SELECT_FIRST_PLAYER(6),
        PLACE_WORKERS(7),
        ACTION_TIME(8),
        WINNER(9);

        private int val;

        private CType(int val) {
            this.val = val;
        }

        /**
         * Convert a constraint into a integer
         * @return integer representation of a constraint
         */
        public int toInt(){
            return val;
        }

        public static CType fromInt(int val)
        {
            for(CType t : values())
            {
                if(t.toInt() == val)
                    return t;
            }
            return  null;
        }
    }


    private int type; // int for the sake of serialization (replace CType)
    private boolean request; // true if the target needs to replay, else its just an "update" from the server

    private int sender;
    private int target;

    private int[] intData;
    private Vector2[] v2Data;
    private NextAction[] nextActionsData;


    public Command(int type, boolean request, int sender, int target)
    {
        this(type, request, sender, target, null, null,null);
    }

    public Command(int type, boolean request, int sender, int target, int[] intData)
    {
        this(type, request, sender, target, intData, null,null);
    }

    public Command(int type, boolean request, int sender, int target, int intData)
    {
        this(type, request, sender, target, new int[]{intData}, null,null);
    }

    public Command(int type, boolean request, int sender, int target, Vector2[] v2Data)
    {
        this(type, request, sender, target, null, v2Data,null);
    }

    public Command(int type, boolean request, int sender, int target, Vector2 v2Data)
    {
        this(type, request, sender, target, null, new Vector2[]{v2Data},null);
    }

    public Command(int type,boolean request, int sender,int target,NextAction[] nextActionsData)
    {
        this(type, request, sender, target, null, null, nextActionsData);
    }

    public  Command(int type, boolean request, int sender, int target, int[] intData, Vector2[] v2Data,NextAction[] nextActionsData)
    {
        this.type = type;
        this.request = request;
        this.sender = sender;
        this.target = target;
        this.intData = intData;
        this.v2Data = v2Data;
        this.nextActionsData= nextActionsData;
    }

    public CType getType()
    {
        return Command.CType.fromInt(type);
    }

    public boolean isRequest()
    {
        return request;
    }

    public int getSender()
    {
        return sender;
    }

    public int getTarget()
    {
        return target;
    }

    public int[] getIntData() { return intData; }

    public Vector2[] getV2Data() { return v2Data; }

    public NextAction[] getNextActionsData(){ return nextActionsData; }

    @Override
    public String Serialize() {
        //TODO: implements Serialize
        return null;
    }

}

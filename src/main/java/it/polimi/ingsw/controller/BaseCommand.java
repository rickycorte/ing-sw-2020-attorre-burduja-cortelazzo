package it.polimi.ingsw.controller;

import com.google.gson.Gson;
import it.polimi.ingsw.network.INetworkSerializable;

public class BaseCommand{

    private int type; // int for the sake of serialization (replace CType)
    private boolean request; // true if the target needs to replay, else its just an "update" from the server

    private int sender;
    private int target;

    public BaseCommand(int type,boolean request,int sender,int target){
        this.type = type;
        this.request = request;
        this.sender = sender;
        this.target = target;
    }

    public int getType() {
        return type;
    }

    public boolean isRequest() {
        return request;
    }

    public int getSender() {
        return sender;
    }

    public int getTarget() {
        return target;
    }

}

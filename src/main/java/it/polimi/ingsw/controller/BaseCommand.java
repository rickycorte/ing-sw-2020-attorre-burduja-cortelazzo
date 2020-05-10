package it.polimi.ingsw.controller;

public class BaseCommand{
    private int sender;
    private int target;

    public BaseCommand(int sender,int target){
        this.sender = sender;
        this.target = target;
    }

    public int getSender() {
        return sender;
    }

    public int getTarget() {
        return target;
    }

}

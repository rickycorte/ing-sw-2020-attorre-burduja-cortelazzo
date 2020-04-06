package it.polimi.ingsw.game;

public class NotAllowedOperationException extends Exception
{

    public NotAllowedOperationException(String message){
        super("Operation not allowed: " + message);
    }

    public NotAllowedOperationException() {
        super("Operation not allowed");
    }
}

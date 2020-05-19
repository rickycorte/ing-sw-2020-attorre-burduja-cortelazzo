package it.polimi.ingsw.game;

/**
 * This exception signals that an action failed to run
 */
public class NotAllowedOperationException extends Exception
{
    public NotAllowedOperationException(String message){
        super("Operation not allowed: " + message);
    }
}

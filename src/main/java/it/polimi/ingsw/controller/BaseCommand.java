package it.polimi.ingsw.controller;

/**
 * This class is the base of all the commands.
 * It contains sender and target ids that are required to understand
 * who issued a command and who should receive and "execute" it.
 *
 * This command is also used to create a ping mechanism
 */
public class BaseCommand{
    private int sender;
    private int target;

    public BaseCommand(int sender,int target){
        this.sender = sender;
        this.target = target;
    }

    /**
     * Get the command sender
     * @return command sender
     */
    public int getSender() {
        return sender;
    }

    /**
     * Get the command target (client/server who should run the command)
     * @return command target
     */
    public int getTarget() {
        return target;
    }

}

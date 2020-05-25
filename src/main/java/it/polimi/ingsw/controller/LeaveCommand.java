package it.polimi.ingsw.controller;

/**
 * Command used by the client to inform the server that it wants to leave the current match
 */
public class LeaveCommand extends BaseCommand {

    public LeaveCommand(int sender, int target) {
        //FROM CLIENT
        super(sender, target);
    }
}

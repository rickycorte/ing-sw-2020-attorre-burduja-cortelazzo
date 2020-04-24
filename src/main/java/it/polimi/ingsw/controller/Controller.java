package it.polimi.ingsw.controller;

import it.polimi.ingsw.game.*;

import java.lang.reflect.Array;
import java.util.List;

public class Controller {

    List<Player> connected_players;
    Game match;

    /**
     * Callback function called by the network layer when a new player joins the game
     *
     * @param id       user id
     * @param username username
     */
    public void onConnect(int id, String username) {
        if (match == null || match.isEnded())
            match = new Game();

        Player p = new Player(id, username);
        connected_players.add(p);
        //TODO: check username

        boolean res = match.join(p);
    }

    /**
     * Callback function called by the network layer when a command is received
     *
     * @param cmd command issued by a client
     */
    void onCommand(Command cmd) {
        Game.GameState prevGameState = match.getCurrentState();
        Player prevPlayer = match.getCurrentPlayer();

        Command nextCmd;

        try {

            if (runCommand(match, cmd)) {
                //command executed
                if (prevGameState != match.getCurrentState()) {
                    nextCmd = changedState(match, cmd, prevGameState);
                } else {
                    nextCmd = gameContinue(match, cmd);
                }

                sendUpdate();
            } else {
                //command failed
                nextCmd = repeatCommand(cmd);
            }

            sendNextCommand(nextCmd);

        } catch (NotAllowedOperationException e) {
            //TODO: inform that a broken command was issued
            //it's not your turn to action
            //quindi scarto command e attendo quello richiesto
        }

    }

    /**
     * Callback function called by the network layer when a player leave the game
     *
     * @param id player id
     */
    public void onDisconnect(int id) {
        if (match == null || match.isEnded())
            return;

        match.left(getPlayer(id));
    }

    // **********************************************************************************************
    // Private
    // **********************************************************************************************

    /**
     * Translate a numeric id into a real connected player
     *
     * @param id player id
     * @return Player instance when passed id
     */
    private Player getPlayer(int id) {
        for (Player p : connected_players) {
            if (p.getId() == id)
                return p;
        }
        return null;
    }


    /**
     * Run command on a selected game
     *
     * @param gm  match instance where command should run
     * @param cmd command to run
     * @return false if a command execution failed
     * @throws NotAllowedOperationException if a not allowed command is run
     */
    private boolean runCommand(Game gm, Command cmd) throws NotAllowedOperationException {
        Player p = getPlayer(cmd.getSender());

        switch (cmd.getType()) {
            case START:
                return gm.start(p);
            case FILTER_GODS:
                return gm.applyGodFilter(p, cmd.getIntData());
            case PICK_GOD:
                return gm.selectGod(p, cmd.getIntData()[0]);
            case SELECT_FIRST_PLAYER:
                return gm.selectFirstPlayer(p, gm.getHost());
            case PLACE_WORKERS:
                return gm.placeWorkers(p, cmd.getV2Data());
            case ACTION_TIME:
                return gm.runAction(p, cmd.getIntData()[0], cmd.getIntData()[1], cmd.getV2Data()[0]);

        }

        //TODO: error here
        return false;
    }


    private Command changedState(Game gm, Command cmd, Game.GameState previousGameState) {

        switch (previousGameState) {
            case WAIT:
                return new Command(Command.CType.FILTER_GODS.toInt(), true, cmd.getTarget(), gm.getCurrentPlayer().getId(), null, null, null);
            case GOD_FILTER:
                return new Command(Command.CType.PICK_GOD.toInt(), true, cmd.getTarget(), gm.getCurrentPlayer().getId(), null, null, null);
            case GOD_PICK:
                //send update quale god (ultimo a scegliere)
                return new Command(Command.CType.SELECT_FIRST_PLAYER.toInt(), true, cmd.getTarget(), gm.getCurrentPlayer().getId(), null, null, null);
            case FIRST_PLAYER_PICK:
                return new Command(Command.CType.PLACE_WORKERS.toInt(), true, cmd.getTarget(), gm.getCurrentPlayer().getId(), null, null, null);
            case WORKER_PLACE:
                NextAction[] array = (gm.getNextActions(gm.getCurrentPlayer())).toArray(new NextAction[0]);
                return new Command(Command.CType.ACTION_TIME.toInt(), true, cmd.getTarget(), gm.getCurrentPlayer().getId(), null, null, array);
            case GAME:
                //finita la prtita
                break;
            case END:
                //restart???
                //never here
                break;
        }

        return null;
    }


    private Command gameContinue(Game gm, Command cmd) {
        //if it's GAME then send next possible actions (should send with starting worker chose)
        if (gm.getCurrentState() == Game.GameState.GAME) { //TODO: lose win condition
            NextAction[] array = (gm.getNextActions(gm.getCurrentPlayer())).toArray(new NextAction[0]);
            return new Command(cmd.getType().toInt(), true, cmd.getTarget(), gm.getCurrentPlayer().getId(), null, null, array);
        }else
            return new Command(cmd.getType().toInt(),true,cmd.getTarget(),cmd.getSender(),null,null,null);
    }

    private Command repeatCommand(Command cmd){
        return new Command(cmd.getType().toInt(),true,cmd.getTarget(),cmd.getSender(),null,null,null);
    }

    private void sendUpdate(){

        //TODO:send Broadcast update
    }

    private void sendNextCommand(Command cmd){

        if(cmd == null)
            // TODO:Error here
            ;
        else {
            cmd.Serialize();

            //TODO:send to client
        }
    }
}

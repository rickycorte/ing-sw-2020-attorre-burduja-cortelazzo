package it.polimi.ingsw.controller.compact;

import it.polimi.ingsw.game.Player;

/**
 * Holds basic player data: id, username, god id
 */
public class CompactPlayer
{
    int id;
    int godID;
    String username;

    /**
     * Generate a new compat player with raw data
     * @param id client id
     * @param username player username
     * @param godID god id picked by the player
     */
    public CompactPlayer(int id, String username, int godID)
    {
        this.id = id;
        this.godID = godID;
        this.username = username;
    }

    /**
     * Generate a new compat player based on player data
     * @param p player
     */
    public CompactPlayer(Player p)
    {
        this(p.getId(), p.getUsername(), p.getGod().getId());
    }

    /**
     * Return client id of the player
     * @return player id
     */
    public int getId()
    {
        return id;
    }

    /**
     * Return selected god id
     * @return god id
     */
    public int getGodID()
    {
        return godID;
    }

    /**
     * Get player username
     * @return player username
     */
    public String getUsername()
    {
        return username;
    }
}

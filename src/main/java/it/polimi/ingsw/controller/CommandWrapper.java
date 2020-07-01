package it.polimi.ingsw.controller;

import com.google.gson.Gson;


/**
 * Command wrapper used to easily detect command type and deserialize with the correct type
 */
public class CommandWrapper
{
    CommandType type;
    String data;
    transient BaseCommand cachedCommand; // this field must no be sent

    /**
     * Create a new command wrapper that encapsulates another command
     * @param type encapsulated command type
     * @param command command to encapsulate
     */
    public CommandWrapper(CommandType type, BaseCommand command)
    {
        this.type = type;
        this.cachedCommand = command;
        var gson = new Gson();
        this.data =  gson.toJson(cachedCommand);
    }

    /**
     * Return command type
     * @return command type
     */
    public CommandType getType()
    {
        return type;
    }


    /**
     * Parse and return a command that is bundled as payload data
     * @param type command type that is wanted as result
     * @param <T> expected command type
     * @return parsed command with required type if parse is successfull
     */
    public <T extends BaseCommand> T getCommand(Class<T> type)
    {
        Gson gson = new Gson();

        return type.cast(gson.fromJson(data, type));
    }


    /**
     * Serialize data as string to send it over network
     * @return commandWrapper as Json string
     */

    public String Serialize()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * Print command to string
     * This function must not be confused with Serialize() because
     * here no json serialization is done thus using this function to serialize the data
     * will cause errors
     * @return command as string
     */
    @Override
    public String toString()
    {
        return  String.format("{ type: %s; data: %s}", getType(), data);
    }
}

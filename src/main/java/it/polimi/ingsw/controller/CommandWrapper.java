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

    public CommandWrapper(CommandType type, BaseCommand command)
    {
        this.type = type;
        this.cachedCommand = command;
    }

    /**
     *
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
            if(cachedCommand == null)
        {
            Gson gson = new Gson();
            cachedCommand = gson.fromJson(data, type);
        }
        return type.cast(cachedCommand);
    }


    /**
     * Serialize data as string to send it over network
     * @return commandWrapper as Json string
     */

    public String Serialize()
    {
        Gson gson = new Gson();
        data = gson.toJson(cachedCommand);

        return gson.toJson(this);
    }
}

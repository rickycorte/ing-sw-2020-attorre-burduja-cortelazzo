package it.polimi.ingsw.network;

/**
 * Interface for a serializable packet
 */
public interface INetworkSerializable
{
    /**
     * Serialize a packet as a string to send it over the network
     * @return serialized packet
     */
    String Serialize();

}

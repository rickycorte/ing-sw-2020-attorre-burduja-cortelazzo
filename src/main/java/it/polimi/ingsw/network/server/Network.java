package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.INetworkSerializable;


/**
 * This is the main network class. It has a server and an interface to interact with it
 */
public class Network implements INetworkAdapter {


    public static Server my_server;

     public  Network(){
        my_server = new Server();
    }

    public static void main(String[] args) {
        Network my_network = new Network();
        my_network.StartServer(9090); //TODO ask the user to choose a port

    }




    //INetworkAdapter methods

    @Override
    public void StartServer(int port) {
        my_server.StartServer(port);
    }

    @Override
    public void StopServer() {
        my_server.StopServer();

    }

    @Override
    public void Connect(String ip, int port, String username) {
        //client method, do nothing in server
    }

    @Override
    public void Disconnect() {
        //client method, do nothing in server
    }

    @Override
    public void AddReceiver(ICommandReceiver receiver) {

    }

    @Override
    public void RemoveReceiver(ICommandReceiver receiver) {

    }

    @Override
    public void Send(int id, INetworkSerializable packet) {
        my_server.Send(id, packet);
    }

    @Override
    public void SendBroadcast(INetworkSerializable packet) {
        my_server.SendBroadcast(packet);
    }
}

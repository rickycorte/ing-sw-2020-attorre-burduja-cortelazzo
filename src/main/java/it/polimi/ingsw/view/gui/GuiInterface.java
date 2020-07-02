package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.view.IHumanInterface;
import javafx.application.Application;

/**
 * Class that implements the IHumanInterface on the Gui side
 */
public class GuiInterface implements IHumanInterface {

    private INetworkAdapter adapter;

    public GuiInterface(INetworkAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void start() {
        adapter.setReceiver(GuiManager.getInstance());
        GuiManager.getInstance().setServerConnection(adapter);
        Application.launch(Gui.class);
    }

    @Override
    public void onConnect(CommandWrapper cmd) {


    }

    @Override
    public void onDisconnect(CommandWrapper cmd) {

    }

    @Override
    public void onCommand(CommandWrapper cmd) {

    }
}

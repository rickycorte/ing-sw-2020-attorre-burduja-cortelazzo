package it.polimi.ingsw.ViewGui;

import it.polimi.ingsw.controller.CommandWrapper;
import it.polimi.ingsw.network.ICommandReceiver;
import it.polimi.ingsw.network.INetworkAdapter;
import it.polimi.ingsw.network.client.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;

public class Gui extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Santorini");
        stage.setMinWidth(853);
        stage.setMinHeight(480);
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.setResizable(false);

        InputStream is = Gui.class.getClassLoader().getResourceAsStream("img/common/icon.png");

        if(is != null){
            System.out.println("[GUI] is isn't null");
            stage.getIcons().add(new Image(is));
        }else{
            System.out.println("[GUI] is is null" );
        }
        Scene scene = new Scene(new Pane());
        stage.setScene(scene);


        GuiManager.getInstance().setScene(scene);

        //GuiManager.setLayout("fxml/gameScene.fxml");
        GuiManager.setLayout("fxml/mainScene.fxml");
        stage.show();

    }

    @Override
    public void stop(){
        GuiManager.getInstance().disconnect();
        System.exit(0);
    }

}

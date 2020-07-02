package it.polimi.ingsw.view.gui;

import javafx.application.Application;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * This class is the entry point for the Gui application
 */
public class Gui extends Application {

    /**
     * Method inherited from the application class, called when the Gui is started
     * @param stage stage of the gui
     */
    @Override
    public void start(Stage stage){
        stage.setTitle("Santorini");
        stage.setMinHeight(500);
        stage.setHeight(580);
        stage.setWidth(1); //this is useless, but without this stage starts in an extra large format

        InputStream stream = Gui.class.getClassLoader().getResourceAsStream("img/common/icon.png");
        if(stream != null){
            //System.out.println("[GUI] Stream isn't null");
            stage.getIcons().add(new Image(stream));
        }else{
            System.out.println("[GUI] Stream is null" );
        }

        Scene scene = new Scene(new Pane());
        stage.setScene(scene);

        DoubleBinding w = stage.heightProperty().multiply(1.777);
        stage.minWidthProperty().bind(w);
        stage.maxWidthProperty().bind(w);

        GuiManager.getInstance().setScene(scene);

        GuiManager.setLayout("fxml/mainScene.fxml");
        stage.show();

    }

    /**
     * Method inherited from the application class. Called when the Gui is closed
     */
    @Override
    public void stop(){
        GuiManager.getInstance().disconnect();
        System.exit(0);
    }
}

package it.polimi.ingsw.view.gui;

import javafx.application.Application;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.InputStream;

public class Gui extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Santorini");
        /*
        stage.setMinWidth(853);
        stage.setMinHeight(480);
        stage.setWidth(1280);
        stage.setHeight(720);

         */
        //stage.setMinWidth(500);
        //stage.setWidth(700);

        stage.setMinHeight(500);
        stage.setHeight(580);
        stage.setWidth(1); //this is useless/wrong but without this, stage starts in an extra large format
        //stage.setMaxHeight(800);


        InputStream is = Gui.class.getClassLoader().getResourceAsStream("img/common/icon.png");

        if(is != null){
            System.out.println("[GUI] is isn't null");
            stage.getIcons().add(new Image(is));
        }else{
            System.out.println("[GUI] is is null" );
        }



        Scene scene = new Scene(new Pane());
        stage.setScene(scene);


        DoubleBinding w = stage.heightProperty().multiply(1.7);
        stage.minWidthProperty().bind(w);
        stage.maxWidthProperty().bind(w);


        /*
        DoubleBinding y = stage.widthProperty().divide(2);
        stage.minHeightProperty().bind(y);
        stage.maxHeightProperty().bind(y);

         */

        GuiManager.getInstance().setScene(scene);

        //GuiManager.setLayout("fxml/gameScene.fxml");
        //GuiManager.setLayout("fxml/chooseGodsScene.fxml");
        //GuiManager.setLayout("fxml/endGameScene.fxml");
        GuiManager.setLayout("fxml/mainScene.fxml");
        stage.show();

    }

    @Override
    public void stop(){
        GuiManager.getInstance().disconnect();
        System.exit(0);
    }

}

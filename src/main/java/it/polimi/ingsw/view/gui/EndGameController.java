package it.polimi.ingsw.view.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;



public class EndGameController implements Initializable {
    private int winnerID;

    @FXML
    private Label winnerLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiManager.getInstance().setEndGameController(this);
        winnerID = GuiManager.getInstance().getWinnerID();
        winnerLabel.setText(String.valueOf(winnerID));
    }


}

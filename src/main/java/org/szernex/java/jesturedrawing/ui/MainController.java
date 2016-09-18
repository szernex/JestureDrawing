package org.szernex.java.jesturedrawing.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
	@FXML
	ProgressBar pbTimer;

	@FXML
	Pane imageParent;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		imageParent.getChildren().add(new ImagePane("images/test.png", "-fx-background-position: center center; -fx-background-size: contain; -fx-background-repeat: no-repeat;"));
	}
}

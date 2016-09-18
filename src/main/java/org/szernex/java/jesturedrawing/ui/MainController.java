package org.szernex.java.jesturedrawing.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.szernex.java.jesturedrawing.GestureClass;

import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class MainController implements Initializable {
	private static final Logger logger = LogManager.getLogger(MainController.class);

	@FXML
	ProgressBar pbTimer;

	@FXML
	Pane imageParent;

	private Ticker ticker;
	private Path currentImage = null;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		imageParent.getChildren().add(new ImagePane("images/test.png", "-fx-background-position: center center; -fx-background-size: contain; -fx-background-repeat: no-repeat;"));


	}

	@FXML
	public void onOptionsClick() {
		GestureClass gestureClass = new GestureClass();
		GestureClass.GestureSession gestureSession = new GestureClass.GestureSession();

		gestureSession.paths.add("d:/test");
		gestureSession.paths.add("d:/test2");
		gestureSession.include_subdirs = true;
		gestureSession.interval = 5;
		gestureSession.image_count = 2;
		gestureSession.break_after = 10;
		gestureClass.sessions.add(gestureSession);

		gestureSession = new GestureClass.GestureSession();

		gestureSession.paths.add("d:/test");
		gestureSession.paths.add("d:/test2");
		gestureSession.include_subdirs = true;
		gestureSession.interval = 10;
		gestureSession.image_count = 2;
		gestureSession.break_after = 10;
		gestureClass.sessions.add(gestureSession);

		ticker = new Ticker();

		ticker.initialize(gestureClass);

		Timeline timeline = new Timeline(
				new KeyFrame(
						Duration.seconds(1),
						event -> tick()
				)
		);
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	private void tick() {
		ticker.tick();

		if (ticker.isFinished())
			return;

		pbTimer.setProgress((1.0 / ticker.getCurrentSession().interval) * ticker.getCurrentTimer());

		logger.debug(ticker);

		if (ticker.getCurrentImage() == null) {
			imageParent.getChildren().add(new ImagePane("images/test.png", "-fx-background-position: center center; -fx-background-size: contain; -fx-background-repeat: no-repeat;"));
		} else if (!ticker.getCurrentImage().equals(currentImage)) {
			currentImage = ticker.getCurrentImage();
			imageParent.getChildren().clear();

			if (currentImage != null)
				imageParent.getChildren().add(new ImagePane(currentImage.toUri().toString(), "-fx-background-position: center center; -fx-background-size: contain; -fx-background-repeat: no-repeat;"));

		}
	}
}

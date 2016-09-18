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
import org.szernex.java.jsonconfig.JsonConfig;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		imageParent.getChildren().add(new ImagePane("images/test.png", ImagePane.DEFAULT_STYLE));
	}

	@FXML
	public void onOptionsClick() {
		JsonConfig<GestureClass> config = new JsonConfig<>(GestureClass.class);
		GestureClass gestureClass = config.load(Paths.get("testclass.json"));

		ticker = new Ticker(gestureClass);

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

		Path image = ticker.getCurrentImage();
		ImagePane imagePane = null;

		if (image == null)
			imagePane = new ImagePane("images/test.png", ImagePane.DEFAULT_STYLE);
		else if (!image.equals(currentImage)) {
			currentImage = image;
			imagePane = new ImagePane(image.toUri().toString(), ImagePane.DEFAULT_STYLE);
		}

		if (imagePane != null) {
			imageParent.getChildren().clear();
			imageParent.getChildren().add(imagePane);
		}

		pbTimer.setProgress(1.0 - ((1.0 / ticker.getCurrentSession().interval) * (ticker.getCurrentTimer())));

		/*ticker.tick();

		if (ticker.isFinished())
			return;

		pbTimer.setProgress((1.0 / ticker.getCurrentSession().interval) * ticker.getCurrentTimer());

		logger.debug(ticker);

		if (ticker.getCurrentImage() == null) {
			imageParent.getChildren().add(new ImagePane("images/test.png", ImagePane.DEFAULT_STYLE));
		} else if (!ticker.getCurrentImage().equals(currentImage)) {
			currentImage = ticker.getCurrentImage();
			imageParent.getChildren().clear();

			if (currentImage != null)
				imageParent.getChildren().add(new ImagePane(currentImage.toUri().toString(), ImagePane.DEFAULT_STYLE));

		}*/
	}
}

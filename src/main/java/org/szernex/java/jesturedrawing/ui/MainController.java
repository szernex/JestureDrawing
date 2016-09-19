package org.szernex.java.jesturedrawing.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.szernex.java.jesturedrawing.GestureClass;
import org.szernex.java.jesturedrawing.R;
import org.szernex.java.jsonconfig.JsonConfig;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class MainController implements Initializable, TickListener {
	private static final Logger logger = LogManager.getLogger(MainController.class);

	@FXML
	private ProgressBar pbTimer;
	@FXML
	private ProgressBar pbProgress;
	@FXML
	private Pane imageParent;

	private ResizableImageView ivImage;
	private Ticker ticker;
	private Path currentImage = null;
	private Timeline tickerTimeline;
	private Timeline timerTimeline;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ivImage = new ResizableImageView();
		imageParent.getChildren().add(ivImage);
	}

	@FXML
	public void onOptionsClick() {
		JsonConfig<GestureClass> config = new JsonConfig<>(GestureClass.class);
		GestureClass gestureClass = config.load(Paths.get("testclass.json"));

		ticker = new Ticker(gestureClass);
		ticker.addTickListener(this);

		if (tickerTimeline != null)
			tickerTimeline.stop();

		tickerTimeline = new Timeline(
				new KeyFrame(
						Duration.seconds(1),
						event -> ticker.tick()
				)
		);
		tickerTimeline.setCycleCount(Animation.INDEFINITE);
		tickerTimeline.playFromStart();
	}

	@Override
	public void onTickStart(Ticker ticker) {

	}

	@Override
	public void onTickEnd(Ticker ticker) {

	}

	@Override
	public void onBreakStart(Ticker ticker) {
		ivImage.setImage(new Image("images/break.png", R.Image.SCALE_RESOLUTION, R.Image.SCALE_RESOLUTION, true, true));

		pbProgress.setProgress(0.0);
		pbTimer.setProgress(0.0);

		KeyValue keyValue = new KeyValue(pbTimer.progressProperty(), (1.0 + (1.0 / ticker.getCurrentSession().break_after_session))); // for some reason the target has to be set to above 1.0 or the bar won't fill before the image changes
		KeyFrame keyFrame = new KeyFrame(Duration.seconds(ticker.getCurrentSession().break_after_session), keyValue);

		if (timerTimeline != null)
			timerTimeline.stop();

		timerTimeline = new Timeline();
		timerTimeline.getKeyFrames().add(keyFrame);
		timerTimeline.playFromStart();


		System.gc();
	}

	@Override
	public void onBreakEnd(Ticker ticker) {

	}

	@Override
	public void onNewImage(Ticker ticker) {
		Path image = ticker.getCurrentImage();

		if (image == null) {
			ivImage.setImage(new Image("images/test.png", R.Image.SCALE_RESOLUTION, R.Image.SCALE_RESOLUTION, true, true));
		}
		else if (!image.equals(currentImage)) {
			ivImage.setImage(new Image(image.toUri().toString(), R.Image.SCALE_RESOLUTION, R.Image.SCALE_RESOLUTION, true, true));
		}

		pbProgress.setProgress((1.0 / ticker.getCurrentSession().image_count) * ticker.getCurrentImageCount());
		pbTimer.setProgress(0.0);

		KeyValue keyValue = new KeyValue(pbTimer.progressProperty(), (1.0 + (1.0 / ticker.getCurrentSession().interval))); // for some reason the target has to be set to above 1.0 or the bar won't fill before the image changes
		KeyFrame keyFrame = new KeyFrame(Duration.seconds(ticker.getCurrentSession().interval), keyValue);

		if (timerTimeline != null)
			timerTimeline.stop();

		timerTimeline = new Timeline();
		timerTimeline.getKeyFrames().add(keyFrame);
		timerTimeline.playFromStart();

		System.gc();
	}

	@Override
	public void onFinished(Ticker ticker) {
		ivImage.setImage(new Image("images/finished.png", R.Image.SCALE_RESOLUTION, R.Image.SCALE_RESOLUTION, true, true));
		tickerTimeline.stop();
		timerTimeline.stop();
	}
}

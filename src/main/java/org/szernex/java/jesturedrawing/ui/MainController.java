package org.szernex.java.jesturedrawing.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.szernex.java.jesturedrawing.C;
import org.szernex.java.jesturedrawing.GestureClass;
import org.szernex.java.jesturedrawing.R;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.ResourceBundle;

public class MainController implements Initializable, TickListener, CustomController, EventHandler<MouseEvent> {
	private static final Logger logger = LogManager.getLogger(MainController.class);

	@FXML
	private Pane mainContainer;
	@FXML
	private ProgressBar pbTimer;
	@FXML
	private ProgressBar pbProgress;
	@FXML
	private Label lblSession;
	@FXML
	private Pane imageParent;
	@FXML
	private Button btnPlayPause;
	@FXML
	private CheckBox chkAlwaysOnTop;

	private Stage mainStage;
	private ResizableImageView ivImage;
	private Ticker ticker;
	private Path currentImage = null;
	private Timeline tickerTimeline;
	private Timeline timerTimeline;
	private SimpleStringProperty sessionTitle = new SimpleStringProperty();
	private double moveOffsetX = 0.0;
	private double moveOffsetY = 0.0;
	private double resizeOffsetX = 0.0;
	private double resizeOffsetY = 0.0;
	private boolean resizing = false;
	private EnumSet<BorderSide> resizingSides = EnumSet.noneOf(BorderSide.class);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		DropShadow dropShadow = new DropShadow();

		dropShadow.setColor(Color.color(1.0, 1.0, 1.0));
		lblSession.setEffect(dropShadow);
		lblSession.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 18));
		lblSession.textProperty().bind(sessionTitle);

		ivImage = new ResizableImageView();
		imageParent.getChildren().add(ivImage);

		mainContainer.getStylesheets().add("css/style.css");

		mainContainer.addEventHandler(MouseEvent.ANY, this);
		ivImage.addEventHandler(MouseEvent.ANY, this);
	}

	@Override
	public void setStage(Stage stage) {
		mainStage = stage;

		chkAlwaysOnTop.selectedProperty().set(mainStage.isAlwaysOnTop());
		chkAlwaysOnTop.selectedProperty().addListener((observable, oldValue, newValue) -> mainStage.setAlwaysOnTop(newValue));
	}

	@Override
	public void handle(MouseEvent event) {
		if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
			EnumSet<BorderSide> sides = isMouseAtBorder(mainStage, event.getSceneX(), event.getSceneY(), 10);

			resizing = !sides.isEmpty();

			if (resizing) {
				resizingSides = sides;
				resizeOffsetX = mainStage.getWidth() - event.getX();
				resizeOffsetY = mainStage.getHeight() - event.getY();
			} else {
				moveOffsetX = event.getSceneX();
				moveOffsetY = event.getSceneY();
			}
		} else if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
			if (resizing) {
				if (resizingSides.contains(BorderSide.East))
					mainStage.setWidth(event.getX() + resizeOffsetX);
				if (resizingSides.contains(BorderSide.South))
					mainStage.setHeight(event.getY() + resizeOffsetY);
			} else {
				mainStage.setX(event.getScreenX() - moveOffsetX);
				mainStage.setY(event.getScreenY() - moveOffsetY);
			}
		} else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
			resizing = false;
			resizingSides.clear();

			if (mainContainer.getWidth() < mainContainer.getMinWidth())
				mainStage.setWidth(mainContainer.getMinWidth() + 5);
			if (mainContainer.getHeight() < mainContainer.getMinHeight())
				mainStage.setHeight(mainContainer.getMinHeight() + 5);
		}
	}

	@FXML
	public void onPlayPauseClick() {
		if (tickerTimeline == null)
			return;

		Animation.Status status = tickerTimeline.getStatus();

		if (status.equals(Animation.Status.PAUSED)) {
			tickerTimeline.play();

			if (timerTimeline != null)
				timerTimeline.play();

			btnPlayPause.textProperty().set("Pause");
		} else if (status.equals(Animation.Status.STOPPED)) {
			tickerTimeline.playFromStart();
			btnPlayPause.textProperty().set("Pause");
		} else if (status.equals(Animation.Status.RUNNING)) {
			tickerTimeline.pause();

			if (timerTimeline != null)
				timerTimeline.pause();

			btnPlayPause.textProperty().set("Play");
		}
	}

	@FXML
	public void onClassClick() throws IOException {
		FXMLLoader loader = new FXMLLoader();

		loader.setLocation(ClassLoader.getSystemResource("ui/options.fxml"));

		Parent parent = loader.load();
		Scene scene = new Scene(parent, 0, 0);
		Stage stage = new Stage();
		boolean alwaysOnTop = mainStage.isAlwaysOnTop();

		stage.setWidth(400);
		stage.setHeight(600);
		stage.setScene(scene);
		stage.initModality(Modality.APPLICATION_MODAL);

		if (loader.getController() instanceof CustomController)
			((CustomController) loader.getController()).setStage(stage);

		if (tickerTimeline != null && tickerTimeline.getStatus().equals(Animation.Status.RUNNING))
			onPlayPauseClick();

		mainStage.setAlwaysOnTop(false);
		stage.showAndWait();
		mainStage.setAlwaysOnTop(alwaysOnTop);

		if (C.getInstance().isNewClass()) {
			initializeTicker(C.getInstance().getGestureClass());
		}
	}

	@Override
	public void onTickStart(Ticker ticker) {
		logger.traceEntry();
	}

	@Override
	public void onTickEnd(Ticker ticker) {
		logger.traceEntry();
	}

	@Override
	public void onBreakStart(Ticker ticker) {
		logger.traceEntry();

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
		logger.traceEntry();
	}

	@Override
	public void onNewImage(Ticker ticker) {
		logger.traceEntry();

		Path image = ticker.getCurrentImage();

		if (image == null) {
			ivImage.setImage(new Image("images/test.png", R.Image.SCALE_RESOLUTION, R.Image.SCALE_RESOLUTION, true, true));
		} else if (!image.equals(currentImage)) {
			ivImage.setImage(new Image(image.toUri().toString(), R.Image.SCALE_RESOLUTION, R.Image.SCALE_RESOLUTION, true, true));
		}

		sessionTitle.set(ticker.getCurrentSession().title);
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
		logger.traceEntry();

		ivImage.setImage(new Image("images/finished.png", R.Image.SCALE_RESOLUTION, R.Image.SCALE_RESOLUTION, true, true));
		tickerTimeline.stop();
		timerTimeline.stop();
	}

	private void initializeTicker(GestureClass gesture_class) {
		logger.debug("Initializing Ticker with GestureClass " + gesture_class);
		ticker = new Ticker(gesture_class);
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
	}

	private EnumSet<BorderSide> isMouseAtBorder(Stage stage, double mouse_x, double mouse_y, double border_width) {
		EnumSet<BorderSide> sides = EnumSet.noneOf(BorderSide.class);

		if (mouse_x < border_width)
			sides.add(BorderSide.West);
		if (mouse_x > (stage.getWidth() - border_width))
			sides.add(BorderSide.East);
		if (mouse_y < border_width)
			sides.add(BorderSide.North);
		if (mouse_y > (stage.getHeight() - border_width))
			sides.add(BorderSide.South);

		return sides;
	}

	private enum BorderSide {
		North, East, South, West
	}
}

package org.szernex.java.jesturedrawing.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import org.szernex.java.jesturedrawing.ApplicationConfig;
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
	private Stage mainStage;
	private ResizableImageView ivImage;
	private Ticker ticker;
	private Path currentImage = null;
	private Timeline tickerTimeline;
	private Timeline timerTimeline;
	private double moveOffsetX = 0.0;
	private double moveOffsetY = 0.0;
	private double resizeOffsetX = 0.0;
	private double resizeOffsetY = 0.0;
	private boolean resizing = false;
	private EnumSet<BorderSide> resizingSides = EnumSet.noneOf(BorderSide.class);
	private SimpleStringProperty sessionTitleProperty = new SimpleStringProperty();
	private SimpleDoubleProperty progressProperty = new SimpleDoubleProperty();
	private SimpleStringProperty playPauseTextProperty = new SimpleStringProperty();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		DropShadow dropShadow = new DropShadow();

		dropShadow.setColor(Color.color(1.0, 1.0, 1.0));
		lblSession.setEffect(dropShadow);
		lblSession.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 18));
		lblSession.textProperty().bind(sessionTitleProperty);
		pbProgress.progressProperty().bind(progressProperty);
		progressProperty.set(0.0);

		ivImage = new ResizableImageView();
		imageParent.getChildren().add(ivImage);

		playPauseTextProperty.set("Play");
		btnPlayPause.textProperty().bind(playPauseTextProperty);

		mainContainer.getStylesheets().add("css/style.css");

		mainContainer.addEventHandler(MouseEvent.ANY, this);
		ivImage.addEventHandler(MouseEvent.ANY, this);
	}

	@Override
	public void setStage(Stage stage) {
		mainStage = stage;

		ApplicationConfig config = C.getInstance().getApplicationConfig();

		// we don't want these values to always be loaded when we re-initialize from config
		mainStage.setMaximized(config.window.maximized);
		mainStage.setWidth(config.window.width);
		mainStage.setHeight(config.window.height);
		mainStage.setX(config.window.pos_x);
		mainStage.setY(config.window.pos_y);

		initializeFromConfig(config);
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

	@Override
	public void onTickStart(Ticker ticker) {
	}

	@Override
	public void onTickEnd(Ticker ticker) {
	}

	@Override
	public void onBreakStart(Ticker ticker) {
		pbTimer.setVisible(true);

		ivImage.setImage(new Image("images/break.png", R.Image.SCALE_RESOLUTION, R.Image.SCALE_RESOLUTION, true, true));
		progressProperty.set(0.0);

		resetTimer(ticker.getCurrentSession().break_after_session);

		System.gc();
	}

	@Override
	public void onBreakEnd(Ticker ticker) {
	}

	@Override
	public void onNewImage(Ticker ticker) {
		tickerTimeline.play();

		Path image = ticker.getCurrentImage();

		pbTimer.setVisible(C.getInstance().getApplicationConfig().timerEnabled);

		if (image == null) {
			ivImage.setImage(new Image("images/noimage.png", R.Image.SCALE_RESOLUTION, R.Image.SCALE_RESOLUTION, true, true));
		} else if (!image.equals(currentImage)) {
			ivImage.setImage(new Image(image.toUri().toString(), R.Image.SCALE_RESOLUTION, R.Image.SCALE_RESOLUTION, true, true));
		}

		sessionTitleProperty.set(ticker.getCurrentSession().title);
		progressProperty.set((1.0 / ticker.getCurrentSession().image_count) * ticker.getCurrentImageCount());

		resetTimer(ticker.getCurrentSession().interval);

		System.gc();
	}

	@Override
	public void onFinished(Ticker ticker) {
		ivImage.setImage(new Image("images/finished.png", R.Image.SCALE_RESOLUTION, R.Image.SCALE_RESOLUTION, true, true));
		onStopClick();
	}

	@FXML
	public void onPlayPauseClick() {
		if (tickerTimeline == null)
			return;

		Animation.Status status = tickerTimeline.getStatus();

		if (status.equals(Animation.Status.PAUSED)) {
			tickerTimeline.play();
		} else if (status.equals(Animation.Status.STOPPED)) {
			tickerTimeline.playFromStart();
		} else if (status.equals(Animation.Status.RUNNING)) {
			tickerTimeline.pause();
		}
	}

	@FXML
	public void onSkipClick() {
		if (ticker != null)
			ticker.skip();
	}

	@FXML
	public void onNewImageClick() {
		if (ticker != null && !ticker.isPaused())
			ticker.nextRandomImage();
	}

	@FXML
	public void onStopClick() {
		initializeTicker(C.getInstance().getGestureClass());
	}

	@FXML
	public void onClassClick() throws IOException {
		showWindowAndWait("ui/class.fxml", 400, 600);

		if (C.getInstance().isNewClass()) {
			initializeTicker(C.getInstance().getGestureClass());
		}
	}

	@FXML
	public void onOptionsClick() throws IOException {
		showWindowAndWait("ui/options.fxml", 400, 600);

		if (C.getInstance().isNewConfig())
			initializeFromConfig(C.getInstance().getApplicationConfig());
	}

	@FXML
	public void onExitClick() {
		mainStage.close();
	}

	private void showWindowAndWait(String fxml, double width, double height) {
		FXMLLoader loader = new FXMLLoader();

		loader.setLocation(ClassLoader.getSystemResource(fxml));

		Parent parent;

		try {
			parent = loader.load();
		} catch (IOException ex) {
			logger.error("Error loading fxml resource " + fxml + ": " + ex.getMessage());
			ex.printStackTrace();

			return;
		}

		Scene scene = new Scene(parent, 0, 0);
		Stage stage = new Stage();
		boolean alwaysOnTop = mainStage.isAlwaysOnTop();

		stage.setWidth(width);
		stage.setHeight(height);
		stage.setScene(scene);
		stage.initModality(Modality.APPLICATION_MODAL);

		if (loader.getController() instanceof CustomController)
			((CustomController) loader.getController()).setStage(stage);

		if (tickerTimeline != null && tickerTimeline.getStatus().equals(Animation.Status.RUNNING))
			tickerTimeline.pause();

		mainStage.setAlwaysOnTop(false);
		stage.showAndWait();
		mainStage.setAlwaysOnTop(alwaysOnTop);
	}

	private void resetTimer(int duration) {
		if (timerTimeline == null)
			timerTimeline = new Timeline();

		pbTimer.setProgress(0.0);
		timerTimeline.stop();

		if (duration > 0) {
			KeyValue keyValue = new KeyValue(pbTimer.progressProperty(), (1.0 + (1.0 / duration))); // for some reason the target has to be set to above 1.0 or the bar won't fill before the image changes
			KeyFrame keyFrame = new KeyFrame(Duration.seconds(duration), keyValue);

			timerTimeline.getKeyFrames().clear();
			timerTimeline.getKeyFrames().add(keyFrame);
			timerTimeline.playFromStart();
		}
	}

	private void initializeFromConfig(ApplicationConfig config) {
		mainStage.setAlwaysOnTop(config.window.always_on_top);
		mainStage.setOpacity(config.window.opacity);

		if (ticker != null && !ticker.isPaused())
			pbTimer.setVisible(config.timerEnabled);
	}

	private void initializeTicker(GestureClass gesture_class) {
		logger.debug("Initializing Ticker with GestureClass " + gesture_class);

		if (gesture_class == null)
			return;

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

		tickerTimeline.statusProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.equals(Animation.Status.RUNNING)) {
				playPauseTextProperty.set("Pause");

				if (timerTimeline != null && timerTimeline.getStatus().equals(Animation.Status.PAUSED))
					timerTimeline.play();
			} else if (newValue.equals(Animation.Status.PAUSED) || newValue.equals(Animation.Status.STOPPED)) {
				playPauseTextProperty.set("Play");

				if (timerTimeline != null)
					timerTimeline.pause();
			}
		});

		progressProperty.set(0.0);
		resetTimer(0);

		logger.debug("Ticker initialized");
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

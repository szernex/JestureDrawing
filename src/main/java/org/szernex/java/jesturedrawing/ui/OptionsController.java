package org.szernex.java.jesturedrawing.ui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.szernex.java.jesturedrawing.C;
import org.szernex.java.jesturedrawing.GestureClass;
import org.szernex.java.jsonconfig.JsonConfig;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class OptionsController implements Initializable, CustomController, ChangeListener<String> {
	private final Logger logger = LogManager.getLogger(OptionsController.class);
	private final JsonConfig<GestureClass> config = new JsonConfig<>(GestureClass.class);
	@FXML
	private ListView<GestureClass.GestureSession> lstSessions;
	@FXML
	private TextField txtSessionTitle;
	@FXML
	private ListView<String> lstPaths;
	@FXML
	private CheckBox chkIncludeSubDirs;
	@FXML
	private FilteredTextField txtImageCount;
	@FXML
	private FilteredTextField txtInterval;
	@FXML
	private FilteredTextField txtBreakAfter;
	private GestureClass currentClass;
	private GestureClass.GestureSession currentSession;
	private Stage mainStage;
	private File lastLoadFile = Paths.get(".", "temp").toAbsolutePath().normalize().toFile();
	private File lastSaveFile = Paths.get(".", "temp").toAbsolutePath().normalize().toFile();
	private File lastLoadDirectory = Paths.get(".").toAbsolutePath().normalize().toFile();

	private SimpleListProperty<GestureClass.GestureSession> sessionListProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
	private SimpleStringProperty sessionTitleProperty = new SimpleStringProperty();
	private SimpleListProperty<String> pathListProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
	private SimpleBooleanProperty includeSubDirsProperty = new SimpleBooleanProperty();
	private SimpleStringProperty imageCountProperty = new SimpleStringProperty();
	private SimpleStringProperty intervalProperty = new SimpleStringProperty();
	private SimpleStringProperty breakAfterProperty = new SimpleStringProperty();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		lstSessions.itemsProperty().bind(sessionListProperty);
		lstSessions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null)
				return;

			updateSession();
			currentSession = newValue;

			// this basically converts the json object to a properties object
			sessionTitleProperty.removeListener(this);
			sessionTitleProperty.set(currentSession.title);
			sessionTitleProperty.addListener(this);
			pathListProperty.setAll(currentSession.paths);
			includeSubDirsProperty.set(currentSession.include_subdirs);
			imageCountProperty.set(String.valueOf(currentSession.image_count));
			intervalProperty.set(String.valueOf(currentSession.interval));
			breakAfterProperty.set(String.valueOf(currentSession.break_after_session));
		});


		txtSessionTitle.textProperty().bindBidirectional(sessionTitleProperty);
		lstPaths.itemsProperty().bindBidirectional(pathListProperty);
		chkIncludeSubDirs.selectedProperty().bindBidirectional(includeSubDirsProperty);
		txtImageCount.textProperty().bindBidirectional(imageCountProperty);
		txtInterval.textProperty().bindBidirectional(intervalProperty);
		txtBreakAfter.textProperty().bindBidirectional(breakAfterProperty);

		if (C.getInstance().getGestureClass() == null) {
			currentClass = initializeNewClass();
			C.getInstance().setNewClass(true);
		} else {
			currentClass = C.getInstance().getGestureClass();
			loadClass(currentClass);
			C.getInstance().setNewClass(false);
		}
	}

	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		// this method causes the list view to be updated in real-time
		if (currentSession != null)
			currentSession.title = sessionTitleProperty.get();

		SimpleListProperty<GestureClass.GestureSession> temp = new SimpleListProperty<>(FXCollections.observableArrayList());

		temp.addAll(sessionListProperty);
		sessionListProperty.clear();
		sessionListProperty.addAll(temp);
	}

	@Override
	public void setStage(Stage stage) {
		mainStage = stage;
	}

	@FXML
	public void onLoadClassClick() {
		logger.debug("Loading class");

		if (currentClass != null && !currentClass.sessions.isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			ButtonType buttonTypeYes = new ButtonType("Yes");
			ButtonType buttonTypeNo = new ButtonType("No");
			ButtonType buttonTypeCancel = new ButtonType("Cancel");

			alert.setHeaderText("Do you want to save the current class?");
			alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);

			Optional<ButtonType> result = alert.showAndWait();

			if (!result.isPresent() || result.get() == buttonTypeCancel)
				return;
			else if (result.get() == buttonTypeYes)
				onSaveClassClick();
		}

		FileChooser fileChooser = new FileChooser();

		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("json config files", "*.json"));
		fileChooser.setInitialDirectory(lastLoadFile.getParentFile());

		File file = fileChooser.showOpenDialog(mainStage);
		logger.debug("Loading class file: " + file);

		if (file == null)
			return;

		lastLoadFile = file;
		initializeNewClass();
		currentClass = config.load(file.toPath());
		loadClass(currentClass);
		logger.debug("Class loaded");
	}

	@FXML
	public void onSaveClassClick() {
		logger.debug("Saving class");

		FileChooser fileChooser = new FileChooser();

		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("json config files", "*.json"));
		fileChooser.setInitialDirectory(lastSaveFile.getParentFile());

		File file = fileChooser.showSaveDialog(mainStage);
		logger.debug("Saving class file: " + file);

		if (file == null || currentClass == null || currentClass.sessions.isEmpty())
			return;

		lastSaveFile = file;
		updateClass();
		config.save(currentClass, file.toPath());
		logger.debug("Class saved");
	}

	@FXML
	public void onMoveSessionTopClick() {
		GestureClass.GestureSession item = lstSessions.getSelectionModel().getSelectedItem();
		int position = lstSessions.getSelectionModel().getSelectedIndex();

		if (moveListItem(item, sessionListProperty, position, position * -1))
			lstSessions.getSelectionModel().select(item);
	}

	@FXML
	public void onMoveSessionUpClick() {
		GestureClass.GestureSession item = lstSessions.getSelectionModel().getSelectedItem();
		int position = lstSessions.getSelectionModel().getSelectedIndex();

		if (moveListItem(item, sessionListProperty, position, -1))
			lstSessions.getSelectionModel().select(item);
	}

	@FXML
	public void onMoveSessionDownClick() {
		GestureClass.GestureSession item = lstSessions.getSelectionModel().getSelectedItem();
		int position = lstSessions.getSelectionModel().getSelectedIndex();

		if (moveListItem(item, sessionListProperty, position, 1))
			lstSessions.getSelectionModel().select(item);
	}

	@FXML
	public void onMoveSessionBottomClick() {
		GestureClass.GestureSession item = lstSessions.getSelectionModel().getSelectedItem();
		int position = lstSessions.getSelectionModel().getSelectedIndex();

		if (moveListItem(item, sessionListProperty, position, (sessionListProperty.size() - 1 - position)))
			lstSessions.getSelectionModel().select(item);
	}

	@FXML
	public void onAddSessionClick() {
		GestureClass.GestureSession session = new GestureClass.GestureSession();

		sessionListProperty.add(session);
		lstSessions.getSelectionModel().select(session);
	}

	@FXML
	public void onRemoveSessionClick() {
		sessionListProperty.remove(lstSessions.getSelectionModel().getSelectedItem());
	}

	@FXML
	public void onAddPathClick() {
		DirectoryChooser directoryChooser = new DirectoryChooser();

		directoryChooser.setInitialDirectory(lastLoadDirectory);

		File directory = directoryChooser.showDialog(mainStage);

		if (directory == null)
			return;

		lastLoadDirectory = directory;

		if (!pathListProperty.contains(directory.toString()))
			pathListProperty.add(directory.toString());
	}

	@FXML
	public void onRemovePathClick() {
		pathListProperty.remove(lstPaths.getSelectionModel().getSelectedItem());
	}

	@FXML
	public void onApplyClick() {
		if (currentClass != null && !currentClass.sessions.isEmpty()) {
			updateClass();
			C.getInstance().setGestureClass(currentClass);
			C.getInstance().setNewClass(true);
		}

		mainStage.close();
	}

	@FXML
	public void onCloseClick() {
		mainStage.close();
	}

	private void loadClass(GestureClass gesture_class) {
		if (gesture_class == null)
			return;

		sessionListProperty.addAll(gesture_class.sessions);
	}

	private <T> boolean moveListItem(T item, List<T> list, int position, int distance) {
		if (item == null || list == null || list.isEmpty())
			return false;

		int newPosition = position + distance;

		if (newPosition < 0 || newPosition > list.size())
			return false;

		list.remove(position);
		list.add(newPosition, item);

		return true;
	}

	private GestureClass initializeNewClass() {
		GestureClass gestureClass = new GestureClass();

		sessionListProperty.clear();
		sessionTitleProperty.set("");
		pathListProperty.clear();
		includeSubDirsProperty.set(false);
		imageCountProperty.set("");
		intervalProperty.set("");
		breakAfterProperty.set("");

		return gestureClass;
	}

	private void updateClass() {
		if (currentClass == null)
			return;

		updateSession();

		currentClass.sessions.clear();
		currentClass.sessions.addAll(sessionListProperty);
	}

	private void updateSession() {
		if (currentSession == null)
			return;

		currentSession.title = sessionTitleProperty.get();
		currentSession.paths.clear();
		currentSession.paths.addAll(pathListProperty);
		currentSession.include_subdirs = includeSubDirsProperty.get();

		try {
			currentSession.image_count = Integer.valueOf(imageCountProperty.get());
		} catch (NumberFormatException ignored) {
		}

		try {
			currentSession.interval = Integer.valueOf(intervalProperty.get());
		} catch (NumberFormatException ignored) {
		}

		try {
			currentSession.break_after_session = Integer.valueOf(breakAfterProperty.get());
		} catch (NumberFormatException ignored) {
		}
	}
}

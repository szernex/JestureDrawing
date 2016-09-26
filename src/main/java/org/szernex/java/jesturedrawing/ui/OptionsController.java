package org.szernex.java.jesturedrawing.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import org.szernex.java.jesturedrawing.ApplicationConfig;
import org.szernex.java.jesturedrawing.C;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class OptionsController implements Initializable, CustomController {
	@FXML
	private CheckBox chkAlwaysOnTop;
	@FXML
	private Slider sldOpacity;
	@FXML
	private TextField txtOpacity;
	@FXML
	private CheckBox chkTimerEnabled;

	private Stage mainStage;

	private SimpleBooleanProperty alwaysOnTopProperty = new SimpleBooleanProperty();
	private SimpleDoubleProperty opacityDoubleProperty = new SimpleDoubleProperty();
	private SimpleStringProperty opacityStringProperty = new SimpleStringProperty();
	private SimpleBooleanProperty timerEnabledProperty = new SimpleBooleanProperty();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		chkAlwaysOnTop.selectedProperty().bindBidirectional(alwaysOnTopProperty);
		sldOpacity.valueProperty().bindBidirectional(opacityDoubleProperty);
		txtOpacity.textProperty().bindBidirectional(opacityStringProperty);
		chkTimerEnabled.selectedProperty().bindBidirectional(timerEnabledProperty);

		Bindings.bindBidirectional(opacityStringProperty, opacityDoubleProperty, new NumberStringConverter(Locale.US));
	}

	@Override
	public void setStage(Stage stage) {
		mainStage = stage;
		mainStage.setTitle("Options");

		ApplicationConfig config = C.getInstance().getApplicationConfig();

		alwaysOnTopProperty.set(config.window.always_on_top);
		opacityDoubleProperty.set(config.window.opacity);
		timerEnabledProperty.set(config.timerEnabled);
	}

	@FXML
	public void onOKClick() {
		C.getInstance().setNewConfig(true);

		ApplicationConfig config = C.getInstance().getApplicationConfig();

		config.window.always_on_top = alwaysOnTopProperty.get();
		config.window.opacity = opacityDoubleProperty.get();
		config.timerEnabled = timerEnabledProperty.get();

		mainStage.close();
	}

	@FXML
	public void onCancelClick() {
		C.getInstance().setNewConfig(false);
		mainStage.close();
	}
}

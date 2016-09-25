package org.szernex.java.jesturedrawing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.szernex.java.jesturedrawing.ui.CustomController;
import org.szernex.java.jsonconfig.JsonConfig;

import java.io.IOException;
import java.nio.file.Paths;

public class Main extends Application {
	private static final Logger logger = LogManager.getLogger(Main.class);

	private static final JsonConfig<ApplicationConfig> jsonConfig = new JsonConfig<>(ApplicationConfig.class);
	public static final ApplicationConfig applicationConfig = jsonConfig.load(Paths.get(R.CONFIG_FILE));

	private Stage mainStage;

	public static void main(String[] args) {
		if (applicationConfig == null) {
			logger.fatal("Application configuration could not be loaded, aborting");
			System.exit(1);
		}

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		logger.trace("Application started");

		FXMLLoader loader = new FXMLLoader();

		loader.setLocation(ClassLoader.getSystemResource("ui/main.fxml"));

		Parent parent = loader.load();
		Scene scene = new Scene(parent, 0, 0);

		primaryStage.setTitle(R.APPLICATION_TITLE);
		primaryStage.setAlwaysOnTop(applicationConfig.window.always_on_top);
		primaryStage.setMaximized(applicationConfig.window.maximized);
		primaryStage.setWidth(applicationConfig.window.width);
		primaryStage.setHeight(applicationConfig.window.height);
		primaryStage.setX(applicationConfig.window.pos_x);
		primaryStage.setY(applicationConfig.window.pos_y);
		//primaryStage.setOpacity(0.5);
		primaryStage.initStyle(StageStyle.UNDECORATED);

		primaryStage.setScene(scene);
		primaryStage.show();

		mainStage = primaryStage;

		if (loader.getController() instanceof CustomController)
			((CustomController) loader.getController()).setStage(primaryStage);
	}

	@Override
	public void stop() throws Exception {
		super.stop();

		applicationConfig.window.always_on_top = mainStage.isAlwaysOnTop();

		if (mainStage.isMaximized())
			applicationConfig.window.maximized = true;
		else {
			applicationConfig.window.maximized = false;
			applicationConfig.window.width = mainStage.getWidth();
			applicationConfig.window.height = mainStage.getHeight();
		}

		applicationConfig.window.pos_x = mainStage.getX();
		applicationConfig.window.pos_y = mainStage.getY();

		jsonConfig.save(applicationConfig, Paths.get(R.CONFIG_FILE));

		logger.trace("Application stopped");
	}
}
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

	private static JsonConfig<ApplicationConfig> jsonConfig;

	private Stage mainStage;

	public static void main(String[] args) {
		jsonConfig = new JsonConfig<>(ApplicationConfig.class);
		ApplicationConfig applicationConfig = jsonConfig.load(Paths.get(R.CONFIG_FILE));

		if (applicationConfig == null) {
			logger.fatal("Application configuration could not be loaded, aborting");
			System.exit(1);
		}

		C.getInstance().setApplicationConfig(applicationConfig);

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		logger.trace("Application started");


		FXMLLoader loader = new FXMLLoader();

		loader.setLocation(ClassLoader.getSystemResource("ui/main.fxml"));

		Parent parent = loader.load();
		Scene scene = new Scene(parent, 0, 0);

		mainStage = primaryStage;
		mainStage.setTitle(R.APPLICATION_TITLE);
		mainStage.initStyle(StageStyle.UNDECORATED);
		mainStage.setScene(scene);
		mainStage.show();

		if (loader.getController() instanceof CustomController)
			((CustomController) loader.getController()).setStage(mainStage);
	}

	@Override
	public void stop() throws Exception {
		super.stop();

		ApplicationConfig applicationConfig = C.getInstance().getApplicationConfig();

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
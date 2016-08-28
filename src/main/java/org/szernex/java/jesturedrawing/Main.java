package org.szernex.java.jesturedrawing;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.szernex.java.jsonconfig.JsonConfig;

import java.nio.file.Paths;

public class Main extends Application {
	private static final Logger logger = LogManager.getLogger(Main.class);

	private static final JsonConfig<ConfigObject> jsonConfig = new JsonConfig<>(ConfigObject.class);

	public static final ConfigObject applicationConfig = jsonConfig.load(Paths.get(R.CONFIG_FILE));

	public static void main(String[] args) {
		if (applicationConfig == null) {
			logger.fatal("Application configuration could not be loaded, aborting");
			System.exit(1);
		}

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		logger.traceEntry("Application starting");
	}

	@Override
	public void stop() throws Exception {
		super.stop();

		jsonConfig.save(applicationConfig, Paths.get(R.CONFIG_FILE));

		logger.traceExit("Application stopped");
	}
}

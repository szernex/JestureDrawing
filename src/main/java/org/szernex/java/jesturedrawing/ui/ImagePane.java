package org.szernex.java.jesturedrawing.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.Pane;

public class ImagePane extends Pane {
	public static final String DEFAULT_STYLE = "-fx-background-position: center center; -fx-background-size: contain; -fx-background-repeat: no-repeat;";

	public ImagePane(String image, String style) {
		styleProperty().bind(
				new SimpleStringProperty("-fx-background-image: url(\"")
						.concat(image)
						.concat("\");")
						.concat(style));
	}
}

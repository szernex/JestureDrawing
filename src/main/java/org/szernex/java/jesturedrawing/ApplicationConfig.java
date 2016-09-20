package org.szernex.java.jesturedrawing;

public class ApplicationConfig {
	public Window window = new Window();

	public class Window {
		boolean maximized = false;
		boolean always_on_top = false;
		double pos_x = 0.0;
		double pos_y = 0.0;
		double width = 400;
		double height = 600;
	}
}

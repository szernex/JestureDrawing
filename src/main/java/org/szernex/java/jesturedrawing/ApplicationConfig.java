package org.szernex.java.jesturedrawing;

public class ApplicationConfig {
	public Window window = new Window();
	public boolean timerEnabled = true;

	public class Window {
		public boolean maximized = false;
		public boolean always_on_top = false;
		public double opacity = 1.0;
		public double pos_x = 0.0;
		public double pos_y = 0.0;
		public double width = 400;
		public double height = 600;
	}
}

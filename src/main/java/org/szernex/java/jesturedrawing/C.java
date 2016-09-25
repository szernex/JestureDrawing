package org.szernex.java.jesturedrawing;

// Global context class
public final class C {
	private static C instance = new C();

	private ApplicationConfig applicationConfig;
	private GestureClass gestureClass;

	public static C getInstance() {
		return instance;
	}

	public ApplicationConfig getApplicationConfig() {
		return applicationConfig;
	}

	public void setApplicationConfig(ApplicationConfig applicationConfig) {
		this.applicationConfig = applicationConfig;
	}

	public GestureClass getGestureClass() {
		return gestureClass;
	}

	public void setGestureClass(GestureClass gestureClass) {
		this.gestureClass = gestureClass;
	}
}

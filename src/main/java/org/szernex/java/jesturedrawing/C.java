package org.szernex.java.jesturedrawing;

// Global context class
public final class C {
	private static C instance = new C();

	private ApplicationConfig applicationConfig = null;
	private GestureClass gestureClass = null;
	private boolean newClass = false;
	private boolean newConfig = false;

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

	public boolean isNewClass() {
		return newClass;
	}

	public void setNewClass(boolean newClass) {
		this.newClass = newClass;
	}

	public boolean isNewConfig() {
		return newConfig;
	}

	public void setNewConfig(boolean newConfig) {
		this.newConfig = newConfig;
	}
}

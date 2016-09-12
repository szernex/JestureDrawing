package org.szernex.java.jesturedrawing;

import java.util.ArrayList;
import java.util.List;

public class GestureClass {
	String title = "Class";
	List<GestureSession> sessions = new ArrayList<>();

	public class GestureSession {
		String title = "Session";
		List<String> paths = new ArrayList<>();
		int image_count = 10;
		int interval = 30;
		int break_after = 30;
		boolean timer = true;
	}
}

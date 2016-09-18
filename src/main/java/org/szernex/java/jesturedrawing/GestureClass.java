package org.szernex.java.jesturedrawing;

import java.util.ArrayList;

public class GestureClass {
	public String title = "Class";
	public ArrayList<GestureSession> sessions = new ArrayList<>();

	public static class GestureSession {
		public String title = "Session";
		public ArrayList<String> paths = new ArrayList<>();
		public boolean include_subdirs = false;
		public int image_count = 10;
		public int interval = 30;
		public int break_after_session = 30;
	}
}

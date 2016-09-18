package org.szernex.java.jesturedrawing;

import java.util.ArrayList;
import java.util.List;

public class GestureClass {
	public String title = "Class";
	public List<GestureSession> sessions = new ArrayList<>();

	public static class GestureSession {
		public String title = "Session";
		public List<String> paths = new ArrayList<>();
		public boolean include_subdirs = false;
		public int image_count = 10;
		public int interval = 30;
		public int break_after = 30;
		public boolean timer = true;
	}
}

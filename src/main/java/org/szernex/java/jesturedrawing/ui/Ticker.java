package org.szernex.java.jesturedrawing.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.szernex.java.jesturedrawing.GestureClass;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class Ticker {
	private static final Logger logger = LogManager.getLogger(Ticker.class);
	private ListIterator<GestureClass.GestureSession> sessionIterator;
	private boolean finished;
	private boolean paused;
	private GestureClass.GestureSession currentSession;
	private ArrayList<Path> imageList;
	private int currentTimer;
	private int currentImageCount;
	private Path currentImage;
	private Random random;
	private HashSet<TickListener> tickListeners = new HashSet<>();

	public Ticker(GestureClass gesture_class) {
		initialize(gesture_class);
	}

	public Path getCurrentImage() {
		return currentImage;
	}

	public GestureClass.GestureSession getCurrentSession() {
		return currentSession;
	}

	public int getCurrentImageCount() {
		return currentImageCount;
	}

	public boolean isPaused() {
		return paused;
	}

	public void addTickListener(TickListener listener) {
		tickListeners.add(listener);
	}

	public void initialize(GestureClass gesture_class) {
		if (gesture_class == null)
			return;

		sessionIterator = gesture_class.sessions.listIterator();
		random = new Random(System.currentTimeMillis());
		currentTimer = 0;
		imageList = new ArrayList<>();
		currentSession = initializeNextSession();
	}

	public void skip() {
		currentTimer = 0;
		tick();
	}

	public void tick() {
		if (finished)
			return;

		tickListeners.forEach(listener -> listener.onTickStart(this));

		currentTimer--;

		if (currentTimer > 0)
			return;

		if (paused) {
			paused = false;
			tickListeners.forEach(listener -> listener.onBreakEnd(this));
		}

		if (currentImageCount < currentSession.image_count) {
			currentImageCount++;
			nextRandomImage();
		} else {
			GestureClass.GestureSession nextSession = initializeNextSession();

			if (nextSession != null) {
				currentTimer = currentSession.break_after_session;
				paused = true;
				tickListeners.forEach(listener -> listener.onBreakStart(this));

				currentSession = nextSession;
			}
		}

		tickListeners.forEach(listener -> listener.onTickEnd(this));
	}

	public void nextRandomImage() {
		if (imageList == null || imageList.isEmpty() || currentSession == null)
			return;

		currentImage = imageList.get(random.nextInt(imageList.size()));
		currentTimer = currentSession.interval;
		tickListeners.forEach(listener -> listener.onNewImage(this));
	}

	private GestureClass.GestureSession initializeNextSession() {
		GestureClass.GestureSession session;

		if (sessionIterator == null || !sessionIterator.hasNext()) {
			finished = true;
			tickListeners.forEach(listener -> listener.onFinished(this));

			return null;
		}

		session = sessionIterator.next();
		currentImage = null;
		imageList.clear();
		imageList.addAll(getFileSet(session.paths, session.include_subdirs));
		finished = false;
		currentImageCount = 0;

		return session;
	}

	private Set<Path> getFileSet(List<String> paths, boolean recursive) {
		ImageFileVisitor visitor = new ImageFileVisitor();
		int depth = (recursive ? Integer.MAX_VALUE : 1);

		for (String p : paths) {
			try {
				Files.walkFileTree(Paths.get(p), new HashSet<>(), depth, visitor);
			} catch (IOException ex) {
				logger.warn("Error walking file tree '" + p + "': " + ex.getMessage());
				ex.printStackTrace();
			}
		}

		return visitor.getFiles();
	}

	public class ImageFileVisitor extends SimpleFileVisitor<Path> {
		private final PathMatcher matcher;
		private Set<Path> files;

		ImageFileVisitor() {
			matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.{jpg,jpeg,png,bmp,tif,tiff}");
			files = new TreeSet<>();
		}

		Set<Path> getFiles() {
			return files;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (matcher.matches(file))
				files.add(file);

			return FileVisitResult.CONTINUE;
		}
	}
}


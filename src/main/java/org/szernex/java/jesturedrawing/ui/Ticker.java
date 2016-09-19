package org.szernex.java.jesturedrawing.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.szernex.java.jesturedrawing.GestureClass;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

// TODO find better name
public class Ticker {
	private static final Logger logger = LogManager.getLogger(Ticker.class);

	private GestureClass gestureClass;
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

	public Ticker() {
	}

	public Ticker(GestureClass gestureClass) {
		initialize(gestureClass);
	}

	public boolean isFinished() {
		return finished;
	}

	public Path getCurrentImage() {
		return currentImage;
	}

	public int getCurrentTimer() {
		return Math.max(0, currentTimer);
	}

	public GestureClass.GestureSession getCurrentSession() {
		return currentSession;
	}

	public int getCurrentImageCount() {
		return currentImageCount;
	}

	public void addTickListener(TickListener listener) {
		tickListeners.add(listener);
	}

	public void initialize(GestureClass gestureClass) {
		this.gestureClass = gestureClass;
		sessionIterator = gestureClass.sessions.listIterator();
		random = new Random(System.currentTimeMillis());
		currentTimer = 0;
		imageList = new ArrayList<>();

		initializeNextSession();
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
			currentImage = getRandomImage(imageList);
			currentTimer = currentSession.interval;
			currentImageCount++;
			tickListeners.forEach(listener -> listener.onNewImage(this));
		} else {
			if (initializeNextSession()) {
				currentTimer = currentSession.break_after_session;
				paused = true;
				tickListeners.forEach(listener -> listener.onBreakStart(this));
			}
		}

		tickListeners.forEach(listener -> listener.onTickEnd(this));
	}

	private Path getRandomImage(List<Path> images) {
		return images.get(random.nextInt(images.size()));
	}

	private boolean initializeNextSession() {
		currentImage = null;
		currentSession = null;

		if (sessionIterator == null || !sessionIterator.hasNext()) {
			finished = true;
			tickListeners.forEach(listener -> listener.onFinished(this));

			return false;
		}

		currentSession = sessionIterator.next();
		imageList.clear();
		imageList.addAll(getFileSet(currentSession.paths, currentSession.include_subdirs));
		finished = false;
		currentImageCount = 0;

		return true;
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
}

class ImageFileVisitor extends SimpleFileVisitor<Path> {
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
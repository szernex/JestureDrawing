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
	private GestureClass.GestureSession currentSession;
	private ArrayList<Path> imageList;
	private int currentTimer;
	private int currentImageCount;
	private Path currentImage;
	private Random random;

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

	public void initialize(GestureClass gestureClass) {
		this.gestureClass = gestureClass;
		sessionIterator = gestureClass.sessions.listIterator();
		random = new Random(System.currentTimeMillis());
		currentTimer = 0;

		initializeNextSession();
	}

	public void tick() {
		if (finished)
			return;

		currentTimer--;

		if (currentTimer > 0)
			return;

		if (currentImageCount < currentSession.image_count) {
			currentImage = getRandomImage(imageList);
			currentTimer = currentSession.interval;
			currentImageCount++;
		} else {
			currentTimer = currentSession.break_after_session;
			initializeNextSession();
		}
	}

	private Path getRandomImage(List<Path> images) {
		return images.get(random.nextInt(images.size()));
	}

	private void initializeNextSession() {
		currentImage = null;
		currentSession = null;

		if (sessionIterator == null || !sessionIterator.hasNext()) {
			finished = true;
			return;
		}

		currentSession = sessionIterator.next();
		imageList = new ArrayList<>(getFileSet(currentSession.paths, currentSession.include_subdirs));
		finished = false;
		currentImageCount = 0;
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
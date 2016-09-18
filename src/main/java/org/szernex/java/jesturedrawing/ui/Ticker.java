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
	private GestureClass.GestureSession currentSession;
	private int currentTimer;
	private Path currentImage;
	private int currentImageCount;
	private boolean finished;

	private int sessionIndex;
	private List<Path> images;
	private Random random;

	public GestureClass.GestureSession getCurrentSession() {
		return currentSession;
	}

	public int getCurrentTimer() {
		return currentTimer;
	}

	public Path getCurrentImage() {
		return currentImage;
	}

	public boolean isFinished() {
		return finished;
	}

	public void initialize(GestureClass c) {
		gestureClass = c;

		if (gestureClass.sessions.size() == 0) {
			logger.error("Ticker initialized with GestureClass containing no GestureSession");
			return;
		}

		sessionIndex = -1;
		selectNextSession(gestureClass);
		currentTimer = currentSession.interval;
		random = new Random(System.currentTimeMillis());
		currentImage = getRandomImagePath(images);

		for (Path p : images) {
			logger.debug(p.toString());
		}
	}

	public void tick() {
		if (finished)
			return;

		currentTimer--;

		if (currentTimer <= 0) {
			logger.debug("Timer reached 0");

			// only decrement counter when we're not finishing a break
			if (currentImage != null)
				currentImageCount--;

			if (currentImageCount > 0) {
				logger.debug("Selected new random image");

				currentImage = getRandomImagePath(images);
				currentTimer = currentSession.interval;
			} else {
				logger.info("Session " + currentSession.title + " finished. Starting break of " + currentSession.break_after + " seconds.");

				currentImage = null;
				currentTimer = currentSession.break_after;
				selectNextSession(gestureClass);
			}
		}
	}

	private void selectNextSession(GestureClass gc) {
		sessionIndex++;

		if (sessionIndex >= gc.sessions.size()) {
			finished = true;
			currentSession = null;
		} else {
			currentSession = gc.sessions.get(sessionIndex);
			currentImageCount = currentSession.image_count;
			images = new ArrayList<>(getFileList(currentSession.paths, currentSession.include_subdirs));
		}
	}

	private Path getRandomImagePath(List<Path> paths) {
		return paths.get(random.nextInt(paths.size()));
	}

	private Set<Path> getFileList(List<String> paths, boolean recursive) {
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
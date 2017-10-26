package ch.bfh.leap;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ij.IJ;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * @author lite
 *
 */
public class ScrollView extends Pane {

	private List<ExImageView> images;
	private STATE state;

	private SimpleScroll scroll; // Handles the scroll animation

	private int index;
	protected Dimension dim;
	
	private double timeSinceZoom;

	/**
	 * Initializes a new ScrollView
	 * 
	 * @param width
	 *            the width of the ScrollView
	 * @param height
	 *            the height of the ScrollView
	 */
	public ScrollView(Dimension dim) {
		state = STATE.Initializing;

		images = new ArrayList<ExImageView>();
		this.dim = dim;
		scroll = new SimpleScroll(0, 0, dim.getWidth());
		timeSinceZoom = 0;
	}

	/**
	 * Initializes a new ScrollView and loads the images from the given paths. The
	 * image loaded from the first path will be centered in the view.
	 * 
	 * @param width
	 *            the width of the ScrollView
	 * @param height
	 *            the height of the ScrollView
	 * @param paths
	 *            the paths of the images (may be absolute)
	 */
	public ScrollView(List<String> paths) {
		loadImages(paths);
		iniView();
		scroll = new SimpleScroll(0, 0, dim.getWidth());
	}

	private void iniView() {
		if (!images.isEmpty()) {
			images.get(0).setX(0);
			images.get(0).setY(0);
			images.get(0).setVisible(true);
			state = STATE.Stationary;
		}
		int width = (int)getCurrentWidth();
		int height = (int)getCurrentHeight();
		
		dim = new Dimension(width, height);
	}

	/**
	 * Loads the image from the given path
	 * 
	 * @param path
	 *            location of the image
	 */
	public void loadImage(String path) {
		addImage(SwingFXUtils.toFXImage(IJ.openImage(path).getBufferedImage(), null));
	}

	/**
	 * Loads all images from the given path
	 * 
	 * @param paths
	 *            the respective location of the images
	 */
	public void loadImages(List<String> paths) {
		for (String s : paths)
			loadImage(s);
	}

	/**
	 * adds all given images
	 * 
	 * @param images
	 *            to add
	 */
	public void addImages(List<Image> images) {
		for (Image i : images) {
			ExImageView iv = getIV(i);
			this.images.add(iv);
			this.getChildren().add(iv);
		}
		if (state.equals(STATE.Initializing))
			iniView();
	}

	/**
	 * Adds the given image
	 * 
	 * @param image
	 *            to add
	 */
	public void addImage(Image image) {
		addImages(Arrays.asList(image));
	}

	private ExImageView getIV(Image image) {
		ExImageView view = new ExImageView(dim, image);
		view.setVisible(false);
		view.setY(0);
		return view;
	}

	/**
	 * @return Whether or not the current image is zoomed
	 */
	public boolean isZoomed() {
		return getCurrent().isZoomed();
	}

	/**
	 * Zoom the image in on a specific point
	 * 
	 * @param zoom
	 *            The degree by which the image will be scaled
	 * @param point
	 *            The center point of the zoom
	 */
	public void zoom(double zoom, Point2D point) {
		if (state.equals(STATE.Stationary))
			getCurrent().zoom(zoom, point);
		timeSinceZoom = 0;
	}

	public double getZoomX() {
		ExImageView current = getCurrent();
		if (current.isZoomed())
			return current.getScaleX();
		return 1.0;
	}

	public double getZoomY() {
		ExImageView current = getCurrent();
		if (current.isZoomed())
			return current.getScaleY();
		return 1.0;
	}

	public Dimension getDimension() {
		return dim;
	}
	
	/**
	 * Moves the current picture by a certain delta
	 * 
	 * @param delta
	 *            How much the current picture will be moved
	 */
	public void move(Point2D delta) {
		ExImageView view = getCurrent();
		if (state.equals(STATE.Stationary)) {
			view.move(delta);
		}
	}

	/**
	 * Rotates the current image by a certain degree
	 * 
	 * @param rot
	 *            degree of rotation
	 */
	public void rotatePicture(double rot) {
		if (state.equals(STATE.Stationary)) {
			getCurrent().setRotate(rot);
		}
	}

	/**
	 * @param d
	 *            The adjusted Brightness
	 */
	public void setBrightness(double d) {
		if (state.equals(STATE.Stationary) || isZoomed())
			getCurrent().setBrightness(d);
	}

	/**
	 * @param d
	 *            By how much to adjust the Brightness of the current picture
	 */
	public void changeBrightnessBy(double d) {
		if (state.equals(STATE.Stationary))
			getCurrent().setBrightness(getCurrent().getBrightness() + d);
	}

	/**
	 * Resets the current image to it's initial status
	 */
	public void reset() {
		if (state.equals(STATE.Stationary))
			getCurrent().reset();
	}
	
	public double getCurrentWidth() {
		return getCurrent().getLayoutBounds().getWidth();
	}
	
	public double getCurrentHeight() {
		return getCurrent().getLayoutBounds().getHeight();
	}
	
	public double getCurrentX() {
		return getLayoutX();
	}
	public double getCurrentY() {
		return getLayoutY();
	}

	/**
	 * Updates actions such as scrolling
	 * 
	 * @param time
	 *            Time in seconds
	 */
	public void update(double time) {
		if(isZoomed()) {
			if(timeSinceZoom > 0.5 && getCurrent().getZoom() > 0.95) {
				getCurrent().resetZoom();
			} else {
				timeSinceZoom += time;
			}
		} else if (isScrolling()) {
			scroll.update(time);
			setShift(scroll.getX());

			if (getShift() == 0 && scroll.isFinished())
				state = STATE.Stationary;
		}
		
	}

	public void flush(double time) {
		if (isZoomed() || getShift() == 0 || getCurrent().getX() == 0)
			return;
		state = STATE.Scrolling;
		double shift = getShift();
		double width = dim.getWidth();

		if (shift > width / 2) {
			scroll = new SimpleScroll(shift, width, dim.getWidth());
		} else if (shift < -width / 2) {
			scroll = new SimpleScroll(shift, -width, dim.getWidth());
		} else {
			scroll = SimpleScroll.getScrollToZero(shift, dim.getWidth());
		}
	}


	public void scrollTo(double shift, double t) {
		state = STATE.Scrolling;
		double speed = (shift-getShift())/t;
		scroll = new SimpleScroll(getShift(), shift, speed);
	}
	
	/**
	 * Initiates a scroll sequence towards the left side
	 */
	public void scrollLeft() {
		if (!isZoomed()) {
			state = STATE.Scrolling;
			scroll = new SimpleScroll(getShift(), -dim.getWidth(), dim.getWidth());
		}
	}

	/**
	 * Initiates a scroll sequences towards the right side
	 */
	public void scrollRight() {
		if (!isZoomed()) {
			state = STATE.Scrolling;
			scroll = new SimpleScroll(getShift(), dim.getWidth(), dim.getWidth());
		}
	}

	/**
	 * Shifts the view to an absolute position
	 * 
	 * Updates the positions of all visible images
	 * 
	 * @param shift
	 *            The distance from the central picture to the left hand border
	 */
	private void setShift(double shift) {
		if (isZoomed())
			return;

		if (shift == 0) {
			getCurrent().setX(0);
			getNext().setX(dim.getWidth());
			getPrev().setX(-dim.getWidth());
			return;
		}

		ImageView current = getCurrent();
		current.setX(shift);

		ImageView next = getNext();
		ImageView prev = getPrev();

		prev.setX(-dim.getWidth() + shift);
		next.setX(dim.getWidth() + shift);

		if (current.getX() >= dim.getWidth()) {
			getNext().setVisible(false);
			previous();

			getPrev().setX(-dim.getWidth() + getShift());
			getPrev().setVisible(true);
		} else if (-current.getX() >= dim.getWidth()) {
			getPrev().setVisible(false);
			next();

			getNext().setX(dim.getWidth() + getShift());
			getNext().setVisible(true);
		}

		setVisible(getCurrent());
		setVisible(getNext());
		setVisible(getPrev());
	}

	public void shiftTo(double shift) {
		if (scroll.isFinished()) {
			setShift(shift);
		}
	}

	private void setVisible(ImageView view) {
		if (!view.isVisible())
			view.setVisible(true);
	}

	/**
	 * @return the current shift of the center image
	 */
	public double getShift() {
		return getCurrent().getX();
	}

	/**
	 * @return whether the ScrollView is currently scrolling or not
	 */
	public boolean isScrolling() {
		return state == STATE.Scrolling;
	}

	/**
	 * Shifts the view by the given amount of pixel
	 * 
	 * @param shift
	 *            by how many pixel to shift the view
	 */
	public void shift(double shift) {
		setShift(getShift() + shift);
	}

	private ExImageView getNext() {
		return images.get(getBounded(index + 1));
	}

	private ExImageView getPrev() {
		return images.get(getBounded(index - 1));
	}

	private ExImageView getCurrent() {
		return images.get(index);
	}

	private void previous() {
		getCurrent().setVisible(false);
		if (--index < 0)
			index = images.size() - 1;
		state = STATE.Stationary;
	}

	private void next() {
		getCurrent().setVisible(false);
		if (++index > images.size() - 1)
			index = 0;
		state = STATE.Stationary;
	}

	private int getBounded(int i) {
		if (i > images.size() - 1)
			return 0;
		if (i < 0)
			return images.size() - 1;
		return i;
	}

	private static enum STATE {
		Initializing, Scrolling, Stationary, Shifting, None;
	}

	private static class SimpleScroll {
		private double speed;
		private double x;
		private double to;

		public SimpleScroll(double from, double to, double speed) {
			x = from;
			this.to = to;
			this.speed = speed;

		}

		public static final SimpleScroll getScrollToZero(double from, double speed) {
			return new SimpleScroll(from, 0.0, speed);
		}

		/**
		 * Updates the scrolling animation
		 * 
		 * @param time
		 *            Time in seconds
		 */
		public void update(double time) {
			if (x != to) {
				double dist = to - x; // Distance to travel
				double diff = time * speed;

				if (diff > Math.abs(dist)) {
					x = to;
					return;
				}

				if (dist > 0) {
					x += diff;
				} else if (dist < 0) {
					x -= diff;
				}
			}
		}

		/**
		 * @return X Position
		 */
		public double getX() {
			return x;
		}

		/**
		 * @return Whether or not the Scroll has reached its finishing line
		 */
		public boolean isFinished() {
			return x == to;
		}
		
	}
}

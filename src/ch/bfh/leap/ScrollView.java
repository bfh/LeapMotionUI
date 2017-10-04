package ch.bfh.leap;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ij.IJ;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * @author lite
 *
 */
public class ScrollView extends Pane {
	private static double PERCENTAGE = 0.55d; // By what percentage the view should have shifted i. e.
												// how much of the next image is visible on screen

	private List<ImageView> images;
	private STATE state;

	private SimpleScroll scroll;
	private double previousShift;
	private double scrollSpeed;
	private int index;

	Dimension screen;

	/**
	 * Initializes a new ScrollView
	 * 
	 * @param width
	 *            the width of the ScrollView
	 * @param height
	 *            the height of the ScrollView
	 */
	public ScrollView() {
		state = STATE.Initializing;

		images = new ArrayList<ImageView>();
		index = 0;
		previousShift = 0;
		scrollSpeed = 0;
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
	}

	private void iniView() {
		if (!images.isEmpty()) {
			images.get(0).setX(0);
			images.get(0).setY(0);
			images.get(0).setVisible(true);
			state = STATE.Stationary;
		}
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
			ImageView iv = getIV(i);
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

	private ImageView getIV(Image image) {
		ImageView view = new ImageView(image);
		view.setVisible(false);
		view.setY(0);
		return view;
	}

	/**
	 * @param time
	 *            Time in seconds
	 */
	public void update(double time) {
		switch (state) {
		case Scrolling: {
			scroll.update(time);
			setShift(scroll.getX());
			
			if(getShift()==0 && scroll.isFinished())
				state = STATE.Stationary;
		}
			break;
		default:
			break;
		}
	}
	public void flush(double time) {
		if(getShift() == 0 || getCurrent().getImage().getWidth() == 0)
			return;
		state = STATE.Scrolling;
		double shift = getShift();
		double width = getCurrent().getImage().getWidth();
		double percentage = shift / width;
		double speed = scrollSpeed / time;
		
		if(percentage > 0) {
			if(percentage > PERCENTAGE || speed > 40) {
				scroll = SimpleScroll.getScroll(speed, shift, width);
			} else {
				scroll = SimpleScroll.getScrollToZero(speed, shift);
			}			
		} else if (percentage < 0) {
			if(-percentage > PERCENTAGE || speed > -40) {			
				scroll = SimpleScroll.getScroll(speed, shift, -width);
			} else {
				scroll = SimpleScroll.getScrollToZero(speed, shift);
			}
		}
	}

	/**
	 * Initiates a scroll sequence towards the left side
	 */
	public void scrollLeft() {
		state = STATE.Scrolling;
		scroll = SimpleScroll.getScroll(getShift(), -getCurrent().getImage().getWidth());
	}
	public void scrollLeft(double initialSpeed) {
		state  = STATE.Scrolling;
		scroll = SimpleScroll.getScroll(initialSpeed, getShift(), -getCurrent().getImage().getWidth());
	}

	/**
	 * Initiates a scroll sequences towards the right side
	 */
	public void scrollRight() {
		state = STATE.Scrolling;
		scroll = SimpleScroll.getScroll(getShift(), getCurrent().getImage().getWidth());
	}
	public void scrollRight(double initialSpeed) {
		state = STATE.Scrolling;
		scroll = SimpleScroll.getScroll(initialSpeed, getShift(), getCurrent().getImage().getWidth());
	}
	/**
	 * Shifts the view to an absolute position
	 * 
	 * @param shift
	 *            The distance from the central picture to the left hand border
	 */
	public void setShift(double shift) {
		previousShift = getShift();
		if (shift == 0) {
			getCurrent().setX(0);
			getNext().setX(getCurrent().getImage().getWidth());
			getPrev().setX(-getPrev().getImage().getWidth());
			return;
		}

		ImageView current = getCurrent();
		current.setX(shift);

		ImageView next = getNext();
		ImageView prev = getPrev();
		
		prev.setX(-current.getImage().getWidth() + shift);
		next.setX(current.getImage().getWidth() + shift);
		
		
		if (current.getX() >= current.getImage().getWidth()) {
			getNext().setVisible(false);
			previous();

			getPrev().setX(-getCurrent().getImage().getWidth() + getShift());
			getPrev().setVisible(true);
		} else if (-current.getX() >= current.getImage().getWidth()) {
			getPrev().setVisible(false);
			next();

			getNext().setX(getCurrent().getImage().getWidth() + getShift());
			getNext().setVisible(true);
		}
		
		setVisible(getCurrent());
		setVisible(getNext());
		setVisible(getPrev());
		scrollSpeed = getShift() - previousShift;
	}
	private void setVisible(ImageView view) {
		if(!view.isVisible())
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

	private ImageView getNext() {
		return images.get(getBounded(index + 1));
	}

	private ImageView getPrev() {
		return images.get(getBounded(index - 1));
	}

	private ImageView getCurrent() {
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
		Initializing, Scrolling, Shifting, Stationary, Flushing, Centering, None;
	}

	private static class SimpleScroll {
		private static final double defaultAcc = 150;
		private static final double defaultSpeed =200;
		
		private double x;
		private double acceleration;
		private double speed;
		private double to;


		private static SimpleScroll getEmpty() {
			return new SimpleScroll(0.0,0.0,0.0,0.0);
		}

		private SimpleScroll(double acceleration, double initialSpeed, double from, double to) {
			x = from;
			speed = initialSpeed;
			this.to = to;
			this.acceleration = acceleration;
		}

		public static final SimpleScroll getScrollToZero(double from) {
			return getScroll(from, 0.0);
		}

		public static final SimpleScroll getScrollToZero(double initialSpeed, double from) {
			return getScroll(initialSpeed, from, 0.0);
		}

		public static final SimpleScroll getScroll(double from, double to) {
			if (from > to)
				return getScroll(-defaultSpeed, from, to);
			if (from < to)
				return getScroll(defaultSpeed, from, to);
			return getEmpty();
		}

		public static final SimpleScroll getScroll(double initialSpeed, double from, double to) {
			if (from > to) {
				return new SimpleScroll(-defaultAcc, initialSpeed, from, to);
			} else if (from < to) {
				return new SimpleScroll(defaultAcc, initialSpeed, from, to);
			}
			return getEmpty();
		}

		/**
		 * Updates the scrolling animation
		 * 
		 * @param time
		 *            Time in seconds
		 */
		public void update(double time) {
			if (x != to) {
				speed = speed + acceleration * time;
				x += speed * time;
				if (acceleration > 0 && x > to) {
					x = to;
				} else if (acceleration < 0 && x < to) {
					x = to;
				}
			}			
		}

		public double getX() {
			return x;
		}
		public boolean isFinished() {
			return x == to;
		}
	}
}


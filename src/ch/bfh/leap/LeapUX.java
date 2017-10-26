package ch.bfh.leap;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.python.google.common.collect.Lists;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Vector;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * @author lite
 *
 */
public class LeapUX {
	private static Vector BOUNDARY = new Vector(199, 199, 199);

	private LeapObserver leap;

	private ScrollView sv;
	private ImageView zoom_indicator;

	private static long time = System.nanoTime();
	private static long previousTime = time;
	private static float pinchThreshhold = 0.9f;
	private static float grabThreshold = 0.9f;
	private static double idleTime = 3d;
	private static double waitTime = 0.4d;

	private double idleTimer;
	private double awaitHand;

	private double speedMod;
	private double previousDistance;
	private boolean handVisible;
	private double xCenter;

	boolean wasScrolling;
	boolean twoHands;

	private Point2D center;

	Rectangle borderRect1;
	Rectangle borderRect2;

	public LeapUX(ScrollView sv, Image zoom_indicator, Dimension dim, LeapObserver leap, double leapMod) {
		this.leap = leap;
		leap.enableGesture(Gesture.Type.TYPE_SWIPE);
		previousDistance = 0.0;
		this.sv = sv;
		handVisible = false;

		this.zoom_indicator = new ImageView(zoom_indicator);
		this.zoom_indicator.setVisible(false);

		center = new Point2D(dim.width / 2, dim.height / 2);
		xCenter = 0d;

		wasScrolling = false;

		double width = sv.getCurrentWidth();
		double height = sv.getCurrentHeight();

		if (width > height) {
			// Configure borders for maximum width
			borderRect1 = new Rectangle(dim.getWidth(), (dim.getHeight() - height) / 2, Color.BLACK);
			borderRect2 = new Rectangle(dim.getWidth() - borderRect1.getWidth(),
					dim.getHeight() - borderRect1.getHeight(), borderRect1.getWidth(), borderRect1.getHeight());
			borderRect2.setFill(Color.BLACK);
			sv.setLayoutY(borderRect1.getHeight());
		} else {
			// Configure borders for maximum height
			borderRect1 = new Rectangle((dim.getWidth() - width) / 2, dim.getHeight(), Color.BLACK);
			borderRect2 = new Rectangle(dim.getWidth() - borderRect1.getWidth(),
					dim.getHeight() - borderRect1.getHeight(), borderRect1.getWidth(), borderRect1.getHeight());
			borderRect2.setFill(Color.BLACK);
			sv.setLayoutX(borderRect1.getWidth());
		}
		this.speedMod = sv.getCurrentWidth() / leapMod;

		idleTimer = 0;
		awaitHand = 0;
	}

	public List<Node> getChildren() {
		List<Node> nodes = new ArrayList<>();
		nodes.add(sv);
		nodes.add(zoom_indicator);
		nodes.add(borderRect1);
		nodes.add(borderRect2);
		return nodes;
	}

	public void update() {
		previousTime = time;
		time = System.nanoTime();

		double elapsedTime = (double) (time - previousTime) / Math.pow(10, 9);

		sv.update(elapsedTime);
		leap.update();
		zoom_indicator.setVisible(false);

		if (!leap.isConnected())
			return;

		if (idleTimer > 0) {
			idleTimer -= elapsedTime;
			if(awaitHand > 0)
				awaitHand -= elapsedTime;
			return;
		}

		List<Integer> hands = leap.getActiveHands();

		if (handVisible && hands.size() == 0) {
			if (!sv.isScrolling() && sv.getShift() != 0.0)
				sv.flush(time);

			handVisible = false;
		} else if (hands.size() == 1) {
			if (handVisible == false) {
				handVisible = true;
				xCenter = clamp(leap.getHandPosition(hands.get(0))).getX();
			} else if (twoHands) {
				if(awaitHand > 0) {
					awaitHand -= elapsedTime;
					return;
				} else {
					twoHands = false;
				}
			}
			if (leap.handGrabbed(hands.get(0)) > grabThreshold) {
				idleTimer = idleTime;
				sv.flush(time);
			}

			float pinched = leap.handPinched(hands.get(0));
			if (pinched > pinchThreshhold) {
				// TODO use pinched movement
			}

			if (sv.isZoomed()) {
				Vector v = leap.getHandTranslation(hands.get(0));
				if (v != null) {
					sv.move(new Point2D(v.getX() * speedMod / sv.getZoomX(), v.getZ() * speedMod / sv.getZoomY()));
				}
			} else {
				List<Gesture> gestures = leap.gestureDetected(Gesture.Type.TYPE_SWIPE);

				for (Gesture g : gestures) {
					if (g != null && g.isValid()) {
						float temp = g.hands().get(0).palmPosition().getX();
						if (temp > 0) {
							sv.scrollRight();
						} else if (temp < 0) {
							sv.scrollLeft();
						}
					}
				}

				if (!sv.isScrolling()) {
					float x = leap.getHandPosition(hands.get(0)).getX();

					if (wasScrolling) {
						xCenter = x;
					} else {
						x = clamp((float) (x - xCenter), -BOUNDARY.getX(), BOUNDARY.getY());
						sv.shiftTo(x * speedMod);
					}
				}
			}
		} else if (hands.size() == 2) {
			sv.flush(time);
			twoHands = true;
			awaitHand = waitTime;

			if (!sv.isScrolling()) {

				zoom_indicator.setVisible(true);

				// handle rotation and scaling
				double distanceTo = leap.getHandPosition(hands.get(0)).distanceTo(leap.getHandPosition(hands.get(1)));

				if (previousDistance == 0)
					previousDistance = distanceTo;

				if (leap.thumbAligned(hands.get(0)) && leap.thumbAligned(hands.get(1))) {
					// zoom
					sv.zoom(previousDistance - distanceTo,center);

				} else {
					// Move target cross
					center = vectorToPoint2D(leap.getCenter(hands, sv.getDimension()));

					// Make it such that the user zooms towards the center of the painted target
					if(borderRect1.getWidth() > borderRect1.getHeight()) {

						zoom_indicator.setX(center.getX() - 0.5 * zoom_indicator.getImage().getWidth());
						zoom_indicator.setY(center.getY() - 0.5 * zoom_indicator.getImage().getHeight() + borderRect1.getHeight());

					} else {

						zoom_indicator.setX(center.getX() - 0.5 * zoom_indicator.getImage().getWidth() + borderRect1.getWidth());
						zoom_indicator.setY(center.getY() - 0.5 * zoom_indicator.getImage().getHeight());

					}
				}
				previousDistance = distanceTo;
			}
		}
		wasScrolling = sv.isScrolling();
	}

	/**
	 * Determines which axis to project on
	 * 
	 * @param v
	 * @return
	 */
	private Point2D vectorToPoint2D(Vector v) {
		return new Point2D(v.getX(), v.getZ());
	}

	private Vector clamp(Vector v) {
		return new Vector(clamp(v.getX(), -BOUNDARY.getX(), BOUNDARY.getX()),
				clamp(v.getY(), -BOUNDARY.getY(), BOUNDARY.getY()), clamp(v.getZ(), -BOUNDARY.getZ(), BOUNDARY.getZ()));
	}

	private float clamp(float d, float min, float max) {
		if (d < min)
			return min;
		if (d > max)
			return max;
		return d;
	}

}

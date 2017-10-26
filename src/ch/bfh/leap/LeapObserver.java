package ch.bfh.leap;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.HandList;
import com.leapmotion.leap.InteractionBox;
import com.leapmotion.leap.Vector;

import ch.bfh.leap.tools.ImageConverter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * @author Dominik Fischli
 * 
 *         This class servers as an intermediate between the Leap Motion
 *         controller and the rest of the application. To keep things neat,
 *         entities must save the frame on which they want to perform actions.
 *         This is because the Frames get constantly updated by the camera,
 *         which would lead to unexpected behavior if not controlled.
 *
 */
public class LeapObserver {
	private static Controller c = new Controller();
	private Frame frame;
	private Frame previousFrame;
	private InteractionBox iBox;

	public LeapObserver() {
		frame = c.frame();
		iBox = frame.interactionBox();
	}

	public LeapObserver(List<Controller.PolicyFlag> flags, List<Gesture.Type> gestures) {
		this();
		setupController(flags, gestures);
	}

	/**
	 * Use this method to setup controller behavior
	 * 
	 * @param flags
	 *            The Policies you want
	 * @param gestures
	 *            Gestures you want the controller to track
	 */
	public void setupController(List<Controller.PolicyFlag> flags, List<Gesture.Type> gestures) {
		if (!(flags == null || flags.isEmpty())) {
			for (Controller.PolicyFlag f : flags) {
				c.setPolicy(f);
			}
		}
		if (!(gestures == null || gestures.isEmpty())) {
			for (Gesture.Type g : gestures) {
				c.enableGesture(g);
			}
		}
	}

	public void update() {
		previousFrame = frame;
		frame = c.frame();
		if (!iBox.isValid())
			iBox = frame.interactionBox();
	}
	public void enableGesture(Gesture.Type g) {
		c.enableGesture(g);
	}
	public void setPolicy(Controller.PolicyFlag p) {
		c.setPolicy(p);
	}
	
	public boolean isRight(int id) {
		return getHand(id).isRight();
	}
	

	/**
	 * @param g
	 *            Searches for the given gesture among the detected ones
	 * @return all such gestures detected
	 */
	public List<Gesture> gestureDetected(Gesture.Type gType) {
		List<Gesture> result = new ArrayList<>();
		for (Gesture g : frame.gestures()) {
			if (g.type().equals(gType)) {
				result.add(g);
			}
		}
		return result;
	}

	/**
	 * @return the positions of the hands relative to the stored coordinate Systems
	 *         given by initialization. Returns an empty list if no hands are
	 *         observed. The integer stores the hands id.
	 * 
	 *         X is scaled along width Y is not scaled Z is scaled along height
	 * 
	 *         This assumes movements will stay level (assumption here is that level
	 *         movement will be easier to perform)
	 */
	public Map<Integer, Vector> getHandsPosition() {
		Map<Integer, Vector> map = new HashMap<>();

		for (Hand hand : frame.hands()) {
			map.put(hand.id(), hand.palmPosition());
		}
		return map;
	}

	public Map<Integer, Vector> getHandsPosition(Dimension dim) {
		Map<Integer, Vector> map = new HashMap<>();

		for (Hand hand : frame.hands()) {
			map.put(hand.id(), getHandPosition(hand, dim));
		}
		return map;
	}

	/**
	 * @param f
	 *            Current Frame
	 * @param id
	 *            ID of the Hand
	 * @return the hands absolute position in leap coordinates
	 */
	public Vector getHandPosition(int id) {
		for (Hand h : frame.hands()) {
			if (h.id() == id)
				return h.palmPosition();
		}
		return Vector.zero();
	}
	
	public Vector getAverageOver(int frames, int id) {
		Vector avrg = new Vector();
		for(int i = frames; i >= 0; i--) {
			Hand h = c.frame(i).hand(id);
			if(h.isValid()) {
				avrg.plus(h.palmPosition());
			}
		}
		return avrg.divide(frames);
	}

	public Vector getHandTranslation(int id) {
		for (Hand h : frame.hands()) {
			if (h.id() == id) {
				return h.translation(previousFrame);
			}
		}
		return Vector.zero();
	}

	public float getHandRotationX(int id) {
		return frame.hand(id).rotationAngle(c.frame(1), Vector.xAxis());
	}
	public float getHandRotationY(int id) {
		return frame.hand(id).rotationAngle(c.frame(1), Vector.yAxis());
	}
	public float getHandRotationZ(int id) {
		return frame.hand(id).rotationAngle(c.frame(1), Vector.zAxis());
	}
	
	public Vector vectorBetween(int hFrom, int hTo) {
		Hand from = getHand(hFrom);
		Hand to = getHand(hTo);
		
		return to.palmPosition().minus(from.palmPosition());
	}
	
	public Vector getTranslation() {
		return frame.translation(previousFrame);
	}

	public Vector getHandNormal(int id) {
		return frame.hand(id).palmNormal();
	}
	
	/**
	 * @param f
	 *            Current Frame
	 * @param id
	 *            ID of the hand
	 * @param dim
	 *            Target Dimension
	 * @return the Hands position relative to the target dimension
	 */
	public Vector getHandPosition(int id, Dimension dim) {
		return getHandPosition(frame.hand(id), dim);
	}

	private Vector getHandPosition(Hand h, Dimension dim) {
		Vector v = iBox.normalizePoint(h.palmPosition());
		return v == null ? null
				: new Vector(v.getX() * (float) dim.getWidth(), v.getY(), v.getZ() * (float) dim.getHeight());
	}

	private Vector project(Vector v, Dimension dim) {
		if (v == null || dim == null || !v.isValid()) {
			throw new Error("Illegal Arguments: " + " : " + v + " : " + dim);
		}
		Vector n = iBox.normalizePoint(v);
		return new Vector(n.getX() * (float) dim.getWidth(), n.getY(), n.getZ() * (float) dim.getHeight());
	}

	/**
	 * @return the most recently recorded frame
	 */
	public Frame getFrame() {
		return c.frame();
	}

	public void setFrame(Frame f) {
		previousFrame = frame;
		frame = f;
	}

	/**
	 * @param x
	 *            how many frames ago
	 * @return the frame recorded x-frames ago
	 */
	public Frame pastFrame(int x) {
		return c.frame(x);
	}

	public Vector getCenter(List<Integer> id, Dimension dim) {

		List<Vector> vectors = new ArrayList<>();
		for (Integer i : id) {
			vectors.add(frame.hand(i).palmPosition());
		}
		float fac = vectors.size();

		float x = 0.0f, y = 0.0f, z = 0.0f;
		for (Vector v : vectors) {
			x += v.getX();
			y += v.getY();
			z += v.getZ();
		}
		return project(new Vector(x / fac, y / fac, z / fac), dim);
	}

	/**
	 * @param id
	 *            the ID of the hand of interest
	 * @return whether or not the hand is upside down
	 */
	public boolean handActive(int id) {
		return handActive(frame.hand(id));
	}

	private boolean handActive(Hand hand) {
		return hand.palmNormal().getY() < 0;
	}

	/**
	 * @return a List of the ID's of all hands facing the camera
	 */
	public List<Integer> getActiveHands() {
		List<Integer> hands = new ArrayList<>();

		for (Hand h : frame.hands()) {
			if (handActive(h)) {
				hands.add(h.id());
			}
		}
		return hands;
	}

	public List<Integer> getHands() {
		List<Integer> hands = new ArrayList<>();

		for (Hand h : frame.hands())
			hands.add(h.id());

		return hands;
	}

	/**
	 * @return the amount of hands currently visible
	 */
	public int handsVisible() {
		return c.frame().hands().count();
	}

	/**
	 * @param f
	 *            Frame under scrutiny
	 * @param id
	 *            ID of the hand of interest
	 * @return
	 */
	public float handPinched(int id) {
		return frame.hand(id).pinchStrength();
	}
	
	public float handGrabbed(int id) {
		float grab = frame.hand(id).grabStrength();
		System.out.println(grab);
		return grab;
	}

	public Vector getVelocity(int id) {
		return frame.hand(id).palmVelocity();
	}

	public Map<Integer, Float> handsPinched() {
		Map<Integer, Float> map = new HashMap<>();
		for (Hand hand : frame.hands()) {
			map.put(hand.id(), hand.pinchStrength());
		}
		return map;
	}

	public boolean thumbAligned(int handId) {
		Hand h = getHand(handId);
		
		if(!h.isValid())
			return false;
		
		
		Finger thumb = h.fingers().fingerType(Finger.Type.TYPE_THUMB).get(0);
		Finger index = h.fingers().fingerType(Finger.Type.TYPE_INDEX).get(0);
		
		Bone meta = index.bone(Bone.Type.TYPE_METACARPAL);
		
		float proximation = 0.5f * meta.center().distanceTo(thumb.tipPosition()) 
				+ 0.5f * meta.nextJoint().distanceTo(thumb.tipPosition());
		
		return proximation < 47;
		
	}
	
	public float averageX(int count, int handID) {
		float result = 0f;
		for(int i = 0; i < count; ++i) {
			result += c.frame(i).hand(handID).palmPosition().getX();
		}
		return result / count;
	}

	private Hand getHand(int id) {
		for (Hand h : frame.hands()) {
			if (h.id() == id)
				return h;
		}
		return Hand.invalid();
	}

	/**
	 * @return whether a Leap Motion camera is connected or not
	 */
	public boolean isConnected() {
		return c.isConnected();
	}

	/**
	 * @return whether the current frame received from the Leap Motion camera is
	 *         valid or not
	 */
	public boolean isValid() {
		return c.frame().isValid();
	}

	/**
	 * @return whether you can load images off the camera
	 */
	public boolean imagesAvaible() {
		return isConnected() && isValid() && !c.frame().images().isEmpty();
	}
	
	public Controller getController() {
		return c;
	}

	/**
	 * @return the BufferedImage of the current camera feed
	 */
	public BufferedImage getGrayscaleBufferedImage() {
		com.leapmotion.leap.Image image = frame.images().get(0);
		return ImageConverter.getGrayscaleBufferedImage(image.height(), image.width(), image.data());
	}

	/**
	 * @return the JavaFX image of the current camera feed
	 */
	public static Image getGrayscaleImage() {
		com.leapmotion.leap.Image image = c.frame().images().get(0);
		return ImageConverter.getGrayscaleJavaFXImage(image.height(), image.width(), image.data());
	}

	/**
	 * Prints currently observed Data
	 */
	public void printData() {
		System.out.println("Frame id: " + frame.id() + ", timestamp: " + frame.timestamp() + ", hands: "
				+ frame.hands().count() + ", fingers: " + frame.fingers().count() + ", tools: " + frame.tools().count()
				+ ", gestures " + frame.gestures().count());
	}
}
package ch.bfh.leap;

import java.awt.image.BufferedImage;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * @author lite
 *
 *	The LeapObserver provides processed data from the Leap Motion camera
 *
 */
public class LeapObserver {

	private LeapObserver() {}
	
	private static Controller controller = getController();
	
	
	
	/**
	 * Sets up the Controller two A: Pass images to the computer and B: enable the tracking of the swipe gesture
	 */
	private static Controller getController() {
		Controller c = new Controller();
		c.setPolicy(Controller.PolicyFlag.POLICY_IMAGES);
		c.enableGesture(Gesture.Type.TYPE_SWIPE);
		return c;
	}
	public static boolean isConnected() {
		return controller.isConnected();
	}
	public static boolean isValid() {
		return controller.frame().isValid();
	}
	public static boolean imagesAvaible() {
		return isConnected() && isValid() && !controller.frame().images().isEmpty();
	}
	public static int handsCount() {
		return controller.frame().hands().count();
	}
	public static Vector getTranslation() {
		return controller.frame().translation(controller.frame(1));
	}
	public static Gesture getSwipe() {
		for(Gesture g : controller.frame().gestures()) {
			if(g.type().equals(Gesture.Type.TYPE_SWIPE))
					return g;
		}
		return Gesture.invalid();
	}
	public static boolean gesturesDetected() {
		return !controller.frame().gestures().isEmpty();
	}
	/**
	 * @return returns the current frame if available, null if not
	 */
	public static Frame getFrame() {
		return isValid() ? controller.frame() : null;
	}
	/**
	 * @return The grayscale image as seen by the camera
	 */
	public static BufferedImage getGrayscaleBufferedImage() {
		return getGrayscaleBufferedImage(controller.frame().images().get(0));
	}
	public static Image getGrayscaleImage() {
		return getGrayscaleImage(controller.frame().images().get(0));
	}
	
	/**
	 * @param image
	 * @return the conversion of the given image to grayscale
	 */
	public static BufferedImage getGrayscaleBufferedImage(com.leapmotion.leap.Image image) {		
		return getBufferedImageFromData(image.height(), image.width(), image.data());	
	}
	public static Image getGrayscaleImage(com.leapmotion.leap.Image image) {
		return getImageFromData(image.height(), image.width(), image.data());
	}
	
	private  static BufferedImage getBufferedImageFromData(int height, int width, byte[] data) {	
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				
		for(int i = 0; i < width * height; ++i){
			int a = 0xFF << 24;
			int r = (data[i] & 0xFF) << 16;
			int g = (data[i] & 0xFF) << 8;
			int b = data[i] & 0xFF;
			
			int x = i % width;
			int y = (i - x)/width;
			
			result.setRGB(x, y, a | r | g | b);
		}
		
		return result;		
	}
	private static Image getImageFromData(int height, int width, byte[] data) {
		WritableImage wr = new WritableImage(width, height);
		PixelWriter pw = wr.getPixelWriter();
		
		for(int i = 0; i < width * height; ++i) {
			int r = (data[i] & 0xFF) << 16;
			int g = (data[i] & 0xFF) << 8;
			int b = data[i] & 0xFF;
			
			int x = i % width;
			int y = (i - x)/width;
			
			pw.setArgb(x, y, (0xFF << 24) | r | g | b);
		}
		return wr;
	}
	
	
	private static boolean handIsPinched(Hand hand) {
		return hand.pinchStrength() > 0.98f; 
	}
	public static boolean handIsPinched() {
		for(Hand hand : controller.frame().hands()) {
			if(handIsPinched(hand))
				return true;
		}
		return false;
	}
	public static boolean swipeExists() {
		return getSwipe() != Gesture.invalid();
	}
	
	public static void printData() {
		Frame frame = controller.frame();
		System.out.println("Frame id: " + frame.id()
        + ", timestamp: " + frame.timestamp()
        + ", hands: " + frame.hands().count()
        + ", fingers: " + frame.fingers().count()
        + ", tools: " + frame.tools().count()
        + ", gestures " + frame.gestures().count()
		+ ", pinched: " + handIsPinched(frame.hand(0)));
	}
}

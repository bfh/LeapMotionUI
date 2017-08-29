package ch.bfh.leap;

import java.awt.image.BufferedImage;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Hand;

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
	private static boolean isConnected() {
		return controller.isConnected();
	}
	private static boolean isValid() {
		return controller.frame().isValid();
	}
	public static boolean imagesAvaible() {
		return isConnected() && isValid() && !controller.frame().images().isEmpty();
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
	public static BufferedImage getGrayscale() {
		Frame frame = controller.frame();
		return getGrayscale(frame.images().get(0));
	}
	
	/**
	 * @param image
	 * @return the conversion of the given image to grayscale
	 */
	public static BufferedImage getGrayscale(com.leapmotion.leap.Image image) {		
		int height = image.height();
		int width = image.width();
				
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				
		byte[] imageData = image.data();
		
		for(int i = 0; i < width * height; ++i){
			int r = (imageData[i] & 0xFF) << 16;
			int g = (imageData[i] & 0xFF) << 8;
			int b = imageData[i] & 0xFF;
			
			int x = i % width;
			int y = (i - x)/width;
			
			result.setRGB(x, y, r | g | b);
		}
		
		return result;		
	}
	
	private static boolean handIsPinched(Hand hand) {
		return hand.pinchStrength() > 0.8f; 
	}
	public static int getPinched() {
		int id = -1;
		for(Hand hand : controller.frame().hands()) {
			if(handIsPinched(hand)) {
				id = hand.id();
			}
		}
		return id;
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
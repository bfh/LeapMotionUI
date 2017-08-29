package ch.bfh.leap;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;

public class MotionTracker {
	private int firstHandID;
	private int secondHandID;
	private float rotation;
	private float distance;
	
	private Vector translation;
	private float zoomFactor;
	private float rotationFactor;	
	
	private Frame previousFrame;
	
	public MotionTracker(Hand h1, Hand h2) {
		updateHands(h1, h2);
	}
	
	public void update(Frame frame) {
		Hand h1 = frame.hand(firstHandID);
		Hand h2 = frame.hand(secondHandID);
		
		float d = h1.palmPosition().distanceTo(h2.palmPosition());
		float r = h1.palmPosition().angleTo(h2.palmPosition());
		
		if(previousFrame.equals(null))
			return;
		
		translation = frame.translation(previousFrame);		
		
		rotationFactor = r - rotation;
		rotation = r;
		
		zoomFactor = d - distance;
		distance = d;	
		
		frame.translation(previousFrame);
	}
	public void updateHands(Hand h1, Hand h2) {
		firstHandID = h1.id();
		secondHandID = h2.id();
		
		distance = h1.palmPosition().distanceTo(h2.palmPosition());
		rotation = h1.palmPosition().angleTo(h2.palmPosition());
		
		translation = null;
		previousFrame = null;
	}
	public float getZoom() {
		return zoomFactor;
	}	
	public float getRotation() {
		return rotationFactor;
	}
	public Vector getTranslation() {
		return translation;
	}
}

package ch.bfh.leap;

import java.util.List;

import com.leapmotion.leap.Gesture;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;

public class LeapUI  extends Displayer{

	public LeapUI(List<String> imagePaths) {
		super(imagePaths);
		current();
	}
	public void handleLeapData() {
		// Checking for hands
		if (LeapObserver.handsCount() < 2) {

			// Checking for pinch
			if (LeapObserver.handIsPinched()) {
				float d = LeapObserver.getTranslation().getX();

				System.out.println("Translation with pinched movement: " + d);

				adjustBrightness(d);
			} else if(LeapObserver.isSwiped()){
				// Handles swipe detection

				Gesture g = LeapObserver.getSwipe();
				if (g.isValid()) {
					float d = LeapObserver.getTranslation().getX();
					if (d > 0)
						previous();
					if (d < 0)
						next();
				}
			}					
		} else {
			//TODO: Handles two hands interactions
		}
	}
}

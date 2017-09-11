package ch.bfh.leap;

import javafx.util.Duration;

import com.leapmotion.leap.Gesture;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
@SuppressWarnings("restriction")
public class Main extends Application {
	// private static Image image = new Image("image.jpeg");
	private static ImageView iv = new ImageView();
	private static Image img = new Image("image.jpeg");

	@Override public void start(Stage stage) {
		final javafx.util.Duration oneFrameAmt = Duration.millis(1000/60);
		final KeyFrame oneFrame = new KeyFrame(oneFrameAmt, new EventHandler<ActionEvent>() {
		
			//Main loop of the program
			public void handle(ActionEvent event) {	

				//Handling the input from the Leap Motion camera
				if(LeapObserver.isValid()) {
					if(LeapObserver.handsCount() < 2) {
						
						//Handles pinch detections
						if(LeapObserver.handIsPinched()) {
							float d = LeapObserver.getTranslation().getX();
							System.out.println("Translation with pinched movement: " + d);
						}
						//Handles swipe detections
						else {
							Gesture g = LeapObserver.getSwipe();
							if(g.isValid()) {
								if(g.state().equals(Gesture.State.STATE_START)) {
									//TODO: Handle Swipe
									float d = LeapObserver.getTranslation().getX();
									if(d > 0)
										System.out.println("Swiped right");
									if(d < 0)
										System.out.println("Swiped left");
								}
							}
						}
					}

					else {
						// TODO: Handle two hands interactions
					}
				}
				
				
				
				//Logic to get the grayscale image
				Image image = null;
				if(LeapObserver.imagesAvaible()) {
					image = SwingFXUtils.toFXImage(LeapObserver.getGrayscale(), null);
					iv.setImage(image);
				}
				else iv.setImage(img);
			}
		});
		
		final Timeline timeline = new Timeline();
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.getKeyFrames().add(oneFrame);
		
		Group root = new Group();
		Scene scene = new Scene(root);
		root.getChildren().add(iv);
		
		Rectangle2D viewport = new Rectangle2D(0,0,img.getHeight(),img.getWidth());
		iv.setImage(img);
		iv.setViewport(viewport);
		
		stage.setTitle("ImageView");
		stage.setScene(scene);
		stage.sizeToScene();
		
		timeline.play();
		
		stage.show();
	}

	public static void main(String... args) {
		Image image = null;
//		do {
//			if (LeapObserver.imagesAvaible())
//				image = SwingFXUtils.toFXImage(LeapObserver.getGrayscale(), null);
//		} while (image == null);
//		iv.setImage(image);
		Application.launch(args);
	}
}

package ch.bfh.leap;

import javafx.util.Duration;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Main extends Application {
	//private static Image image = new Image("image.jpeg");
	private static ImageView iv = new ImageView();
	
	@Override public void start(Stage stage) {
		final javafx.util.Duration oneFrameAmt = Duration.millis(1000/60);
		final KeyFrame oneFrame = new KeyFrame(oneFrameAmt, new EventHandler<ActionEvent>() {
			
			//Main loop of the program
			@Override
			public void handle(ActionEvent event) {	
				
				//Logic to get the grayscale image
				Image image = null;
				if(LeapObserver.imagesAvaible())
					image = SwingFXUtils.toFXImage(LeapObserver.getGrayscale(), null);
				iv.setImage(image);
				//LeapObserver.printData();
			}
		});
		
		final Timeline timeline = new Timeline();
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.getKeyFrames().add(oneFrame);
		
		Group root = new Group();
		Scene scene = new Scene(root);
		root.getChildren().add(iv);
		
		stage.setTitle("ImageView");
		stage.setScene(scene);
		stage.sizeToScene();
		
		timeline.play();
		
		stage.show();
	}
	
	public static void main(String... args) {
		Image image = null;
		do {
			if(LeapObserver.imagesAvaible())
				image = SwingFXUtils.toFXImage(LeapObserver.getGrayscale(), null);
		}while(image==null);
		iv.setImage(image);
		Application.launch(args);
	}
}

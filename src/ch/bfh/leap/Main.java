package ch.bfh.leap;

import javafx.util.Duration;

import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
	private static List<String> files;

	@Override
	public void start(Stage stage) throws Exception {
		files = ImagePrep.select();
		
		LeapUI leapUI = new LeapUI(files);
		
		
		EventHandler<ActionEvent> update = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				//leapUI.updateLeapVision();
				leapUI.handleLeapData();
			}
		};
		
		final Duration oneFrameAmt = Duration.millis(10000 / 60);
		final KeyFrame oneFrame = new KeyFrame(oneFrameAmt, update);
		final Timeline timeline = new Timeline();
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.getKeyFrames().add(oneFrame);
		
		Group root = new Group();
		Scene scene = new Scene(root);
		root.getChildren().add(leapUI);
		
		stage.setTitle("DICOM Viewer");
		stage.setScene(scene);
		stage.setFullScreen(true);

		timeline.play();
		stage.show();

	}
	
	public static void main(String... args) {

		Application.launch(args);
	}
}

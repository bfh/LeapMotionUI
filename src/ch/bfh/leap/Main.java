package ch.bfh.leap;

import javafx.util.Duration;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {
	private static List<String> files;

	private static Dimension dim = getDimension();
	
	private static Dimension getDimension() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Dimension dim = new Dimension(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
		return dim;
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		files = ImagePrep.select();
		files = Arrays.asList("/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000000.dcm",
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000001.dcm",
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000002.dcm",
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000003.dcm",
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000004.dcm"
				);	

		LeapUI leapUI = new LeapUI(files);
		
		ImageView iv = initializeLeapView();
		
		BorderPane bp = new BorderPane();
		bp.setBottom(iv);
		bp.setCenter(leapUI);
		
		
		EventHandler<ActionEvent> update = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				leapUI.handleLeapData();
				updateLeapVision(iv);
			}
		};
		
		final Duration oneFrameAmt = Duration.millis(10000 / 60);
		final KeyFrame oneFrame = new KeyFrame(oneFrameAmt, update);
		final Timeline timeline = new Timeline();
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.getKeyFrames().add(oneFrame);
		
		Group root = new Group();
		Scene scene = new Scene(root);
		root.getChildren().add(bp);
		
		stage.setTitle("DICOM Viewer");
		stage.setScene(scene);
		
		timeline.play();
		stage.show();

	}
	private void updateLeapVision(ImageView view) {
		if(LeapObserver.imagesAvaible()) {
			view.setImage(SwingFXUtils.toFXImage(LeapObserver.getGrayscale(), null));
		}
		else
			view.setVisible(false);
	}
	private ImageView initializeLeapView() {
		ImageView iv = new ImageView();
		do {
			updateLeapVision(iv);
		}while(!LeapObserver.imagesAvaible());
		iv.setVisible(true);
		return iv;
	}
	public static void main(String... args) {

		Application.launch(args);
	}
}

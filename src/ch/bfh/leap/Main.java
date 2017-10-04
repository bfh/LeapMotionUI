package ch.bfh.leap;

import javafx.util.Duration;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import com.leapmotion.leap.Gesture;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {
	private static List<String> files;

	// Handling the time
	private static long time = System.nanoTime();
	private static long previousTime = time;

	private static boolean handlingHand = false;

	private static Dimension dim = getDimension();

	private static Dimension getDimension() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Dimension dim = new Dimension(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
		return dim;
	}

	@Override
	public void start(Stage stage) throws Exception {
		// files = ImagePrep.select(); //Select the images to be displayed
		files = Arrays.asList(
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000000.dcm",
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000001.dcm",
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000002.dcm",
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000003.dcm",
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000004.dcm");
		List<Image> images = Arrays.asList(new Image("img_01.png"), new Image("img_02.png"), new Image("img_03.png"),
				new Image("img_04.png"), new Image("img_05.png"), new Image("img_06.png"));

		ScrollView sv = new ScrollView();
		// sv.loadImages(files);
		sv.addImages(images);

		EventHandler<ActionEvent> update = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {

				previousTime = time;
				time = System.nanoTime();

				double currentTime = ((double) (time - previousTime)) / Math.pow(10, 9);

				sv.update(currentTime);

				updateLeapView(sv, currentTime);
			}
		};

		final Duration oneFrameAmt = Duration.millis(1000 / 60);
		final KeyFrame oneFrame = new KeyFrame(oneFrameAmt, update);
		final Timeline timeline = new Timeline();
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.getKeyFrames().add(oneFrame);

		BorderPane bp = new BorderPane();
		bp.setCenter(sv);

		Button b1 = new Button("Left");
		b1.setOnAction(e -> sv.scrollLeft());

		Button b2 = new Button("Right");
		b2.setOnAction(e -> sv.scrollRight());

		HBox box = new HBox();
		box.getChildren().addAll(b1, b2);
		bp.setBottom(box);

		Group root = new Group();
		Scene scene = new Scene(root);
		root.getChildren().add(bp);

		stage.setTitle("DICOM Viewer");
		stage.setScene(scene);

		timeline.play();
		stage.show();

	}

	public static void main(String... args) throws IOException {
		Application.launch(args);
	}

	private static void updateLeapView(ScrollView sv, double time) {
		if (LeapObserver.isConnected()) {
			if (LeapObserver.handsCount() > 0) {
				handlingHand = true;

				Gesture g = LeapObserver.getSwipe();
				if (g != null && g.isValid()) {
					System.out.println(sv.isScrolling());
					float temp = g.hands().get(0).palmPosition().getX();
					if (temp > 0) {
						sv.scrollRight();
					} else if (temp < 0) {						
						sv.scrollLeft();
					}
					System.out.println(sv.isScrolling());
				}
				if (!sv.isScrolling()) {
					double d = LeapObserver.getFrame().hands().get(0).palmPosition().getX() * 7;
					sv.setShift(d);					
				}
			}
			if (!sv.isScrolling() && handlingHand && LeapObserver.handsCount() == 0) {
				sv.flush(time);
				handlingHand = false;
			}
		}
		// // Checking for hands
		// if (LeapObserver.handsCount() < 2) {
		//
		// // Checking for pinch
		// if (LeapObserver.handIsPinched()) {
		// float d = LeapObserver.getTranslation().getX();
		//
		// System.out.println("Translation with pinched movement: " + d);
		//
		// } else if (LeapObserver.isSwiped()) {
		// // Handles swipe detection
		//
		// Gesture g = LeapObserver.getSwipe();
		// if (g.isValid()) {
		// float d = LeapObserver.getTranslation().getX();
		// if (d > 0)
		// sv.scrollLeft();
		// if (d < 0)
		// sv.scrollRight();
		// }
		// } else {
		// double d = LeapObserver.getFrame().hands().get(0).palmPosition().getX()*300;
		// System.out.println("Shifting to " + d);
		// sv.setShift(d);
		// }
		// } else {
		// // TODO: Handles two hands interactions
	}
}

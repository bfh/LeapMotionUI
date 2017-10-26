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
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;

import ij.IJ;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
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

	private static Dimension DIM = getDimension();

	private static Dimension getDimension() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Dimension dim = new Dimension(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
		return dim;
	}

	private static double leapMod = 200;

	@Override
	public void start(Stage stage) throws Exception {
		//files = ImagePrep.select(); //Select the images to be displayed
		files = Arrays.asList(
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000000.dcm",
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000001.dcm",
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000002.dcm",
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000003.dcm",
				"/home/lite/Desktop/passerelle_LEAP/DICOM-EDES-DEMO-Manual Orginal.dcm/PA000001/ST000000/SE000002/IM000004.dcm");
		// List<Image> images = Arrays.asList(new Image("img_01.png"), new
		// Image("img_02.png"), new Image("img_03.png"),
		// new Image("img_04.png"), new Image("img_05.png"), new Image("img_06.png"));

		ScrollView sv = new ScrollView(DIM);
		sv.loadImages(files);

		LeapObserver leap = new LeapObserver();

		LeapUX l = new LeapUX(sv,
				SwingFXUtils.toFXImage(IJ
						.openImage("/home/lite/eclipse-workspace/LeapMotionUI/src/ch/bfh/leap/target_cross.png").getBufferedImage(), null),
				DIM, leap, leapMod);
		Leap3D l3d = new Leap3D();

		EventHandler<ActionEvent> update = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				l3d.update(leap);
				leap.update();
				l.update();
			}
		};

		final Duration oneFrameAmt = Duration.millis(1000 / 60);
		final KeyFrame oneFrame = new KeyFrame(oneFrameAmt, update);
		final Timeline timeline = new Timeline();
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.getKeyFrames().add(oneFrame);

		Group root = new Group();
		Scene scene1 = new Scene(root);
		root.getChildren().addAll(l.getChildren());

		Scene scene2 = l3d.getScene();

		stage.setTitle("DICOM Viewer");
		stage.setScene(scene2);
		stage.setFullScreen(true);

		timeline.play();
		stage.show();
	}

	public static void main(String... args) {
		Application.launch(args);
	}
}
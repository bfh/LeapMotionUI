package ch.bfh.leap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ij.IJ;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Dominik Fischli
 *
 *
 *	The Displayer class loads and displays the given images, providing methods for the simple switching between them
 *
 */
public class Displayer extends ImageView {
	private ImageList images;
	private ColorAdjust brightness;

	public Displayer() {
		super();
		images = new ImageList();
		brightness = new ColorAdjust();
	}

	public Displayer(String Path) {
		this(new ArrayList<String>(Arrays.asList(Path)));
	}

	public Displayer(List<String> Paths) {
		this();
		for (String Path : Paths) {
			if(validPath(Path))
				addImage(Path);
		}
	}

	public void addImage(String Path) {
		images.addImage(SwingFXUtils.toFXImage(IJ.openImage(Path).getBufferedImage(), null));
	}

	public void addImage(Image image) {
		images.addImage(image);
	}

	public void removeImage(int index) {
		images.removeImage(index);
	}

	public void next() {
		setImage(images.next());
	}

	public void previous() {
		setImage(images.previous());
	}

	public void current() {
		setImage(images.get());
	}
	public void setBrightness(float brightness) {
		this.brightness.setBrightness(brightness);
	}
	public void adjustBrightness(float adjustment) {
		this.brightness.setBrightness(this.brightness.getBrightness()+adjustment);
	}

	private boolean validPath(String Path) {
		boolean valid = Path.endsWith(".dcm");
		if(!valid) {
			throw new Error("Invalid DICOM path: " + Path);
		}
		return valid;
	}
}

package ch.bfh.leap;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;

public class ImageList {
	private int index;
	
	private List<Image> images;
	
	public ImageList() {
		index = 0;
		images = new ArrayList<Image>();
	}
	public ImageList(List<Image> images) {
		index = 0;
		this.images = new ArrayList<Image>(images);
	}
	public Image get() {
		return images.get(index);
	}
	public Image previous() {
		if(--index < 0)
			index = images.size()-1;
		return images.get(index);
	}
	public Image next() {
		index++;
		if(index > images.size())
			index = 0;
		return images.get(index);
	}
	public void addImage(Image image) {
		images.add(image);
	}
	public void removeImage(int index) {
		if(withinBounds(index))
			images.remove(index);
		else
			System.out.println("Tried to remove image from out of bounds");
	}
	private boolean withinBounds(int i) {
		return 0 < i && i < images.size();
	}
	
	
}

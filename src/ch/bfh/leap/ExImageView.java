package ch.bfh.leap;

import java.awt.Dimension;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ExImageView extends ImageView {
	private ColorAdjust ca;
	private Rectangle2D bounds;

	private static double MIN_PIXELS = 10.0;

	public ExImageView(Dimension dim, Image image) {
		super(image);
		iniView(dim);
	}

	public ExImageView(Dimension dim, String url) {
		super(url);
		iniView(dim);
	}

	private void iniView(Dimension dim) {
		setPreserveRatio(true);
		setSmooth(true);
		setCache(true);
		bounds = new Rectangle2D(0, 0, getImage().getWidth(), getImage().getHeight());
		setViewport(bounds);
		ca = new ColorAdjust();
		setEffect(ca);
		if(bounds.getMaxX() > bounds.getMaxY()) {
			setFitWidth(dim.getWidth());
		} else {
			setFitHeight(dim.getHeight());
		}
		
	}

	public void reset() {
		setViewport(bounds);
		setBrightness(1);
		setRotate(0);
		scaleXProperty().set(1);
		scaleYProperty().set(1);
	}
	public void resetZoom() {
		setViewport(bounds);
	}
	
	public double getZoom() {
		double zoom = getViewport().getWidth() / bounds.getWidth();
		return zoom;
	}

	public void setBrightness(double value) {
		ca.setBrightness(value);
	}

	public double getBrightness() {
		return ca.getBrightness();
	}

	public void move(Point2D delta) {
		// No point moving the picture if it isn't zoomed in
		if (!isZoomed())
			return;

		Rectangle2D viewport = getViewport();

		double width = getImage().getWidth();
		double height = getImage().getHeight();

		double newMinX = clamp(viewport.getMinX() + delta.getX(), 0, width - viewport.getWidth());
		double newMinY = clamp(viewport.getMinY() + delta.getY(), 0, height - viewport.getHeight());

		setViewport(new Rectangle2D(newMinX, newMinY, viewport.getWidth(), viewport.getHeight()));
	}

	public void zoom(double zoom, Point2D point) {
		Rectangle2D viewport = getViewport();
		double scale = clamp(Math.pow(1.01, zoom),
				Math.min(MIN_PIXELS / viewport.getWidth(), MIN_PIXELS / viewport.getHeight()),
				Math.max(getImage().getWidth() / viewport.getWidth(), getImage().getHeight() / viewport.getHeight()));

		Point2D mouse = cc(point);

		double newWidth = viewport.getWidth() * scale;
		double newHeight = viewport.getHeight() * scale;

		double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale, 0,
				getImage().getWidth() - newWidth);
		double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale, 0,
				getImage().getHeight() - newHeight);

		setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
	}
	

	public boolean isZoomed() {
		Rectangle2D viewport = getViewport();
		return !(viewport.getMinX() == 0 && viewport.getMinY() == 0 && viewport.getHeight() == getImage().getHeight()
				&& viewport.getWidth() == getImage().getWidth());
	}

	/**Convert coordinates
	 * 
	 * @param imageViewCoordinates coordinates relative to the imageView
	 * @return coordinates relative to the image
	 */
	private Point2D cc(Point2D imageViewCoordinates) {
		double xProportion = imageViewCoordinates.getX() / getBoundsInLocal().getWidth();
		double yProportion = imageViewCoordinates.getY() / getBoundsInLocal().getHeight();

		Rectangle2D viewport = getViewport();
		return new Point2D(viewport.getMinX() + xProportion * viewport.getWidth(),
				viewport.getMinY() + yProportion * viewport.getHeight());
	}

	private double clamp(double value, double min, double max) {
		if (value > max)
			return max;
		if (value < min)
			return min;
		return value;
	}
}

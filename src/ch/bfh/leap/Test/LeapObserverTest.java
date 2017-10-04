package ch.bfh.leap.Test;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

import com.leapmotion.leap.Frame;

import ch.bfh.leap.LeapObserver;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelReader;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class LeapObserverTest {

	@Test
	public void compareImages() throws InterruptedException {
		do {
			Thread.sleep(100);
		}while(!LeapObserver.imagesAvaible());
		
		Frame f = LeapObserver.getFrame();
		
		javafx.scene.image.Image fxImage = LeapObserver.getGrayscaleImage(f.images().get(0));
		BufferedImage awtImage = LeapObserver.getGrayscaleBufferedImage(f.images().get(0));
		BufferedImage formerFXImage = SwingFXUtils.fromFXImage(fxImage, null); 
		
		assertEquals(awtImage.getHeight(), formerFXImage.getHeight());
		assertEquals(awtImage.getWidth(), formerFXImage.getWidth());
		
		for(int y = 0; y < fxImage.getHeight(); ++y) {
			for(int x = 0; x < fxImage.getWidth(); ++ x) {
				assertEquals(awtImage.getRGB(x, y), formerFXImage.getRGB(x,y));				
			}
		}	
	}
	@Test
	public void testGetImage() throws InterruptedException, IOException {
		do {
			System.out.println("offline");
			Thread.sleep(100);
		}while(!LeapObserver.imagesAvaible());
		System.out.println("online");
		Image image = LeapObserver.getGrayscaleImage();
		
		PixelReader pr = image.getPixelReader();
		for(int y = 0; y < image.getHeight(); ++y) {
			for(int x = 0; x < image.getWidth(); ++x) {
				System.out.println(pr.getArgb(x, y));
			}
		}
		File output = new File("testGetImage.jpg");
		ImageIO.write(SwingFXUtils.fromFXImage(image, null), "jpg", output);
	}
}

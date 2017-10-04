package ch.bfh.leap.Test;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.bfh.leap.LeapObserver;

public class LeapValuesTest {

	@Test
	public void test() throws InterruptedException {
		do {
			Thread.sleep(100);
		}while(!LeapObserver.isConnected());
		
		while(true) {
			if(LeapObserver.handsCount() > 0) {
				System.out.println("Visible hands: " + LeapObserver.handsCount());
				System.out.println("Translation:   " + LeapObserver.getTranslation().toString());
				System.out.println("Hand position: [" + LeapObserver.getFrame().hands().get(0).palmPosition().getX()
						+ "," + LeapObserver.getFrame().hands().get(0).palmPosition().getY() + ","
						+ LeapObserver.getFrame().hands().get(0).palmPosition().getX() + "]");
			}
		}
	}

}

package ch.bfh.leap.Test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import ch.bfh.leap.ExtendedList;

public class ExtendedListTest {

	@Test
	public void testRunThrough() {
		ExtendedList<Integer> l = new ExtendedList<Integer>(Arrays.asList(1,2,3,4,5,6));
		assertEquals(1, l.current().intValue());
		assertEquals(2, l.next().intValue());
		assertEquals(3, l.next().intValue());
		assertEquals(4, l.next().intValue());
		assertEquals(5, l.next().intValue());
		assertEquals(6, l.next().intValue());
		
		assertEquals(1, l.next().intValue());
		
		for(int i = 6; i > 0; i--) {
			
		}
	}
	@Test
	public void testWithinBounds() {
		ExtendedList<Integer> l = new ExtendedList<Integer>(Arrays.asList(1,2,3,4,5,6,7));
		for(int i = 0; i < l.size(); i++)
			assertTrue(l.withinBounds(i));
	}
	
}

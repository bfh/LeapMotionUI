package ch.bfh.leap;

import java.util.ArrayList;
import java.util.List;

public class ExtendedList <T> extends ArrayList <T> {

	/**
	 * 	Eclipse-generated serial version UID
	 */
	private static final long serialVersionUID = 1750941770794561514L;

	private int index;
	
	public ExtendedList() {
		super();
		index = 0;
	}
	public ExtendedList(List<T> items) {		
		super(items);
		index = 0;
	}
	public T current() {
		return get(index);
	}
	public T previous() {
		if(--index < 0)
			index = size()-1;
		return get(index);
	}
	public T next() {		
		if(++index >= size())
			index = 0;
		return get(index);
	}
	public T getNext() {
		int temp = index+1;
		return temp >= size() ? get(0) : get(temp);
	}
	public T getPrevious() {
		int temp = index-1;
		return temp < 0 ? get(size()-1) : get(temp);
	}
	public boolean withinBounds(int i) {
		return 0 <= i && i < size();
	}
	
	
}

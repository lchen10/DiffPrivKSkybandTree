package kSpacePartition;

import java.util.Comparator;

import skyband.Comparison;
import skyband.Tuple;

public class YTupleValueComparator implements Comparator<Tuple> {

	Comparison comp;
	
	public YTupleValueComparator(Comparison comp) {
		this.comp = comp;
	}

	@Override
	public int compare(Tuple x, Tuple y) {
		double xdiff = x.getValue(0) - y.getValue(0);
		double ydiff = x.getValue(1) - y.getValue(1);
		
		if (comp == Comparison.MIN) {
			if (ydiff < 0.0001 && ydiff > -0.0001) { // equal
				return Double.compare(x.getValue(0), y.getValue(0));
			}

			return Double.compare(x.getValue(1), y.getValue(1));
		}
		
		if (ydiff < 0.0001 && ydiff > -0.0001) { // equal
			return Double.compare(y.getValue(0),x.getValue(0));
		}

		return Double.compare(y.getValue(1), x.getValue(1));
	}

}

package kSpacePartition;

import java.util.Comparator;

import skyband.Comparison;
import skyband.Tuple;

public class XTupleValueComparator implements Comparator<Tuple> {

	Comparison comp;
	
	public XTupleValueComparator(Comparison comp) {
		this.comp = comp;
	}

	@Override
	public int compare(Tuple x, Tuple y) {
		double xdiff = x.getValue(0) - y.getValue(0);
		double ydiff = x.getValue(1) - y.getValue(1);
		
		if (comp == Comparison.MIN) {
			if (xdiff < 0.0001 && xdiff > -0.0001) { // equal
				return Double.compare(x.getValue(1), y.getValue(1));
			}

			return Double.compare(x.getValue(0), y.getValue(0));
		}
		
		if (xdiff < 0.0001 && xdiff > -0.0001) { // equal
			return Double.compare( y.getValue(1), x.getValue(1));
		}

		return Double.compare(y.getValue(0), x.getValue(0));
		
	}

}

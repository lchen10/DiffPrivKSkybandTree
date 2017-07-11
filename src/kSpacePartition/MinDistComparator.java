package kSpacePartition;

import java.util.Comparator;

import skyband.Tuple;

public class MinDistComparator implements Comparator<Tuple> {

	@Override
	public int compare(Tuple x, Tuple y) {

		double xdist = x.getValue(0) + x.getValue(1);
		double ydist = y.getValue(0) + y.getValue(1);
		return Double.compare(xdist,ydist);
	}

}

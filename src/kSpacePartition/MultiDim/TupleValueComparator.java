package kSpacePartition.MultiDim;

import java.util.Comparator;

import skyband.Comparison;
import skyband.Tuple;

public class TupleValueComparator implements Comparator<Tuple> {

	Comparison comp;
	int dim;

	public TupleValueComparator(Comparison comp, int dim) {
		this.comp = comp;
		this.dim = dim;
	}

	@Override
	public int compare(Tuple x, Tuple y) {
		double diff = x.getValue(dim) - y.getValue(dim);
		if (comp == Comparison.MIN) {
			return Double.compare(x.getValue(dim), y.getValue(dim));
		}
		return Double.compare(y.getValue(dim), x.getValue(dim));

	}

}

package kSpacePartition.MultiDim;

import java.util.ArrayList;
import java.util.List;

import skyband.Comparison;
import skyband.Tuple;

public class MultiDimGrid implements Comparable<MultiDimGrid> {
	public double[][] ranges;
	public double count;
	public String id;
	public int level;
	public MultiDimGrid parent;
	public int dim;
	public ArrayList<MultiDimGrid> children = new ArrayList<>();
	public Tuple tuple;
	public List<Tuple> data = new ArrayList<>();
	public Comparison comp;

	public boolean inBound(Tuple t) {
		for (int i = 0; i < ranges.length; i++) {
			if (t.getValue(i) < ranges[i][0] || t.getValue(i) >= ranges[i][1]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		String pid = parent != null ? parent.id : "null";

		return "Grid [count=" + count + ", id=" + id + ", level=" + level + ", parent=" + pid + ", children=" + children
				+ "]";
	}

	public MultiDimGrid() {
	
	}

	public MultiDimGrid(int d) {
		this.dim = d;
		ranges = new double[dim][2];
	}

	@Override
	public int compareTo(MultiDimGrid o) {
		if (comp == Comparison.MIN) {
			double dist = 0;
			for (int i = 0; i < ranges.length; i++) {
				dist += ranges[i][0];
			}
			double odist = 0;
			for (int i = 0; i < ranges.length; i++) {
				odist += o.ranges[i][0];
			}
			return Double.compare(dist, odist);
		}

		double dist = 0;
		for (int i = 0; i < ranges.length; i++) {
			dist += ranges[i][1];
		}
		double odist = 0;
		for (int i = 0; i < ranges.length; i++) {
			odist += o.ranges[i][1];
		}
		return Double.compare( odist,dist);
	}
}
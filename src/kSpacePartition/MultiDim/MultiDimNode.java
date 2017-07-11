package kSpacePartition.MultiDim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import kSpacePartition.IDTuple;

import org.apache.commons.math3.distribution.LaplaceDistribution;

import skyband.*;

public class MultiDimNode {
	public MultiDimNode parent;
	public List<MultiDimNode> children = new ArrayList<MultiDimNode>();
	public HashMap<String, MultiDimNode> childMap = new HashMap<String, MultiDimNode>();
	public List<IDTuple> oridata = new ArrayList<IDTuple>();
	public List<PriorityQueue<IDTuple>> dimQueues;
	public int id;
	public double count;
	public double[][] ranges;
	public int dim;
	public int level;
	public boolean isLeave;
	public static int currentID = 0;
	public Comparison comp;
	public double remaineps = 0.0;
	public double counteps;

	public MultiDimNode(double[][] ranges, Comparison comp) {
		this.ranges = ranges;
		this.id = currentID;
		this.comp = comp;
		currentID++;
		this.dim = ranges.length;
		dimQueues = new ArrayList<PriorityQueue<IDTuple>>();

		for (int i = 0; i < dim; i++) {
			dimQueues.add(new PriorityQueue<IDTuple>(11,
					new TupleValueComparator(comp, i)));
		}
	}

	public MultiDimNode(double[][] newranges, MultiDimNode node) {
		this(newranges, node.comp);
		parent = node;
		level = parent.level + 1;
	}

	public void spawnIndexes(Integer[] context, int d, ArrayList<Integer[]> r) {
		if (d < dim) {
			Integer[] newcontext = Arrays.copyOf(context, context.length);
			Integer[] newcontext2 = Arrays.copyOf(context, context.length);
			newcontext[d] = 0;
			newcontext2[d] = 1;
			spawnIndexes(newcontext, d + 1, r);
			spawnIndexes(newcontext2, d + 1, r);
		} else {
			r.add(context);
		}
	}

	public List<MultiDimNode> split(double[] splits) throws Exception {
		children = new ArrayList<MultiDimNode>();

		if (splits.length != dim) {
			throw new Exception("Need " + dim + " split values");
		}

		Integer[] indexes = new Integer[dim];
		ArrayList<Integer[]> r = new ArrayList<Integer[]>();
		spawnIndexes(indexes, 0, r);

		int j = 0;
		for (Integer[] is : r) {
			double[][] newranges = new double[dim][2];
			for (int i = 0; i < dim; i++) {
				double x = Math.min(ranges[i][is[i]], splits[i]);
				double y = Math.max(ranges[i][is[i]], splits[i]);
				newranges[i][0] = x;
				newranges[i][1] = y;
			}
			MultiDimNode node = new MultiDimNode(newranges, this);
			children.add(node);
			if (j == 0) {
				childMap.put("sw", node);
			}
			if (j == r.size() - 1) {
				childMap.put("ne", node);
			}

			j++;
		}

		for (IDTuple t : oridata) {
			boolean found = false;
			for (MultiDimNode n : children) {
				if (n.inBound(t)) {
					n.oridata.add(t);
					for (int i = 0; i < dim; i++) {
						n.dimQueues.get(i).offer(t);
					}
					found = true;
					break;
				}
			}

			if (!found) {
				throw new Exception("out of domain!");
			}

		}

		for (MultiDimNode n : children) {
			n.count = n.oridata.size();
		}
		return children;
	}

	@Override
	public String toString() {
		String pid = "null";
		if (parent != null) {
			pid = parent.id + "";
		}

		String pre = "MultiDimNode [parent=" + pid + ", id=" + id + ", count="
				+ count;
		String post = ", level=" + level + ", isLeave=" + isLeave + "]";

		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (double[] range : ranges) {
			sb.append("[" + range[0] + "," + range[1] + "]" + ",");
		}
		sb.append("}");
		return pre + sb + post;
	}

	public boolean inBound(Tuple t) {

		for (int i = 0; i < ranges.length; i++) {
			if (t.getValue(i) < ranges[i][0] || t.getValue(i) >= ranges[i][1]) {
				return false;
			}
		}
		return true;
	}

	public void loadTuples(List<IDTuple> tuples) {
		double xmin = Double.MAX_VALUE;
		double xmax = -1.0;
		double ymin = Double.MAX_VALUE;
		double ymax = -1.0;

		for (IDTuple tuple : tuples) {
			oridata.add(tuple);
			for (int i = 0; i < dim; i++) {
				dimQueues.get(i).offer(tuple);
			}
		}

		// this.xmin = xmin;
		// this.xmax = xmax + 0.1;
		// this.ymin = ymin;
		// this.ymax = ymax + 0.1;
		this.ranges = new double[dim][2];
		for (int i = 0; i < ranges.length; i++) {
			ranges[i][0] = MultiParam.MINS[i];
			ranges[i][1] = MultiParam.MAXS[i];
		}

		count = oridata.size();
	}

}

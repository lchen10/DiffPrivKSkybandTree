package skyband;

import java.util.ArrayList;
import java.util.List;

public class Grid implements Comparable<Grid> {
	public double xmin;
	public double xmax;
	public double ymin;
	public double ymax;
	public double count;
	public String id;
	public int level;
	public Grid parent;
	public ArrayList<Grid> children = new ArrayList<>();
	public Tuple tuple = new Tuple(new double[] { xmin, ymin });
	public List<Tuple> data = new ArrayList<>();
	public Comparison comp;

	public boolean inBound(Tuple t) {
		double x = t.getValue(0);
		double y = t.getValue(1);
		if (x >= xmin && x < xmax && y >= ymin && y < ymax) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		String pid = parent != null ? parent.id : "null";

		return "Grid [xmin=" + xmin + ", xmax=" + xmax + ", ymin=" + ymin
				+ ", ymax=" + ymax + ", count=" + count + ", id=" + id
				+ ", level=" + level + ", parent=" + pid + ", children="
				+ children + "]";
	}

	public Grid() {
	}

	@Override
	public int compareTo(Grid o) {
		if (comp == Comparison.MIN) {
			double dist = xmin + ymin;
			return Double.compare(dist, o.xmin + o.ymin);
		}

		double dist = xmax + ymax;
		return Double.compare(o.xmax + o.ymax, dist);
	}
}
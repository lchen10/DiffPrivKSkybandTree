package kSpacePartition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.math3.distribution.LaplaceDistribution;

import skyband.*;

public class Node {
	public Node parent;
	public List<Node> children = new ArrayList<Node>();
	public HashMap<String, Node> childMap = new HashMap<String, Node>();
	public PriorityQueue<Tuple> data = new PriorityQueue<Tuple>(11, new MinDistComparator());
	public List<IDTuple> oridata = new ArrayList<IDTuple>();
	public PriorityQueue<IDTuple> xlist;
	public PriorityQueue<IDTuple> ylist;

	public int id;
	public double count;
	public double xmin;
	public double xmax;
	public double ymin;
	public double ymax;
	public int level;
	public boolean isLeave;
	public double n_F; // for post processing
	public static int currentID = 0;
	public Comparison comp;
	public double remaineps = 0.0;
	public double counteps;

	public Node(double xmin, double ymin, double xmax, double ymax, Comparison comp) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.id = currentID;
		this.comp = comp;
		currentID++;

		xlist = new PriorityQueue<IDTuple>(11, new XTupleValueComparator(comp));
		ylist = new PriorityQueue<IDTuple>(11, new YTupleValueComparator(comp));
	}

	public Node(double xmin, double ymin, double xmax, double ymax, Node node) {
		this(xmin, ymin, xmax, ymax, node.comp);
		parent = node;
		level = parent.level + 1;
	}

	public List<Node> split(double x, double y) throws Exception {
		children = new ArrayList<Node>();

		Node sw = new Node(xmin, ymin, x, y, this);
		Node se = new Node(x, ymin, xmax, y, this);
		Node nw = new Node(xmin, y, x, ymax, this);
		Node ne = new Node(x, y, xmax, ymax, this);

		children.add(sw);
		children.add(se);
		children.add(nw);
		children.add(ne);
		childMap.put("sw", sw);
		childMap.put("se", se);
		childMap.put("nw", nw);
		childMap.put("ne", ne);

		for (IDTuple t : oridata) {
			if (sw.inBound(t)) {
				sw.data.offer(t);
				sw.oridata.add(t);
				sw.xlist.add(t);
				sw.ylist.add(t);
			} else if (se.inBound(t)) {
				se.data.offer(t);
				se.oridata.add(t);
				se.xlist.add(t);
				se.ylist.add(t);
			} else if (nw.inBound(t)) {
				nw.data.offer(t);
				nw.oridata.add(t);
				nw.xlist.add(t);
				nw.ylist.add(t);
			} else if (ne.inBound(t)) {
				ne.data.offer(t);
				ne.oridata.add(t);
				ne.xlist.add(t);
				ne.ylist.add(t);
			} else
				throw new Exception("out of domain!");
		}

		sw.count = sw.data.size();
		se.count = se.data.size();
		nw.count = nw.data.size();
		ne.count = ne.data.size();
		return children;
	}

	@Override
	public String toString() {
		String pid = "null";
		if (parent != null) {
			pid = parent.id + "";
		}

		return "Node [parent=" + pid + ", id=" + id + ", count=" + count + ", xmin=" + xmin + ", xmax=" + xmax
				+ ", ymin=" + ymin + ", ymax=" + ymax + ", level=" + level + ", isLeave=" + isLeave + "]";
	}

	public boolean inBound(Tuple t) {
		double x = t.getValue(0);
		double y = t.getValue(1);
		if (x >= xmin && x < xmax && y >= ymin && y < ymax) {
			return true;
		}
		return false;
	}

	public void loadTuples(List<IDTuple> tuples) {
		double xmin = Double.MAX_VALUE;
		double xmax = -1.0;
		double ymin = Double.MAX_VALUE;
		double ymax = -1.0;

		for (IDTuple tuple : tuples) {
			data.offer(tuple);
			oridata.add(tuple);
			xlist.offer(tuple);
			ylist.offer(tuple);

			double x = tuple.getValue(0);
			double y = tuple.getValue(1);
			if (x < xmin) {
				xmin = x;
			}

			if (x > xmax) {
				xmax = x;
			}
			if (y < ymin) {
				ymin = y;
			}
			if (y > ymax) {
				ymax = y;
			}
		}


//		this.xmin = xmin;
//		this.xmax = xmax + 0.1;
//		this.ymin = ymin;
//		this.ymax = ymax + 0.1;
		
		this.xmin = Param.XMIN;
		this.xmax = Param.XMAX;
		this.ymin = Param.YMIN;
		this.ymax = Param.YMAX;

		count = data.size();
	}

	public void updateCount() {
		if (!isLeave) {
			for (Node sub : children) {
				sub.updateCount();
			}
			double sum = 0.0;
			for (Node sub : children) {
				sum += sub.count;
			}
			count = sum;
		}

	}

	public void get_z(int maxlevel) {
		if (isLeave)
			return;
		else {
			Node nw = childMap.get("nw");
			Node ne = childMap.get("nw");
			Node sw = childMap.get("sw");
			Node se = childMap.get("se");
			nw.get_z(maxlevel);
			ne.get_z(maxlevel);
			sw.get_z(maxlevel);
			se.get_z(maxlevel);

			int l = maxlevel - level;
			// self.n_count = (4**l-4**(l-1))/float(4**l-1)*self.n_count + \
			// (4**(l-1)-1)/float(4**l-1)*(self.nw.n_count+ \
			// self.ne.n_count+self.sw.n_count+self.se.n_count)

			count = (Math.pow(4, l) - Math.pow(4, l - 1)) / (Math.pow(4, l) - 1) * count
					+ (Math.pow(4, l - 1) - 1) / (Math.pow(4, l) - 1) * (nw.count + ne.count + sw.count + se.count);
		}
	}
	
	public List<Node> splitByUniformGrid(double domainwidth, double domainheight, double err) throws Exception {
		children = new ArrayList<Node>();
		double ratio = Math.sqrt((18/remaineps));
		double xlength = xmax - xmin;
		double ylength = ymax - ymin;
		int xstep = (int) Math.ceil(xlength / (domainwidth * err * ratio));
		int ystep = (int) Math.ceil(ylength / (domainheight * err * ratio));

		double currx = xmin;
		double curry = ymin;
		double stepw = xlength / xstep;
		double steph = ylength / ystep;

		for (int i = 0; i < xstep; i++) {
			for (int j = 0; j < ystep; j++) {
				Node n = new Node(currx, curry, currx + stepw, curry + steph, this);
				curry += steph;
				children.add(n);
			}
			curry = ymin;
			currx += stepw;
		}

		for (IDTuple t : oridata) {
			boolean found = false;
			for (Node node : children) {
				if (node.inBound(t)) {
					node.oridata.add(t);
					found = true;
					break;
				}
			}

			if (!found)
				throw new Exception("out of domain!");
		}

		for (Node node : children) {
			node.count = node.oridata.size();
		}
		return children;

	}


	public List<Node> splitByAdaptiveUniformGrid(double domainwidth, double domainheight, double err) throws Exception {
		children = new ArrayList<Node>();
		double firstleveleps = remaineps * 0.5;
		double secondleveleps = remaineps - firstleveleps;
		int m1 = (int) Math.max(10,(int) (1/4.0)*Math.ceil(Math.sqrt(count*firstleveleps)/10.0));
		if(m1 < 2){
			return children;
		}
		
		double xlength = xmax - xmin;
		double ylength = ymax - ymin;
//		int xstep = (int) Math.ceil(xlength / m);
//		int ystep = (int) Math.ceil(ylength / m);

		double currx = xmin;
		double curry = ymin;
		double stepw = xlength / m1;
		double steph = ylength / m1;

		for (int i = 0; i < m1; i++) {
			for (int j = 0; j < m1; j++) {
				Node n = new Node(currx, curry, currx + stepw, curry + steph, this);
				curry += steph;
				children.add(n);
			}
			curry = ymin;
			currx += stepw;
		}

		for (IDTuple t : oridata) {
			boolean found = false;
			for (Node node : children) {
				if (node.inBound(t)) {
					node.oridata.add(t);
					found = true;
					break;
				}
			}

			if (!found)
				throw new Exception("out of domain!");
		}
		
		LaplaceDistribution lap = new LaplaceDistribution(0, 1.0/firstleveleps);
		LaplaceDistribution lap2 = new LaplaceDistribution(0, 1.0/secondleveleps);
		for (Node node : children) {
			node.count = node.oridata.size() + lap.sample();
			int m2 = (int) Math.ceil(Math.sqrt(node.count*secondleveleps)/5.0);
			if(m2 < 2){
				node.count = node.oridata.size() + new LaplaceDistribution(0, 1.0/remaineps).sample();
				node.isLeave = true;
				continue;
			}
			
			xlength = node.xmax - node.xmin;
			ylength = node.ymax - node.ymin;
//			int xstep = (int) Math.ceil(xlength / m);
//			int ystep = (int) Math.ceil(ylength / m);

			currx = node.xmin;
			curry = node.ymin;
			stepw = xlength / m2;
			steph = ylength / m2;

			for (int i = 0; i < m2; i++) {
				for (int j = 0; j < m2; j++) {
					Node n = new Node(currx, curry, currx + stepw, curry + steph, node);
					curry += steph;
					node.children.add(n);
					n.isLeave = true;
				}
				curry = node.ymin;
				currx += stepw;
			}

			for (IDTuple t : node.oridata) {
				boolean found = false;
				for (Node n : node.children) {
					if (n.inBound(t)) {
						n.oridata.add(t);
						found = true;
						break;
					}
				}

				if (!found)
					throw new Exception("out of domain!");
			}
			
			
			for (Node n : node.children) {
				n.count = n.oridata.size() + lap2.sample();
			}
		}
		
		return children;

	}
}

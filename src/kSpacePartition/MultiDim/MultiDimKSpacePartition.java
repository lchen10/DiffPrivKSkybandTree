package kSpacePartition.MultiDim;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import kSpacePartition.EM;
import kSpacePartition.IDTuple;
import kSpacePartition.Node;
import kSpacePartition.NodeCountComparator;
import kSpacePartition.Param;

import org.apache.commons.math3.distribution.LaplaceDistribution;

import skyband.Comparison;
import skyband.Tuple;

public class MultiDimKSpacePartition {

	private void splitByQuadTree(MultiDimNode node) throws Exception {
		int dim = node.dim;
		double[] splits = new double[dim];
		for (int i = 0; i < dim; i++) {
			splits[i] = node.ranges[i][0] + (node.ranges[i][1] - node.ranges[i][0]) / 2.0;
		}

		node.split(splits);
	}

	private void splitByOptimalKComp(int k, MultiDimNode node, boolean diff, double eps, Comparison comp)
			throws Exception {
		HashSet<IDTuple> found = new HashSet<IDTuple>();
		HashSet<IDTuple> notQualified = new HashSet<IDTuple>();
		int dim = node.dim;
		double[] splits = new double[dim];
		while (found.size() < k + 1) {
			boolean isempty = false;
			for (int i = 0; i < dim; i++) {
				if (node.dimQueues.get(i).size() == 0) {
					isempty = true;
					break;
				}
			}
			if (isempty) {
				break;
			}

			for (int i = 0; i < dim; i++) {
				IDTuple poll = node.dimQueues.get(i).poll();
				splits[i] = poll.getValue(i);
				notQualified.add(poll);
			}

			HashSet<IDTuple> toremove = new HashSet<>();
			for (IDTuple tuple : notQualified) {
				boolean in = true;
				for (int i = 0; i < dim; i++) {
					if (splits[i] > tuple.getValue(i)) {
						in = false;
						break;
					}
				}
				if (in) {
					toremove.add(tuple);
					found.add(tuple);
				}
			}

			for (IDTuple r : toremove) {
				notQualified.remove(r);
			}
		}

		if (diff) {
			for (int i = 0; i < dim; i++) {
				splits[i] = EM.getValueBasedOnRank(dim, node.oridata, eps, splits[i], i, node.ranges[i][0],
						node.ranges[i][1]) + 0.001;
			}
		}

		node.split(splits);
	}

	private void splitByOptimalK(int k, Node node, boolean diff, double eps) throws Exception {
		double xsplit = 0;
		double ysplit = 0;
		HashSet<IDTuple> found = new HashSet<IDTuple>();
		HashSet<IDTuple> xNotQualified = new HashSet<IDTuple>();
		HashSet<IDTuple> yNotQualified = new HashSet<IDTuple>();

		while (found.size() < k) {
			if (node.xlist.size() == 0 || node.ylist.size() == 0) {
				break;
			}

			IDTuple xnode = node.xlist.poll();
			IDTuple ynode = node.ylist.poll();
			xsplit = xnode.getValue(0);
			ysplit = ynode.getValue(1);

			HashSet<IDTuple> toremove = new HashSet<IDTuple>();
			for (IDTuple t : xNotQualified) {
				if (t.getValue(1) < ysplit) {
					found.add(t);
					toremove.add(t);
				}
			}

			for (IDTuple i : toremove) {
				xNotQualified.remove(i);
			}

			toremove = new HashSet<IDTuple>();
			for (IDTuple t : yNotQualified) {
				if (t.getValue(0) < xsplit) {
					found.add(t);
					toremove.add(t);
				}
			}

			for (IDTuple i : toremove) {
				yNotQualified.remove(i);
			}

			if (xnode.getValue(1) < ysplit) {
				found.add(xnode);
			} else {
				xNotQualified.add(xnode);
			}

			if (ynode.getValue(0) < xsplit) {
				found.add(ynode);
			} else {
				yNotQualified.add(ynode);
			}

		}

		if (diff) {
			xsplit = EM.getValueBasedOnRank(node.oridata, eps, xsplit, 0, node.xmin, node.xmax);
			ysplit = EM.getValueBasedOnRank(node.oridata, eps, ysplit, 1, node.ymin, node.ymax);
		}

		node.split(xsplit + 0.001, ysplit + 0.001);
	}

	private boolean testLeave(Node node, int k, int maxlevel, double max, double rate) {
		if (node.isLeave) {
			return true;
		}

		if (node.level == maxlevel) {
			return true;
		}

		if (node.count <= k && (node.xmax - node.xmin < max * rate) && (node.ymax - node.ymin < max * rate)) {
			return true;
		}
		return false;
	}

	private boolean testLeaveNS(Node node, int k, int maxlevel, double max, double rate) {
		if (node.isLeave) {
			return true;
		}

		// if (node.level == maxlevel || node.oridata.size() <= 2) {
		if (node.level == maxlevel) {
			return true;
		}

		return false;
	}

	private boolean testLeaveMinSize(MultiDimNode node, int k, int maxlevel, int minsize) {
		if (node.isLeave) {
			return true;
		}

		// if (node.oridata.size() == 0) {
		// return true;
		// }

		// if (node.level == maxlevel
		// || ((node.xmax - node.xmin < domainWidth * rate * 0.5) && (node.ymax
		// - node.ymin < domainHeight * rate * 0.5))) {
		if (node.level == maxlevel) {
			return true;
		}

		if (node.count <= minsize) {
			return true;
		}

		return false;
	}

	public MultiDimNode partitionByOptimalKNoisyNegative(int k, String folder, String infile, int maxlevel,
			boolean diff, double[] epslevel, double epscount, String deliminator, double[] rangewidths, Comparison comp,
			int minsize, int kdelta) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(folder + "/" + infile), Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int id = 1;
		int dim = 0;
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split(deliminator);
			Double[] v = new Double[line.length];
			dim = line.length;
			for (int j = 0; j < line.length; j++) {
				if (comp == Comparison.MIN) {
					v[j] = MultiParam.MAXS[j] - Double.parseDouble(line[j]);
				} else {
					v[j] = Double.parseDouble(line[j]);
				}
			}
			tuples.add(new IDTuple(v, id));
			id++;
		}

		double[][] ranges = new double[dim][2];
		for (int i = 0; i < rangewidths.length; i++) {
			ranges[i][0] = 0;
			ranges[i][1] = rangewidths[i];
		}
		MultiDimNode root = new MultiDimNode(ranges, comp);
		root.loadTuples(tuples);

		if (diff) {
			LaplaceDistribution laproot = new LaplaceDistribution(0, 1 / (epslevel[0] * (1 - Param.SPLIT_BUDGET_RATE)));
			root.count = root.count + laproot.sample();
		}

		Queue<MultiDimNode> queue = new LinkedList<>();
		queue.add(root);

		// double uniformeps = eps / maxlevel;
		HashSet<MultiDimNode> leaves = new HashSet<>();
		int noisynegativecount = 0;

		while (!queue.isEmpty()) {
			MultiDimNode node = queue.remove();

			if (testLeaveMinSize(node, k, maxlevel, minsize)) {
				node.isLeave = true;
				if (leaves.add(node)) {
					if (node.level != maxlevel) { // remain budget
						double remaineps = 0.0;
						for (int i = node.level; i < maxlevel; i++) {
							remaineps += epslevel[node.level];
						}
						LaplaceDistribution lapr = new LaplaceDistribution(0, 1 / remaineps);
						node.count = node.oridata.size() + lapr.sample();
					}
					if (node.oridata.size() == 0) {
						noisynegativecount++;
					}
				}
				continue;
			}

			double eps = epslevel[node.level];
			double counteps = epslevel[node.level + 1] * (1 - Param.SPLIT_BUDGET_RATE);
			double kprime = Math.ceil(k + Math.sqrt(2) / counteps);
			if (node.count > k + Math.sqrt(2) / counteps) {
				splitByOptimalKComp((int) kprime, node, diff, eps * Param.SPLIT_BUDGET_RATE, comp);
			} else {
				splitByQuadTree(node);
			}

			if (comp == Comparison.MIN) {

				// Node sw = node.childMap.get("sw");
				// if (sw.oridata.size() == node.oridata.size()) {
				// node.childMap = new HashMap<String, Node>();
				// node.children = new ArrayList<Node>();
				// splitByQuadTree(node);
				// sw = node.childMap.get("sw");
				// }
				//
				// queue.add(sw);
				// Node ne = node.childMap.get("ne");
				// Node se = node.childMap.get("se");
				// Node nw = node.childMap.get("nw");
				//
				// if (diff) {
				//
				// if (node.count > k + Math.sqrt(2) / counteps) {
				// LaplaceDistribution lap = new LaplaceDistribution(
				// 0,
				// 1 / (epslevel[node.level + 1] * (1 -
				// Param.SPLIT_BUDGET_RATE)));
				// sw.count = sw.count + lap.sample();
				// ne.count = ne.count + lap.sample();
				// se.count = se.count + lap.sample();
				// nw.count = nw.count + lap.sample();
				// } else {
				// LaplaceDistribution lap = new LaplaceDistribution(0,
				// 1 / (epslevel[node.level + 1]));
				// sw.count = sw.count + lap.sample();
				// ne.count = ne.count + lap.sample();
				// se.count = se.count + lap.sample();
				// nw.count = nw.count + lap.sample();
				// }
				//
				// }
				//
				// if (sw.count < k) {
				// queue.add(ne);
				// } else {
				// ne.isLeave = true;
				// }
				//
				// queue.add(se);
				// queue.add(nw);
			} else {
				MultiDimNode ne = node.childMap.get("ne");
				MultiDimNode sw = node.childMap.get("sw");
				if (ne.oridata.size() == node.oridata.size()) {
					node.childMap = new HashMap<String, MultiDimNode>();
					node.children = new ArrayList<MultiDimNode>();
					splitByQuadTree(node);
					ne = node.childMap.get("ne");
				}

				queue.add(ne);

				if (diff) {
					if (node.count > k + Math.sqrt(2) / counteps) {
						LaplaceDistribution lap = new LaplaceDistribution(0,
								1 / (epslevel[node.level + 1] * (1 - Param.SPLIT_BUDGET_RATE)));
						for (MultiDimNode child : node.children) {
							child.count = child.count + lap.sample();
						}
					} else {
						LaplaceDistribution lap = new LaplaceDistribution(0, 1 / (epslevel[node.level + 1]));
						for (MultiDimNode child : node.children) {
							child.count = child.count + lap.sample();
						}
					}
				}

				if (ne.count <= k) {
					for (MultiDimNode child : node.children) {
						queue.add(child);
					}
				} else {
					sw.isLeave = true;
					for (MultiDimNode child : node.children) {
						if (child.id != sw.id) {
							queue.add(child);
						}
					}
				}

			}

		}

		int slevel = MultiParam.REMOVAL_LEVEL; // ignore the first 3 levels
		ArrayList<MultiDimNode> nlist = new ArrayList<>(root.children);
		noisynegativecount = 0;
		for (int l = 1; l <= maxlevel; l++) {
			ArrayList<MultiDimNode> templist = new ArrayList<>();
			if (l > slevel) {
				Collections.sort(nlist, new MultiDimNodeCountComparator());
				System.out.println("noisy negative count " + noisynegativecount);
				int index = 0;
				for (int i = 0; i < nlist.size(); i++) {
					if (Math.ceil(nlist.get(i).count) > 0) {
						System.out.println("empty noisy count " + i + " current node count " + nlist.get(i).count);
						index = i;
						break;
					}
				}

				for (int i = 0; i < noisynegativecount && index < nlist.size(); i++) {
					MultiDimNode node = nlist.get(index);
					System.out.println(node.count + " -> 0");
					node.count = 0;
					node.isLeave = true;
					node.children = new ArrayList<>();
					node.childMap = new HashMap<>();
					index++;
				}
			}
			noisynegativecount = 0;
			for (MultiDimNode n : nlist) {
				if (!n.isLeave) {
					templist.addAll(n.children);
					for (MultiDimNode c : n.children) {
						if (c.count < 0) {
							noisynegativecount++;
						}
					}

				}
			}
			nlist = templist;
		}

		// for (int i = 0; i < nlist.size(); i++) {
		// MultiDimNode node = nlist.get(i);
		// System.out.println(node.count + " -> 0");
		// if (node.count < 1.0) {
		// node.count = 0;
		// node.isLeave = true;
		// node.children = new ArrayList<>();
		// node.childMap = new HashMap<>();
		// }
		// }

		return root;
	}

	private boolean testQuadTreeLeave(Node node, int k, int maxlevel, double rate, double domainWidth,
			double domainHeight, int minsize) {
		if (node.isLeave) {
			return true;
		}

		if (node.level == maxlevel) {
			return true;
		}

		// if (node.count <= minsize) {
		// return true;
		// }

		return false;
	}

}

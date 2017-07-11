package kSpacePartition;

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

import org.apache.commons.math3.distribution.LaplaceDistribution;

import skyband.Comparison;
import skyband.Tuple;

public class KSpacePartition {
	public Node partitionByMinDist(int k, String infile, int maxlevel)
			throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("data/" + infile),
				Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split("\\t");
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }));
		}

		Node root = new Node(0, 0, 1000000, 1000000, Comparison.MIN);
		root.loadTuples(tuples);

		Queue<Node> queue = new LinkedList<>();
		queue.add(root);

		while (!queue.isEmpty()) {
			Node node = queue.remove();
			if (testLeave(node, k, maxlevel, 1000000, 0.01)) {
				continue;
			}

			double xsplit = 0;
			double ysplit = 0;
			for (int i = 0; i < k; i++) {
				Tuple p = node.data.poll();
				if (xsplit < p.getValue(0)) {
					xsplit = p.getValue(0);
				}
				if (ysplit < p.getValue(1)) {
					ysplit = p.getValue(1);
				}
			}

			node.split(xsplit + 0.001, ysplit + 0.001);
			queue.add(node.childMap.get("sw"));
			queue.add(node.childMap.get("se"));
			queue.add(node.childMap.get("nw"));
		}

		return root;

	}

	public Node partitionByOptimalK(int k, String infile, int maxlevel,
			boolean diff, double[] epslevel) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("data/" + infile),
				Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int id = 1;
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split("\\s+");
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }, id));
			id++;
		}

		Node root = new Node(0, 0, 1000000, 1000000, Comparison.MIN);
		root.loadTuples(tuples);

		Queue<Node> queue = new LinkedList<>();
		queue.add(root);

		// double uniformeps = eps / maxlevel;

		while (!queue.isEmpty()) {
			Node node = queue.remove();
			if (testLeave(node, k, maxlevel, 1000000, 0.01)) {
				node.isLeave = true;
				continue;
			}

			if (node.oridata.size() > k) {
				splitByOptimalK(k, node, diff, epslevel[node.level]);
			} else {
				splitByQuadTree(node);
				// continue;
			}

			Node sw = node.childMap.get("sw");
			if (sw.oridata.size() == node.oridata.size()) {
				node.childMap = new HashMap<String, Node>();
				node.children = new ArrayList<Node>();
				splitByQuadTree(node);
				sw = node.childMap.get("sw");
			}

			queue.add(sw);
			queue.add(node.childMap.get("se"));
			if (sw.oridata.size() < k) {
				queue.add(node.childMap.get("ne"));
			} else {
				node.childMap.get("ne").isLeave = true;
			}

			queue.add(node.childMap.get("nw"));
		}

		return root;
	}

	public Node partitionByOptimalKNSNoRemoval(int k, String infile,
			int maxlevel, boolean diff, double[] epslevel, double epscount)
			throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("data/" + infile),
				Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int id = 1;
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split("\\s+");
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }, id));
			id++;
		}

		Node root = new Node(0, 0, 1000000, 1000000, Comparison.MIN);
		root.loadTuples(tuples);

		Queue<Node> queue = new LinkedList<>();
		queue.add(root);

		// double uniformeps = eps / maxlevel;
		HashSet<Node> leaves = new HashSet<>();

		while (!queue.isEmpty()) {
			Node node = queue.remove();

			if (testLeaveNS(node, k, maxlevel, 1000000, 0.01 / 2.0)) {
				node.isLeave = true;
				if (leaves.add(node)) {
					// if (node.level != maxlevel) { // remain budget
					// double remaineps = 0.0;
					// for (int i = node.level; i < maxlevel; i++) {
					// remaineps += epslevel[node.level];
					// }
					// LaplaceDistribution lapr = new LaplaceDistribution(0, 1 /
					// remaineps);
					// node.count = node.oridata.size() + lapr.sample();
					// }
				}
				continue;
			}

			double eps = epslevel[node.level];

			if (node.count > k) {
				splitByOptimalK(k, node, diff, eps * Param.SPLIT_BUDGET_RATE);
			} else {
				splitByQuadTree(node);
			}

			Node sw = node.childMap.get("sw");
			if (sw.oridata.size() == node.oridata.size()) {
				node.childMap = new HashMap<String, Node>();
				node.children = new ArrayList<Node>();
				splitByQuadTree(node);
				sw = node.childMap.get("sw");
			}

			queue.add(sw);
			Node ne = node.childMap.get("ne");
			Node se = node.childMap.get("se");
			Node nw = node.childMap.get("nw");

			if (diff) {
				LaplaceDistribution lap = new LaplaceDistribution(0,
						1 / (eps * (1 - Param.SPLIT_BUDGET_RATE)));
				sw.count = sw.count + lap.sample();
				ne.count = ne.count + lap.sample();
				se.count = se.count + lap.sample();
				nw.count = nw.count + lap.sample();
			}

			queue.add(ne);
			if (sw.count < k) {
				queue.add(ne);
			} else {
				ne.isLeave = true;
			}

			queue.add(se);
			queue.add(nw);

		}

		return root;
	}

	public Node partitionByOptimalKNS(int k, String infile, int maxlevel,
			boolean diff, double[] epslevel, double epscount) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("data/" + infile),
				Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int id = 1;
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split("\\s+");
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }, id));
			id++;
		}

		Node root = new Node(0, 0, 1000000, 1000000, Comparison.MIN);
		root.loadTuples(tuples);

		Queue<Node> queue = new LinkedList<>();
		queue.add(root);

		// double uniformeps = eps / maxlevel;
		HashSet<Node> leaves = new HashSet<>();
		int emptycount = 0;

		while (!queue.isEmpty()) {
			Node node = queue.remove();

			if (testLeaveNS(node, k, maxlevel, 1000000, 0.01 / 2.0)) {
				node.isLeave = true;
				if (leaves.add(node)) {
					if (node.level != maxlevel) { // remain budget
						double remaineps = 0.0;
						for (int i = node.level; i < maxlevel; i++) {
							remaineps += epslevel[node.level];
						}
						LaplaceDistribution lapr = new LaplaceDistribution(0,
								1 / remaineps);
						node.count = node.oridata.size() + lapr.sample();
					}
					if (node.oridata.size() == 0) {
						emptycount++;
					}
				}
				continue;
			}

			double eps = epslevel[node.level];

			if (node.count > k) {
				splitByOptimalK(k, node, diff, eps * Param.SPLIT_BUDGET_RATE);
			} else {
				splitByQuadTree(node);
			}

			Node sw = node.childMap.get("sw");
			if (sw.oridata.size() == node.oridata.size()) {
				node.childMap = new HashMap<String, Node>();
				node.children = new ArrayList<Node>();
				splitByQuadTree(node);
				sw = node.childMap.get("sw");
			}

			queue.add(sw);
			Node ne = node.childMap.get("ne");
			Node se = node.childMap.get("se");
			Node nw = node.childMap.get("nw");

			if (diff) {
				LaplaceDistribution lap = new LaplaceDistribution(0,
						1 / (eps * (1 - Param.SPLIT_BUDGET_RATE)));
				sw.count = sw.count + lap.sample();
				ne.count = ne.count + lap.sample();
				se.count = se.count + lap.sample();
				nw.count = nw.count + lap.sample();
			}

			queue.add(ne);
			if (sw.count < k) {
				queue.add(ne);
			} else {
				ne.isLeave = true;
			}

			queue.add(se);
			queue.add(nw);

		}

		ArrayList<Node> elist = new ArrayList<>(leaves);
		Collections.sort(elist, new NodeCountComparator());

		System.out.println("leave size: " + leaves.size() + " empty count "
				+ emptycount);
		LaplaceDistribution lapn = new LaplaceDistribution(0, 1.0 / epscount);
		int noiseemptycount = (int) (emptycount + lapn.sample());
		System.out.println("noisy empty count " + noiseemptycount);
		int index = 0;
		for (int i = 0; i < leaves.size(); i++) {
			if (Math.ceil(elist.get(i).count) > 0) {
				System.out.println("empty noisy count " + i
						+ " current node count " + elist.get(i).count);
				index = i;
				break;
			}
		}

		for (int i = 0; i < noiseemptycount / 2.0; i++) {
			System.out.println(elist.get(index).count + " -> 0");
			elist.get(index).count = 0;
			index++;
		}

		return root;
	}

	public Node partitionByOptimalKMinSize(int k, String infile, int maxlevel,
			boolean diff, double[] epslevel, double epscount) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("data/" + infile),
				Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int id = 1;
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split("\\s+");
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }, id));
			id++;
		}

		Node root = new Node(0, 0, 1000000, 1000000, Comparison.MIN);
		root.loadTuples(tuples);

		if (diff) {
			LaplaceDistribution laproot = new LaplaceDistribution(0,
					1 / (epslevel[0] * (1 - Param.SPLIT_BUDGET_RATE)));
			root.count = root.count + laproot.sample();
		}

		Queue<Node> queue = new LinkedList<>();
		queue.add(root);

		// double uniformeps = eps / maxlevel;
		HashSet<Node> leaves = new HashSet<>();
		int emptycount = 0;

		while (!queue.isEmpty()) {
			Node node = queue.remove();

			if (testLeaveMinSize(node, k, maxlevel, 0.01, 1000000, 1000000, 8)) {
				node.isLeave = true;
				if (leaves.add(node)) {
					if (node.level != maxlevel) { // remain budget
						double remaineps = 0.0;
						for (int i = node.level; i < maxlevel; i++) {
							remaineps += epslevel[node.level];
						}
						LaplaceDistribution lapr = new LaplaceDistribution(0,
								1 / remaineps);
						node.count = node.oridata.size() + lapr.sample();
					}
					if (node.oridata.size() == 0) {
						emptycount++;
					}
				}
				continue;
			}

			double eps = epslevel[node.level];

			if (node.count > k) {
				splitByOptimalK(k, node, diff, eps * Param.SPLIT_BUDGET_RATE);
			} else {
				splitByQuadTree(node);
			}

			Node sw = node.childMap.get("sw");
			if (sw.oridata.size() == node.oridata.size()) {
				node.childMap = new HashMap<String, Node>();
				node.children = new ArrayList<Node>();
				splitByQuadTree(node);
				sw = node.childMap.get("sw");
			}

			queue.add(sw);
			Node ne = node.childMap.get("ne");
			Node se = node.childMap.get("se");
			Node nw = node.childMap.get("nw");

			if (diff) {
				LaplaceDistribution lap = new LaplaceDistribution(
						0,
						1 / (epslevel[node.level + 1] * (1 - Param.SPLIT_BUDGET_RATE)));
				sw.count = sw.count + lap.sample();
				ne.count = ne.count + lap.sample();
				se.count = se.count + lap.sample();
				nw.count = nw.count + lap.sample();
			}

			queue.add(ne);
			if (sw.count < k) {
				queue.add(ne);
			} else {
				ne.isLeave = true;
			}

			queue.add(se);
			queue.add(nw);

		}

		// root = postProcessing(root, epslevel, maxlevel);
		// root = adjustConsistency(root, epslevel, maxlevel);

		ArrayList<Node> elist = new ArrayList<>(leaves);
		Collections.sort(elist, new NodeCountComparator());

		System.out.println("leave size: " + leaves.size() + " empty count "
				+ emptycount);
		LaplaceDistribution lapn = new LaplaceDistribution(0, 1.0 / epscount);
		int noiseemptycount = (int) (emptycount + lapn.sample());
		System.out.println("noisy empty count " + noiseemptycount);
		int index = 0;
		for (int i = 0; i < leaves.size(); i++) {
			if (Math.ceil(elist.get(i).count) > 0) {
				System.out.println("empty noisy count " + i
						+ " current node count " + elist.get(i).count);
				index = i;
				break;
			}
		}

		for (int i = 0; i < noiseemptycount / 2.0; i++) {
			System.out.println(elist.get(index).count + " -> 0");
			elist.get(index).count = 0;
			index++;
		}

		return root;
	}

	public Node partitionByOptimalKLevelRemoval(int k, String infile,
			int maxlevel, boolean diff, double[] epslevel, double epscount)
			throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("data/" + infile),
				Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int id = 1;
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split("\\s+");
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }, id));
			id++;
		}

		Node root = new Node(0, 0, 1000000, 1000000, Comparison.MIN);
		root.loadTuples(tuples);

		if (diff) {
			LaplaceDistribution laproot = new LaplaceDistribution(0,
					1 / (epslevel[0] * (1 - Param.SPLIT_BUDGET_RATE)));
			root.count = root.count + laproot.sample();
		}

		Queue<Node> queue = new LinkedList<>();
		queue.add(root);

		// double uniformeps = eps / maxlevel;
		HashSet<Node> leaves = new HashSet<>();
		int emptycount = 0;

		while (!queue.isEmpty()) {
			Node node = queue.remove();

			if (testLeaveMinSize(node, k, maxlevel, 0.01, 1000000, 1000000, 8)) {
				node.isLeave = true;
				if (leaves.add(node)) {
					if (node.level != maxlevel) { // remain budget
						double remaineps = 0.0;
						for (int i = node.level; i < maxlevel; i++) {
							remaineps += epslevel[node.level];
						}
						LaplaceDistribution lapr = new LaplaceDistribution(0,
								1 / remaineps);
						node.count = node.oridata.size() + lapr.sample();
					}
					if (node.oridata.size() == 0) {
						emptycount++;
					}
				}
				continue;
			}

			double eps = epslevel[node.level];

			if (node.count > k) {
				splitByOptimalK(k, node, diff, eps * Param.SPLIT_BUDGET_RATE);
			} else {
				splitByQuadTree(node);
			}

			Node sw = node.childMap.get("sw");
			if (sw.oridata.size() == node.oridata.size()) {
				node.childMap = new HashMap<String, Node>();
				node.children = new ArrayList<Node>();
				splitByQuadTree(node);
				sw = node.childMap.get("sw");
			}

			queue.add(sw);
			Node ne = node.childMap.get("ne");
			Node se = node.childMap.get("se");
			Node nw = node.childMap.get("nw");

			if (diff) {
				LaplaceDistribution lap = new LaplaceDistribution(
						0,
						1 / (epslevel[node.level + 1] * (1 - Param.SPLIT_BUDGET_RATE)));
				sw.count = sw.count + lap.sample();
				ne.count = ne.count + lap.sample();
				se.count = se.count + lap.sample();
				nw.count = nw.count + lap.sample();
			}

			queue.add(ne);
			if (sw.count < k) {
				queue.add(ne);
			} else {
				ne.isLeave = true;
			}

			queue.add(se);
			queue.add(nw);

		}

		// root = postProcessing(root, epslevel, maxlevel);
		// root = adjustConsistency(root, epslevel, maxlevel);

		int slevel = Param.REMOVAL_LEVEL; // ignore the first 3 levels
		int rlevel = maxlevel - slevel;
		double[] rleps = new double[maxlevel - slevel];
		for (int i = 0; i < rleps.length; i++) {
			rleps[i] = epscount / (maxlevel - slevel);
		}

		ArrayList<Node> nlist = new ArrayList<>(root.children);
		emptycount = 0;
		for (int l = 1; l <= maxlevel; l++) {
			ArrayList<Node> templist = new ArrayList<>();
			if (l > slevel) {
				double reps = rleps[l - slevel - 1];
				Collections.sort(nlist, new NodeCountComparator());
				LaplaceDistribution lapn = new LaplaceDistribution(0,
						1.0 / reps);
				int noiseemptycount = (int) (emptycount + lapn.sample());
				System.out.println("empty count " + emptycount
						+ " noisy count " + noiseemptycount);
				int index = 0;
				for (int i = 0; i < leaves.size(); i++) {
					if (Math.ceil(nlist.get(i).count) > 0) {
						System.out.println("empty noisy count " + i
								+ " current node count " + nlist.get(i).count);
						index = i;
						break;
					}
				}

				for (int i = 0; i < noiseemptycount / 2.0; i++) {
					Node node = nlist.get(index);
					System.out.println(node.count + " -> 0");
					node.count = 0;
					node.isLeave = true;
					node.children = new ArrayList<>();
					node.childMap = new HashMap<>();
					index++;
				}
			}
			emptycount = 0;
			for (Node n : nlist) {
				if (!n.isLeave) {
					templist.addAll(n.children);
					for (Node c : n.children) {
						if (c.oridata.size() == 0) {
							emptycount++;
						}
					}

				}
			}
			nlist = templist;
		}

		// ArrayList<Node> elist = new ArrayList<>(leaves);
		// Collections.sort(elist, new NodeCountComparator());
		//
		// System.out.println("leave size: " + leaves.size() + " empty count "
		// + emptycount);
		// LaplaceDistribution lapn = new LaplaceDistribution(0, 1.0 /
		// epscount);
		// int noiseemptycount = (int) (emptycount + lapn.sample());
		// System.out.println("noisy empty count " + noiseemptycount);
		// int index = 0;
		// for (int i = 0; i < leaves.size(); i++) {
		// if (Math.ceil(elist.get(i).count) > 0) {
		// System.out.println("empty noisy count " + i
		// + " current node count " + elist.get(i).count);
		// index = i;
		// break;
		// }
		// }
		//
		// for (int i = 0; i < noiseemptycount / 2.0; i++) {
		// System.out.println(elist.get(index).count + " -> 0");
		// elist.get(index).count = 0;
		// index++;
		// }

		return root;
	}

	public Node adjustConsistency(Node root, double[] epslevel, int maxlevel) {
		// logging.debug('adjusting consistency...')
		// ### upward pass
		// self.root.get_z()
		// ### downward pass
		// queue = deque()
		// queue.append(self.root)
		// while (len(queue) > 0):
		// curr = queue.popleft()
		// if curr.n_isLeaf is False:
		// adjust = (curr.n_count - curr.nw.n_count - curr.ne.n_count -
		// curr.sw.n_count - curr.se.n_count)/4.0
		// for subnode in [curr.nw, curr.ne, curr.sw, curr.se]:
		// subnode.n_count += adjust
		// queue.append(subnode)

		// upward pass
		root.get_z(maxlevel);

		// downward pass
		Queue<Node> queue = new LinkedList<>();
		queue.add(root);
		while (queue.size() > 0) {
			Node curr = queue.remove();
			if (!curr.isLeave) {
				Node nw = curr.childMap.get("nw");
				Node ne = curr.childMap.get("nw");
				Node sw = curr.childMap.get("sw");
				Node se = curr.childMap.get("se");
				double adjust = (curr.count - nw.count - ne.count - sw.count - se.count) / 4.0;
				for (Node child : curr.children) {
					child.count += adjust;
					queue.add(child);
				}
			}

		}
		return root;
	}

	public Node postProcessing(Node root, double[] epslevel, int maxlevel) {

		// Phase 1 (top-down)
		Queue<Node> queue = new LinkedList<>();
		root.count *= Math.pow(epslevel[root.level], 2);
		queue.add(root);
		while (queue.size() > 0) {
			Node curr = queue.remove();
			if (!curr.isLeave) {
				for (Node sub : curr.children) {
					sub.count = curr.count + sub.count
							* (Math.pow(epslevel[sub.level], 2));
					queue.add(sub);
				}

			}
		}

		// while (len(queue) > 0):
		// curr = queue.popleft()
		// if curr.n_isLeaf is False:
		// for subnode in [curr.nw, curr.ne, curr.sw, curr.se]:
		// subnode.n_count = curr.n_count +
		// subnode.n_count*(budget[subnode.n_depth]**2)
		// queue.append(subnode)
		// Phase 2 (bottom-up)
		root.updateCount();

		// Phase 3 (top-down)
		queue = new LinkedList<>();
		double Eroot = 0;
		for (int j = 0; j < epslevel.length; j++) {
			Eroot += Math.pow(4, j) * epslevel[maxlevel - j]
					* epslevel[maxlevel - j];
		}

		root.count /= Eroot;
		root.n_F = 0;
		queue.add(root);
		while (queue.size() > 0) {
			Node curr = queue.remove();
			if (!curr.isLeave) {
				int h = maxlevel - curr.level - 1;
				double E_h = 0;
				for (int j = 0; j < h + 1; j++) {
					E_h += Math.pow(4, j) * epslevel[maxlevel - j]
							* epslevel[maxlevel - j];
				}
				for (Node sub : curr.children) {
					sub.n_F = curr.n_F + curr.count
							* (Math.pow(epslevel[curr.level], 2));
					sub.count = (sub.count - Math.pow(4, h) * sub.n_F) / E_h;
					queue.add(sub);
				}
			}
		}

		return root;
	}

	public Node partitionByOptimalKNSTrueCount(int k, String infile,
			int maxlevel, boolean diff, double[] epslevel, double epscount)
			throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("data/" + infile),
				Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int id = 1;
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split("\\s+");
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }, id));
			id++;
		}

		Node root = new Node(0, 0, 1000000, 1000000, Comparison.MIN);
		root.loadTuples(tuples);

		Queue<Node> queue = new LinkedList<>();
		queue.add(root);

		// double uniformeps = eps / maxlevel;
		ArrayList<Node> leaves = new ArrayList<>();
		int emptycount = 0;

		while (!queue.isEmpty()) {
			Node node = queue.remove();
			if (testLeaveNS(node, k, maxlevel, 1000000, 0.01 / 2.0)) {
				node.isLeave = true;
				leaves.add(node);
				if (node.oridata.size() == 0) {
					emptycount++;
				}
				continue;
			}

			double eps = epslevel[node.level];
			double rate = 0.3;

			if (node.oridata.size() > k) {
				splitByOptimalK(k, node, diff, eps * rate);
			} else {
				splitByQuadTree(node);
			}

			Node sw = node.childMap.get("sw");
			if (sw.oridata.size() == node.oridata.size()) {
				node.childMap = new HashMap<String, Node>();
				node.children = new ArrayList<Node>();
				splitByQuadTree(node);
				sw = node.childMap.get("sw");
			}

			queue.add(sw);
			Node ne = node.childMap.get("ne");
			Node se = node.childMap.get("se");
			Node nw = node.childMap.get("nw");

			if (diff) {
				LaplaceDistribution lap = new LaplaceDistribution(0,
						1 / (eps * (1 - rate)));
				sw.count = sw.count + Math.round(lap.sample());
				ne.count = ne.count + Math.round(lap.sample());
				se.count = se.count + Math.round(lap.sample());
				nw.count = nw.count + Math.round(lap.sample());
			}

			if (sw.oridata.size() < k) {
				queue.add(ne);
			} else {
				ne.isLeave = true;
			}

			queue.add(se);
			queue.add(nw);

		}

		Collections.sort(leaves, new NodeCountComparator());
		System.out.println("leave size: " + leaves.size() + " empty count "
				+ emptycount);
		LaplaceDistribution lapn = new LaplaceDistribution(0, 1.0 / epscount);
		int noiseemptycount = (int) (emptycount + lapn.sample());
		System.out.println("noisy empty count " + noiseemptycount);
		int index = 0;
		for (int i = 0; i < leaves.size(); i++) {
			if (leaves.get(i).count > 0) {
				System.out.println("empty noisy count " + i
						+ " current node count " + leaves.get(i).count);
				index = i;
				break;
			}
		}

		for (int i = 0; i < noiseemptycount; i++) {
			leaves.get(i).count = 0;
		}

		return root;
	}

	public Node partitionByOptimalKWithNE(int k, String infile, int maxlevel,
			boolean diff, double[] epslevel) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("data/" + infile),
				Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int id = 1;
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split("\\t");
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }, id));
			id++;
		}

		Node root = new Node(0, 0, 1000000, 1000000, Comparison.MIN);
		root.loadTuples(tuples);

		Queue<Node> queue = new LinkedList<>();
		queue.add(root);

		// double uniformeps = eps / maxlevel;

		while (!queue.isEmpty()) {
			Node node = queue.remove();
			if (testLeave(node, k, maxlevel, 1000000, 0.01)) {
				node.isLeave = true;
				continue;
			}

			if (node.oridata.size() > k) {
				splitByOptimalK(k, node, diff, epslevel[node.level]);
			} else {
				splitByQuadTree(node);
				// continue;
			}

			Node sw = node.childMap.get("sw");
			if (sw.oridata.size() == node.oridata.size()) {
				node.isLeave = true;
				node.childMap = new HashMap<String, Node>();
				node.children = new ArrayList<Node>();
				continue;
			}

			queue.add(sw);
			queue.add(node.childMap.get("se"));
			queue.add(node.childMap.get("ne"));
			queue.add(node.childMap.get("nw"));
		}

		return root;
	}

	private void splitByQuadTree(Node node) throws Exception {
		double xsplit = node.xmin + (node.xmax - node.xmin) / 2.0;
		double ysplit = node.ymin + (node.ymax - node.ymin) / 2.0;
		node.split(xsplit, ysplit);
	}

	private void splitByOptimalKComp(int k, Node node, boolean diff,
			double eps, Comparison comp) throws Exception {
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
				if (comp == Comparison.MIN) {
					if (t.getValue(1) < ysplit) {
						found.add(t);
						toremove.add(t);
					}
				} else {
					if (t.getValue(1) > ysplit) {
						found.add(t);
						toremove.add(t);
					}
				}

			}

			for (IDTuple i : toremove) {
				xNotQualified.remove(i);
			}

			toremove = new HashSet<IDTuple>();
			for (IDTuple t : yNotQualified) {
				if (comp == Comparison.MIN) {
					if (t.getValue(0) < xsplit) {
						found.add(t);
						toremove.add(t);
					}
				} else {
					if (t.getValue(0) > xsplit) {
						found.add(t);
						toremove.add(t);
					}
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
			xsplit = EM.getValueBasedOnRank(node.oridata, eps, xsplit, 0,
					node.xmin, node.xmax);
			ysplit = EM.getValueBasedOnRank(node.oridata, eps, ysplit, 1,
					node.ymin, node.ymax);
		}

		node.split(xsplit + 0.001, ysplit + 0.001);
	}

	private void splitByOptimalK(int k, Node node, boolean diff, double eps)
			throws Exception {
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
			xsplit = EM.getValueBasedOnRank(node.oridata, eps, xsplit, 0,
					node.xmin, node.xmax);
			ysplit = EM.getValueBasedOnRank(node.oridata, eps, ysplit, 1,
					node.ymin, node.ymax);
		}

		node.split(xsplit + 0.001, ysplit + 0.001);
	}

	private boolean testLeave(Node node, int k, int maxlevel, double max,
			double rate) {
		if (node.isLeave) {
			return true;
		}

		if (node.level == maxlevel) {
			return true;
		}

		if (node.count <= k && (node.xmax - node.xmin < max * rate)
				&& (node.ymax - node.ymin < max * rate)) {
			return true;
		}
		return false;
	}

	private boolean testLeaveNS(Node node, int k, int maxlevel, double max,
			double rate) {
		if (node.isLeave) {
			return true;
		}

		// if (node.level == maxlevel || node.oridata.size() <= 2) {
		if (node.level == maxlevel) {
			return true;
		}

		return false;
	}

	private boolean testLeaveMinSize(Node node, int k, int maxlevel,
			double rate, double domainWidth, double domainHeight, int minsize) {
		if (node.isLeave) {
			return true;
		}

		// if (node.oridata.size() == 0) {
		// return true;
		// }

//		if (node.level == maxlevel
//				|| ((node.xmax - node.xmin < domainWidth * rate * 0.5) && (node.ymax
//						- node.ymin < domainHeight * rate * 0.5))) {
		if (node.level == maxlevel) {
			return true;
		}

		if (node.count <= minsize) {
			return true;
		}

		return false;
	}

	public Node partitionByOptimalKNoisyNegative(int k, String folder,
			String infile, int maxlevel, boolean diff, double[] epslevel,
			double epscount, String deliminator, double domainwidth,
			double domainheight, Comparison comp, int minsize, int kdelta)
			throws Exception {
		List<String> lines = Files.readAllLines(
				Paths.get(folder + "/" + infile), Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int id = 1;
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split(deliminator);
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }, id));
			id++;
		}

		Node root = new Node(0, 0, domainwidth, domainheight, comp);
		root.loadTuples(tuples);

		if (diff) {
			LaplaceDistribution laproot = new LaplaceDistribution(0,
					1 / (epslevel[0] * (1 - Param.SPLIT_BUDGET_RATE)));
			root.count = root.count + laproot.sample();
		}

		Queue<Node> queue = new LinkedList<>();
		queue.add(root);

		// double uniformeps = eps / maxlevel;
		HashSet<Node> leaves = new HashSet<>();
		int noisynegativecount = 0;

		while (!queue.isEmpty()) {
			Node node = queue.remove();

			if (testLeaveMinSize(node, k, maxlevel, 0.01, domainwidth,
					domainheight, minsize)) {
				node.isLeave = true;
				if (leaves.add(node)) {
					if (node.level != maxlevel) { // remain budget
						double remaineps = 0.0;
						for (int i = node.level; i < maxlevel; i++) {
							remaineps += epslevel[node.level];
						}
						LaplaceDistribution lapr = new LaplaceDistribution(0,
								1 / remaineps);
						node.count = node.oridata.size() + lapr.sample();
					}
					if (node.oridata.size() == 0) {
						noisynegativecount++;
					}
				}
				continue;
			}

			double eps = epslevel[node.level];
			double counteps = epslevel[node.level + 1]
					* (1 - Param.SPLIT_BUDGET_RATE);
			double kprime = Math.ceil(k + Math.sqrt(2) / counteps);
			if (node.count > k + Math.sqrt(2) / counteps) {
				splitByOptimalKComp((int) kprime, node, diff, eps
						* Param.SPLIT_BUDGET_RATE, comp);
			} else {
				splitByQuadTree(node);
			}

			if (comp == Comparison.MIN) {

				Node sw = node.childMap.get("sw");
				if (sw.oridata.size() == node.oridata.size()) {
					node.childMap = new HashMap<String, Node>();
					node.children = new ArrayList<Node>();
					splitByQuadTree(node);
					sw = node.childMap.get("sw");
				}

				queue.add(sw);
				Node ne = node.childMap.get("ne");
				Node se = node.childMap.get("se");
				Node nw = node.childMap.get("nw");

				if (diff) {

					if (node.count > k + Math.sqrt(2) / counteps) {
						LaplaceDistribution lap = new LaplaceDistribution(
								0,
								1 / (epslevel[node.level + 1] * (1 - Param.SPLIT_BUDGET_RATE)));
						sw.count = sw.count + lap.sample();
						ne.count = ne.count + lap.sample();
						se.count = se.count + lap.sample();
						nw.count = nw.count + lap.sample();
					} else {
						LaplaceDistribution lap = new LaplaceDistribution(0,
								1 / (epslevel[node.level + 1]));
						sw.count = sw.count + lap.sample();
						ne.count = ne.count + lap.sample();
						se.count = se.count + lap.sample();
						nw.count = nw.count + lap.sample();
					}

				}

				if (sw.count < k) {
					queue.add(ne);
				} else {
					ne.isLeave = true;
				}

				queue.add(se);
				queue.add(nw);
			} else {
				Node ne = node.childMap.get("ne");
				if (ne.oridata.size() == node.oridata.size()) {
					node.childMap = new HashMap<String, Node>();
					node.children = new ArrayList<Node>();
					splitByQuadTree(node);
					ne = node.childMap.get("ne");
				}

				queue.add(ne);
				Node sw = node.childMap.get("sw");
				Node se = node.childMap.get("se");
				Node nw = node.childMap.get("nw");

				if (diff) {
					if (node.count > k + Math.sqrt(2) / counteps) {
						LaplaceDistribution lap = new LaplaceDistribution(
								0,
								1 / (epslevel[node.level + 1] * (1 - Param.SPLIT_BUDGET_RATE)));
						sw.count = sw.count + lap.sample();
						ne.count = ne.count + lap.sample();
						se.count = se.count + lap.sample();
						nw.count = nw.count + lap.sample();
					} else {
						LaplaceDistribution lap = new LaplaceDistribution(0,
								1 / (epslevel[node.level + 1]));
						sw.count = sw.count + lap.sample();
						ne.count = ne.count + lap.sample();
						se.count = se.count + lap.sample();
						nw.count = nw.count + lap.sample();
					}
				}

				if (ne.count < k) {
					queue.add(sw);
				} else {
					sw.isLeave = true;
				}

				queue.add(se);
				queue.add(nw);
			}

		}

		// root = postProcessing(root, epslevel, maxlevel);
		// root = adjustConsistency(root, epslevel, maxlevel);

		int slevel = Param.REMOVAL_LEVEL; // ignore the first 3 levels
		int rlevel = maxlevel - slevel;
		ArrayList<Node> nlist = new ArrayList<>(root.children);
		noisynegativecount = 0;
		for (int l = 1; l <= maxlevel; l++) {
			ArrayList<Node> templist = new ArrayList<>();
			if (l > slevel) {
				Collections.sort(nlist, new NodeCountComparator());
				System.out
						.println("noisy negative count " + noisynegativecount);
				int index = 0;
				for (int i = 0; i < nlist.size(); i++) {
					if (Math.ceil(nlist.get(i).count) > 0) {
						System.out.println("empty noisy count " + i
								+ " current node count " + nlist.get(i).count);
						index = i;
						break;
					}
				}

				for (int i = 0; i < noisynegativecount && index < nlist.size(); i++) {
					Node node = nlist.get(index);
					System.out.println(node.count + " -> 0");
					node.count = 0;
					node.isLeave = true;
					node.children = new ArrayList<>();
					node.childMap = new HashMap<>();
					index++;
				}
			}
			noisynegativecount = 0;
			for (Node n : nlist) {
				if (!n.isLeave) {
					templist.addAll(n.children);
					for (Node c : n.children) {
						if (c.count < 0) {
							noisynegativecount++;
						}
					}

				}
			}
			nlist = templist;
		}
//
//		for (int i = 0; i < nlist.size(); i++) {
//			Node node = nlist.get(i);
//			System.out.println(node.count + " -> 0");
//			if (node.count < 1.0) {
//				node.count = 0;
//				node.isLeave = true;
//				node.children = new ArrayList<>();
//				node.childMap = new HashMap<>();
//			}
//		}

		
		return root;
	}

	public Node partitionByAdaptiveKNoisyNegative(int k, String folder,
			String infile, int maxlevel, boolean diff, double eps,
			String deliminator, double domainwidth, double domainheight,
			Comparison comp, int minsize, int kdelta) throws Exception {
		List<String> lines = Files.readAllLines(
				Paths.get(folder + "/" + infile), Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int id = 1;
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split(deliminator);
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }, id));
			id++;
		}

		Node root = new Node(0, 0, domainwidth, domainheight, comp);
		root.loadTuples(tuples);
		root.remaineps = eps;

		double alpha = 0.1;
		double splitrate = Param.SPLIT_BUDGET_RATE;

		Queue<Node> queue = new LinkedList<>();
		queue.add(root);

		// double uniformeps = eps / maxlevel;
		HashSet<Node> tobePartitioned = new HashSet<>();

		while (!queue.isEmpty()) {
			Node node = queue.remove();
			double curreps = node.remaineps * alpha;
			double spliteps = curreps * splitrate;
			int kwithvariant = (int) (k + kdelta + Math.sqrt(2)
					/ (curreps * (1 - splitrate)));
			if (node.count > kwithvariant && node.level < 2) {
				splitByOptimalKComp(kwithvariant, node, diff, spliteps, comp);
				if (comp == Comparison.MIN) {

					Node sw = node.childMap.get("sw");
					sw.remaineps = node.remaineps - curreps;
					if (sw.oridata.size() == node.oridata.size()) {
						tobePartitioned.add(node);
						continue;
					}

					tobePartitioned.add(sw); // no more split by k
					Node ne = node.childMap.get("ne");
					ne.remaineps = node.remaineps - curreps + curreps
							* (1 - splitrate);
					Node se = node.childMap.get("se");
					se.remaineps = node.remaineps - curreps + curreps
							* (1 - splitrate);
					Node nw = node.childMap.get("nw");
					nw.remaineps = node.remaineps - curreps + curreps
							* (1 - splitrate);

					if (diff) {
						LaplaceDistribution lap = new LaplaceDistribution(0,
								1 / (curreps * (1 - splitrate)));
						sw.count = sw.count + lap.sample();
						sw.counteps = curreps * (1 - splitrate);
					}

					if (sw.count < k) {
						queue.add(ne);
					} else {
						ne.isLeave = true;
					}

					queue.add(se);
					queue.add(nw);
				} else {
					Node ne = node.childMap.get("ne");
					ne.remaineps = node.remaineps - curreps;
					if (ne.oridata.size() == node.oridata.size()) {
						tobePartitioned.add(node);
						continue;
					}

					tobePartitioned.add(ne); // no more split by k
					Node sw = node.childMap.get("sw");
					sw.remaineps = node.remaineps - curreps + curreps
							* (1 - splitrate);
					Node se = node.childMap.get("se");
					se.remaineps = node.remaineps - curreps + curreps
							* (1 - splitrate);
					Node nw = node.childMap.get("nw");
					nw.remaineps = node.remaineps - curreps + curreps
							* (1 - splitrate);

					if (diff) {
						LaplaceDistribution lap = new LaplaceDistribution(0,
								1 / (curreps * (1 - splitrate)));
						ne.count = ne.count + lap.sample();
						ne.counteps = curreps * (1 - splitrate);
					}

					if (ne.count < k) {
						queue.add(sw);
					} else {
						sw.isLeave = true;
					}

					queue.add(se);
					queue.add(nw);
				}
			} else {
				tobePartitioned.add(node);
			}
		}

		ArrayList<Node> nlist = new ArrayList<Node>();
		int noisynegativecount = 0;
		for (Node node : tobePartitioned) {
			node.splitByUniformGrid(domainwidth, domainheight, 0.01);
			for (Node child : node.children) {
				if (diff) {
					LaplaceDistribution lap = new LaplaceDistribution(0,
							1 / (node.remaineps));
					child.count = child.count + lap.sample();
				}
				if (child.count < 0) {
					noisynegativecount++;
				}
				nlist.add(child);
				child.isLeave = true;
			}
		}

		Collections.sort(nlist, new NodeCountComparator());
		System.out.println("noisy negative count " + noisynegativecount);
		int index = 0;
		for (int i = 0; i < nlist.size(); i++) {
			if (Math.ceil(nlist.get(i).count) > 0) {
				System.out.println("empty noisy count " + i
						+ " current node count " + nlist.get(i).count);
				index = i;
				break;
			}
		}

		for (int i = 0; i < noisynegativecount && index < nlist.size(); i++) {
			Node node = nlist.get(index);
			System.out.println(node.count + " -> 0");
			node.count = 0;
			node.isLeave = true;
			index++;
		}

		for (int i = 0; i < nlist.size(); i++) {
			Node node = nlist.get(i);
			if (node.count < 1) {
				System.out.println(node.count + " -> 0");
				node.count = 0;
				node.isLeave = true;
			}
		}

		return root;
	}

	public Node partitionByAdaptiveUniformGridNoisyNegative(int k,
			String folder, String infile, int maxlevel, boolean diff,
			double eps, String deliminator, double domainwidth,
			double domainheight, Comparison comp, int minsize, int kdelta)
			throws Exception {
		List<String> lines = Files.readAllLines(
				Paths.get(folder + "/" + infile), Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int id = 1;
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split(deliminator);
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }, id));
			id++;
		}

		Node root = new Node(0, 0, domainwidth, domainheight, comp);
		root.loadTuples(tuples);
		root.remaineps = eps;

		double alpha = 0.1;
		double splitrate = Param.SPLIT_BUDGET_RATE;

		Queue<Node> queue = new LinkedList<>();
		queue.add(root);

		// double uniformeps = eps / maxlevel;
		HashSet<Node> tobePartitioned = new HashSet<>();

		while (!queue.isEmpty()) {
			Node node = queue.remove();
			double curreps = node.remaineps * alpha;
			double spliteps = curreps * splitrate;
			int kwithvariant = (int) (k + kdelta + Math.sqrt(2)
					/ (curreps * (1 - splitrate)));
			if (node.count > kwithvariant && node.level < 2) {
				splitByOptimalKComp(kwithvariant, node, diff, spliteps, comp);
				if (comp == Comparison.MIN) {

					Node sw = node.childMap.get("sw");
					sw.remaineps = node.remaineps - curreps;
					if (sw.oridata.size() == node.oridata.size()) {
						tobePartitioned.add(node);
						continue;
					}

					tobePartitioned.add(sw); // no more split by k
					Node ne = node.childMap.get("ne");
					ne.remaineps = node.remaineps - curreps;
					Node se = node.childMap.get("se");
					se.remaineps = node.remaineps - curreps;
					Node nw = node.childMap.get("nw");
					nw.remaineps = node.remaineps - curreps;

					if (diff) {
						LaplaceDistribution lap = new LaplaceDistribution(0,
								1 / (curreps * (1 - splitrate)));
						sw.count = sw.count + lap.sample();
						sw.counteps = curreps * (1 - splitrate);

						ne.count = ne.count + lap.sample();
						ne.counteps = curreps * (1 - splitrate);

						se.count = se.count + lap.sample();
						se.counteps = curreps * (1 - splitrate);

						nw.count = nw.count + lap.sample();
						nw.counteps = curreps * (1 - splitrate);
					}

					if (sw.count < k) {
						queue.add(ne);
					} else {
						ne.isLeave = true;
					}

					queue.add(se);
					queue.add(nw);
				} else {
					Node ne = node.childMap.get("ne");
					ne.remaineps = node.remaineps - curreps;
					if (ne.oridata.size() == node.oridata.size()) {
						tobePartitioned.add(node);
						continue;
					}

					tobePartitioned.add(ne); // no more split by k
					Node sw = node.childMap.get("sw");
					sw.remaineps = node.remaineps - curreps + curreps
							* (1 - splitrate);
					Node se = node.childMap.get("se");
					se.remaineps = node.remaineps - curreps + curreps
							* (1 - splitrate);
					Node nw = node.childMap.get("nw");
					nw.remaineps = node.remaineps - curreps + curreps
							* (1 - splitrate);

					if (diff) {
						LaplaceDistribution lap = new LaplaceDistribution(0,
								1 / (curreps * (1 - splitrate)));
						ne.count = ne.count + lap.sample();
						ne.counteps = curreps * (1 - splitrate);

						sw.count = sw.count + lap.sample();
						sw.counteps = curreps * (1 - splitrate);

						se.count = se.count + lap.sample();
						se.counteps = curreps * (1 - splitrate);

						nw.count = nw.count + lap.sample();
						nw.counteps = curreps * (1 - splitrate);
					}

					if (ne.count < k) {
						queue.add(sw);
					} else {
						sw.isLeave = true;
					}

					queue.add(se);
					queue.add(nw);
				}
			} else {
				tobePartitioned.add(node);
			}
		}

		ArrayList<Node> nlist = new ArrayList<Node>();
		int noisynegativecount = 0;
		for (Node node : tobePartitioned) {
			node.splitByAdaptiveUniformGrid(domainwidth, domainheight, 0.01);
			if (node.children.size() == 0) {
				node.count = node.oridata.size()
						+ new LaplaceDistribution(0,
								1 / (node.remaineps + node.counteps)).sample();
				node.isLeave = true;
				nlist.add(node);
			} else {
				for (Node child : node.children) {
					if (diff) {
						LaplaceDistribution lap = new LaplaceDistribution(0,
								1 / (node.remaineps));
						child.count = child.count + lap.sample();
					}
					if (child.count < 0) {
						noisynegativecount++;
					}
					nlist.add(child);
					child.isLeave = true;
				}
			}
		}

		Collections.sort(nlist, new NodeCountComparator());
		System.out.println("noisy negative count " + noisynegativecount);
		int index = 0;
		for (int i = 0; i < nlist.size(); i++) {
			if (Math.ceil(nlist.get(i).count) > 0) {
				System.out.println("empty noisy count " + i
						+ " current node count " + nlist.get(i).count);
				index = i;
				break;
			}
		}

		for (int i = 0; i < noisynegativecount && index < nlist.size(); i++) {
			Node node = nlist.get(index);
			System.out.println(node.count + " -> 0");
			node.count = 0;
			node.isLeave = true;
			index++;
		}

		for (int i = 0; i < nlist.size(); i++) {
			Node node = nlist.get(i);
			if (node.count < 1) {
				System.out.println(node.count + " -> 0");
				node.count = 0;
				node.isLeave = true;
			}
		}

		return root;
	}

	private boolean testQuadTreeLeave(Node node, int k, int maxlevel,
			double rate, double domainWidth, double domainHeight, int minsize) {
		if (node.isLeave) {
			return true;
		}

		if (node.level == maxlevel) {
			return true;
		}

//		if (node.count <= minsize) {
//			return true;
//		}

		return false;
	}

	public Node partitionByQuadtree(int k, String folder, String infile,
			int maxlevel, boolean diff, double[] epslevel, double epscount,
			String deliminator, double domainwidth, double domainheight,
			Comparison comp, int minsize, int kdelta) throws Exception {
		List<String> lines = Files.readAllLines(
				Paths.get(folder + "/" + infile), Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int id = 1;
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split(deliminator);
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }, id));
			id++;
		}

		Node root = new Node(0, 0, domainwidth, domainheight, comp);
		root.loadTuples(tuples);

		if (diff) {
			LaplaceDistribution laproot = new LaplaceDistribution(0,
					1 / (epslevel[0]));
			root.count = root.count + laproot.sample();
		}

		Queue<Node> queue = new LinkedList<>();
		queue.add(root);

		while (!queue.isEmpty()) {
			Node node = queue.remove();

			if (testQuadTreeLeave(node, k, maxlevel, 0.01, domainwidth,
					domainheight, minsize)) {
				node.isLeave = true;
//				if (node.level != maxlevel) { // remain budget
//					double remaineps = 0.0;
//					for (int i = 0; i < epslevel.length; i++) {
//						remaineps += epslevel[i];
//					}
//					LaplaceDistribution lapr = new LaplaceDistribution(0,
//							1 / remaineps);
//					node.count = node.oridata.size() + lapr.sample();
//				}
				continue;
			}

			double counteps = epslevel[node.level + 1];
			splitByQuadTree(node);
			LaplaceDistribution lap = new LaplaceDistribution(0, 1 / counteps);

			for (Node child : node.children) {
				child.count = child.count + lap.sample();
				queue.add(child);
			}
		}

		return root;
	}
}

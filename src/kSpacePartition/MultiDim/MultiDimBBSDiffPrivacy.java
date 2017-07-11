package kSpacePartition.MultiDim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import kSpacePartition.IDTuple;
import kSpacePartition.PerfStatistics;
import skyband.Comparison;
import skyband.Tuple;
import skyband.UniformRandom;

public class MultiDimBBSDiffPrivacy {

	public static void computeBBSWithSynthesis(int dim, String inputfile,
			String outputfile, int k, String outputfolder, String inputfolder,
			Comparison[] comp, double delta, ArrayList<PerfStatistics> perfs)
			throws Exception {
		ArrayList<Tuple> skyband = new ArrayList<>();
		HashMap<String, MultiDimGrid> map = new HashMap<>();
		FileInputStream in = new FileInputStream(inputfolder + "/" + inputfile);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		PrintWriter out = new PrintWriter(outputfolder + "/" + outputfile);
		PrintWriter perfout = new PrintWriter("debug.txt");

		String strLine = null;
		String[] mystring = new String[9];
		PriorityQueue<MultiDimGrid> queue = new PriorityQueue<>();

		while ((strLine = br.readLine()) != null) {

			strLine = strLine.trim();
			mystring = strLine.split(",");
			// System.out.println("mystring length is: " + mystring.length);

			if (mystring.length < 8) {
				// System.out.println("test");
				continue;
			}

			double count_double = Double.parseDouble(mystring[0].trim());
			int index = 1;
			double[][] ranges = new double[dim][2];
			for (int i = 0; i < dim; i++) {
				ranges[i][0] = Double.parseDouble(mystring[index].trim());
				index++;
				ranges[i][1] = Double.parseDouble(mystring[index].trim());
				index++;
			}
			int level = (int) Double.parseDouble(mystring[index].trim());
			index++;
			String id = mystring[index].trim();
			index++;
			String parentid = mystring[index].trim();
			index++;
			level = (int) Double.parseDouble(mystring[index].trim());
			index++;
			MultiDimGrid grid = new MultiDimGrid(dim);
			grid.ranges = ranges;
			grid.count = count_double;
			grid.id = id;
			grid.level = level;
			grid.comp = comp[0];
			if (comp[0] == Comparison.MIN) {
				double[] minvalues = new double[dim];
				for (int i = 0; i < minvalues.length; i++) {
					minvalues[i] = ranges[i][0];
				}
				grid.tuple = new Tuple(minvalues);
			} else if (comp[0] == Comparison.MAX) {
				double[] maxvalues = new double[dim];
				for (int i = 0; i < maxvalues.length; i++) {
					maxvalues[i] = ranges[i][1];
				}
				grid.tuple = new Tuple(maxvalues);
			}
			if (!parentid.equals("null")) {
				grid.parent = map.get(parentid);
				grid.parent.children.add(grid);
			}
			map.put(id, grid);
			if (parentid.equals("null")) {
				queue.offer(grid);
			}
		}

		long starttime = System.currentTimeMillis();
		while (!queue.isEmpty()) {
			MultiDimGrid head = queue.poll();
			// System.out.println("grid: " + head.level);
			perfout.println("grid: " + head.level);

			// to check whether the node is dominated by more than k pts at
			// skyband. If so, prune it.
			boolean needtoadd = true;
			for (Tuple t : skyband) {
				if (t.dominate(head.tuple, comp) == 1) {
					head.tuple.dominatedCount++;
					if (head.tuple.dominatedCount > k) {
						needtoadd = false; // discard grid
						// System.out.println("discard grid " + head);
						perfout.println("discard grid " + head);
					}
				}
			}

			// if the node is not dominated by more than k pts at skyband, there
			// are 3 cases: 1) if it is not
			// a leaf node, expand its children and check each child to see
			// whether the child is dominated by more
			// than k pts at skyband, if the child is not dominated, added it to
			// the queue.
			// 2) if it is a leaf node, get its boundary and synthesize the data
			// points. For each random generation,
			// check the randomly generated point against the skyband points to
			// see whether it is dominated k times.
			// if not, add the randomly generated point to the queue.
			// 3) if it is a synthesized point and not dominated by > k times,
			// added to skyband
			if (needtoadd) {
				if (head.children.size() > 1) {
					for (MultiDimGrid child : head.children) {
						for (Tuple t : skyband) {
							if (t.dominate(child.tuple, comp) == 1) {
								head.tuple.dominatedCount++;
							}
						}
						if (child.tuple.dominatedCount <= k) {
							queue.offer(child);
							// System.out.println("add child to queue: " +
							// child);
							perfout.println("add child to queue: " + child);
						}
					}
				}

				if (head.children.size() == 0 && head.level != -1) {
					if (head.count > 0) {
						for (int i = 0; i < Math.ceil(head.count); i++) {
							Tuple s = UniformRandom.getRandomTuple(head.ranges,
									delta);
							for (Tuple t : skyband) {
								if (t.dominate(s, comp) == 1) {
									s.dominatedCount++;
								}
							}

							if (s.dominatedCount <= k) {
								MultiDimGrid newg = new MultiDimGrid(dim);
								for (int j = 0; j < dim; j++) {
									newg.ranges[j][0] = s.getValue(j);
									newg.ranges[j][1] = s.getValue(j);
								}

								newg.level = -1;
								newg.comp = comp[0];

								newg.tuple = new Tuple(s.getValues());
								queue.offer(newg);
								// System.out.println("add synthesized child to
								// queue: " + newg);
								perfout.println("add synthesized child to queue: "
										+ newg);
							}
						}
					}
				}

				if (head.level == -1) {
					skyband.add(head.tuple);
					// System.out.println("found skyband node: " + head.tuple);
					perfout.println("found skyband node: " + head.tuple);
				}
			}

		}

		long endtime = System.currentTimeMillis();
		if (perfs != null) {
			perfs.add(new PerfStatistics(k,
					(double) (endtime - starttime) / 1000.0));
		}

		for (Tuple t : skyband) {

			String delimit = "";
			for (int i = 0; i < t.size(); i++) {
				out.print(delimit + t.getValue(i));
				delimit = ",";
			}
			out.println();
		}

		in.close();
		out.close();
		perfout.close();

	}

	public ArrayList<Tuple> synthesize(MultiDimGrid g) {
		ArrayList<Tuple> r = new ArrayList<Tuple>();
		double delta = 1;
		if (g.count > 0) {
			for (int i = 0; i < g.count; i++) {
				Tuple t = UniformRandom.getRandomTuple(g.ranges, delta);
				r.add(t);
			}
		}

		return r;
	}

}

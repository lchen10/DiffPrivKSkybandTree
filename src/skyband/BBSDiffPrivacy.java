package skyband;

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

public class BBSDiffPrivacy {

	public static void computeBBS(String datafile, String inputfile,
			String outputfile, int k, Comparison[] comp) throws Exception {

		ArrayList<Tuple> skyband = new ArrayList<>();
		double delta = 1;
//		Comparison[] comp = new Comparison[] { Comparison.MIN, Comparison.MIN };
		HashMap<String, Grid> map = new HashMap<>();
		FileInputStream in = new FileInputStream("grids/" + inputfile);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		PrintWriter out = new PrintWriter("bbs_output/" + outputfile + "-k-"
				+ k + ".csv");
		PrintWriter debug = new PrintWriter("debug.txt");

		String strLine = null;
		String[] mystring = new String[8];
		PriorityQueue<Grid> queue = new PriorityQueue<>();
		ArrayList<Grid> leaves = new ArrayList<>();

		while ((strLine = br.readLine()) != null) {

			strLine = strLine.trim();
			mystring = strLine.split(",");
			// System.out.println("mystring length is: " + mystring.length);

			if (mystring.length != 8) {
				// System.out.println("test");
				continue;
			}

			double count_double = Double.parseDouble(mystring[0].trim());
			int count = (int) Math.round(count_double);
			double xmin = Double.parseDouble(mystring[1].trim());
			double ymin = Double.parseDouble(mystring[2].trim());
			double xmax = Double.parseDouble(mystring[3].trim());
			double ymax = Double.parseDouble(mystring[4].trim());
			int level = Integer.parseInt(mystring[5].trim());
			String id = mystring[6].trim();
			String parentid = mystring[7].trim();
			Grid grid = new Grid();
			grid.xmin = xmin;
			grid.xmax = xmax;
			grid.ymin = ymin;
			grid.ymax = ymax;
			grid.count = count;
			grid.id = id;
			grid.level = level;
			if (grid.level == 1) {
				leaves.add(grid);
			}

			if (comp[0] == Comparison.MIN) {
				grid.tuple = new Tuple(new double[] { xmin, ymin });
			}
			else if(comp[0] == Comparison.MAX){
				grid.tuple = new Tuple(new double[] { xmax, ymax });
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

		List<String> lines = Files.readAllLines(Paths.get("data/" + datafile),
				Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int dataid = 1;
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split("\\t");
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			IDTuple t = new IDTuple(new Double[] { x, y }, dataid);
			tuples.add(t);

			for (Grid leaf : leaves) {
				if (leaf.inBound(t)) {
					leaf.data.add(t);
				}
			}

			dataid++;
		}

		long startTime = System.currentTimeMillis();

		while (!queue.isEmpty()) {
			Grid head = queue.poll();
			// System.out.println("grid: " + head.level);
			debug.println("grid: " + head.level);

			// to check whether the node is dominated by more than k pts at
			// skyband. If so, prune it.
			boolean needtoadd = true;
			for (Tuple t : skyband) {
				if (t.dominate(head.tuple, comp) == 1) {
					head.tuple.dominatedCount++;
					if (head.tuple.dominatedCount > k) {
						needtoadd = false; // discard grid
						// System.out.println("discard grid " + head);
						debug.println("discard grid " + head);
					}
				}
			}

			if (needtoadd) {
				if (head.children.size() > 0) {
					for (Grid child : head.children) {
						for (Tuple t : skyband) {
							if (t.dominate(child.tuple, comp) == 1) {
								head.tuple.dominatedCount++;
							}
						}
						if (child.tuple.dominatedCount <= k) {
							queue.offer(child);
							// System.out.println("add child to queue: " +
							// child);
							debug.println("add child to queue: " + child);
						}
					}
				}

				if (head.children.size() == 0 && head.level != -1) {
					for (int i = 0; i < head.data.size(); i++) {
						Tuple s = head.data.get(i);
						for (Tuple t : skyband) {
							if (t.dominate(s, comp) == 1) {
								s.dominatedCount++;
							}
						}
						if (s.dominatedCount <= k) {
							Grid newg = new Grid();
							newg.xmin = s.getValue(0);
							newg.ymin = s.getValue(1);
							newg.xmax = s.getValue(0);
							newg.ymax = s.getValue(1);
							newg.level = -1;
							newg.tuple = new Tuple(new double[] { newg.xmin,
									newg.ymin });
							queue.offer(newg);
							// System.out.println("add synthesized child to
							// queue: " + newg);
							debug.println("add synthesized child to queue: "
									+ newg);
						}
					}
				}

				if (head.level == -1) {
					skyband.add(head.tuple);
					// System.out.println("found skyband node: " + head.tuple);
					debug.println("found skyband node: " + head.tuple);
				}
			}

		}

		long endTime = System.currentTimeMillis();
		System.out.println("finished computing for k " + k);
		System.out.println("Duration " + (endTime - startTime) / 1000.0 + "s");

		for (Tuple t : skyband) {
			out.println(t.getValue(0) + "," + t.getValue(1));
		}

		in.close();
		out.close();
		debug.close();

	}

	public static void computeBBSWithSynthesis(String inputfile,
			String outputfile, int k, String outputfolder, String inputfolder, Comparison[] comp, double delta, ArrayList<PerfStatistics> perfs)
			throws Exception {
		ArrayList<Tuple> skyband = new ArrayList<>();
//		double delta = 1;
//		Comparison[] comp = new Comparison[] { Comparison.MIN, Comparison.MIN };
		HashMap<String, Grid> map = new HashMap<>();
		FileInputStream in = new FileInputStream(inputfolder + "/" + inputfile);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		PrintWriter out = new PrintWriter(outputfolder + "/" + outputfile);
		PrintWriter perfout = new PrintWriter("debug.txt");

		String strLine = null;
		String[] mystring = new String[9];
		PriorityQueue<Grid> queue = new PriorityQueue<>();

		while ((strLine = br.readLine()) != null) {

			strLine = strLine.trim();
			mystring = strLine.split(",");
			// System.out.println("mystring length is: " + mystring.length);

			if (mystring.length < 8) {
				// System.out.println("test");
				continue;
			}

			double count_double = Double.parseDouble(mystring[0].trim());
			double xmin = Double.parseDouble(mystring[1].trim());
			double ymin = Double.parseDouble(mystring[2].trim());
			double xmax = Double.parseDouble(mystring[3].trim());
			double ymax = Double.parseDouble(mystring[4].trim());
			int level = (int) Double.parseDouble(mystring[5].trim());
			String id = mystring[6].trim();
			String parentid = mystring[7].trim();
			level = (int) Double.parseDouble(mystring[8].trim());
			Grid grid = new Grid();
			grid.xmin = xmin;
			grid.xmax = xmax;
			grid.ymin = ymin;
			grid.ymax = ymax;
			grid.count = count_double;
			grid.id = id;
			grid.level = level;
			grid.comp = comp[0];
			if (comp[0] == Comparison.MIN) {
				grid.tuple = new Tuple(new double[] { xmin, ymin });
			}
			else if(comp[0] == Comparison.MAX){
				grid.tuple = new Tuple(new double[] { xmax, ymax });
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
			Grid head = queue.poll();
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
					for (Grid child : head.children) {
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
							Tuple s = UniformRandom.getRandomTuple(head.xmin,
									head.xmax, head.ymin, head.ymax, delta);
							for (Tuple t : skyband) {
								if (t.dominate(s, comp) == 1) {
									s.dominatedCount++;
								}
							}

							if (s.dominatedCount <= k) {
								Grid newg = new Grid();
								newg.xmin = s.getValue(0);
								newg.ymin = s.getValue(1);
								newg.xmax = s.getValue(0);
								newg.ymax = s.getValue(1);
								newg.level = -1;
								newg.comp = comp[0];
								newg.tuple = new Tuple(new double[] {
										newg.xmin, newg.ymin });
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
		if (perfs!= null) {
			perfs.add(new PerfStatistics(k, (double)(endtime-starttime)/1000.0));
		}
		
		// Random r = new Random();
		// for (int i = 0; i < skyband.size()*2.0/3.0; i++) {
		// int idx = r.nextInt(skyband.size());
		// Tuple t = skyband.get(idx);
		// out.println(t.getValue(0) + "," + t.getValue(1));
		// skyband.remove(idx);
		// }

		for (Tuple t : skyband) {
			out.println(t.getValue(0) + "," + t.getValue(1));
		}

		in.close();
		out.close();
		perfout.close();

	}

	public ArrayList<Tuple> synthesize(Grid g) {
		ArrayList<Tuple> r = new ArrayList<Tuple>();
		double delta = 1;
		if (g.count > 0) {
			for (int i = 0; i < g.count; i++) {
				Tuple t = UniformRandom.getRandomTuple(g.xmin, g.xmax, g.ymin,
						g.ymax, delta);
				r.add(t);
			}
		}

		return r;
	}

}

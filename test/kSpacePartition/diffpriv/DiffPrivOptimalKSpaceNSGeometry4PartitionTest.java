package kSpacePartition.diffpriv;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

import kSpacePartition.KSpacePartition;
import kSpacePartition.Node;
import kSpacePartition.Param;
import skyband.Tuple;

public class DiffPrivOptimalKSpaceNSGeometry4PartitionTest {
	@Test
	public void testLoadPoints() throws Exception {
		KSpacePartition p = new KSpacePartition();
		int k = 100;
		int[] levels = new int[] { 8 };
		double[] epslist = new double[] { 1,2,4 };
		double[] splitrates = new double[] { 0.03, 0.05, 0.1  };
		double rate = 0.9;
		int ite = 1;
		for (double srate : splitrates) {
			Param.SPLIT_BUDGET_RATE = srate;
			for (int level : levels) {
				for (int x = 0; x < ite; x++) {
					for (double eps : epslist) {
						System.out.println(x
								+ "th partitioning space diff priv for k " + k
								+ " level " + level + " eps " + eps);
						// double[] epslevel = new double[level];
						// for (int i = 0; i < epslevel.length; i++) {
						// epslevel[i] = (eps * rate) / level;
						// }
						double[] epslevel = new double[level];
						for (int i = 0; i < level;i++) {
							epslevel[i] = Math.pow(2, (1.0/4)*i);
						}

						double sum = 0;
						for (double v : epslevel) {
							sum += v;
						}

						for (int i = 0; i < epslevel.length; i++) {
							epslevel[i] = (eps * rate) * (epslevel[i] / sum);
							System.out.println("eps level-" + i + ": "
									+ epslevel[i]);
						}

						Node root = p.partitionByOptimalKNS(k,
								"15-clusters.dat", level, true, epslevel, eps
										* (1 - rate));
						PrintWriter out = new PrintWriter(
								"kspaceoutput/dfoptimalKout.txt");
						PrintWriter grid = new PrintWriter(
								"kspaceoutput/dfoptimalKgrid-ns-exp-k-" + k
										+ "-level-" + level + "-r-" + srate
										+ "-eps-" + eps + "." + x + ".csv");
						Queue<Node> q = new LinkedList<>();
						q.add(root);
						while (!q.isEmpty()) {
							Node n = q.remove();
							out.println(n);

							String parentstring = "null";
							if (n.parent != null) {
								parentstring = n.parent.id + "";
							}

							int childrencount = 4;
							if (n.isLeave) {
								childrencount = 1;
							}

							grid.println(n.count + "," + n.xmin + "," + n.ymin
									+ "," + n.xmax + "," + n.ymax + ","
									+ childrencount + "," + n.id + ","
									+ parentstring + "," + n.level);
							for (Node child : n.children) {
								q.add(child);
							}
						}
						out.close();
						grid.close();
					}
				}
			}
		}

	}
}

package kSpacePartition.diffpriv;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

import kSpacePartition.KSpacePartition;
import kSpacePartition.Node;
import skyband.Tuple;

public class DiffPrivOptimalKSpacePartitionExponentialNSTest {
	@Test
	public void testLoadPoints() throws Exception {
		KSpacePartition p = new KSpacePartition();
		int k = 100;
		int[] levels = new int[] { 2,3,4, 5, 6, 7, 8 };
		double[] epslist = new double[] { 0.1, 0.5, 1.0, 2.0 };
		int ite = 1;
		for (int x = 0; x < ite; x++) {
			for (int level : levels) {
				for (double eps : epslist) {
					System.out.println(x + "th partitioning space diff priv for k " + k + " eps " + eps);
					double[] epslevel = new double[level];
					epslevel[level - 1] = 1;
					for (int i = level - 2; i >= 0; i--) {
						epslevel[i] = epslevel[i + 1] * 0.5;
					}

					double sum = 0;
					for (double v : epslevel) {
						sum += v;
					}

					for (int i = 0; i < epslevel.length; i++) {
						epslevel[i] = eps * (epslevel[i] / sum);
						System.out.println("eps level-" + i + ": " + epslevel[i]);
					}

					Node root = p.partitionByOptimalKNS(k, "15-clusters.dat", level, true, epslevel, 0.5);
					PrintWriter out = new PrintWriter("kspaceoutput/dfoptimalKout.txt");
					PrintWriter grid = new PrintWriter("kspaceoutput/dfoptimalKgrid-ns-exp-k-" + k + "-level-" + level
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

						grid.println(n.count + "," + n.xmin + "," + n.ymin + "," + n.xmax + "," + n.ymax + ","
								+ childrencount + "," + n.id + "," + parentstring + "," + n.level);
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

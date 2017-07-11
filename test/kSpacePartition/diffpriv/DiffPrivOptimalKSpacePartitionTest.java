package kSpacePartition.diffpriv;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

import kSpacePartition.KSpacePartition;
import kSpacePartition.Node;
import skyband.Tuple;

public class DiffPrivOptimalKSpacePartitionTest {
	@Test
	public void testLoadPoints() throws Exception {
		KSpacePartition p = new KSpacePartition();
		int k = 100;
		int level = 8;
		double[] epslist = new double[] { 100 };
		int ite = 1;
		for (int x = 0; x < ite; x++) {

			for (double eps : epslist) {
				System.out.println(x + "th partitioning space diff priv for k " + k + " eps " + eps);
				double[] epslevel = new double[level];
				for (int i = 0; i < epslevel.length; i++) {
					epslevel[i] = eps / level;
				}
				Node root = p.partitionByOptimalK(k, "15-clusters.dat", level, true, epslevel);
				PrintWriter out = new PrintWriter("kspaceoutput/dfoptimalKout.txt");
				PrintWriter grid = new PrintWriter("kspaceoutput/dfoptimalKgrid-k-" + k + "-eps-" + eps + "." + x+".csv");
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

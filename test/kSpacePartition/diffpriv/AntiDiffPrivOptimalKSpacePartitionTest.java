package kSpacePartition.diffpriv;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

import kSpacePartition.KSpacePartition;
import kSpacePartition.Node;
import skyband.Tuple;

public class AntiDiffPrivOptimalKSpacePartitionTest {
	@Test
	public void testLoadPoints() throws Exception {
		KSpacePartition p = new KSpacePartition();
		int k = 100;
		int level = 8;
		double[] epslist = new double[] { 0.1, 0.5, 1.0, 2.0 };
		for (double eps : epslist) {
			System.out.println("partitioning space diff priv for k " + k + " eps " + eps);
			double[] epslevel = new double[level];
			for (int i = 0; i < epslevel.length; i++) {
				epslevel[i] = eps / level;
			}

			Node root = p.partitionByOptimalK(k, "anti5k-normal.txt", level, true, epslevel);
			PrintWriter out = new PrintWriter("kspaceoutput/dfoptimalKout-anti.txt");
			PrintWriter grid = new PrintWriter("kspaceoutput/dfoptimalKgrid-anti-k-" + k + "-eps-" + eps + ".csv");
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

				grid.println(n.oridata.size() + "," + n.xmin + "," + n.ymin + "," + n.xmax + "," + n.ymax + ","
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

package kSpacePartition;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

import skyband.Tuple;

public class OptimalKSpaceNSPartitionTest {
	@Test
	public void testLoadPoints() throws Exception {
		KSpacePartition p = new KSpacePartition();
		for (int k = 100; k <= 100; k += 50) {
			int level = 4;
			Node root = p.partitionByOptimalKNS(k, "15-clusters.dat", level, false, new double[level], 0.5);
			PrintWriter out = new PrintWriter("kspaceoutput/optimalKout.txt");
			PrintWriter grid = new PrintWriter("kspaceoutput/optimalKgrid-ns-k-" + k + "-level-" + level + ".csv");
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

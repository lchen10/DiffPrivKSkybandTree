package kSpacePartition;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

import skyband.Tuple;

public class KSpacePartitionTest {
	@Test
	public void testLoadPoints() throws Exception {
		KSpacePartition p = new KSpacePartition();
		Node root = p.partitionByMinDist(100, "15-clusters.dat", 8);
		PrintWriter out = new PrintWriter("kspaceoutput/out.txt");
		Queue<Node> q = new LinkedList<>();
		q.add(root);
		while (!q.isEmpty()) {
			Node n = q.remove();
			out.println(n);
			for (Node child : n.children) {
				q.add(child);
			}
		}
		out.close();
	}
}

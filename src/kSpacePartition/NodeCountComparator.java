package kSpacePartition;

import java.util.Comparator;

public class NodeCountComparator implements Comparator<Node> {

	@Override
	public int compare(Node o1, Node o2) {
		// TODO Auto-generated method stub
		return Double.compare(o1.count, o2.count);
	}

}

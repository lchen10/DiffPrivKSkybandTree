package kSpacePartition.MultiDim;

import java.util.Comparator;

public class MultiDimNodeCountComparator implements Comparator<MultiDimNode> {

	@Override
	public int compare(MultiDimNode o1, MultiDimNode o2) {
		// TODO Auto-generated method stub
		return Double.compare(o1.count, o2.count);
	}

}

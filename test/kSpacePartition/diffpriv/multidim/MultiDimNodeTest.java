package kSpacePartition.diffpriv.multidim;

import static org.junit.Assert.*;

import java.util.Random;

import kSpacePartition.MultiDim.MultiDimNode;

import org.junit.Test;

import skyband.Comparison;
import skyband.Tuple;
import skyband.UniformRandom;

public class MultiDimNodeTest {
	@Test
	public void testName() throws Exception {
		double[][] ranges = new double[3][2];
		ranges[0][0] = 0;
		ranges[0][1] = 8;
		ranges[1][0] = 0;
		ranges[1][1] = 8;
		ranges[2][0] = 0;
		ranges[2][1] = 8;
		MultiDimNode node = new MultiDimNode(ranges,Comparison.MAX);
		
		node.split(new double[]{6,6,6});
		
		for (MultiDimNode child : node.children) {
			System.out.println("child: " + child);
		}
		
		Random r = new Random();
		
		for (int i = 0; i < 10; i++) {
			Tuple t = UniformRandom.getRandomTuple(ranges, 0.1);
			for (MultiDimNode child : node.children) {
				if (child.inBound(t)) {
					System.out.println(t + " in " + child);
				}
			}
		}
	}
}

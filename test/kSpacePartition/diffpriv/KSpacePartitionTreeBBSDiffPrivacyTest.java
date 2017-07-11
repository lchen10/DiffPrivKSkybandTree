package kSpacePartition.diffpriv;
import static org.junit.Assert.*;

import org.junit.Test;

import skyband.BBSDiffPrivacy;
import skyband.Comparison;

public class KSpacePartitionTreeBBSDiffPrivacyTest {
	@Test
	public void testBBSDiffPrivacy() throws Exception {
		for (int k = 0; k <= 100; k += 20) {
			System.out.println("computing for k " + k);
			long startTime = System.currentTimeMillis();
			BBSDiffPrivacy.computeBBSWithSynthesis("optimalKgrid-k-100.csv","optimalKgrid-bbs-skyband.csv", k, "bbs_output", "grids", new Comparison[] { Comparison.MIN, Comparison.MIN }, 1, null);
			long endTime = System.currentTimeMillis();
			System.out.println("finished computing for k " + k);
			System.out.println("Duration " + (endTime - startTime) / 1000.0 + "s");
		}

	}
}

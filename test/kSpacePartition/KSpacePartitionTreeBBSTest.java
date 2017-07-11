package kSpacePartition;

import static org.junit.Assert.*;

import org.junit.Test;

import skyband.BBSDiffPrivacy;
import skyband.Comparison;

public class KSpacePartitionTreeBBSTest {
	@Test
	public void testBBSNoSynthesis() throws Exception {
		int level = 8;
		for (int k = 100; k <= 100; k += 20) {
			System.out.println("computing for k " + k);
			BBSDiffPrivacy.computeBBSWithSynthesis("optimalKgrid-k-" + k + "-level-" + level + ".csv",
					"optimalKgrid-bbs-skyband-k-" + k + "-level-" + level + ".csv", k, "bbs_output", "kspaceoutput", new Comparison[] { Comparison.MIN, Comparison.MIN }, 1, null);

		}
	}
}

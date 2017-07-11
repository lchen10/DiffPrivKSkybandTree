package kSpacePartition;

import static org.junit.Assert.*;

import org.junit.Test;

import skyband.BBSDiffPrivacy;
import skyband.Comparison;

public class KSpacePartitionNSTreeBBSTest {
	@Test
	public void testBBSNoSynthesis() throws Exception {
		int level = 4;
		int ite = 1;
		for (int x = 0; x < ite; x++) {
			for (int k = 100; k <= 100; k += 50) {
				System.out.println(x+"th computing for k " + k);
				BBSDiffPrivacy.computeBBSWithSynthesis("optimalKgrid-ns-k-" + k + "-level-" + level + ".csv",
						"optimalKgrid-ns-bbs-skyband-k-" + k + "-level-" + level + "." + x + ".csv", 100, "bbs_output",
						"kspaceoutput", new Comparison[] { Comparison.MIN, Comparison.MIN }, 1, null);
			}
		}

	}

}

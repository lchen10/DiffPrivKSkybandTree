package kSpacePartition.diffpriv;

import static org.junit.Assert.*;

import org.junit.Test;

import skyband.BBSDiffPrivacy;
import skyband.Comparison;

public class AntiDiffPrivKSpacePartitionTreeBBSDiffPrivacyTest {
	@Test
	public void testBBSDiffPrivacy() throws Exception {
		double[] epslist = new double[] { 0.1, 0.5, 1.0, 2.0 };

		for (int k = 100; k <= 100; k += 20) {
			for (double eps : epslist) {
				System.out.println("computing for k " + k + " eps " + eps);
				BBSDiffPrivacy.computeBBSWithSynthesis("dfoptimalKgrid-anti-k-" + k + "-eps-" + eps + ".csv",
						"dfoptimalKgrid-anti-bbs-skyband-k-" + k + "-eps-" + eps + ".csv", k, "privkspace_bbs_output", "kspaceoutput", new Comparison[] { Comparison.MIN, Comparison.MIN }, 1, null);
				System.out.println("finished computing for k " + k);
			}

		}

	}
}

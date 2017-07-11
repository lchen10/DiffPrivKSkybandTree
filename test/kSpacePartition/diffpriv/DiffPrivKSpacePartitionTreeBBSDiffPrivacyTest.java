package kSpacePartition.diffpriv;

import static org.junit.Assert.*;

import org.junit.Test;

import skyband.BBSDiffPrivacy;
import skyband.Comparison;

public class DiffPrivKSpacePartitionTreeBBSDiffPrivacyTest {
	@Test
	public void testBBSDiffPrivacy() throws Exception {
		double[] epslist = new double[] { 0.1, 0.5, 1.0, 2.0 };
		int ite = 1;
		for (int x = 0; x < ite; x++) {

			for (int k = 100; k <= 100; k += 20) {
				for (double eps : epslist) {
					System.out.println(x+"th computing for k " + k + " eps " + eps);
					BBSDiffPrivacy.computeBBSWithSynthesis("dfoptimalKgrid-k-" + k + "-eps-" + eps + "."+x+".csv",
							"dfoptimalKgrid-bbs-skyband-k-" + k + "-eps-" + eps + "."+x+".csv", k, "privkspace_bbs_output",
							"kspaceoutput", new Comparison[] { Comparison.MIN, Comparison.MIN }, 1, null);
					System.out.println("finished computing for k " + k);
				}

			}
		}
	}
}

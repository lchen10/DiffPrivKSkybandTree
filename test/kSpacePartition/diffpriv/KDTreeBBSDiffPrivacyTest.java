package kSpacePartition.diffpriv;

import static org.junit.Assert.*;

import org.junit.Test;

import skyband.BBSDiffPrivacy;
import skyband.Comparison;

public class KDTreeBBSDiffPrivacyTest {
	@Test
	public void testBBSDiffPrivacy() throws Exception {
		double[] epslist = new double[] { 0.1, 0.5, 1.0, 2.0 };

		for (int k = 100; k <= 100; k += 20) {
			for (double eps : epslist) {
				System.out.println("computing for k " + k + " eps " + eps);
				BBSDiffPrivacy.computeBBSWithSynthesis("kdtree-15-clusters-eps-"+eps+".txt",
						"kdtree-15-clusters-bbs-skyband-eps-"+eps+".csv", k, "privkd_bbs_output", "grids", new Comparison[] { Comparison.MIN, Comparison.MIN }, 1, null);
				System.out.println("finished computing for k " + k);

			}
		}

	}
}

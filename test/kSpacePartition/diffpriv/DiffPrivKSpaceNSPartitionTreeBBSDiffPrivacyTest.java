package kSpacePartition.diffpriv;

import static org.junit.Assert.*;

import org.junit.Test;

import kSpacePartition.Param;
import skyband.BBSDiffPrivacy;
import skyband.Comparison;

public class DiffPrivKSpaceNSPartitionTreeBBSDiffPrivacyTest {
	@Test
	public void testBBSDiffPrivacy() throws Exception {
		int[] levels = new int[] { 8 };
		double[] epslist = new double[] { 2, 4, 10, 100 };
		double[] srates = new double[] { 0.2, 0.3, 0.4, 0.5 };
		double[] treerates = new double[] { 0.9 };
		int ite = 1;
		for (int x = 0; x < ite; x++) {
			for (double trate : treerates) {
				for (double srate : srates) {
					for (int level : levels) {
						for (int k = 100; k <= 100; k += 20) {
							for (double eps : epslist) {
								System.out.println(x + "th computing for k " + k + " level " + level + " split rate "
										+ srate + " tree rate " + trate + " eps " + eps);
								BBSDiffPrivacy.computeBBSWithSynthesis(
										"dfoptimalKgrid-ns-k-" + k + "-level-" + level + "-r-" + srate + "-t-" + trate
												+ "-eps-" + eps + "." + x + ".csv",
										"dfoptimalKgrid-ns-bbs-skyband-k-" + k + "-level-" + level + "-r-" + srate
												+ "-t-" + trate + "-eps-" + eps + "." + x + ".csv",
										k, "privkspace_bbs_output", "kspaceoutput", new Comparison[] { Comparison.MIN, Comparison.MIN }, 1, null);
								System.out.println("finished computing for k " + k);
							}

						}
					}
				}

			}
		}
	}
}

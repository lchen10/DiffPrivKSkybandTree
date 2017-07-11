package kSpacePartition.diffpriv;

import static org.junit.Assert.*;

import org.junit.Test;

import skyband.BBSDiffPrivacy;
import skyband.Comparison;

public class DiffPrivKSpacePartitionExponentialNSTreeBBSDiffPrivacyTest {
	@Test
	public void testBBSDiffPrivacy() throws Exception {
		double[] epslist = new double[] { 2, 4,100 };
		double[] srates = new double[] { 0.05,0.1,0.2  };
		int ite = 1;
		int[] levels = new int[] { 8 };
		for (int x = 0; x < ite; x++) {
			for (double srate : srates) {
				for (int k = 100; k <= 100; k += 20) {
					for (int level : levels) {
						for (double eps : epslist) {
							System.out.println(x + "th computing for k " + k
									+ " level " + level + " eps " + eps);
							BBSDiffPrivacy.computeBBSWithSynthesis(
									"dfoptimalKgrid-ns-exp-k-" + k + "-level-"
											+ level + "-r-" + srate + "-eps-"
											+ eps + "." + x + ".csv",
									"dfoptimalKgrid-ns-exp-bbs-skyband-k-" + k
											+ "-level-" + level + "-r-" + srate
											+ "-eps-" + eps + "." + x + ".csv",
									k, "privkspace_bbs_output", "kspaceoutput", new Comparison[] { Comparison.MIN, Comparison.MIN }, 1, null);
							System.out.println("finished computing for k " + k);
						}
					}

				}
			}
		}
	}
}

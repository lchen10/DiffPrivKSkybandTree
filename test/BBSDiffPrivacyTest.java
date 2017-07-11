import static org.junit.Assert.*;

import org.junit.Test;

import skyband.BBSDiffPrivacy;
import skyband.Comparison;

public class BBSDiffPrivacyTest {
	@Test
	public void testBBSDiffPrivacy() throws Exception {
		for (int k = 0; k <= 100; k += 20) {
			System.out.println("computing for k " + k);
			BBSDiffPrivacy.computeBBSWithSynthesis("15-clusters-budget-2.txt","15-clusters-bbs-skyband.csv", k, "bbs_output", "grids", new Comparison[] { Comparison.MIN, Comparison.MIN }, 1, null);
			System.out.println("finished computing for k " + k);
		}

	}
}

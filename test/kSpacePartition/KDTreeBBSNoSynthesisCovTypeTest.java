package kSpacePartition;

import static org.junit.Assert.*;

import org.junit.Test;

import skyband.BBSDiffPrivacy;
import skyband.Comparison;

public class KDTreeBBSNoSynthesisCovTypeTest {
	@Test
	public void testBBSDiffPrivacy() throws Exception {
		for (int k = 100; k <= 100; k += 20) {
			System.out.println("computing for k " + k);
			BBSDiffPrivacy.computeBBS("15-clusters.dat",
					"kdpure-15-clusters.txt",
					"kdpure-15-clusters-bbs-skyband-no-synthesis.csv", k,
					new Comparison[] { Comparison.MIN, Comparison.MIN });
		}

	}
}

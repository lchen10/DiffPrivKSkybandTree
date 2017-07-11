import static org.junit.Assert.*;

import org.apache.commons.math3.distribution.LaplaceDistribution;
import org.junit.Test;

public class LaplacianTest {
	@Test
	public void testLaplacianRandomNoise() throws Exception {
		LaplaceDistribution lap = new LaplaceDistribution(0,1);
		
		for (int i = 0; i < 10000; i++) {
			double r = lap.sample();
			if (r > 1) {
				
//				System.out.println("Noise " + lap.sample());
			}
		}

		
	}
	
	@Test
	public void testCeiling() throws Exception {
		System.out.println(Math.ceil(2.001));
	}
}

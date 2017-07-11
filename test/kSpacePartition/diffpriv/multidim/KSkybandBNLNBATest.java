package kSpacePartition.diffpriv.multidim;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import skyband.BNL;
import skyband.Comparison;
import skyband.Tuple;

public class KSkybandBNLNBATest {
	@Test
	public void testKSkylineBandOn15Clusters() throws Exception {

		// String[] files = new String[] { "anti5k-normal.txt" };
		String[] files = new String[] { "normal3d.csv","corr3d.csv","anti3d.csv" };
		int[] ks = new int[] { 0, 20, 40, 60, 80, 100, 150, 200 };

		for (String filename : files) {
			List<Tuple> tuples = new ArrayList<>();
			List<String> lines = Files.readAllLines(Paths.get("dataset/" + filename), Charset.defaultCharset());
			for (int i = 1; i < lines.size(); i++) {
				String[] line = lines.get(i).split(",");
				double x = Double.parseDouble(line[0]);
				double y = Double.parseDouble(line[1]);
				double z = Double.parseDouble(line[2]);
				tuples.add(new Tuple(new Double[] { x, y, z }));
			}
			ArrayList<Comparison> comparisons = new ArrayList<Comparison>();
			comparisons.add(Comparison.MAX);
			comparisons.add(Comparison.MAX);
			comparisons.add(Comparison.MAX);

			for (int k : ks) {
				System.out.println("compute " + k + "-skylineband for " + filename);
				Tuple[] band = BNL.computeKSkylineBand(tuples, null, comparisons, k);
				PrintWriter out = new PrintWriter("dataset/" + filename + "-BNL-k-" + k + ".csv");
				for (Tuple tuple : band) {
					double x = tuple.getValue(0);
					out.print(x + ",");
					double y = tuple.getValue(1);
					out.print(y + ",");
					double z = tuple.getValue(2);
					out.print(z);
					out.println();
				}
				out.close();
				System.out.println("k= " + k);
				for (Tuple tuple : tuples) {
					tuple.dominatedCount = 0;
				}
			}
		}

	}
}

package bnl;

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
		String[] files = new String[] {  "anti10k.csv", "correlated10k.csv", "normal10k.csv"};
		int[] ks = new int[] { 0,1,3,5,10,13,15,18 };

		for (String filename : files) {
			List<Tuple> tuples = new ArrayList<>();
			List<String> lines = Files.readAllLines(Paths.get("dataset/" + filename), Charset.defaultCharset());
			for (int i = 1; i < lines.size(); i++) {
				String[] line = lines.get(i).split(",");
				double x = Double.parseDouble(line[0]);
				double y = Double.parseDouble(line[1]);
				tuples.add(new Tuple(new Double[] { x, y }));
			}
			ArrayList<Comparison> comparisons = new ArrayList<Comparison>();
			comparisons.add(Comparison.MIN);
			comparisons.add(Comparison.MIN);

			for (int k : ks) {
				System.out.println("compute " + k + "-skylineband for " + filename);
				Tuple[] band = BNL.computeKSkylineBand(tuples, null, comparisons, k);
				PrintWriter out = new PrintWriter("dataset/" + filename + "-BNL-k-" + k + ".csv");
				for (Tuple tuple : band) {
					double x = tuple.getValue(0);
					out.print(x + ",");
					double y = tuple.getValue(1);
					out.print(y);
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

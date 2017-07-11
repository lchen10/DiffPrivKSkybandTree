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

public class KSkybandBNLTest {
	@Test
	public void testKSkylineBandOn15Clusters() throws Exception {

		// String[] files = new String[] { "anti5k-normal.txt" };
		String[] files = new String[] { "15-clusters.dat" };
		int[] ks = new int[] { 20, 40, 60, 80, 100, 150, 200 };
		int ite = 10;
		for (String filename : files) {
			PrintWriter perfout = new PrintWriter(filename.split("\\.")[0] + ".perf.csv");
			perfout.println("k,bnl");
			List<Tuple> tuples = new ArrayList<>();
			List<String> lines = Files.readAllLines(Paths.get("data/" + filename), Charset.defaultCharset());
			for (int i = 1; i < lines.size(); i++) {
				String[] line = lines.get(i).split("\\s+");
				double x = Double.parseDouble(line[0]);
				double y = Double.parseDouble(line[1]);
				tuples.add(new Tuple(new Double[] { x, y }));
			}
			ArrayList<Comparison> comparisons = new ArrayList<Comparison>();
			comparisons.add(Comparison.MIN);
			comparisons.add(Comparison.MIN);
			
			for (int i = 0; i < ite; i++) {
				for (int k : ks) {
					System.out.println("compute " + k + "-skylineband for " + filename);
					long starttime = System.currentTimeMillis();
					Tuple[] band = BNL.computeKSkylineBand(tuples, null, comparisons, k);
					long endtime = System.currentTimeMillis();
					perfout.println(k + "," + ((double) (endtime - starttime)) / 1000.0);
					PrintWriter out = new PrintWriter("data/" + filename + "-BNL-k-" + k + ".csv");
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

			perfout.close();
		}

	}
}

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

public class KSkylineBandDiffPrivacyTest {
	@Test
	public void testKSkylineBandOnOriginalData() throws Exception {

		 String[] files = new String[] { "15-clusters.csv" };
//		String[] files = new String[] { "15-clusters-AdaptiveGrids-2.csv" };
		// String[] files = new String[] { "anti5k.txt"};

		for (String filename : files) {
			List<Tuple> tuples = new ArrayList<>();
			List<String> lines = Files.readAllLines(Paths.get("adaptiveGrid/" + filename), Charset.defaultCharset());
			PrintWriter origout = new PrintWriter("dpoutput/" + filename + "-original.csv");
			for (int i = 0; i < lines.size(); i++) {
				String[] line = lines.get(i).split(",");
				double x = Double.parseDouble(line[0]);
				double y = Double.parseDouble(line[1]);
				tuples.add(new Tuple(new Double[] { x, y }));
				origout.println(x + "," + y);
			}
			origout.close();
			ArrayList<Comparison> comparisons = new ArrayList<Comparison>();
			comparisons.add(Comparison.MIN);
			comparisons.add(Comparison.MIN);
			Domain d = new Domain(2);
			Tuple first = tuples.get(0);
			d.dimMIN.set(0, first.getValue(0));
			d.dimMAX.set(0, first.getValue(0));
			d.dimMIN.set(1, first.getValue(1));
			d.dimMAX.set(1, first.getValue(1));

			for (int i = 1; i < tuples.size(); i++) {
				Tuple t = tuples.get(i);
				double x = t.getValue(0);
				double y = t.getValue(1);
				if (d.dimMAX.get(0) < x) {
					d.dimMAX.set(0, x);
				}
				if (d.dimMIN.get(0) > x) {
					d.dimMIN.set(0, x);
				}
				if (d.dimMAX.get(1) < y) {
					d.dimMAX.set(1, y);
				}
				if (d.dimMIN.get(1) > y) {
					d.dimMIN.set(1, y);
				}
			}

			System.out.println(d.toString());

			for (int k = 0; k <= 100; k += 20) {
				System.out.println("compute " + k + "-skylineband for " + filename);
				Tuple[] band = BNL.computeKSkylineBand(tuples, null, comparisons, k);
				PrintWriter out = new PrintWriter("dpoutput/" + filename + "-k-" + k + ".csv");

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

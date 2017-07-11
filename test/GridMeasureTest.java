import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import skyband.Tuple;

public class GridMeasureTest {
	@Test
	public void testPartitionGrid() throws Exception {
		String truefilename = "15-clusters.csv";
		String privatefilename = "kdoutput-15-clusters-2-bbs-skyband.csv-sampled";
		int[] partitionxs = new int[] { 20, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000 };
		int[] partitionys = new int[] { 20, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000 };
		PrintWriter out = new PrintWriter("kdoutput/k-20-100-sampled" + ".csv");

		for (int k = 0; k <= 100; k += 20) {

			String truefile = truefilename + "-k-" + k + ".csv";
			String privatefile = privatefilename + "-k-" + k + ".csv";
			List<String> truelines = Files.readAllLines(Paths.get("dpoutput/" + truefile), Charset.defaultCharset());
			List<String> privatelines = Files.readAllLines(Paths.get("bbs_output/" + privatefile),
					Charset.defaultCharset());

			out.println("k,partitionx,partitiony,trueset,privateset,precision,recall,fmeasure");
			for (int q = 0; q < partitionxs.length; q++) {
				int partitionx = partitionxs[q];
				int partitiony = partitionys[q];

				double xlength = 1000000;
				double ylength = 1000000;
				double xmin = 0;
				double xmax = 1000000;
				double ymin = 0;
				double ymax = 1000000;
				double xinterval = xlength / partitionx;
				double yinterval = ylength / partitiony;

				System.out.println("x length: " + xlength);
				System.out.println("x interval: " + xinterval);
				System.out.println("y length: " + ylength);
				System.out.println("y interval: " + yinterval);

				HashSet<Tuple> trueset = new HashSet<>();
				for (int i = 0; i < truelines.size(); i++) {
					String[] line = truelines.get(i).split(",");
					int x = (int) (Double.parseDouble(line[0]) / xinterval);
					int y = (int) (Double.parseDouble(line[1]) / yinterval);
					trueset.add(new Tuple(new int[] { x, y }));
				}

				HashSet<Tuple> privateset = new HashSet<>();
				for (int i = 0; i < privatelines.size(); i++) {
					String[] line = privatelines.get(i).split(",");
					int x = (int) (Double.parseDouble(line[0]) / xinterval);
					int y = (int) (Double.parseDouble(line[1]) / yinterval);
					privateset.add(new Tuple(new int[] { x, y }));
				}

				double tp = 0;
				double fp = 0;
				double fn = 0;

				for (Tuple tuple : privateset) {
					if (trueset.contains(tuple)) {
						tp++;
					} else {
						fp++;
					}
				}

				for (Tuple tuple : trueset) {
					if (!privateset.contains(tuple)) {
						fn++;
					}
				}

				double precision = tp / (tp + fp);
				double recall = tp / (tp + fn);
				double fmeasure = 2 * (precision * recall) / (precision + recall);

				out.println(k + "," + partitionx + "," + partitiony + "," + trueset.size() + "," + privateset.size()
						+ "," + precision + "," + recall + "," + fmeasure);

				System.out.println("k: " + k);
				System.out.println("trueset: " + trueset.size());
				System.out.println("privateset: " + privateset.size());
				System.out.println("tp:" + tp);
				System.out.println("fp:" + fp);
				System.out.println("fn:" + fn);
				System.out.println("precision: " + precision);
				System.out.println("recall: " + recall);
				System.out.println("fmeasure: " + fmeasure);
			}

		}
		out.close();
	}
}

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

public class MergeFmeasureTest {
	@Test
	public void testName() throws Exception {
		String[] files = new String[] { "anti", "corr", "normal", "covtype", "nba" };
		int[] ks = new int[] { 0, 1, 3, 5, 10, 13, 15, 18 };
		double[] eps = new double[] { 0.1, 0.5, 1, 2 };
		String method = "kdtree";

		for (String filename : files) {
			for (double ep : eps) {
				List<Tuple> tuples = new ArrayList<>();
				String fmeasurefile = filename + "-geometric-eps-" + ep + ".csv";
				List<String> lines = Files.readAllLines(Paths.get("fmeasure/" + method + "/" + fmeasurefile),
						Charset.defaultCharset());

				List<String> oldlines = Files.readAllLines(Paths.get("fmeasure-VLDB/" + method + "/" + fmeasurefile),
						Charset.defaultCharset());

				PrintWriter out = new PrintWriter("fmeasure/" + method + "/0.03/" + fmeasurefile);

				for (String line : lines) {
					out.println(line);
				}

				for (int i = 1; i < oldlines.size(); i++) {

					String line = oldlines.get(i);
					String[] split = line.split(",");
					StringBuffer sb = new StringBuffer();
					String delimit = "";
					for (int j = 0; j < split.length; j++) {
						if (j <= 1 || j >= 8) {
							sb.append(delimit + split[j]);
							delimit = ",";
						}
					}
					out.println(sb.toString());
				}

				out.close();
			}
		}
	}
}

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import skyband.Tuple;

public class UniformSampleTest {

	@Test
	public void testUniformSample() throws Exception {
		String truefilename = "15-clusters.csv";
		String privatefilename = "kdoutput-15-clusters-2-bbs-skyband.csv";
		int[] partitionxs = new int[] { 20, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000 };
		int[] partitionys = new int[] { 20, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000 };
		double rate = 0.2;
		Random r = new Random();
		for (int k = 0; k <= 100; k += 20) {

			String privatefile = privatefilename + "-k-" + k + ".csv";
			List<String> privatelines = Files.readAllLines(Paths.get("bbs_output/" + privatefile),
					Charset.defaultCharset());
			PrintWriter out = new PrintWriter("bbs_output/" + privatefilename + "-sampled-k-" + k + ".csv");

			ArrayList<Tuple> tuples = new ArrayList<>();
			for (int i = 0; i < privatelines.size(); i++) {
				String[] line = privatelines.get(i).split(",");
				double x = Double.parseDouble(line[0]);
				double y = Double.parseDouble(line[1]);
				tuples.add(new Tuple(new double[] { x, y }));
			}

			ArrayList<Tuple> results = new ArrayList<>();
			int total = (int) (tuples.size() * rate);
			for (int i = 0; i < total; i++) {
				int rint = r.nextInt(tuples.size());
				results.add(tuples.get(rint));
				tuples.remove(rint);
			}
			
			for (Tuple tuple : results) {
				out.println(tuple.getValue(0) + "," + tuple.getValue(1));
			}
			
			out.close();
		}
	}
}

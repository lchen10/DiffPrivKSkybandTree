package result;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

public class TrueCountPrivCountMergeResultTest {
	@Test
	public void testMergeFmeasure() throws Exception {
		String[] folders = new String[] { "noisynegative", "kdtree", "quadtree" };
		double[] epslist = new double[] { 0.1,0.5,1,2 };
		HashSet<String> datasets = new HashSet<>();
		datasets.add("normal");
		datasets.add("corr");
		datasets.add("anti");
		datasets.add("nba");
		datasets.add("covtype");
		
		// for (File f : new File("fmeasure/" + folders[0]).listFiles()) {
		// datasets.add(f.getName().split("-")[0]);
		// }

		for (double eps : epslist) {
			PrintWriter out = new PrintWriter("evaluations/countdiff/results-eps-" + eps
					+ ".csv");
			for (String dataset : datasets) {
				out.println(dataset);
				out.print("k,");
				for (String folder : folders) {
					out.print(folder + ",");
				}
				out.println();
				ArrayList<List<String>> folderlines = new ArrayList<>();
				int ks = 0;
				for (String folder : folders) {
					List<String> lines = Files.readAllLines(
							Paths.get("fmeasure/" + folder + "/" + dataset
									+ "-geometric-eps-" + eps + ".csv"),
							Charset.defaultCharset());
					ks = lines.size() - 1;
					folderlines.add(lines);
				}

				try {
					for (int i = 0; i < ks; i++) {
						String k = "";
						String fs = "";
						for (List<String> lines : folderlines) {
							try {
								String[] split = lines.get(i + 1).split(",");
								k = split[0];
								if (split.length < 13) {
									double tc = Double.parseDouble(split[2]);
									double pc = Double.parseDouble(split[3]);
									fs += Math.abs(tc-pc)/(double)tc+",";

								} else {
									double tc = Double.parseDouble(split[8]);
									double pc = Double.parseDouble(split[9]);
									fs += Math.abs(tc-pc)/(double)tc+",";
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						out.println(k + "," + fs);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				out.println();
			}

			out.close();
		}
	}
}

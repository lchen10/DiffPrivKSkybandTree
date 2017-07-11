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

public class MergeResultTest {
	@Test
	public void testMergeFmeasure() throws Exception {
		// String[] folders = new String[] { "noisynegative", "kdtree",
		// "quadtree" };
		String[] folders = new String[] { "noisynegative", "kdtree", "quadtree" };
		double[] epslist = new double[] { 0.1, 0.5, 1, 2 };
		HashSet<String> datasets = new HashSet<>();
		datasets.add("corr");
		datasets.add("normal");
		datasets.add("anti");
		datasets.add("nba");
		datasets.add("covtype");
		// for (File f : new File("fmeasure/" + folders[0]).listFiles()) {
		// datasets.add(f.getName().split("-")[0]);
		// }
		double err = 0.1;
		String name = "ICDE/" + err;
		for (double eps : epslist) {
			PrintWriter out = new PrintWriter("evaluations/" + name + "/results-eps-" + eps + ".csv");
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
					List<String> lines = Files.readAllLines(Paths.get(
							"fmeasure/" + folder + "/" + err + "/" + dataset + "-geometric-eps-" + eps + ".csv"),
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
									double tp = Double.parseDouble(split[4]);
									double fp = Double.parseDouble(split[5]);
									double fn = Double.parseDouble(split[6]);
									double prec = tp / (tp + fp);
									double rec = tp / (tp + fn);
									fs += (2 * prec * rec) / (prec + rec) + ",";

								} else {
									double tp = Double.parseDouble(split[10]);
									double fp = Double.parseDouble(split[11]);
									double fn = Double.parseDouble(split[12]);
									double prec = tp / (tp + fp);
									double rec = tp / (tp + fn);
									fs += (2 * prec * rec) / (prec + rec) + ",";
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

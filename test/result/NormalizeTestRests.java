package result;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

public class NormalizeTestRests {
	@Test
	public void testRemoveUnusedKs() throws Exception {

		String[] methods = new String[] { "noisynegative", "kdtree", "quadtree" };
		double[] errs = new double[] { 0.03, 0.05 };
		int[] ks = new int[] { 0, 1, 3, 5, 10, 20, 40, 60, 80, 100, 150, 200 };
		HashSet<Integer> kset = new HashSet<Integer>();
		for (Integer k : ks) {
			kset.add(k);
		}
		for (String m : methods) {
			for (double err : errs) {
				String cleanfolder = "fmeasure/" + m + "/" + err + "-clean";
				if (!Files.exists(Paths.get(cleanfolder))) {
					Files.createDirectory(Paths.get(cleanfolder));
				}

				Stream<Path> lists = Files.list(Paths.get("fmeasure/" + m + "/" + err));
				lists.forEach(f -> {
					try {
						PrintWriter out = new PrintWriter(cleanfolder + "/" + f.getName(f.getNameCount()-1));
						List<String> lines = Files.readAllLines(f);
						out.println(lines.get(0));

						for (int i = 1; i < lines.size(); i++) {
							String line = lines.get(i);
							String[] split = line.split(",");
							int k = Integer.parseInt(split[0]);
							if (kset.contains(k)) {
								out.println(line);
							}
						}

						out.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		}

	}
}

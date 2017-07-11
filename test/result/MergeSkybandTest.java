package result;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MergeSkybandTest {
	@Test
	public void testMergeSkybandResults() throws Exception {
		int[] ks = new int[] { 20, 40, 60, 80, 100, 150, 200 };
		String[] datasets = new String[] { "15-clusters.dat", "normal10k.csv", "anti10k.csv", "correlated10k.csv",
				"covtype.csv", "nba.csv", };

		for (String ds : datasets) {
			List<String> trueLines = Files.readAllLines(Paths.get("dataset/" + ds));
			List<List<String>> bandlines = new ArrayList<>();
			PrintWriter out = new PrintWriter("evaluations/" +ds+"-skyband.csv");
			out.print("x,y,,");
			for (int k : ks) {
				List<String> kband = Files.readAllLines(Paths.get("dataset/" + ds + "-BNL-k-" + k + ".csv"));
				bandlines.add(kband);
				out.print(k+"-x,"+k+"-y,,");
			}
			out.println();
			
			for (int i = 0; i < trueLines.size(); i++) {
				out.print(trueLines.get(i)+",,");
				for (int j = 0; j < bandlines.size(); j++) {
					List<String> list = bandlines.get(j);
					if (i < list.size()) {
						out.print(list.get(i)+",,");
					}else{
						out.print(",,,");
					}
					
				}
				out.println();
			}
			out.close();
		}
	}
}

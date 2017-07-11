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

public class FMeasureTest {
	@Test
	public void testFMeasure() throws Exception {
		
		int partitionx = 50;
		int partitiony = 50;
		
		for (int k = 0; k <= 100; k+=20) {
			String truefile = "15-clusters.csv-grid-k-"+k+ "-x-" + partitionx + "-y-" + partitiony +".csv";
			String privatefile = "15-clusters-AdaptiveGrids-2.csv-grid-k-"+k+ "-x-" + partitionx + "-y-" + partitiony +".csv";
			List<Tuple> truetuples = new ArrayList<>();
			List<String> truelines = Files.readAllLines(Paths.get("dpoutput/" + truefile), Charset.defaultCharset());
			
			List<Tuple> privatetuples = new ArrayList<>();
			List<String> privatelines = Files.readAllLines(Paths.get("dpoutput/" + privatefile), Charset.defaultCharset());
			
			
			HashSet<Tuple> trueset = new HashSet<>();
			for (int i = 0; i < truelines.size(); i++) {
				String[] line = truelines.get(i).split(",");
				int x = Integer.parseInt(line[0]);
				int y = Integer.parseInt(line[1]);
				trueset.add(new Tuple(new int[] { x,y }));
			}
			
			HashSet<Tuple> privateset = new HashSet<>();
			for (int i = 0; i < privatelines.size(); i++) {
				String[] line = privatelines.get(i).split(",");
				int x = Integer.parseInt(line[0]);
				int y = Integer.parseInt(line[1]);
				privateset.add(new Tuple(new int[] { x,y }));
			}
			
			double tp = 0;
			double fp = 0;
			double fn = 0;
			
			for (Tuple tuple : privateset) {
				if (trueset.contains(tuple)) {
					tp++;
				}
				else{
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
			double fmeasure = 2*(precision*recall)/(precision+recall);
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
}

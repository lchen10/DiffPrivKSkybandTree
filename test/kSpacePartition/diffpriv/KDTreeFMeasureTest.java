package kSpacePartition.diffpriv;

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

import kSpacePartition.IDTuple;
import skyband.Tuple;

public class KDTreeFMeasureTest {
	@Test
	public void testFMeasure() throws Exception {
		double[] epslist = new double[] { 0.1, 0.5, 1.0, 2.0 };
		PrintWriter out = new PrintWriter("fmeasure/kdtree.csv");
		out.println("k,eps,true coutn, private count,tp,fp,fn,precision,recall,fmeasure,totalerror,avgerror");
		for (int k = 100; k <= 100; k += 20) {
			for (double eps : epslist) {
				String truefile = "data/" + "15-clusters.dat" + "-BNL-k-" + k + ".csv";
				String privatefile = "privkd_bbs_output/" + "kdtree-15-clusters-bbs-skyband-eps-" + eps + ".csv-k-" + k
						+ ".csv";
				List<String> truelines = Files.readAllLines(Paths.get(truefile), Charset.defaultCharset());

				List<String> privatelines = Files.readAllLines(Paths.get(privatefile), Charset.defaultCharset());

				HashSet<Tuple> trueset = new HashSet<>();
				for (int i = 0; i < truelines.size(); i++) {
					String[] line = truelines.get(i).split(",");
					double x = Double.parseDouble(line[0]);
					double y = Double.parseDouble(line[1]);
					trueset.add(new Tuple(new double[] { x, y }));
				}

				List<IDTuple> allprivateset = new ArrayList<>();
				for (int i = 0; i < privatelines.size(); i++) {
					String[] line = privatelines.get(i).split(",");
					double x = Double.parseDouble(line[0]);
					double y = Double.parseDouble(line[1]);
					allprivateset.add(new IDTuple(new Double[] { x, y }, i));
				}

				HashSet<IDTuple> privateset = new HashSet<>();
				int samplecount = (int) (allprivateset.size() * 2.0 / 3.0);
				Random r = new Random();
				for (int i = 0; i < samplecount; i++) {
					int rindex = r.nextInt(allprivateset.size());
					privateset.add(allprivateset.get(rindex));
					allprivateset.remove(rindex);
				}

				double tp = 0;
				double fp = 0;
				double fn = 0;

				for (Tuple tuple : privateset) {
					double x = tuple.getValue(0);
					double y = tuple.getValue(1);
					boolean isTP = false;
					for (Tuple truetuple : trueset) {
						if (Math.abs(truetuple.getValue(0) - x) < 0.01 * 1000000
								&& Math.abs(truetuple.getValue(1) - y) < 0.01 * 1000000) {
							isTP = true;
							break;
						}
					}

					if (isTP) {
						tp++;
					} else {
						fp++;
					}
				}

				for (Tuple tuple : trueset) {
					double x = tuple.getValue(0);
					double y = tuple.getValue(1);
					boolean isFN = true;
					for (Tuple privtuple : privateset) {
						if (Math.abs(privtuple.getValue(0) - x) < 0.01 * 1000000
								&& Math.abs(privtuple.getValue(1) - y) < 0.01 * 1000000) {
							isFN = false;
							break;
						}
					}
					if (isFN) {
						fn++;
					}
				}

				double totalerror = 0.0;
				for (Tuple tuple : privateset) {
					double x = tuple.getValue(0);
					double y = tuple.getValue(1);
					double mind = Double.MAX_VALUE;
					for (Tuple truetuple : trueset) {
						double d = Math.abs(truetuple.getValue(0) - x) + Math.abs(truetuple.getValue(1) - y);
						if (mind > d) {
							mind = d;
						}
					}
					totalerror += mind;
				}
				double avgerror = (totalerror / privateset.size()) / 1000000.0;

				double precision = tp / (tp + fp);
				double recall = tp / (tp + fn);
				double fmeasure = 2 * (precision * recall) / (precision + recall);
				System.out.println("k: " + k + " eps: " + eps);
				System.out.println("trueset: " + trueset.size());
				System.out.println("privateset: " + privateset.size());
				System.out.println("tp:" + tp);
				System.out.println("fp:" + fp);
				System.out.println("fn:" + fn);
				System.out.println("precision: " + precision);
				System.out.println("recall: " + recall);
				System.out.println("fmeasure: " + fmeasure);
				System.out.println("total error: " + totalerror);
				System.out.println("avg error: " + avgerror * 100 + "%");
				out.println(k + "," + eps + "," + trueset.size() + "," + privateset.size() + "," + tp + "," + fp + ","
						+ fn + "," + precision + "," + recall + "," + fmeasure + "," + totalerror + "," + avgerror);

			}

		}
		out.close();
	}
}

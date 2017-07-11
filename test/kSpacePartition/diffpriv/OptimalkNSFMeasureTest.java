package kSpacePartition.diffpriv;

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

public class OptimalkNSFMeasureTest {
	@Test
	public void testFMeasure() throws Exception {
		double[] epslist = new double[] { 1, 2, 4 };
		double[] srates = new double[] { 0.05,0.1, 0.2 };
		int[] levels = new int[] { 8 };
		PrintWriter out = new PrintWriter("fmeasure/optimalK-ns.csv");
		int ite = 1;
		out.println("k,srate,level,eps,true coutn, private count,tp,fp,fn,precision,recall,fmeasure,totalerror,avgerror");
		for (double srate : srates) {
			for (int k = 100; k <= 100; k += 20) {
				for (int level : levels) {
					for (double eps : epslist) {
						double t_tp = 0;
						double t_fp = 0;
						double t_fn = 0;
						double t_truesize = 0.0;
						double t_privsize = 0.0;
						double t_avgerror = 0.0;
						double t_precision = 0.0;
						double t_recall = 0.0;
						double t_fmeasure = 0.0;
						double t_totalerror = 0.0;

						for (int o = 0; o < ite; o++) {
							double tp = 0;
							double fp = 0;
							double fn = 0;

							double precision = 0.0;
							double recall = 0.0;
							double fmeasure = 0.0;

							String truefile = "data/" + "15-clusters.dat"
									+ "-BNL-k-" + k + ".csv";
							String privatefile = "privkspace_bbs_output/"
									+ "dfoptimalKgrid-ns-bbs-skyband-k-" + k
									+ "-level-" + level + "-r-" + srate
									+ "-eps-" + eps + "." + o + ".csv";
							List<String> truelines = Files.readAllLines(
									Paths.get(truefile),
									Charset.defaultCharset());

							List<String> privatelines = Files.readAllLines(
									Paths.get(privatefile),
									Charset.defaultCharset());

							HashSet<Tuple> trueset = new HashSet<>();
							for (int i = 0; i < truelines.size(); i++) {
								String[] line = truelines.get(i).split(",");
								double x = Double.parseDouble(line[0]);
								double y = Double.parseDouble(line[1]);
								trueset.add(new Tuple(new double[] { x, y }));
							}

							HashSet<Tuple> privateset = new HashSet<>();
							for (int i = 0; i < privatelines.size(); i++) {
								String[] line = privatelines.get(i).split(",");
								double x = Double.parseDouble(line[0]);
								double y = Double.parseDouble(line[1]);
								privateset
										.add(new Tuple(new double[] { x, y }));
							}

							for (Tuple tuple : privateset) {
								double x = tuple.getValue(0);
								double y = tuple.getValue(1);
								boolean isTP = false;
								for (Tuple truetuple : trueset) {
									if (Math.abs(truetuple.getValue(0) - x) < 0.01 * 1000000
											&& Math.abs(truetuple.getValue(1)
													- y) < 0.01 * 1000000) {
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
											&& Math.abs(privtuple.getValue(1)
													- y) < 0.01 * 1000000) {
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
									double d = Math.abs(truetuple.getValue(0)
											- x)
											+ Math.abs(truetuple.getValue(1)
													- y);
									if (mind > d) {
										mind = d;
									}
								}
								totalerror += mind;
							}
							double avgerror = (totalerror / privateset.size()) / 1000000.0;
							precision = tp / (tp + fp);
							recall = tp / (tp + fn);
							fmeasure = 2 * (precision * recall)
									/ (precision + recall);
							System.out.println("k: " + k + " eps: " + eps);
							System.out.println("trueset: " + trueset.size());
							System.out.println("privateset: "
									+ privateset.size());
							System.out.println("tp:" + tp);
							System.out.println("fp:" + fp);
							System.out.println("fn:" + fn);
							System.out.println("precision: " + precision);
							System.out.println("recall: " + recall);
							System.out.println("fmeasure: " + fmeasure);
							System.out.println("total error: " + totalerror);
							System.out.println("avg error: " + avgerror * 100
									+ "%");
							t_truesize += trueset.size();
							t_privsize += privateset.size();
							t_tp += tp;
							t_fp += fp;
							t_fn += fn;
							t_precision += precision;
							t_recall += recall;
							t_fmeasure += fmeasure;
							t_totalerror += totalerror;
							t_avgerror += avgerror;
						}

						out.println(k + "," + srate + "," + level + "," + eps
								+ "," + t_truesize / ite + ","
								+ t_privsize / ite + "," + t_tp / ite + ","
								+ t_fp / ite + "," + t_fn / ite + ","
								+ t_precision / ite + "," + t_recall / ite
								+ "," + t_fmeasure / ite + "," + t_totalerror
								/ ite + "," + t_avgerror / ite);
					}
				}

			}
		}
		out.close();
	}
}

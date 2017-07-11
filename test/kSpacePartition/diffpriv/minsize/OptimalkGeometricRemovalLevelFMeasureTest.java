package kSpacePartition.diffpriv.minsize;

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

public class OptimalkGeometricRemovalLevelFMeasureTest {
	@Test
	public void testFMeasure() throws Exception {
		int[] levels = new int[] { 8 };
		double[] epslist = new double[] { 2, 4, 10, 100 };
		double[] splitrates = new double[] { 0.1 };
		double[] treerates = new double[] { 0.8, 0.9 };
		int[] removalLevels = new int[] { 4, 5, 6, 7 };
		
		int ite = 10;
		PrintWriter out = new PrintWriter("fmeasure/optimalK-geometriclevelremoval.csv");
		out.println("k,eps, max level, split rate, tree rate, removal level, true coutn, private count,tp,fp,fn,precision,recall,fmeasure,totalerror,avgerror");
		for (int k = 100; k <= 100; k += 20) {
			for (int removalLevel : removalLevels) {
				for (double trate : treerates) {
					for (double srate : splitrates) {
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

									String truefile = "data/"
											+ "15-clusters.dat" + "-BNL-k-" + k
											+ ".csv";
									String privatefile = "levelremoval/"
											+ "dfoptimalKgrid-glr-bbs-skyband-k-"
											+ k + "-level-" + level
											+ "-r-" + srate + "-t-"
											+ trate + "-rl-"
											+ removalLevel + "-eps-"
											+ eps + "." + o + ".csv";
									List<String> truelines = Files
											.readAllLines(Paths.get(truefile),
													Charset.defaultCharset());

									List<String> privatelines = Files
											.readAllLines(
													Paths.get(privatefile),
													Charset.defaultCharset());

									HashSet<Tuple> trueset = new HashSet<>();
									for (int i = 0; i < truelines.size(); i++) {
										String[] line = truelines.get(i).split(
												",");
										double x = Double.parseDouble(line[0]);
										double y = Double.parseDouble(line[1]);
										trueset.add(new Tuple(new double[] { x,
												y }));
									}

									HashSet<Tuple> privateset = new HashSet<>();
									for (int i = 0; i < privatelines.size(); i++) {
										String[] line = privatelines.get(i)
												.split(",");
										double x = Double.parseDouble(line[0]);
										double y = Double.parseDouble(line[1]);
										privateset.add(new Tuple(new double[] {
												x, y }));
									}

									for (Tuple tuple : privateset) {
										double x = tuple.getValue(0);
										double y = tuple.getValue(1);
										boolean isTP = false;
										for (Tuple truetuple : trueset) {
											if (Math.abs(truetuple.getValue(0)
													- x) < 0.01 * 1000000
													&& Math.abs(truetuple
															.getValue(1) - y) < 0.01 * 1000000) {
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
											if (Math.abs(privtuple.getValue(0)
													- x) < 0.01 * 1000000
													&& Math.abs(privtuple
															.getValue(1) - y) < 0.01 * 1000000) {
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
											double d = Math.abs(truetuple
													.getValue(0) - x)
													+ Math.abs(truetuple
															.getValue(1) - y);
											if (mind > d) {
												mind = d;
											}
										}
										totalerror += mind;
									}
									double avgerror = (totalerror / privateset
											.size()) / 1000000.0;
									precision = tp / (tp + fp);
									recall = tp / (tp + fn);
									fmeasure = 2 * (precision * recall)
											/ (precision + recall);
									System.out.println("k: " + k + " eps: "
											+ eps);
									System.out.println("trueset: "
											+ trueset.size());
									System.out.println("privateset: "
											+ privateset.size());
									System.out.println("tp:" + tp);
									System.out.println("fp:" + fp);
									System.out.println("fn:" + fn);
									System.out.println("precision: "
											+ precision);
									System.out.println("recall: " + recall);
									System.out.println("fmeasure: " + fmeasure);
									System.out.println("total error: "
											+ totalerror);
									System.out.println("avg error: " + avgerror
											* 100 + "%");
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

								out.println(k + "," + eps + ","
										+ level + ","+ srate + ","
										+ trate + ","+ removalLevel + ","
										+ t_truesize / ite + ","
										+ t_privsize / ite + "," + t_tp / ite
										+ "," + t_fp / ite + "," + t_fn / ite
										+ "," + t_precision / ite + ","
										+ t_recall / ite + ","
										+ t_fmeasure / ite + ","
										+ t_totalerror / ite + "," + t_avgerror
										/ ite);
							}
						}
					}
				}
			}

		}
		out.close();
	}
}

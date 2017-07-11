package bnl;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import skyband.BNL;
import skyband.Comparison;
import skyband.Tuple;

public class SkybandBNLAdaptiveGridNBATest {
	@Test
	public void testKSkylineBandOnDatasetsMIN() throws Exception {

		// String[] files = new String[] { "anti5k-normal.txt" };
		double[] epslist = new double[] { 0.1, 0.5, 1.0, 2.0 };
		String[] files = new String[] { "nba.csv" };
		int ite = 10;
		int[] ks = new int[] { 20, 40, 60, 80, 100, 150, 200 };
		double dw = 5000.0;
		double dh = 5000.0;
		double err = 0.01;

		for (String file : files) {
			for (double eps : epslist) {
				for (int j = 0; j < 10; j++) {
					String filename = file.split("\\.")[0];
					List<Tuple> tuples = new ArrayList<>();
					List<String> lines = Files.readAllLines(
							Paths.get("adaptiveGrid_data/" + filename + "-eps-"
									+ eps + "-" + j + ".csv"),
							Charset.defaultCharset());
					for (int i = 1; i < lines.size(); i++) {
						String[] line = lines.get(i).split(",");
						double x = Double.parseDouble(line[0]);
						double y = Double.parseDouble(line[1]);
						tuples.add(new Tuple(new Double[] { x, y }));
					}
					ArrayList<Comparison> comparisons = new ArrayList<Comparison>();
					comparisons.add(Comparison.MAX);
					comparisons.add(Comparison.MAX);

					for (int k : ks) {
						System.out.println("compute " + j + "th for file " + filename + " eps " + eps + " k "+ k + "-skylineband for "
								+ filename);
						Tuple[] band = BNL.computeKSkylineBand(tuples, null,
								comparisons, k);
						PrintWriter out = new PrintWriter(
								"adaptiveGrid_kskyband/" + filename + "-eps-"
										+ eps + "-BNL-k-" + k + "." + j
										+ ".csv");
						for (Tuple tuple : band) {
							double x = tuple.getValue(0);
							out.print(x + ",");
							double y = tuple.getValue(1);
							out.print(y);
							out.println();
						}
						out.close();
						System.out.println("k= " + k);
						for (Tuple tuple : tuples) {
							tuple.dominatedCount = 0;
						}
					}
				}
			}
		}

		for (String file : files) {
			String filename = file.split("\\.")[0];
			PrintWriter out = new PrintWriter("fmeasure/ad-" + filename
					+ "-noisynegativelevelremoval.csv");
			out.println("k,eps, true count, private count,tp,fp,fn,precision,recall,fmeasure,totalerror,avgerror");
			for (int k : ks) {
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

						String truefile = "dataset/" + file + "-BNL-k-" + k
								+ ".csv";
						String privatefile = "adaptiveGrid_kskyband/"
								+ filename + "-eps-" + eps + "-BNL-k-" + k
								+ "." + o + ".csv";
						List<String> truelines = Files.readAllLines(
								Paths.get(truefile), Charset.defaultCharset());

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
							privateset.add(new Tuple(new double[] { x, y }));
						}

						for (Tuple tuple : privateset) {
							double x = tuple.getValue(0);
							double y = tuple.getValue(1);
							boolean isTP = false;
							for (Tuple truetuple : trueset) {
								if (Math.abs(truetuple.getValue(0) - x) < err
										* dw
										&& Math.abs(truetuple.getValue(1) - y) < err
												* dh) {
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
								if (Math.abs(privtuple.getValue(0) - x) < err
										* dw
										&& Math.abs(privtuple.getValue(1) - y) < err
												* dh) {
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
								double d = Math.abs(truetuple.getValue(0) - x)
										+ Math.abs(truetuple.getValue(1) - y);
								if (mind > d) {
									mind = d;
								}
							}
							totalerror += mind;
						}
						double avgerror = (totalerror / privateset.size())
								/ (dw + dh);
						precision = tp / (tp + fp);
						recall = tp / (tp + fn);
						fmeasure = 2 * (precision * recall)
								/ (precision + recall);
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
						System.out
								.println("avg error: " + avgerror * 100 + "%");
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

					out.println(k + "," + eps + "," + +t_truesize / ite + ","
							+ t_privsize / ite + "," + t_tp / ite + ","
							+ t_fp / ite + "," + t_fn / ite + ","
							+ t_precision / ite + "," + t_recall / ite + ","
							+ t_fmeasure / ite + "," + t_totalerror / ite + ","
							+ t_avgerror / ite);

				}
			}
			out.close();

		}
	}
}

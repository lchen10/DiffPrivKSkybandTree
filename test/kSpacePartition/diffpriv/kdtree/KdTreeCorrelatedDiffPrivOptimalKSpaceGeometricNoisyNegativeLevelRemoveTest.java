package kSpacePartition.diffpriv.kdtree;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.Test;

import kSpacePartition.KSpacePartition;
import kSpacePartition.Node;
import kSpacePartition.Param;
import kSpacePartition.diffpriv.kdtree.KdTreeAntiDiffPrivOptimalKSpaceGeometricNoisyNegativeLevelRemoveTest.BBSThread;
import skyband.BBSDiffPrivacy;
import skyband.Comparison;
import skyband.Tuple;

public class KdTreeCorrelatedDiffPrivOptimalKSpaceGeometricNoisyNegativeLevelRemoveTest {
	class BBSThread extends Thread {

		public KSpacePartition p = new KSpacePartition();
		public int k;
		public int level;
		public int minsize;
		public double eps;
		public double srate;
		public int delta = 1;
		public double trate;
		public int removalLevel;
		public double dw = 1000000.0;
		public double dh = 1000000.0;
		public double err = 0.02;
		public int kdelta;
		public int x;

		@Override
		public void run() {

			try {
				System.out.println(x + "th computing for k " + k + " eps " + eps);
				BBSDiffPrivacy.computeBBSWithSynthesis("correlated10k-eps-" + eps + "." + x + ".txt",
						"dfoptimalKgrid-corr-bbs-skyband-k-" + k + "-eps-" + eps
						+ "." + x + ".csv", k, "kdtree/corr",
						"kdtree_data", new Comparison[] { Comparison.MIN, Comparison.MIN }, delta, null);
				System.out.println("finished computing for k " + k);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testLoadPoints() throws Exception {
		KSpacePartition p = new KSpacePartition();
//		int[] ks = new int[] { 20, 40, 60, 80, 100, 150, 200 };
		int[] ks = new int[] { 0, 1, 3, 5, 10, 20, 40, 60, 80, 100, 150, 200 };
		double[] epslist = new double[] { 0.1, 0.5, 1, 2 };
		int delta = 1;
		Param.XMIN = 11680;
		Param.XMAX = 1000000;
		Param.YMIN = 13218;
		Param.YMAX = 1000000;
		double dw = Param.XMAX - Param.XMIN;
		double dh = Param.YMAX - Param.YMIN;
		double err = 0.1;
		int ite = 10;

		int maxthread = 4;
		ArrayList<BBSThread> bbsthreads = new ArrayList<>();
		int tnum = 0;
//		for (int x = 0; x < ite; x++) {
//			for (int k : ks) {
//				for (double eps : epslist) {
//					BBSThread sthread = new BBSThread();
//					sthread.eps = eps;
//					sthread.delta = delta;
//					sthread.k = k;
//					sthread.x = x;
//					bbsthreads.add(sthread);
//					sthread.start();
//					tnum++;
//
//					if (tnum == maxthread) {
//						for (BBSThread t : bbsthreads) {
//							t.join();
//						}
//						bbsthreads = new ArrayList<>();
//						tnum = 0;
//					}
//
//				}
//			}
//		}
//
//		for (BBSThread sthread : bbsthreads) {
//			sthread.join();
//		}

		for (double eps : epslist) {
			PrintWriter out = new PrintWriter("fmeasure/kdtree/corr-geometric" + "-eps-" + eps + ".csv");
			out.println("k,eps, true count, private count,tp,fp,fn,precision,recall,fmeasure,totalerror,avgerror");
			for (int k : ks) {

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

					String truefile = "dataset/" + "correlated10k.csv" + "-BNL-k-" + k + ".csv";
					String privatefile = "kdtree/corr/" + "dfoptimalKgrid-corr-bbs-skyband-k-" + k + "-eps-" + eps
							+ "." + o + ".csv";
					List<String> truelines = Files.readAllLines(Paths.get(truefile), Charset.defaultCharset());

					List<String> privatelines = Files.readAllLines(Paths.get(privatefile), Charset.defaultCharset());

					HashSet<Tuple> trueset = new HashSet<>();
					for (int i = 0; i < truelines.size(); i++) {
						String[] line = truelines.get(i).split(",");
						double x = Double.parseDouble(line[0]);
						double y = Double.parseDouble(line[delta]);
						trueset.add(new Tuple(new double[] { x, y }));
					}

					HashSet<Tuple> privateset = new HashSet<>();
					for (int i = 0; i < privatelines.size(); i++) {
						String[] line = privatelines.get(i).split(",");
						double x = Double.parseDouble(line[0]);
						double y = Double.parseDouble(line[delta]);
						privateset.add(new Tuple(new double[] { x, y }));
					}

					for (Tuple tuple : privateset) {
						double x = tuple.getValue(0);
						double y = tuple.getValue(delta);
						boolean isTP = false;
						for (Tuple truetuple : trueset) {
							if (Math.abs(truetuple.getValue(0) - x) < err * dw
									&& Math.abs(truetuple.getValue(delta) - y) < err * dh) {
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
						double y = tuple.getValue(delta);
						boolean isFN = true;
						for (Tuple privtuple : privateset) {
							if (Math.abs(privtuple.getValue(0) - x) < err * dw
									&& Math.abs(privtuple.getValue(delta) - y) < err * dh) {
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
						double y = tuple.getValue(delta);
						double mind = Double.MAX_VALUE;
						for (Tuple truetuple : trueset) {
							double d = Math.abs(truetuple.getValue(0) - x) + Math.abs(truetuple.getValue(delta) - y);
							if (mind > d) {
								mind = d;
							}
						}
						totalerror += mind;
					}
					double avgerror = (totalerror / privateset.size()) / (dw + dh);
					precision = tp / (tp + fp);
					recall = tp / (tp + fn);
					fmeasure = 2 * (precision * recall) / (precision + recall);
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

				out.println(k + "," + eps + "," + +t_truesize / ite + "," + t_privsize / ite + "," + t_tp / ite + ","
						+ t_fp / ite + "," + t_fn / ite + "," + t_precision / ite + "," + t_recall / ite + ","
						+ t_fmeasure / ite + "," + t_totalerror / ite + "," + t_avgerror / ite);
			}

			out.close();
		}
	}
}

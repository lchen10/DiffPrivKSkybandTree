package kSpacePartition.diffpriv.adaptiveskyband;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
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
import kSpacePartition.PerfStatistics;
import skyband.BBSDiffPrivacy;
import skyband.Comparison;
import skyband.Tuple;

public class NormalAdaptiveSkybandGeometricTest {

	class SpaceSplitThread extends Thread {

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
		public double err = 0.01;
		public int kdelta;
		public int ite;
		public ArrayList<PerfStatistics> perfs;

		@Override
		public void run() {

			try {

				Node.currentID = delta;
				Node root = p.partitionByAdaptiveKNoisyNegative(k, "dataset",
						"normal10k.csv", level, true, eps, ",", dw, dh,
						Comparison.MIN, minsize, kdelta);
				// root = p.postProcessing(root,
				// epslevel,
				// level);
				PrintWriter out = new PrintWriter(
						"adaptiveskyband/normal/dfoptimalKout.txt");
				PrintWriter grid = new PrintWriter(
						"adaptiveskyband/normal/dfoptimalKgrid-nglr-k-" + k
								+ "-level-" + level + "-r-" + srate + "-t-"
								+ trate + "-rl-" + removalLevel + "-min-"
								+ minsize + "-kdelta-" + kdelta + "-eps-" + eps
								+ "." + ite + ".csv");
				Queue<Node> q = new LinkedList<>();
				q.add(root);
				while (!q.isEmpty()) {
					Node n = q.remove();
					out.println(n);

					String parentstring = "null";
					if (n.parent != null) {
						parentstring = n.parent.id + "";
					}

					int childrencount = 4;
					if (n.isLeave) {
						childrencount = delta;
					}

					grid.println(n.count + "," + n.xmin + "," + n.ymin + ","
							+ n.xmax + "," + n.ymax + "," + childrencount + ","
							+ n.id + "," + parentstring + "," + n.level);
					for (Node child : n.children) {
						q.add(child);
					}
				}
				out.close();
				grid.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

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
		public double err = 0.01;
		public int kdelta;
		public int ite;
		public ArrayList<PerfStatistics> perfs;

		@Override
		public void run() {

			try {
				System.out.println(ite + "th computing for k " + k + " level "
						+ level + " split rate " + srate + " tree rate "
						+ trate + " removal level " + removalLevel + " eps "
						+ eps);
				BBSDiffPrivacy.computeBBSWithSynthesis("dfoptimalKgrid-nglr-k-"
						+ k + "-level-" + level + "-r-" + srate + "-t-" + trate
						+ "-rl-" + removalLevel + "-min-" + minsize
						+ "-kdelta-" + kdelta + "-eps-" + eps + "." + ite
						+ ".csv", "dfoptimalKgrid-nglr-bbs-skyband-k-" + k
						+ "-level-" + level + "-r-" + srate + "-t-" + trate
						+ "-rl-" + removalLevel + "-min-" + minsize
						+ "-kdelta-" + kdelta + "-eps-" + eps + "." + ite
						+ ".csv", k, "adaptiveskyband/normal", "adaptiveskyband/normal",
						new Comparison[] { Comparison.MIN, Comparison.MIN },
						delta, perfs);
				System.out.println("finished computing for k " + k);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testLoadPoints() throws Exception {
		KSpacePartition p = new KSpacePartition();
		int[] ks = new int[] { 20, 40, 60, 80, 100, 150, 200 };
		int[] levels = new int[] { 7 };
		int[] minsizes = new int[] { 8 };
		double[] epslist = new double[] { 0.1, 0.5, 1, 2 };
		double[] splitrates = new double[] { 0.2 };
		int delta = 1;
		double[] treerates = new double[] { 1 };
		int[] removalLevels = new int[] { 6 };
		double dw = 1000000.0;
		double dh = 1000000.0;
		double err = 0.01;
		int ite = 10;
		int[] kdeltas = new int[] { 0 };
		int maxthread = 4;

		ArrayList<SpaceSplitThread> sthreads = new ArrayList<>();
		ArrayList<BBSThread> bbsthreads = new ArrayList<>();
		PrintWriter perfout = new PrintWriter("perf/15-cluster.perf.csv");
		ArrayList<PerfStatistics> perfs = new ArrayList<>();
		ArrayList<PerfStatistics> parperfs = new ArrayList<>();

		int tnum = 0;

		for (int kdelta : kdeltas) {
			for (int k : ks) {
				for (int minsize : minsizes) {
					for (int removalLevel : removalLevels) {
						Param.REMOVAL_LEVEL = removalLevel;
						for (double trate : treerates) {
							for (double srate : splitrates) {
								Param.SPLIT_BUDGET_RATE = srate;
								for (int level : levels) {
									for (int x = 0; x < ite; x++) {
										for (double eps : epslist) {
											System.out
													.println(x
															+ "th partitioning space diff priv for k "
															+ k + " level "
															+ level
															+ " split rate "
															+ srate
															+ " tree rate "
															+ trate + " eps "
															+ eps);

											tnum++;

											SpaceSplitThread sthread = new SpaceSplitThread();
											sthread.k = k;
											sthread.level = level;
											sthread.minsize = minsize;
											sthread.eps = eps;
											sthread.srate = srate;
											sthread.delta = delta;
											sthread.trate = trate;
											sthread.removalLevel = removalLevel;
											sthread.dw = dw;
											sthread.dh = dh;
											sthread.err = err;
											sthread.kdelta = kdelta;
											sthread.ite = x;
											sthread.perfs = parperfs;
											sthreads.add(sthread);
											sthread.start();

											if (tnum == maxthread) {
												for (SpaceSplitThread t : sthreads) {
													t.join();
												}
												sthreads = new ArrayList<>();
												tnum = 0;
											}

										}
									}
								}
							}
						}
					}
				}
			}
		}

		for (SpaceSplitThread sthread : sthreads) {
			sthread.join();
		}

		tnum = 0;
		for (int x = 0; x < ite; x++) {
			for (int kdelta : kdeltas) {
				for (int minsize : minsizes) {
					for (int removalLevel : removalLevels) {
						for (double trate : treerates) {
							for (double srate : splitrates) {
								for (int level : levels) {
									for (int k : ks) {
										for (double eps : epslist) {
											BBSThread sthread = new BBSThread();
											sthread.k = k;
											sthread.level = level;
											sthread.minsize = minsize;
											sthread.eps = eps;
											sthread.srate = srate;
											sthread.delta = delta;
											sthread.trate = trate;
											sthread.removalLevel = removalLevel;
											sthread.dw = dw;
											sthread.dh = dh;
											sthread.err = err;
											sthread.kdelta = kdelta;
											sthread.ite = x;
											sthread.perfs = perfs;
											bbsthreads.add(sthread);
											sthread.start();
											tnum++;
											if (tnum == maxthread) {
												for (BBSThread t : bbsthreads) {
													t.join();
												}
												bbsthreads = new ArrayList<>();
												tnum = 0;
											}
										}

									}
								}
							}
						}
					}
				}
			}
		}

		for (BBSThread sthread : bbsthreads) {
			sthread.join();
		}

		for (PerfStatistics perf : perfs) {
			perfout.println(perf.k + "," + perf.elapsedtime);
		}
		perfout.close();

		PrintWriter out = new PrintWriter(
				"fmeasure/as-normal-noisynegativelevelremoval-geometric.csv");
		out.println("k,eps,minsize, kdetal, max level, split rate, tree rate, removal level, true coutn, private count,tp,fp,fn,precision,recall,fmeasure,totalerror,avgerror");
		for (int k : ks) {
			for (int kdelta : kdeltas) {
				for (int minsize : minsizes) {
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

											String truefile = "dataset/"
													+ "normal10k.csv"
													+ "-BNL-k-" + k + ".csv";
											String privatefile = "adaptiveskyband/normal/"
													+ "dfoptimalKgrid-nglr-bbs-skyband-k-"
													+ k
													+ "-level-"
													+ level
													+ "-r-"
													+ srate
													+ "-t-"
													+ trate
													+ "-rl-"
													+ removalLevel
													+ "-min-"
													+ minsize
													+ "-kdelta-"
													+ kdelta
													+ "-eps-"
													+ eps
													+ "." + o + ".csv";
											List<String> truelines = Files
													.readAllLines(
															Paths.get(truefile),
															Charset.defaultCharset());

											List<String> privatelines = Files
													.readAllLines(
															Paths.get(privatefile),
															Charset.defaultCharset());

											HashSet<Tuple> trueset = new HashSet<>();
											for (int i = 0; i < truelines
													.size(); i++) {
												String[] line = truelines
														.get(i).split(",");
												double x = Double
														.parseDouble(line[0]);
												double y = Double
														.parseDouble(line[delta]);
												trueset.add(new Tuple(
														new double[] { x, y }));
											}

											HashSet<Tuple> privateset = new HashSet<>();
											for (int i = 0; i < privatelines
													.size(); i++) {
												String[] line = privatelines
														.get(i).split(",");
												double x = Double
														.parseDouble(line[0]);
												double y = Double
														.parseDouble(line[delta]);
												privateset.add(new Tuple(
														new double[] { x, y }));
											}

											for (Tuple tuple : privateset) {
												double x = tuple.getValue(0);
												double y = tuple
														.getValue(delta);
												boolean isTP = false;
												for (Tuple truetuple : trueset) {
													if (Math.abs(truetuple
															.getValue(0) - x) < err
															* dw
															&& Math.abs(truetuple
																	.getValue(delta)
																	- y) < err
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
												double y = tuple
														.getValue(delta);
												boolean isFN = true;
												for (Tuple privtuple : privateset) {
													if (Math.abs(privtuple
															.getValue(0) - x) < err
															* dw
															&& Math.abs(privtuple
																	.getValue(delta)
																	- y) < err
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
												double y = tuple
														.getValue(delta);
												double mind = Double.MAX_VALUE;
												for (Tuple truetuple : trueset) {
													double d = Math
															.abs(truetuple
																	.getValue(0)
																	- x)
															+ Math.abs(truetuple
																	.getValue(delta)
																	- y);
													if (mind > d) {
														mind = d;
													}
												}
												totalerror += mind;
											}
											double avgerror = (totalerror / privateset
													.size()) / (dw + dh);
											precision = tp / (tp + fp);
											recall = tp / (tp + fn);
											fmeasure = 2 * (precision * recall)
													/ (precision + recall);
											System.out.println("k: " + k
													+ " eps: " + eps);
											System.out.println("trueset: "
													+ trueset.size());
											System.out.println("privateset: "
													+ privateset.size());
											System.out.println("tp:" + tp);
											System.out.println("fp:" + fp);
											System.out.println("fn:" + fn);
											System.out.println("precision: "
													+ precision);
											System.out.println("recall: "
													+ recall);
											System.out.println("fmeasure: "
													+ fmeasure);
											System.out.println("total error: "
													+ totalerror);
											System.out.println("avg error: "
													+ avgerror * 100 + "%");
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
												+ minsize + "," + kdelta + ","
												+ level + "," + srate + ","
												+ trate + "," + removalLevel
												+ "," + t_truesize / ite + ","
												+ t_privsize / ite + ","
												+ t_tp / ite + "," + t_fp / ite
												+ "," + t_fn / ite + ","
												+ t_precision / ite + ","
												+ t_recall / ite + ","
												+ t_fmeasure / ite + ","
												+ t_totalerror / ite + ","
												+ t_avgerror / ite);
									}
								}
							}
						}
					}
				}
			}
		}
		out.close();

	}
}

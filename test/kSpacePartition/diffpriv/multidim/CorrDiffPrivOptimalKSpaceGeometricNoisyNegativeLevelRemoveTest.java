package kSpacePartition.diffpriv.multidim;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.Test;

import kSpacePartition.KSpacePartition;
import kSpacePartition.Node;
import kSpacePartition.Param;
import kSpacePartition.MultiDim.MultiDimBBSDiffPrivacy;
import kSpacePartition.MultiDim.MultiDimKSpacePartition;
import kSpacePartition.MultiDim.MultiDimNode;
import kSpacePartition.MultiDim.MultiParam;
import skyband.BBSDiffPrivacy;
import skyband.Comparison;
import skyband.Tuple;

public class CorrDiffPrivOptimalKSpaceGeometricNoisyNegativeLevelRemoveTest {
	class SpaceSplitThread extends Thread {

		public MultiDimKSpacePartition p = new MultiDimKSpacePartition();
		public int k;
		public int level;
		public int minsize;
		public double eps;
		public double srate;
		public double delta = 1;
		public double trate;
		public int removalLevel;
		public double err = 0.01;
		public int kdelta;
		public int ite;
		public int dim = 3;
		public double[] rangewidth;

		@Override
		public void run() {

			try {
				double[] epslevel = new double[level + 1];
				for (int i = 0; i < epslevel.length; i++) {
					epslevel[i] = Math.pow(2, (1.0 / 3) * i);
				}

				double sum = 0;
				for (double v : epslevel) {
					sum += v;
				}

				for (int i = 0; i < epslevel.length; i++) {
					epslevel[i] = (eps * trate) * (epslevel[i] / sum);
					System.out.println("eps level-" + i + ": " + epslevel[i]);
				}
				MultiDimNode.currentID = 1;

				MultiDimNode root = p.partitionByOptimalKNoisyNegative(k, "dataset", "corr3d.csv", level, true,
						epslevel, eps * (delta - trate), ",", rangewidth, Comparison.MAX, minsize, kdelta);
				// root = p.postProcessing(root,
				// epslevel,
				// level);
				PrintWriter out = new PrintWriter("multidim/corr/dfoptimalKout.txt");
				PrintWriter grid = new PrintWriter("multidim/corr/dfoptimalKgrid-nglr-k-" + k + "-level-" + level
						+ "-r-" + srate + "-t-" + trate + "-rl-" + removalLevel + "-min-" + minsize + "-kdelta-"
						+ kdelta + "-eps-" + eps + "." + ite + ".csv");
				Queue<MultiDimNode> q = new LinkedList<>();
				q.add(root);
				while (!q.isEmpty()) {
					MultiDimNode n = q.remove();
					out.println(n);

					String parentstring = "null";
					if (n.parent != null) {
						parentstring = n.parent.id + "";
					}

					int childrencount = 4;
					if (n.isLeave) {
						childrencount = 1;
					}

					String prefix = n.count + ",";
					String postix = childrencount + "," + n.id + "," + parentstring + "," + n.level;
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < n.ranges.length; i++) {
						sb.append(n.ranges[i][0] + "," + n.ranges[i][1] + ",");
					}

					grid.println(prefix + sb + postix);
					for (MultiDimNode child : n.children) {
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

		public int k;
		public int level;
		public int minsize;
		public double eps;
		public double srate;
		public double delta = 1;
		public double trate;
		public int removalLevel;
		public double dw = 1000000.0;
		public double dh = 1000000.0;
		public double err = 0.01;
		public int kdelta;
		public int ite;
		public double[] rangewidth;
		public int dim;

		@Override
		public void run() {

			try {
				System.out.println(ite + "th computing for k " + k + " level " + level + " split rate " + srate
						+ " tree rate " + trate + " removal level " + removalLevel + " eps " + eps);
				MultiDimBBSDiffPrivacy.computeBBSWithSynthesis(dim,
						"dfoptimalKgrid-nglr-k-" + k + "-level-" + level + "-r-" + srate + "-t-" + trate + "-rl-"
								+ removalLevel + "-min-" + minsize + "-kdelta-" + kdelta + "-eps-" + eps + "." + ite
								+ ".csv",
						"dfoptimalKgrid-nglr-bbs-skyband-k-" + k + "-level-" + level + "-r-" + srate + "-t-" + trate
								+ "-rl-" + removalLevel + "-min-" + minsize + "-kdelta-" + kdelta + "-eps-" + eps + "."
								+ ite + ".csv",
						k, "multidim/corr", "multidim/corr",
						new Comparison[] { Comparison.MAX, Comparison.MAX, Comparison.MAX }, delta, null);
				System.out.println("finished computing for k " + k);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testLoadPoints() throws Exception {
		KSpacePartition p = new KSpacePartition();
		// int[] ks = new int[] { 20, 40, 60, 80, 100, 150, 200 };
		int[] ks = new int[] { 20, 40, 60, 80, 100, 150, 200 };
		int[] levels = new int[] { 7 };
		int[] minsizes = new int[] { 8 };
		double[] epslist = new double[] { 2, };
		// double[] epslist = new double[] { 2, 4, 10 };
		double[] splitrates = new double[] { 0.1 };
		double[] treerates = new double[] { 1 };
		int[] removalLevels = new int[] { 6 };
		double delta = 0.01;
		double err = 0.03;
		int ite = 10;
		int dim = 3;
		int[] kdeltas = new int[] { 0 };
		int maxthread = 4;
		// Param.XMIN = 0;
		// Param.XMAX = 3156 + 1;
		// Param.YMIN = 0;
		// Param.YMAX = 1449 + 1;
		MultiParam.MINS = new double[dim];
		MultiParam.MAXS = new double[dim];
		MultiParam.MINS[0] = 0;
		MultiParam.MINS[1] = 0;
		MultiParam.MINS[2] = 0;
		MultiParam.MAXS[0] = 1000000 + 1;
		MultiParam.MAXS[1] = 1000000 + 1;
		MultiParam.MAXS[2] = 1000000 + 1;
		double[] rangewidth = new double[dim];
		for (int i = 0; i < rangewidth.length; i++) {
			rangewidth[i] = MultiParam.MAXS[i] - MultiParam.MINS[i];
		}

		// double dw = Param.XMAX - Param.XMIN;
		// double dh = Param.YMAX - Param.YMIN;

		ArrayList<SpaceSplitThread> sthreads = new ArrayList<>();
		ArrayList<BBSThread> bbsthreads = new ArrayList<>();

		int tnum = 0;

		for (int kdelta : kdeltas) {
			for (int k : ks) {
				for (int minsize : minsizes) {
					for (int removalLevel : removalLevels) {
						MultiParam.REMOVAL_LEVEL = removalLevel;
						for (double trate : treerates) {
							for (double srate : splitrates) {
								Param.SPLIT_BUDGET_RATE = srate;
								for (int level : levels) {
									for (int x = 0; x < ite; x++) {
										for (double eps : epslist) {
											System.out.println(x + "th partitioning space diff priv for k " + k
													+ " level " + level + " split rate " + srate + " tree rate " + trate
													+ " eps " + eps);

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
											sthread.rangewidth = rangewidth;
											sthread.err = err;
											sthread.kdelta = kdelta;
											sthread.ite = x;
											sthread.dim = dim;
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
											sthread.rangewidth = rangewidth;
											sthread.err = err;
											sthread.kdelta = kdelta;
											sthread.ite = x;
											sthread.dim = dim;
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

		for (double eps : epslist) {
			PrintWriter out = new PrintWriter("fmeasure/multidim/corr-geometric" + "-eps-" + eps + ".csv");
			out.println(
					"k,eps,minsize, kdetal, max level, split rate, tree rate, removal level, true coutn, private count,tp,fp,fn,precision,recall,fmeasure,totalerror,avgerror");
			PrintWriter outdetail = new PrintWriter("fmeasure/multidim/corr-detail" + "-eps-" + eps + ".csv");
			outdetail.println(
					"k,eps,minsize, kdetal, max level, split rate, tree rate, removal level, true coutn, private count,tp,fp,fn,precision,recall,fmeasure,totalerror,avgerror");
			PrintWriter outbox = new PrintWriter("fmeasure/multidim/corr-boxplotF1" + "-eps-" + eps + ".csv");
			String delimit = "";
			for (int k : ks) {
				outbox.print(delimit + k);
				delimit = ",";
			}
			outbox.println();
			double[][] f1s = new double[ite][ks.length];
			int row = 0;
			int col = 0;
			for (int k : ks) {
				for (int kdelta : kdeltas) {
					for (int minsize : minsizes) {
						for (int removalLevel : removalLevels) {
							for (double trate : treerates) {
								for (double srate : splitrates) {
									for (int level : levels) {

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

											String truefile = "dataset/" + "corr3d.csv" + "-BNL-k-" + k + ".csv";
											String privatefile = "multidim/corr/" + "dfoptimalKgrid-nglr-bbs-skyband-k-"
													+ k + "-level-" + level + "-r-" + srate + "-t-" + trate + "-rl-"
													+ removalLevel + "-min-" + minsize + "-kdelta-" + kdelta + "-eps-"
													+ eps + "." + o + ".csv";
											List<String> truelines = Files.readAllLines(Paths.get(truefile),
													Charset.defaultCharset());

											List<String> privatelines = Files.readAllLines(Paths.get(privatefile),
													Charset.defaultCharset());

											HashSet<Tuple> trueset = new HashSet<>();
											for (int i = 0; i < truelines.size(); i++) {
												String[] line = truelines.get(i).split(",");
												double[] v = new double[line.length];
												for (int j = 0; j < v.length; j++) {
													v[j] = Double.parseDouble(line[j]);
												}
												trueset.add(new Tuple(v));
											}

											HashSet<Tuple> privateset = new HashSet<>();
											for (int i = 0; i < privatelines.size(); i++) {
												String[] line = privatelines.get(i).split(",");
												double[] v = new double[line.length];
												for (int j = 0; j < v.length; j++) {
													v[j] = Double.parseDouble(line[j]);
												}
												privateset.add(new Tuple(v));
											}

											for (Tuple tuple : privateset) {
												boolean isTP = false;
												for (Tuple truetuple : trueset) {
													boolean found = true;
													for (int i = 0; i < rangewidth.length; i++) {

														if (Math.abs(truetuple.getValue(i) - tuple.getValue(i)) > err
																* rangewidth[i]) {

															found = false;
															break;
														}
													}
													if (found) {
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
												// double x = tuple.getValue(0);
												// double y = tuple.getValue(1);
												boolean isFN = true;
												for (Tuple privtuple : privateset) {
													boolean found = true;
													for (int i = 0; i < rangewidth.length; i++) {
														if (Math.abs(privtuple.getValue(i) - tuple.getValue(i)) >= err
																* rangewidth[i]) {
															found = false;
															break;
														}
													}

													if (found) {
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
												// double x = tuple.getValue(0);
												// double y = tuple.getValue(1);
												double mind = Double.MAX_VALUE;
												for (Tuple truetuple : trueset) {
													double d = 0;
													for (int i = 0; i < dim; i++) {
														d += Math.abs(truetuple.getValue(i) - tuple.getValue(i));
													}
													if (mind > d) {
														mind = d;
													}
												}
												totalerror += mind;
											}

											double avgerror = (totalerror / privateset.size())
													/ (Arrays.stream(rangewidth).sum());
											precision = tp / (tp + fp);
											recall = tp / (tp + fn);
											fmeasure = 2.0 * (precision * recall) / (precision + recall);
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

											outdetail.println(k + "," + eps + "," + minsize + "," + kdelta + "," + level
													+ "," + srate + "," + trate + "," + removalLevel + ","
													+ trueset.size() + "," + privateset.size() + "," + tp + "," + fp
													+ "," + fn + "," + precision + "," + recall + "," + fmeasure + ","
													+ totalerror + "," + avgerror);

											f1s[row][col] = fmeasure;
											row++;

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

										out.println(k + "," + eps + "," + minsize + "," + kdelta + "," + level + ","
												+ srate + "," + trate + "," + removalLevel + "," + t_truesize / ite
												+ "," + t_privsize / ite + "," + t_tp / ite + "," + t_fp / ite + ","
												+ t_fn / ite + "," + t_precision / ite + "," + t_recall / ite + ","
												+ t_fmeasure / ite + "," + t_totalerror / ite + "," + t_avgerror / ite);
									}
								}
							}
						}
					}
				}
				col++;
				row = 0;
			}
			for (double[] f1row : f1s) {
				delimit = "";
				for (double f1 : f1row) {
					outbox.print(delimit + f1);
					delimit = ",";
				}
				outbox.println();
			}

			out.close();
			outdetail.close();
			outbox.close();
		}
	}

}

package kSpacePartition.diffpriv.levelremoval;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.Test;

import kSpacePartition.KSpacePartition;
import kSpacePartition.Node;
import kSpacePartition.Param;
import skyband.BBSDiffPrivacy;
import skyband.Comparison;
import skyband.Tuple;

public class DiffPrivOptimalKSpaceGeometricLevelRemovalPartitionTest {
	@Test
	public void testLoadPoints() throws Exception {
		KSpacePartition p = new KSpacePartition();
		int k = 100;
		int[] levels = new int[] { 8 };
		double[] epslist = new double[] { 2, 4, 10, 100 };
		double[] splitrates = new double[] { 0.1 };
		double[] treerates = new double[] { 0.9 };
		int[] removalLevels = new int[] { 4, 5, 6, 7 };
		int ite = 10;
		double dw = 1000000.0;
		double dh = 1000000.0;
		double err = 0.01;

		for (int removalLevel : removalLevels) {
			Param.REMOVAL_LEVEL = removalLevel;
			for (double trate : treerates) {
				for (double srate : splitrates) {
					Param.SPLIT_BUDGET_RATE = srate;
					for (int level : levels) {
						for (int x = 0; x < ite; x++) {
							for (double eps : epslist) {
								System.out.println(x + "th partitioning space diff priv for k " + k + " level " + level
										+ " split rate " + srate + " tree rate " + trate + " eps " + eps);
								// double[] epslevel = new double[level + 1];
								// for (int i = 0; i < epslevel.length; i++) {
								// epslevel[i] = (eps * trate) / (level + 1);
								// }
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

								Node root = p.partitionByOptimalKLevelRemoval(k, "15-clusters.dat", level, true,
										epslevel, eps * (1 - trate));
								// root = p.postProcessing(root, epslevel,
								// level);
								PrintWriter out = new PrintWriter("kspaceoutput/dfoptimalKout.txt");
								PrintWriter grid = new PrintWriter("kspaceoutput/dfoptimalKgrid-glr-k-" + k + "-level-"
										+ level + "-r-" + srate + "-t-" + trate + "-rl-" + removalLevel + "-eps-" + eps
										+ "." + x + ".csv");
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
										childrencount = 1;
									}

									grid.println(n.count + "," + n.xmin + "," + n.ymin + "," + n.xmax + "," + n.ymax
											+ "," + childrencount + "," + n.id + "," + parentstring + "," + n.level);
									for (Node child : n.children) {
										q.add(child);
									}
								}
								out.close();
								grid.close();
							}
						}
					}
				}
			}
		}

		for (int x = 0; x < ite; x++) {
			for (int removalLevel : removalLevels) {
				for (double trate : treerates) {
					for (double srate : splitrates) {
						for (int level : levels) {
							for (k = 100; k <= 100; k += 20) {
								for (double eps : epslist) {
									System.out.println(x + "th computing for k " + k + " level " + level
											+ " split rate " + srate + " tree rate " + trate + " removal level "
											+ removalLevel + " eps " + eps);
									BBSDiffPrivacy.computeBBSWithSynthesis(
											"dfoptimalKgrid-glr-k-" + k + "-level-" + level + "-r-" + srate + "-t-"
													+ trate + "-rl-" + removalLevel + "-eps-" + eps + "." + x + ".csv",
											"dfoptimalKgrid-glr-bbs-skyband-k-" + k + "-level-" + level + "-r-" + srate
													+ "-t-" + trate + "-rl-" + removalLevel + "-eps-" + eps + "." + x
													+ ".csv",
											k, "levelremoval", "kspaceoutput", new Comparison[] { Comparison.MIN, Comparison.MIN }, 1, null);
									System.out.println("finished computing for k " + k);
								}

							}
						}
					}

				}
			}
		}

		PrintWriter out = new PrintWriter("fmeasure/optimalK-levelremoval-geometric.csv");
		out.println(
				"k,eps, max level, split rate, tree rate, removal level, true coutn, private count,tp,fp,fn,precision,recall,fmeasure,totalerror,avgerror");
		for (k = 100; k <= 100; k += 20) {
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

									String truefile = "data/" + "15-clusters.dat" + "-BNL-k-" + k + ".csv";
									String privatefile = "levelremoval/" + "dfoptimalKgrid-glr-bbs-skyband-k-" + k
											+ "-level-" + level + "-r-" + srate + "-t-" + trate + "-rl-" + removalLevel
											+ "-eps-" + eps + "." + o + ".csv";
									List<String> truelines = Files.readAllLines(Paths.get(truefile),
											Charset.defaultCharset());

									List<String> privatelines = Files.readAllLines(Paths.get(privatefile),
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
											double d = Math.abs(truetuple.getValue(0) - x)
													+ Math.abs(truetuple.getValue(1) - y);
											if (mind > d) {
												mind = d;
											}
										}
										totalerror += mind;
									}
									double avgerror = (totalerror / privateset.size()) / 1000000.0;
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

								out.println(k + "," + eps + "," + level + "," + srate + "," + trate + "," + removalLevel
										+ "," + t_truesize / ite + "," + t_privsize / ite + "," + t_tp / ite + ","
										+ t_fp / ite + "," + t_fn / ite + "," + t_precision / ite + "," + t_recall / ite
										+ "," + t_fmeasure / ite + "," + t_totalerror / ite + "," + t_avgerror / ite);
							}
						}
					}
				}
			}

		}
		out.close();

		PrintWriter out2 = new PrintWriter("fmeasure/optimalK-levelremoval-geometric-grid.csv");
		out2.println(
				"k,eps, max level, split rate, tree rate, removal level, true coutn, private count,tp,fp,fn,precision,recall,fmeasure,totalerror,avgerror");
		double winterval = dw * err;
		double hinterval = dh * err;
		for (k = 100; k <= 100; k += 20) {
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

									String truefile = "data/" + "15-clusters.dat" + "-BNL-k-" + k + ".csv";
									String privatefile = "levelremoval/" + "dfoptimalKgrid-glr-bbs-skyband-k-" + k
											+ "-level-" + level + "-r-" + srate + "-t-" + trate + "-rl-" + removalLevel
											+ "-eps-" + eps + "." + o + ".csv";
									List<String> truelines = Files.readAllLines(Paths.get(truefile),
											Charset.defaultCharset());

									List<String> privatelines = Files.readAllLines(Paths.get(privatefile),
											Charset.defaultCharset());

									HashSet<Tuple> trueset = new HashSet<>();
									for (int i = 0; i < truelines.size(); i++) {
										String[] line = truelines.get(i).split(",");
										double x = Double.parseDouble(line[0]);
										double y = Double.parseDouble(line[1]);
										int xi = (int) (x / winterval) + 1;
										int yi = (int) (y / hinterval) + 1;

										trueset.add(new Tuple(new int[] { xi, yi }));
									}

									HashSet<Tuple> privateset = new HashSet<>();
									for (int i = 0; i < privatelines.size(); i++) {
										String[] line = privatelines.get(i).split(",");
										double x = Double.parseDouble(line[0]);
										double y = Double.parseDouble(line[1]);
										int xi = (int) (x / winterval) + 1;
										int yi = (int) (y / hinterval) + 1;
										privateset.add(new Tuple(new int[] { xi, yi }));
									}

									for (Tuple tuple : privateset) {
										double x = tuple.getValue(0);
										double y = tuple.getValue(1);
										boolean isTP = false;

										if (trueset.contains(tuple)) {
											isTP = true;
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
										if (privateset.contains(tuple)) {
											isFN = false;
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
									double avgerror = (totalerror / privateset.size()) / 1000000.0;
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

								out2.println(k + "," + eps + "," + level + "," + srate + "," + trate + ","
										+ removalLevel + "," + t_truesize / ite + "," + t_privsize / ite + ","
										+ t_tp / ite + "," + t_fp / ite + "," + t_fn / ite + "," + t_precision / ite
										+ "," + t_recall / ite + "," + t_fmeasure / ite + "," + t_totalerror / ite + ","
										+ t_avgerror / ite);
							}
						}
					}
				}
			}

		}

		out2.close();

	}
}

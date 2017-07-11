package result;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import kSpacePartition.Param;
import skyband.Tuple;

public class FMeasureComputationTest {

	class DomainStats {
		public double xmin;
		public double xmax;
		public double ymin;
		public double ymax;
		public String truefile;
		public String privfile;
		public String name;

	}

	class Method {

		public Method(String name, int minsize) {
			this.name = name;
			this.minsize = minsize;
		}

		public String name;
		public int minsize = 32;
	}

	@Test
	public void testFMeasure() throws Exception {
		int[] ks = new int[] { 20, 40, 60, 80, 100, 150, 200 };
		int[] levels = new int[] { 7 };
		int[] minsizes = new int[] { 32 };
		double[] epslist = new double[] { 0.1, 0.5, 1, 2 };
		double[] splitrates = new double[] { 0.1 };
		int delta = 1;
		double[] treerates = new double[] { 1 };
		int[] removalLevels = new int[] { 6 };

		Param.XMIN = 32375.473;
		Param.XMAX = 1000000;
		Param.YMIN = 51380.725;
		Param.YMAX = 1000000;
		double dw = Param.XMAX - Param.XMIN;
		double dh = Param.YMAX - Param.YMIN;
		double err = 0.03;
		int ite = 10;
		int[] kdeltas = new int[] { 0 };

		HashMap<String, DomainStats> methodMap = new HashMap<>();
		DomainStats fifteenclusters = new DomainStats();
		fifteenclusters.xmin = 32375.473;
		fifteenclusters.xmax = 1000000;
		fifteenclusters.ymin = 51380.725;
		fifteenclusters.ymax = 1000000;
		fifteenclusters.truefile = "data/" + "15-clusters.dat";
		fifteenclusters.privfile = "dfoptimalKgrid-nglr-bbs-skyband-k-";
		fifteenclusters.name = "15clusters";

		DomainStats anti = new DomainStats();
		anti.xmin = 529;
		anti.xmax = 1000000;
		anti.ymin = 339;
		anti.ymax = 1000000;
		anti.truefile = "dataset/" + "anti10k.csv";
		anti.privfile = "dfoptimalKgrid-nglr-bbs-skyband-k-";
		anti.name = "anti";

		DomainStats corr = new DomainStats();
		corr.xmin = 11680;
		corr.xmax = 1000000;
		corr.ymin = 13218;
		corr.ymax = 1000000;
		corr.truefile = "dataset/" + "correlated10k.csv";
		corr.privfile = "dfoptimalKgrid-nglr-bbs-skyband-k-";
		corr.name = "corr";

		DomainStats normal = new DomainStats();
		normal.xmin = 43;
		normal.xmax = 1000000;
		normal.ymin = 80;
		normal.ymax = 1000000;
		normal.truefile = "dataset/" + "normal10k.csv";
		normal.privfile = "dfoptimalKgrid-nglr-bbs-skyband-k-";
		normal.name = "normal";

		DomainStats covtype = new DomainStats();
		covtype.xmin = 1859;
		covtype.xmax = 3858 + 1;
		covtype.ymin = 0;
		covtype.ymax = 7117 + 1;
		covtype.truefile = "dataset/" + "covtype.csv";
		covtype.privfile = "dfoptimalKgrid-nglr-bbs-skyband-k-";
		covtype.name = "covtype";

		DomainStats nba = new DomainStats();
		nba.xmin = 0;
		nba.xmax = 3156 + 1;
		nba.ymin = 0;
		nba.ymax = 1449 + 1;
		nba.truefile = "dataset/" + "nba.csv";
		nba.privfile = "dfoptimalKgrid-nglr-bbs-skyband-k-";
		nba.name = "nba";

		DomainStats fan = new DomainStats();
		fan.xmin = 0;
		fan.xmax = 5000;
		fan.ymin = 0;
		fan.ymax = 5000;
		fan.truefile = "dataset/" + "fan.csv";
		fan.privfile = "dfoptimalKgrid-nglr-bbs-skyband-k-";
		fan.name = "fan";
		
		DomainStats fanuniform = new DomainStats();
		fan.xmin = 0;
		fan.xmax = 5000;
		fan.ymin = 0;
		fan.ymax = 5000;
		fan.truefile = "dataset/" + "fan-uniform.csv";
		fan.privfile = "dfoptimalKgrid-nglr-bbs-skyband-k-";
		fan.name = "fan-uniform";

		Method noisynegative = new Method("noisynegative", 8);
		Method kdtree = new Method("kdtree", 32);
		Method quadtree = new Method("quadtree", 32);
		Method multidim = new Method("multidim", 8);

		Method[] methods = new Method[] { noisynegative, kdtree, quadtree };
		DomainStats[] datasets = new DomainStats[] { fanuniform };

		for (Method method : methods) {
			for (DomainStats ds : datasets) {
				dw = ds.xmax - ds.xmin;
				dh = ds.ymax - ds.ymin;

				for (double eps : epslist) {
					PrintWriter out = new PrintWriter("fmeasure/" + method.name
							+ "/" + ds.name + "-geometric" + "-eps-" + eps
							+ ".csv");
					out.println("k,eps,minsize, kdetal, max level, split rate, tree rate, removal level, true coutn, private count,tp,fp,fn,precision,recall,fmeasure,totalerror,avgerror");
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

													String truefile = ds.truefile
															+ "-BNL-k-"
															+ k
															+ ".csv";
													String privatefile = method.name
															+ "/"
															+ ds.name
															+ "/"
															+ ds.privfile
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
															+ method.minsize
															+ "-kdelta-"
															+ kdelta
															+ "-eps-"
															+ eps
															+ "."
															+ o
															+ ".csv";
													if (method.name
															.equals("kdtree")) {
														privatefile = method.name
																+ "/"
																+ ds.name
																+ "/"
																+ ds.privfile
																+ k
																+ "-level-"
																+ 0
																+ "-r-"
																+ 0.0
																+ "-t-"
																+ 0.0
																+ "-rl-"
																+ 0
																+ "-min-"
																+ 0
																+ "-kdelta-"
																+ 0
																+ "-eps-"
																+ eps
																+ "."
																+ o
																+ ".csv";
													}

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
																.get(i).split(
																		",");
														double x = Double
																.parseDouble(line[0]);
														double y = Double
																.parseDouble(line[delta]);
														trueset.add(new Tuple(
																new double[] {
																		x, y }));
													}

													HashSet<Tuple> privateset = new HashSet<>();
													for (int i = 0; i < privatelines
															.size(); i++) {
														String[] line = privatelines
																.get(i).split(
																		",");
														double x = Double
																.parseDouble(line[0]);
														double y = Double
																.parseDouble(line[delta]);
														privateset
																.add(new Tuple(
																		new double[] {
																				x,
																				y }));
													}

													for (Tuple tuple : privateset) {
														double x = tuple
																.getValue(0);
														double y = tuple
																.getValue(delta);
														boolean isTP = false;
														for (Tuple truetuple : trueset) {
															if (Math.abs(truetuple
																	.getValue(0)
																	- x) < err
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
														double x = tuple
																.getValue(0);
														double y = tuple
																.getValue(delta);
														boolean isFN = true;
														for (Tuple privtuple : privateset) {
															if (Math.abs(privtuple
																	.getValue(0)
																	- x) < err
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
														double x = tuple
																.getValue(0);
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
															.size())
															/ (dw + dh);
													precision = tp / (tp + fp);
													recall = tp / (tp + fn);
													fmeasure = 2
															* (precision * recall)
															/ (precision + recall);
													System.out.println("k: "
															+ k + " eps: "
															+ eps);
													System.out
															.println("trueset: "
																	+ trueset
																			.size());
													System.out
															.println("privateset: "
																	+ privateset
																			.size());
													System.out.println("tp:"
															+ tp);
													System.out.println("fp:"
															+ fp);
													System.out.println("fn:"
															+ fn);
													System.out
															.println("precision: "
																	+ precision);
													System.out
															.println("recall: "
																	+ recall);
													System.out
															.println("fmeasure: "
																	+ fmeasure);
													System.out
															.println("total error: "
																	+ totalerror);
													System.out
															.println("avg error: "
																	+ avgerror
																	* 100 + "%");
													t_truesize += trueset
															.size();
													t_privsize += privateset
															.size();
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
														+ minsize + ","
														+ kdelta + "," + level
														+ "," + srate + ","
														+ trate + ","
														+ removalLevel + ","
														+ t_truesize / ite
														+ ","
														+ t_privsize / ite
														+ "," + t_tp / ite
														+ "," + t_fp / ite
														+ "," + t_fn / ite
														+ "," + t_precision
														/ ite + "," + t_recall
														/ ite + ","
														+ t_fmeasure / ite
														+ "," + t_totalerror
														/ ite + ","
														+ t_avgerror / ite);
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

		}
	}

}

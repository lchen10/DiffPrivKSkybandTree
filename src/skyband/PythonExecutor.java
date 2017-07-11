package skyband;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map.Entry;

public class PythonExecutor {

	/**
	 * @param args
	 * @throws Exception
	 * @throws Exception
	 */

	public static void ExecuteKDTree(String outputfile, double eps, String inputfile, double xmin, double ymin,
			double xmax, double ymax) throws Exception {
		// String folder =
		// "/Users/lingchen/Documents/BitBucket/TopkDominatingQueryDiffPrivate/Code/KD-TreeGenerationBeingCalledForUsefulness/src";
		String folder = "../kdtree";
		// String outputfile =
		// "\"E:/D_disk/workspace/Python
		// Workspace/KD-TreeGenerationBeingCalledForUsefulness/KDTreeOutput.txt\"";
		// int eps = 1;
		// String inputfile = "dataset/tiger_NMWA.dat";

		String command = "python MainExp.py" + " " + outputfile + " " + eps + " " + inputfile + " " + xmin + " " + ymin
				+ " " + xmax + " " + ymax;

		Process tr = Runtime.getRuntime().exec(command, null, new File(folder));
		BufferedReader rd = new BufferedReader(new InputStreamReader(tr.getInputStream()));

		String line = null;
		while ((line = rd.readLine()) != null) {
			System.out.println(line);

		}

		BufferedReader rd2 = new BufferedReader(new InputStreamReader(tr.getErrorStream()));

		line = null;
		while ((line = rd2.readLine()) != null) {
			System.out.println(line);

		}
	}

	public static void ExecuteAdaptiveGrid(String outputfile, double eps, String inputfile, double dw, double dh)
			throws Exception {
		String folder = "C:\\Users\\xsxiao\\JavaWorkspace\\AdaptiveGridDiffGen\\";
		// String outputfile =
		// "\"E:/D_disk/workspace/Python
		// Workspace/KD-TreeGenerationBeingCalledForUsefulness/KDTreeOutput.txt\"";
		// int eps = 1;
		// String inputfile = "dataset/tiger_NMWA.dat";

		String command = "python flat_origin.py" + " " + inputfile + " " + eps + " " + outputfile + " " + dw + " " + dh;

		Process tr = Runtime.getRuntime().exec(command, null, new File(folder));
		BufferedReader rd = new BufferedReader(new InputStreamReader(tr.getInputStream()));

		String line = null;
		while ((line = rd.readLine()) != null) {
			System.out.println(line);

		}

		BufferedReader rd2 = new BufferedReader(new InputStreamReader(tr.getErrorStream()));

		line = null;
		while ((line = rd2.readLine()) != null) {
			System.out.println(line);

		}
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// String inputfile = "15-clusters.dat";
		// int eps = 2;
		// String outputfile =
		// "/Users/lingchen/Documents/workspace/AdaptiveGridGenerationBeingCalledByJava/data_grids/15-clusters-budget-"
		// + eps + ".txt";
		// ExecuteAdaptiveGrid(outputfile, eps, inputfile);

		double[] epslist = new double[] { 0.1, 0.5, 1.0, 2.0 };
//		String[] files = new String[] { "covtype.dat", "nba.dat", "anti10k.dat", "15-clusters.dat", "normal10k.dat",
//				"correlated10k.dat" };
		int ite = 10;

		HashMap<String, double[]> params = new HashMap<String, double[]>();
//		params.put("15-clusters.dat", new double[] { 32375.473, 51380.725, 1000000.0, 1000000.0, });
//		params.put("anti10k.dat", new double[] { 529, 339, 1000000.0, 1000000.0, });
//		params.put("correlated10k.dat", new double[] { 11680, 13218, 1000000.0, 1000000.0, });
//		params.put("normal10k.dat", new double[] { 43, 80, 1000000.0, 1000000.0, });
//		params.put("covtype.dat", new double[] { 1859, 0, 3859, 7118 });
//		params.put("nba.dat", new double[] { 0, 0, 3157, 1450, });
//		params.put("fan.dat", new double[] { 0, 0, 5000, 5000, });
//		params.put("covtype-8k.dat", new double[] {1859, 0, 3859, 7118 });
		params.put("fan-uniform.dat", new double[] { 0, 0, 5000, 5000, });

		for (int x = 0; x < ite; x++) {
			for (Entry<String, double[]> e : params.entrySet()) {
				String file = e.getKey();
				for (double eps : epslist) {
					// String outputfile =
					// "C:\\Users\\xsxiao\\JavaWorkspace\\kskylineband\\kdtree_data\\"
					// + file.split("\\.")[0]
					// + "-eps-"
					// + eps
					// + "."
					// + x
					// + ".txt";
					// ExecuteKDTree(outputfile, eps, file, e.getValue()[0],
					// e.getValue()[1]);
					String outputfile = "temp/" + file.split("\\.")[0] + "-eps-" + eps + "." + x + ".txt";
					ExecuteKDTree(outputfile, eps, file, e.getValue()[0], e.getValue()[1], e.getValue()[2],
							e.getValue()[3]);

					// ExecuteAdaptiveGrid(outputfile, eps, file,
					// e.getValue()[0],
					// e.getValue()[1]);
				}
			}
		}

	}
}

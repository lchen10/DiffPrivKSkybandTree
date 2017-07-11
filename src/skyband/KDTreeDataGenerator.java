package skyband;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class KDTreeDataGenerator {

	public static void main(String[] args) throws IOException {

		double[] epslist = new double[] { 2 };
		String[] files = new String[] { 
				"15-clusters.dat", };

		PrintWriter perfout = new PrintWriter("perf/15-clusters-synthesis.csv");
		perfout.println("synthesis");
		for (double eps : epslist) {
			for (int j = 0; j < 10; j++) {
				for (String file : files) {
					String filename = file.split("\\.")[0];
					FileInputStream in = new FileInputStream(
							"kdtree_data/" + filename + "-eps-" + eps + "." + j
									+ ".txt");
					BufferedReader br = new BufferedReader(
							new InputStreamReader(in));

					BufferedWriter out = null;
					// Create file
					String outputfilename = "temp/" + filename + "-eps-"
							+ eps + "-" + j + ".csv";
					FileWriter fstream = new FileWriter(
							outputfilename);

					System.out.println("Synthesizing " + outputfilename);
					
					double delta = 0.01;
					out = new BufferedWriter(fstream);

					String strLine = null;
					double totaltime = 0.0;
					ArrayList<Grid> grids = new ArrayList<>(); 
					HashMap<String, Grid> map = new HashMap<>();
					while ((strLine = br.readLine()) != null) {

						strLine = strLine.trim();
						String[] mystring = strLine.split(",");
						// System.out.println("mystring length is: " + mystring.length);

						if (mystring.length < 8) {
							// System.out.println("test");
							continue;
						}

						double count_double = Double.parseDouble(mystring[0].trim());
						double xmin = Double.parseDouble(mystring[1].trim());
						double ymin = Double.parseDouble(mystring[2].trim());
						double xmax = Double.parseDouble(mystring[3].trim());
						double ymax = Double.parseDouble(mystring[4].trim());
						int level = (int) Double.parseDouble(mystring[5].trim());
						String id = mystring[6].trim();
						String parentid = mystring[7].trim();
						level = (int) Double.parseDouble(mystring[8].trim());
						Grid grid = new Grid();
						grid.xmin = xmin;
						grid.xmax = xmax;
						grid.ymin = ymin;
						grid.ymax = ymax;
						grid.count = count_double;
						grid.id = id;
						grid.level = level;
						map.put(id, grid);
						if (!parentid.equals("null")) {
							grid.parent = map.get(parentid);
							grid.parent.children.add(grid);
						}
						grids.add(grid);
					}
					
					ArrayList<Grid> leaves = new ArrayList<>(); 
					for (Grid grid : grids) {
						if(grid.children.size() == 0){
							leaves.add(grid);
						}
					}
					ArrayList<Tuple> points = new ArrayList<>();
					for (Grid grid : leaves) {
						long starttime = System.currentTimeMillis();
						if (grid.count > 0) {
							for (int i = 0; i < grid.count; i++) {
								Tuple t = UniformRandom.getRandomTuple(grid.xmin,
										grid.xmax, grid.ymin, grid.ymax, delta);
								points.add(t);

//								double x = t.getValue(0);
//								double y = t.getValue(1);
//								out.write(x + ", " + y);
//								out.newLine();
							}
						}
						long endtime = System.currentTimeMillis();
						
						totaltime+=(((double)endtime-starttime)/1000.0);
					}
					System.out.println("synthesized points: " + points.size());
					in.close();
					out.flush();
					out.close();
					perfout.println(totaltime);
				}
				
			}
		}
		perfout.close();
	}

}

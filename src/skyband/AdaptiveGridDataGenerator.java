package skyband;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class AdaptiveGridDataGenerator {

	public static void main(String[] args) throws IOException {

		double[] epslist = new double[] { 0.1, 0.5, 1, 2 };
		String[] files = new String[] { 
				"covtype.dat", "nba.dat", };

		for (double eps : epslist) {
			for (int j = 0; j < 10; j++) {
				for (String file : files) {
					String filename = file.split("\\.")[0];
					FileInputStream in = new FileInputStream(
							"adaptiveGrid/" + filename + "-eps-" + eps + "." + j
									+ ".txt");
					BufferedReader br = new BufferedReader(
							new InputStreamReader(in));

					BufferedWriter out = null;
					// Create file
					String outputfilename = "adaptiveGrid_data/" + filename + "-eps-"
							+ eps + "-" + j + ".csv";
					FileWriter fstream = new FileWriter(
							outputfilename);

					System.out.println("Synthesizing " + outputfilename);
					
					double delta = 0.01;
					out = new BufferedWriter(fstream);

					String strLine = null;

					while ((strLine = br.readLine()) != null) {

						strLine = strLine.trim();
						String[] mystring = strLine.split(",");
						// System.out.println("mystring length is: " +
						// mystring.length);

						if (mystring.length < 5) {
							// System.out.println("test");
							continue;
						}
						// If all the values are interger
						// int count = Integer.parseInt(mystring[0]);
						// int xmin = Integer.parseInt(mystring[1]);
						// int ymin = Integer.parseInt(mystring[2]);
						// int xmax = Integer.parseInt(mystring[3]);
						// int ymax = Integer.parseInt(mystring[4]);

						// if all the values are double
						double count_double = Double.parseDouble(mystring[0]);
						int count = (int) Math.round(count_double);
						double xmin = Double.parseDouble(mystring[1]);
						double ymin = Double.parseDouble(mystring[2]);
						double xmax = Double.parseDouble(mystring[3]);
						double ymax = Double.parseDouble(mystring[4]);

						// int cluster_label = Integer.parseInt(mystring[5]);

						if (count > 0) {
							for (int i = 0; i < count; i++) {
								Tuple t = UniformRandom.getRandomTuple(xmin,
										xmax, ymin, ymax, delta);

								// out.write(t.toString() + "C" +
								// cluster_label);
								double x = t.getValue(0);
								double y = t.getValue(1);
								out.write(x + ", " + y);
								out.newLine();
								// System.out.println(t.toString());
							}
						}

					}
					in.close();
					out.flush();
					out.close();
				}

			}
		}

	}

}

package skyband;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PrivateTopKDom {

	/**
	 * @param args
	 */

	public static boolean isDebug = false;

	private static void debug(String text) {
		if (isDebug) {
			System.out.println("DEBUG: " + text);
		}
	}

	public static List<Tuple> CalculateTopKDominating(List<Tuple> group,
			List<Comparison> comparisons, int k) throws Exception {
		List<Tuple> privateResults = new ArrayList<Tuple>();
		List<Tuple> trueResults = new ArrayList<Tuple>();
		List<Tuple> currentGroup = new ArrayList<Tuple>(group);

		for (int i = 0; i < k; i++) {

			List<DistTuple> distTuple = new ArrayList<DistTuple>();
			distTuple.add(new DistTuple(new Tuple(), 0.0));

			Tuple[] skylines = BNL.computeSkyline(currentGroup, comparisons);
//			debug("skyline tuples: " + Arrays.asList(skylines));

			for (Tuple skylineTuple : skylines) {
				skylineTuple.dominatingCount = 0;
				for (Tuple currentTuple : currentGroup) {
					if (skylineTuple.dominate(currentTuple, comparisons) == 1)
						skylineTuple.dominatingCount++;
				}

			}

			Tuple maxTuple = skylines[0];
			for (int j = 1; j < skylines.length; j++) {
				if (skylines[j].dominatingCount > maxTuple.dominatingCount) {
					maxTuple = skylines[j];
				}
			}
//			debug("Dominating [" + maxTuple.dominatingCount + "]: " + maxTuple);

			if (!currentGroup.remove(maxTuple)) {
				throw new Exception("Cannot remove " + maxTuple);
			}

			// calculate the distance between each dominated point with the
			// top-i result
			// add them to DistTuples
			for (int m = 0; m < currentGroup.size(); m++) {
				double distance = Distance(currentGroup.get(m), maxTuple);
				distTuple.add(new DistTuple(currentGroup.get(m), distance));
			}

			// sort all the tuples according to their distance to cluster center
			Collections.sort(distTuple, new DistTupleComparator());
			debug("sorted: " + distTuple);

			double eps = 1 / (Math.log(1 + 1 * (Math.exp(1) - 1)));
			debug("eps: " + eps);

			double base = Math.exp(-eps / 2);
			debug("base: " + base);

			double[] probSum = new double[distTuple.size()];
			for (int n = 1; n < probSum.length; n++) {
				double t1 = distTuple.get(n).getDist();
				// double t2 = distTuple.get(i-1).getDist();
				// probSum[i] = probSum[i-1] + (t1-t2) * (Math.pow(base, t1));
				probSum[n] = probSum[n - 1] + (2 * Math.PI * t1)
						* (Math.pow(base, n));
			}

			debug("probSum is: " + probSum[probSum.length - 1] + "\n");

			// a random number in [0, 1)
			double randTemp = Math.random();
			debug("rand number is: " + randTemp);

			double rand = randTemp * probSum[probSum.length - 1];
			debug("rand: " + rand);
			int idx = 0;

			// idx is the index of the bucket which "rand" falls into
			for (int s = 0; s < probSum.length; s++) {
				if (probSum[s] > rand) {
					idx = s;
					break;
				}
			}

			//Tuple SelectedTuple = distTuple.get(idx).getTuple();
			double radius = distTuple.get(idx).getDist();
			
			double x = maxTuple.getValue(0);
			double y = maxTuple.getValue(1);
			debug("=========PRIVATE found: " + distTuple.get(idx));
			debug("radiius is: " + radius);
			debug("x is: " + x);
			debug("y is: " + y);

			Random generator = new Random();
			double angle = generator.nextDouble() * 360;

			debug("Angle is: " + angle);

			angle = angle * 2.0 * Math.PI / 360.0;
			debug("Angle is: " + angle);

//			System.out.println("sin(" + angle + ") is " + Math.sin(angle));
//			System.out.println("cos(" + angle + ") is " + Math.cos(angle));

			double randx = x + radius * Math.cos(angle);
			double randy = y + radius * Math.sin(angle);

			debug("======randx is: " + randx);
			debug("======randy is: " + randy);

			Tuple PrivateTuple = new Tuple(new double[] { randx, randy });
			trueResults.add(maxTuple);
			privateResults.add(PrivateTuple);
		}

		for(int i = 0; i < privateResults.size(); i++)
		{
			currentGroup.add(privateResults.get(i));
		}
		
		for (Tuple privateTuple : privateResults) {
			privateTuple.dominatingCount = 0;
			for (Tuple currentTuple : currentGroup) {
				if(privateTuple.dominate(currentTuple, comparisons) == 1)
					privateTuple.dominatingCount++;
			}
			
		}
		
//		BufferedWriter out = null;
//		
//		try {
//			// Create file
//			FileWriter fstream = new FileWriter("E:/anti-5k-out-10.txt");
//			out = new BufferedWriter(fstream);
//			out.write("The data is in 2 columns: top-k dominating result | its dominating count");
//			out.newLine();
//			out.write("=============TrueResults==================");
//			out.newLine();
//			
//			for(Tuple trueTuple : trueResults)
//			{
//				out.write( trueTuple.toString() + ", " + trueTuple.dominatingCount);
//				out.newLine();
//			}
//			
//			out.write("=============PrivateResults==================");
//			out.newLine();
//			
//			for(Tuple privateTuple : privateResults)
//			{
//				out.write(privateTuple.toString() + ", " + privateTuple.dominatingCount);
//				out.newLine();
//			}
//			
//		} catch (Exception e) {// Catch exception if any
//			System.err.println("Error: " + e.getMessage());
//		}
//		
//		out.close();
//		
		
		
		System.out.println("=============TrueResults==================");
		for(Tuple trueTuple : trueResults)
		{
			System.out.println("true resulst is: " + trueTuple.toString() + ", " + trueTuple.dominatingCount);
		}
		
		System.out.println("=============PrivateResults==================");	
		for(Tuple privateTuple : privateResults)
		{
			System.out.println("private resulst is: " + privateTuple.toString() + ", " + privateTuple.dominatingCount);
		}
		
		
		return privateResults;
	}

	public static double Distance(Tuple t1, Tuple t2) throws Exception {
		double dist = 0.0;
		if (t1.size() != t2.size()) {
			throw new Exception("t1.size does not equal with t2.size!");
		}

		int dim = t1.size();

		for (int i = 0; i < dim; i++) {
			dist += Math.pow((t1.getValue(i) - t2.getValue(i)), 2);
		}

		dist = Math.sqrt(dist);

		return dist;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

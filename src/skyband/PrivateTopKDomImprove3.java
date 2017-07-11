package skyband;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class PrivateTopKDomImprove3 {

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

		List<Tuple> trueResults = new ArrayList<Tuple>();
		List<Tuple> privateResults = new ArrayList<Tuple>();
		List<Tuple> currentGroup = new ArrayList<Tuple>(group);

		for (Tuple tuple : currentGroup) {
			tuple.dominatingCount = 0;
	//		System.out.println("the tuple is: " + tuple.toString());
			for (Tuple currentTuple : currentGroup) {
				if (tuple.dominate(currentTuple, comparisons) == 1)
					tuple.dominatingCount++;
			}
//			System.out.println("the tuple dominting count is: "
//					+ tuple.dominatingCount);

		}
		Collections.sort(currentGroup, new TupleComparator());

		for (int i = 0; i < k; i++) {
			trueResults.add(currentGroup.get(i));
		}

		for (int j = 0; j < k; j++) {
			int trueCount = trueResults.get(j).dominatingCount;

			List<DomCountDiffTuple> domCountDiffTuple = new ArrayList<DomCountDiffTuple>();
			domCountDiffTuple.add(new DomCountDiffTuple(new Tuple(), 0));

			for (int n = 0; n < currentGroup.size(); n++) {
				// double distance = Distance(group.get(k), mean);

				int countAbsDiff = Math.abs(currentGroup.get(n).dominatingCount
						- trueCount);
				domCountDiffTuple.add(new DomCountDiffTuple(
						currentGroup.get(n), countAbsDiff));
			}

			// double t1 =
			// Math.abs(currentGroup.get(n).dominatingCount-trueCount);
			// sort all the tuples according to their distance to cluster center
			Collections.sort(domCountDiffTuple,
					new DomCountDiffTupleComparator());

			double eps = 1 / (Math.log(1 + 1 * (Math.exp(1) - 1)));
			debug("eps: " + eps);

			double base = Math.exp(-eps / 2);
			debug("base: " + base);

			double[] probSum = new double[domCountDiffTuple.size()];
			for (int n = 1; n < probSum.length; n++) {
			//	int t1 = domCountDiffTuple.get(n).getDomCountDiff();
				// double t2 = distTuple.get(i-1).getDist();
				// probSum[i] = probSum[i-1] + (t1-t2) * (Math.pow(base, t1));
				probSum[n] = probSum[n - 1] + Math.pow(base, n);
				
				if (n == 1)
				{
					debug("probSum for n = 1 is: " + Math.pow(base, n) + "\n");
				}
				
				if (n == 2)
				{
					debug("probSum for n = 2 is: " + Math.pow(base, n) + "\n");
				}
				
				if (n == 3)
				{
					debug("probSum for n = 3 is: " + Math.pow(base, n) + "\n");
				}
				
				if (n == 4)
				{
					debug("probSum for n = 4 is: " + Math.pow(base, n) + "\n");
				}
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

	//		int privateCount = domCountDiffTuple.get(idx).getDomCountDiff();

//			System.out.println("private top-" + k + " dominating result is: "
//					+ domCountDiffTuple.get(idx).getTuple() + ", "
//					+ domCountDiffTuple.get(idx).getTuple().dominatingCount);

			privateResults.add(domCountDiffTuple.get(idx).getTuple());
		}
		
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

class TupleComparator implements Comparator<Tuple> {

	@Override
	public int compare(Tuple x, Tuple y) {
		// TODO Auto-generated method stub

		// return Double.compare(x.getDist(), y.getDist());
		return Integer.valueOf(y.dominatingCount).compareTo(x.dominatingCount);
	}

}

class DomCountDiffTupleComparator implements Comparator<DomCountDiffTuple> {

	@Override
	public int compare(DomCountDiffTuple x, DomCountDiffTuple y) {
		// TODO Auto-generated method stub

		return Integer.valueOf(x.getDomCountDiff()).compareTo(
				y.getDomCountDiff());
	}

}

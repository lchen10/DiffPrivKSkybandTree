package skyband;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BNL {

	public static boolean isDebug = false;
	public static boolean writeToFile = false;

	public static Tuple[] computeSkyline(Tuple[] tuples) {
		// TODO Auto-generated method stub
		return computeSkyline(tuples, null);
	}

	public static Tuple[] removeFirst(Tuple[] array) {
		if (array.length <= 1) {
			return new Tuple[0];
		}

		Tuple[] result = new Tuple[array.length - 1];
		for (int i = 1; i < array.length; i++) {
			result[i - 1] = array[i];
		}
		return result;
	}

	public static Tuple[] computeSkyline(Tuple[] tuples, Comparison[] comparisons) {

		Tuple[] tuplesToBeComputed = removeFirst(tuples);
		Tuple[] initialSkylineTuples = new Tuple[] { tuples[0] };
		return computeSkyline(tuplesToBeComputed, initialSkylineTuples, comparisons);
	}

	public static Tuple[] computeKSkylineBand(Tuple[] tuples, Comparison[] comparisons, int k) {

		Tuple[] tuplesToBeComputed = removeFirst(tuples);
		Tuple[] initialSkylineTuples = new Tuple[] { tuples[0] };
		return computeKSkylineBand(tuplesToBeComputed, initialSkylineTuples, comparisons, k);
	}
	
	public static Tuple[] computeKSkylineBand(List<Tuple> tuplesTobeComputed, List<Tuple> partialSkyline,
			List<Comparison> comparisons, int k) {
		Comparison[] comparisonArray = comparisons.toArray(new Comparison[comparisons.size()]);
		Tuple[] tuplesTobeComputedArray = tuplesTobeComputed.toArray(new Tuple[tuplesTobeComputed.size()]);
		if (partialSkyline == null) {
			return computeKSkylineBand(tuplesTobeComputedArray, new Tuple[0], comparisonArray,k);
		}
		return computeKSkylineBand(tuplesTobeComputedArray, partialSkyline.toArray(new Tuple[partialSkyline.size()]),
				comparisonArray,k);

	}

	public static Tuple[] computeKSkylineBand(Tuple[] tuplesToBeComputed, Tuple[] initialSkylineTuples,
			Comparison[] comparisons, int k) {

		debug("============");
		debug("Start calculate skyline");
		List<Tuple> result = new ArrayList<Tuple>();
		debug("Add initialSkylineTuples...");
		for (Tuple t : initialSkylineTuples) {
			debug("add " + t);
			result.add(t);
		}

		for (int i = 0; i < tuplesToBeComputed.length; i++) {
			Tuple tobeChecked = tuplesToBeComputed[i];
			boolean needToAdd = true;
			for (int j = 0; j < result.size(); j++) {
				Tuple current = result.get(j);
				debug("current: " + current);
				debug("tobeChecked: " + tobeChecked);
				int dominanceValue = current.dominate(tobeChecked, comparisons);
				debug("dominanceValue: " + dominanceValue);
				if (dominanceValue == 1) {
					debug("check next tuple");
					tobeChecked.dominatedCount++;
					if (tobeChecked.dominatedCount > k) {
						needToAdd = false;
						break;
					}
				}
				if (tobeChecked.dominate(current, comparisons) == 1) {
					debug(tobeChecked + " dominate " + current);
					current.dominatedCount++;
					if (current.dominatedCount > k) {
						debug("removing " + current);
						result.remove(j);
						j--;
					}
				}
			}
			if (needToAdd) {
				debug("Add " + tobeChecked);
				result.add(tobeChecked);
			}
		}
		debug("End");
		return (Tuple[]) result.toArray(new Tuple[result.size()]);
	}

	private static void debug(String text) {
		if (isDebug) {
			System.out.println(text);
		}
		if (writeToFile) {
			BufferedWriter bw = null;

			try {
				bw = new BufferedWriter(new FileWriter("temp", true));
				bw.write(text);
				bw.flush();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally { // always close the file
				if (bw != null)
					try {
						bw.close();
					} catch (IOException ioe2) {
						// just ignore it
					}
			}
		}
	}

	public static Tuple[] computeSkyline(Tuple[] tuplesToBeComputed, Tuple[] initialSkylineTuples,
			Comparison[] comparisons) {
		// TODO Auto-generated method stub
		debug("============");
		debug("Start calculate skyline");
		List<Tuple> result = new ArrayList<Tuple>();
		debug("Add initialSkylineTuples...");
		for (Tuple t : initialSkylineTuples) {
			debug("add " + t);
			result.add(t);
		}

		for (int i = 0; i < tuplesToBeComputed.length; i++) {
			Tuple tobeChecked = tuplesToBeComputed[i];
			boolean needToAdd = true;
			for (int j = 0; j < result.size(); j++) {
				Tuple current = result.get(j);
				debug("current: " + current);
				debug("tobeChecked: " + tobeChecked);
				int dominanceValue = current.dominate(tobeChecked, comparisons);
				debug("dominanceValue: " + dominanceValue);
				if (dominanceValue == 1 || dominanceValue == 0) {
					debug("check next tuple");
					needToAdd = false;
					break;
				}
				if (tobeChecked.dominate(current, comparisons) == 1) {
					debug(tobeChecked + " dominate " + current);
					debug("removing " + current);
					result.remove(j);
					j--;
				}
			}
			if (needToAdd) {
				debug("Add " + tobeChecked);
				result.add(tobeChecked);
			}
		}
		debug("End");
		return (Tuple[]) result.toArray(new Tuple[result.size()]);
	}

	public static Tuple[] computeSkyline(List<Tuple> group, Comparison[] comparisons) {
		// TODO Auto-generated method stub
		return computeSkyline(group.toArray(new Tuple[group.size()]), comparisons);
	}

	public static Tuple[] computeSkyline(List<Tuple> group, List<Comparison> comparisons) {
		// TODO Auto-generated method stub
		return computeSkyline(group.toArray(new Tuple[group.size()]),
				comparisons.toArray(new Comparison[comparisons.size()]));
	}

	public static Tuple[] computeSkyline(List<Tuple> tuplesTobeComputed, List<Tuple> partialSkyline,
			List<Comparison> comparisons) {
		Comparison[] comparisonArray = comparisons.toArray(new Comparison[comparisons.size()]);
		Tuple[] tuplesTobeComputedArray = tuplesTobeComputed.toArray(new Tuple[tuplesTobeComputed.size()]);
		if (partialSkyline == null) {
			return computeSkyline(tuplesTobeComputedArray, new Tuple[0], comparisonArray);
		}
		return computeSkyline(tuplesTobeComputedArray, partialSkyline.toArray(new Tuple[partialSkyline.size()]),
				comparisonArray);

	}
}

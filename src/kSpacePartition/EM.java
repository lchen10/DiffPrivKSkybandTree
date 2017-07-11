package kSpacePartition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import kSpacePartition.MultiDim.TupleValueComparator;
import skyband.Comparison;
import skyband.Tuple;

public class EM {
	public static double getValueBasedOnRank(List<IDTuple> data, double eps, double x, int vindex, double min,
			double max) throws Exception {
		ArrayList<Tuple> sortedlist = new ArrayList<Tuple>();
		sortedlist.add(new Tuple(new double[] { min, min }));
		sortedlist.addAll(data);
		sortedlist.add(new Tuple(new double[] { max, max }));
		if (vindex == 0) {
			Collections.sort(sortedlist, new XTupleValueComparator(Comparison.MIN));
		} else if (vindex == 1) {
			Collections.sort(sortedlist, new YTupleValueComparator(Comparison.MIN));
		} else {
			throw new Exception("not supported vindex");
		}

		int rank = 0;
		for (Tuple t : sortedlist) {
			if (t.getValue(vindex) - x > -0.001
					&& t.getValue(vindex) - x < 0.001) {
				break;
			}
			rank++;
		}
		
//		System.out.println("rank: " + rank);

		double[] prob_sum = new double[sortedlist.size()];
		double base = 2.0;

		for (int i = 1; i < sortedlist.size(); i++) {
			int quality = Math.abs(i - rank);
			double prob = Math.exp(-1 * eps * quality / base);
			prob_sum[i] = prob_sum[i - 1] + prob;
//			System.out.println("rank" + i + " prob: " + prob);
		}

		double rand = Math.random() * prob_sum[sortedlist.size() - 1];
		int idx = 0;
		for (int i = 0; i < prob_sum.length; i++) {
			if (prob_sum[i] > rand) {
				idx = i;
				break;
			}
		}

		double uniform = Math.random();

		double left = sortedlist.get(idx - 1).getValue(vindex);
		double right = sortedlist.get(idx).getValue(vindex);
		double length = right - left;

		return left + length * uniform;
	}

	public static double getValueBasedOnRank(int dim, List<IDTuple> data, double eps, double x, int vindex, double min,
			double max) throws Exception {
		ArrayList<Tuple> sortedlist = new ArrayList<Tuple>();
		if (data.size() == 0) {
			double uniform = Math.random();
			double length = max - min;
			return min + length * uniform;
		}
		double[] mins = new double[dim];
		for (int i = 0; i < dim; i++) {
			mins[i] = min;
		}
		double[] maxs = new double[dim];
		for (int i = 0; i < dim; i++) {
			maxs[i] = max;
		}
		sortedlist.add(new Tuple(mins));
		sortedlist.addAll(data);
		sortedlist.add(new Tuple(maxs));
		Collections.sort(sortedlist, new TupleValueComparator(Comparison.MIN, vindex));

		int rank = 0;
		for (Tuple t : sortedlist) {
			if (t.getValue(vindex) - x > -0.001 && t.getValue(vindex) - x < 0.001) {
				break;
			}
			rank++;
		}

		// System.out.println("rank: " + rank);

		double[] prob_sum = new double[sortedlist.size()];
		double base = 2.0;

		for (int i = 1; i < sortedlist.size(); i++) {
			int quality = Math.abs(i - rank);
			double prob = Math.exp(-1 * eps * quality / base);
			prob_sum[i] = prob_sum[i - 1] + prob;
			// System.out.println("rank" + i + " prob: " + prob);
		}

		double rand = Math.random() * prob_sum[sortedlist.size() - 1];
		int idx = 0;
		for (int i = 0; i < prob_sum.length; i++) {
			if (prob_sum[i] > rand) {
				idx = i;
				break;
			}
		}

		double uniform = Math.random();

		double left = sortedlist.get(idx - 1).getValue(vindex);
		double right = sortedlist.get(idx).getValue(vindex);
		double length = right - left;

		return left + length * uniform;
	}
}

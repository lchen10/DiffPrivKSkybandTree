package skyband;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ExpMecTopKDomShort {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static boolean isDebug = false;
	private static void debug(String text) {
		if (isDebug) {
			System.out.println("DEBUG: " + text);
		}
	}
	
	
	public static List<Tuple> ExpMecTopKDom(List<Tuple> group,
			List<Comparison> comparisons, int k) throws Exception
	{
		List<Tuple> results = new ArrayList<Tuple>();
		
		List<DistTuple> distTuple = new ArrayList<DistTuple>();
		distTuple.add(new DistTuple(new Tuple(), 0.0));
		
		// calculate the top-k dominating points
		List<Tuple> topKDom = TopKDominating.computeTopKDominating(group, comparisons, k);
		debug("top k: " + topKDom);
		
		// regard the top-k dominating points as a cluster and calculate
		// the center point of the cluster
		Tuple mean = Mean(topKDom);
		
		// calculate the distance between each dominated point with the cluster center
		// add them to DistTuples
		for (int i = 0; i < group.size(); i++) {
			double distance = Distance(group.get(i), mean);
			distTuple.add(new DistTuple(group.get(i), distance));			
		}
		
	
		// sort all the tuples according to their distance to cluster center
		Collections.sort(distTuple, new DistTupleComparator());		
		debug("sorted: " + distTuple);
			
		double eps = 1/(Math.log(1+1*(Math.exp(1)-1)));
		debug("eps: " + eps);
		
		double base = Math.exp(-eps/2);		
		debug("base: " + base);
		
		double[] probSum = new double[distTuple.size()];
		for (int i = 1; i < probSum.length; i++) {
			double t1 = distTuple.get(i).getDist()/distTuple.get(i).getTuple().dominatingCount;
		//	double t2 = distTuple.get(i-1).getDist();
		//	probSum[i] = probSum[i-1] + (t1-t2) * (Math.pow(base, t1));
			probSum[i] = probSum[i-1] + (Math.pow(base, t1));
		}
		
		debug("probSum is: " + probSum[probSum.length - 1] + "\n");
		
		debug("Begin to print the private top-k dominating points:" + "\n");
		
		
		for (int x = 0; x < k; x++) {
			// a random number in [0, 1) 
			double randTemp = Math.random(); 
			debug("rand number is: " + randTemp);
			
			double rand = randTemp * probSum[probSum.length - 1];
			debug("rand: " + rand);
			int idx = 0;
			
			// idx is the index of the bucket which "rand" falls into
			for (int i = 0; i < probSum.length; i++) {
				if(probSum[i] > rand)
				{
					idx = i;
					break;
				}
			}
			debug("found: " + distTuple.get(idx));
			
			
//			DistTuple found = distTuple.get(idx);
//			double uniform = Math.random();
//			int[] result = new int[found.getTuple().size()];
// 			for (int i = 0; i < mean.getValues().size(); i++) {
//				Tuple current = found.getTuple();
//				if(mean.getValue(i) > current.getValue(i)){
//					int delta = mean.getValue(i) - current.getValue(i);
//					result[i] = (int) (delta*uniform + current.getValue(i));
//				}else{
//					int delta = current.getValue(i) - mean.getValue(i);
//					result[i] = (int) (delta*uniform + mean.getValue(i));
//				}
//			}
// 			debug("result: " + new Tuple(result));
// 			results.add(new Tuple(result));
		}
	
		
		return results;
		
	}
	
	
	// regard the top-k dominating points as a cluster and calculate
	// the center point of the cluster
	public static Tuple Mean(List<Tuple> tuples)
	{
		int dim = tuples.get(0).size();
		double[] mean = new double[dim];
		
		for(int i = 0; i < dim; i++)
		{
			mean[i] = 0;
			for(int j = 0; j < tuples.size(); j++){
				mean[i] += tuples.get(j).getValue(i);		
			}
		//	System.out.println("mean " + i + " =" + mean[i]);
			mean[i] = mean[i]/tuples.size();
		}
				
		return new Tuple(mean);
	}
	
	public static double Distance(Tuple t1, Tuple t2) throws Exception
	{
		double dist = 0;
		if (t1.size() != t2.size())
		{
			throw new Exception("t1.size does not equal with t2.size!");
		}
		
		int dim = t1.size();
		
		for (int i = 0; i < dim; i++) {
			dist += Math.pow((t1.getValue(i)-t2.getValue(i)), 2);
		}
		
		dist = Math.sqrt(dist);
		
		return dist;
	}
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}


//class DistTupleComparator implements Comparator<DistTuple>
//{
//
//	@Override
//	public int compare(DistTuple x, DistTuple y) {
//		// TODO Auto-generated method stub
//		
//		return Double.compare(x.getDist(), y.getDist());
//	}
//	
//}

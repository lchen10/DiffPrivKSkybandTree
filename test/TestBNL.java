import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import skyband.BNL;
import skyband.Comparison;
import skyband.Tuple;


public class TestBNL {
	@Test
	public void testBNL()  {
		List<Tuple> tuples = new ArrayList<Tuple>();
		tuples.add(new Tuple(new double[]{1,8}));
		tuples.add(new Tuple(new double[]{5,7}));
		tuples.add(new Tuple(new double[]{7,6}));
		tuples.add(new Tuple(new double[]{2,3}));
		tuples.add(new Tuple(new double[]{4,5}));
		tuples.add(new Tuple(new double[]{5,2}));
		tuples.add(new Tuple(new double[]{9,1}));
		
		List<Comparison> comparisons = new ArrayList<Comparison>();
		comparisons.add(Comparison.MIN);
		comparisons.add(Comparison.MIN);
		
//		Tuple[] skylineTuples = BNL.computeSkyline(tuples, null, comparisons);
//		Assert.assertEquals(4, skylineTuples.length);
//		System.out.println("skyline tuples: "
//				+ Arrays.asList(skylineTuples));
	}	
	
	@Test
	public void testTopKDominating() throws Exception
	{
	
		List<Tuple> tuples = new ArrayList<Tuple>();
		tuples.add(new Tuple(new double[]{1,8}));
		tuples.add(new Tuple(new double[]{5,7}));
		tuples.add(new Tuple(new double[]{7,6}));
		tuples.add(new Tuple(new double[]{2,3}));
		tuples.add(new Tuple(new double[]{4,5}));
		tuples.add(new Tuple(new double[]{5,2}));
		tuples.add(new Tuple(new double[]{9,1}));
		
		List<Comparison> comparisons = new ArrayList<Comparison>();
		comparisons.add(Comparison.MIN);
		comparisons.add(Comparison.MIN);
		
//		List<Tuple> skylineTuples = TopKDominating.computeTopKDominating(tuples, comparisons, 2);
//		
//		System.out.println("Top-k Dominating tuples: "
//				+ Arrays.asList(skylineTuples));
		
	}
	
	@Test
	public void testkSkylineBand() throws Exception
	{
	
		List<Tuple> tuples = new ArrayList<Tuple>();
		tuples.add(new Tuple(new double[]{1,9}));
		tuples.add(new Tuple(new double[]{2,10}));
		tuples.add(new Tuple(new double[]{4,8}));
		tuples.add(new Tuple(new double[]{6,7}));
		tuples.add(new Tuple(new double[]{9,10}));
		tuples.add(new Tuple(new double[]{7,5}));
		tuples.add(new Tuple(new double[]{5,6}));
		tuples.add(new Tuple(new double[]{4,3}));
		tuples.add(new Tuple(new double[]{3,2}));
		tuples.add(new Tuple(new double[]{6,2}));
		tuples.add(new Tuple(new double[]{9,1}));
		tuples.add(new Tuple(new double[]{10,4}));
		tuples.add(new Tuple(new double[]{8,3}));
		
		List<Comparison> comparisons = new ArrayList<Comparison>();
		comparisons.add(Comparison.MIN);
		comparisons.add(Comparison.MIN);
		
		Tuple[] skylineTuples = BNL.computeKSkylineBand(tuples, null, comparisons, 3);
		
		System.out.println("k skyline band tuples: "
				+ Arrays.asList(skylineTuples));
		
	}
	
}

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import skyband.Comparison;
import skyband.ExpMecTopKDom;
import skyband.ExpMecTopKDomShort;
import skyband.Tuple;


public class TestExpMec {

	/**
	 * @param args
	 */
	@Test
	public void TestMean()
	{
		List<Tuple> tuples = new ArrayList<Tuple>();
		tuples.add(new Tuple(new double[]{5,7}));
		tuples.add(new Tuple(new double[]{7,6}));
		tuples.add(new Tuple(new double[]{3,5}));
	//	tuples.add(new Tuple(new int[]{5,2}));
		
		Tuple mean = ExpMecTopKDom.Mean(tuples);
		Assert.assertEquals(5.0, mean.getValue(0));
		Assert.assertEquals(6.0, mean.getValue(1));
	}
	
	@Test
	public void TestDistance() throws Exception
	{
		Tuple t1 = new Tuple(new double[]{5,7});
		Tuple t2 = new Tuple(new double[]{8,3});
		
		double dist = ExpMecTopKDom.Distance(t1, t2);
		Assert.assertEquals(5.0, dist);
		
	}
	
	@Test
	public void testExpMec() throws Exception  {
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
		
		List<Tuple> results = ExpMecTopKDomShort.ExpMecTopKDom(tuples, comparisons, 3);
		
		for(int i = 0; i < results.size(); i++)
		{
		//	avgCount[i] += results.get(i).dominatingCount;
			System.out.println("the dominance count is: " + results.get(i).dominatingCount);
		}
//		Assert.assertEquals(0, results.size());
//		System.out.println("skyline tuples: "
//				+ Arrays.asList(results));
	}
	
}

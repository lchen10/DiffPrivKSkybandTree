import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import skyband.Comparison;
import skyband.PrivateTopKDomImprove3;
import skyband.Tuple;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;



public class TestPrivTopkDom3 {
	
	@Test
	public void testPrivTopkDom3 () throws Exception
	{
		
		List<Tuple> tuples = new ArrayList<Tuple>();
//		tuples.add(new Tuple(new double[] { 1, 8 }));
//		tuples.add(new Tuple(new double[] { 5, 7 }));
//		tuples.add(new Tuple(new double[] { 7, 6 }));
//		tuples.add(new Tuple(new double[] { 2, 3 }));
//		tuples.add(new Tuple(new double[] { 4, 5 }));
//		tuples.add(new Tuple(new double[] { 5, 2 }));
//		tuples.add(new Tuple(new double[] { 9, 1 }));
		
		List<Comparison> comparisons = new ArrayList<Comparison>();
		comparisons.add(Comparison.MIN);
		comparisons.add(Comparison.MIN);
		
//		List<Tuple> results = PrivateTopKDomImprove3.CalculateTopKDominating(tuples,
//				comparisons, 3);
//		
//		for(int j = 0; j < 3; j++)
//		{
//			System.out.println("DominanceCount for private top-" + j + " is: " + results.get(j).toString() + results.get(j).dominatingCount);
//		}
//		
		
		FileInputStream in = new FileInputStream("normal1k.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String strLine = null;
		String[] mystring = new String[2];
		Integer[] myarray = new Integer[2];


		int s = 0;
		strLine = br.readLine();
		while ((strLine = br.readLine()) != null) {

			strLine = strLine.trim();
			mystring = strLine.split(" ");
//			System.out.println("mystring length is: " + mystring.length);

			for (int t = 0; t < 2; t++) {
				myarray[t] = (int) (Double.parseDouble(mystring[t]) * 1000000);
//				System.out.println("the number is: " + myarray[t]);
			}
			s++;
//			System.out.println("the i is: " + s);
			tuples.add(new Tuple(myarray));

		}
		in.close();
		

		
		double[] avgCount = new double[3];
		
		int exeTimes = 1;
		System.out.println("execution times are: " + exeTimes);
		
		for(int t = 0; t < exeTimes; t++)
		{
			
			List<Tuple> results = PrivateTopKDomImprove3.CalculateTopKDominating(tuples,
			comparisons, 3);
			for(int i = 0; i < results.size(); i++)
			{
				avgCount[i] += results.get(i).dominatingCount;
			//	System.out.println("the dominance count is: " + results.get(i).dominatingCount );
			}
			
		}
		
		for(int j = 0; j < avgCount.length; j++)
		{
			System.out.println("AVG DominanceCount for private top-" + j + " is: " + avgCount[j] / exeTimes);
		}

		
	}

}

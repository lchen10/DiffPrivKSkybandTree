package skyband;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReadData {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

//		FileInputStream in = new FileInputStream("normal1k.txt");
//		BufferedReader br = new BufferedReader(new InputStreamReader(in));
//
//		String strLine = null;
//		String[] mystring = new String[2];
//		Object[] myarray = new Object[2];
//
//		List<Tuple> tuples = new ArrayList<Tuple>();
//
//		int i = 0;
//		strLine = br.readLine();
//		while ((strLine = br.readLine()) != null) {
//
//			strLine = strLine.trim();
//			mystring = strLine.split(" ");
//			System.out.println("mystring length is: " + mystring.length);
//
//			for (int t = 0; t < 2; t++) {
//				myarray[t] = Double.parseDouble(mystring[t]);
//				System.out.println("the number is: " + myarray[t]);
//			}
//			i++;
//			System.out.println("the i is: " + i);
//			tuples.add(new Tuple(myarray));
//
//		}
//		in.close();
		
		double rangeMax = 5.99;
		double rangeMin = 5.00;
		
		Random r = new Random();
		double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
		
		System.out.println("random is: " + randomValue);

	}

}

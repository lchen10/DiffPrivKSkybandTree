package skyband;

import java.util.*;

public class UniformRandom {
	public static Tuple getRandomTuple(double xmin, double xmax, double ymin, double ymax, double delta) {
		List<Tuple> list = new ArrayList<Tuple>();

		// ceil up, so +1
		long xcount = (long) Math.ceil(Math.abs(xmax - xmin) / delta) + 1;
		// System.out.println("xcount is: " + xcount);
		long ycount = (long) Math.ceil(Math.abs(ymax - ymin) / delta) + 1;
		// System.out.println("ycount is: " + ycount);

		// int xcurrent = xmin;
		// int ycurrent = ymin;
		// for (int i = 0; i < xcount; i++) {
		// for (int j = 0; j < ycount; j++) {
		// Tuple tuple = new Tuple(new int[] { xcurrent, ycurrent });
		// tuple.x = xcurrent;
		// tuple.y = ycurrent;
		// list.add(tuple);
		// ycurrent += delta;
		// }
		// xcurrent += delta;
		// }

		long total = xcount * ycount;
		// System.out.println("total is: " + total);
		Random r = new Random();
		double rand = r.nextFloat();

		double rint = Math.ceil(rand * total);
		// System.out.println("random is: " + rand);
		// System.out.println("random * total is: " + rand * total);
		// System.out.println("rint is: " + rint);
		//
		// if (rint == 0) {
		// System.out.println("rint is negative:" + rint);
		// rint = 0;
		// }
		long y_loc = (long) Math.ceil((float) rint / (float) xcount) - 1;
		// if (y_loc < 0)
		// {
		// y_loc = 5;
		// }
		// System.out.println("y_loc is: " + y_loc);

		long x_loc = (long) rint % xcount - 1;
		// System.out.println("x_loc is: " + x_loc);
		if (x_loc < 0) {
			x_loc = 5;
			// System.out.println("x_loc after is: " + x_loc);
		}

		double x = 0.0;
		double y = 0.0;
		if (xmax >= xmin && ymax >= ymin) // first case: (xmin, ymin) is at the
											// lower left of (xmax, ymax)
		{
			x = x_loc * delta + xmin;
			y = y_loc * delta + ymin;
		} else if (xmax <= xmin && ymax <= ymin) // second case: (xmax, ymax) is
													// at the lower left of
													// (xmin, ymin)
		{ // switch to the case: (xmin, ymin);(xmax, ymax)=>(xmax, ymax);(xmin,
			// ymin)
			x = x_loc * delta + xmax;
			y = y_loc * delta + ymax;
		} else if (xmin >= xmax && ymin <= ymax) // third case: (xmin, ymin) is
													// at the lower right of
													// (xmax, ymax)
		{ // switch to the case: (xmin, ymin);(xmax, ymax)=>(xmax, ymin);(xmin,
			// ymax)

			x = x_loc * delta + xmax;
			y = y_loc * delta + ymin;
		} else if (xmin <= xmax && ymin >= ymax) // fourth case: (xmax, ymax) is
													// at the lower right of
													// (xmin, ymin)
		{ // switch to the case: (xmin, ymin);(xmax, ymax)=>(xmin, ymax);(xmax,
			// ymin)
			x = x_loc * delta + xmin;
			y = y_loc * delta + ymax;
		} else {
			System.out.println("the input is not in the four cases");
			System.out.println("xmin-xmax-ymin-ymax: " + xmin + ", " + xmax + ", " + ymin + ", " + ymax);
		}

		// long final_x = (long) Math.round(x);
		// long final_y = (long) Math.round(y);

		// Tuple tuple = new Tuple(new double[] { final_x, final_y });

		Tuple tuple = new Tuple(new double[] { x, y });

		return tuple;

	}

	public static Tuple getRandomTuple(double[][] ranges, double delta) {
		List<Tuple> list = new ArrayList<Tuple>();
		Random r = new Random();
		double[] result = new double[ranges.length];
		for (int i = 0; i < ranges.length; i++) {
			long xcount = (long) Math.ceil(Math.abs(ranges[i][1] - ranges[i][0]) / delta) + 1;
			double rand = r.nextFloat();
			double rint = Math.ceil(rand * xcount);
			double x = rint * delta + ranges[i][0];
			result[i] = x;
		}
		Tuple tuple = new Tuple(result);
		return tuple;

	}

	public static void main(String[] args) {
		double xmax = 6.0;
		double ymax = 0.0;
		double xmin = 3.0;
		double ymin = 3.0;
		double delta = 0.001;

		for (int i = 0; i < 200; i++) {
			Tuple t = UniformRandom.getRandomTuple(xmin, xmax, ymin, ymax, delta);

			System.out.println(t.toString());
		}

	}

}

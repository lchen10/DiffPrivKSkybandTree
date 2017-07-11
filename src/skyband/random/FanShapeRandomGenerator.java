package skyband.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import skyband.Tuple;

public class FanShapeRandomGenerator {
	public List<Tuple> generateFanRandomTuple(int count, double xmin, double xmax, double ymin, double ymax) {
		ArrayList<Tuple> result = new ArrayList<Tuple>();
		double xlength = xmax - xmin;
		double ylength = ymax - ymin;

		double rmax = Math.sqrt(Math.pow(xlength, 2) + Math.pow(ylength, 2));

		int totalinterval = 8;
		double rinterval = rmax / totalinterval;

		double sum = 0.0;
		double[] probs = new double[totalinterval + 1];
		for (int i = 0; i < totalinterval; i++) {
			probs[i + 1] = Math.pow(2.5, i);
			sum += probs[i + 1];
			probs[i+1] += probs[i];

		}

		Random r = new Random();
		while (result.size() < count) {
			double angle = r.nextDouble() * 70 + 10;
			double rindex = r.nextDouble() * sum;
			double rlength = rmax * r.nextDouble();
			for (int i = 0; i < probs.length; i++) {
				if (probs[i] >= rindex) {
					double min = rmax - rinterval*(i);
					double max = rmax - rinterval*(i-1);
					rlength = min + (max - min) * r.nextDouble();				
					break;
				}
			}
			
			
			double x = Math.cos(Math.toRadians(angle)) * rlength;
			double y = Math.sin(Math.toRadians(angle)) * rlength;
			
			if (x >= xmin && x <= xmax && y >= ymin && y <= ymax) {
				result.add(new Tuple(new double[]{x,y}));
			}

		}

		return result;
	}

	
	public List<Tuple> generateFanUniformRandomTuple(int count, double xmin, double xmax, double ymin, double ymax) {
		ArrayList<Tuple> result = new ArrayList<Tuple>();
		double xlength = xmax - xmin;
		double ylength = ymax - ymin;

//		double rmax = Math.sqrt(Math.pow(xlength, 2) + Math.pow(ylength, 2));
		double rmax = xlength;
		int totalinterval = 8;
		double rinterval = rmax / totalinterval;

		double sum = 0.0;
		double[] probs = new double[totalinterval + 1];
		for (int i = 0; i < totalinterval; i++) {
			probs[i + 1] = 1;
			sum += probs[i + 1];
			probs[i+1] += probs[i];

		}

		Random r = new Random();
		while (result.size() < count) {
			double angle = r.nextDouble() * 70 + 10;
			double rindex = r.nextDouble() * sum;
			double rlength = rmax * r.nextDouble();
			for (int i = 0; i < probs.length; i++) {
				if (probs[i] >= rindex) {
					double min = rmax - rinterval*(i);
					double max = rmax - rinterval*(i-1);
					rlength = min + (max - min) * r.nextDouble();				
					break;
				}
			}
			
			
			double x = Math.cos(Math.toRadians(angle)) * rlength;
			double y = Math.sin(Math.toRadians(angle)) * rlength;
			
			if (x >= xmin && x <= xmax && y >= ymin && y <= ymax) {
				result.add(new Tuple(new double[]{x,y}));
			}

		}

		return result;
	}

}

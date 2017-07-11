package kSpacePartition;

import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import skyband.Tuple;

public class EMTest {
	@Test
	public void testExponentialGetX() throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("data/em.dat"),
				Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split("\\t");
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }));
		}

		for (int i = 0; i < 50; i++) {
			double x = EM.getValueBasedOnRank(tuples, 1.0, 5.0, 0, 0.0, 10.0);
			System.out.println("x: " + x);
		}

	}

	@Test
	public void testExponentialGetY() throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("data/em.dat"),
				Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split("\\t");
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			tuples.add(new IDTuple(new Double[] { x, y }));
		}

		for (int i = 0; i < 50; i++) {
			double x = EM.getValueBasedOnRank(tuples, 1.0, 5.0, 1, 0.0, 10.0);
			System.out.println("y: " + x);
		}

	}
}

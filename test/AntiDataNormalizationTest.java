
import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.Test;

import kSpacePartition.IDTuple;
import kSpacePartition.KSpacePartition;
import kSpacePartition.Node;
import skyband.Tuple;

public class AntiDataNormalizationTest {
	@Test
	public void testLoadPoints() throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("data/" + "anti5k.txt"), Charset.defaultCharset());
		List<IDTuple> tuples = new ArrayList<IDTuple>();
		int id = 1;
		PrintWriter out = new PrintWriter("data/anti5k-normal.txt");
		for (int i = 0; i < lines.size(); i++) {
			String[] line = lines.get(i).split("\\s+");
			double x = Double.parseDouble(line[0]);
			double y = Double.parseDouble(line[1]);
			out.println(x * 1000000 + " " + y * 1000000);
			id++;
		}

		out.close();

	}
}

package result;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class DataConversionTest {
	@Test
	public void testConvertFromDatToCSV() throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("data/15-clusters.dat"));
		PrintWriter out = new PrintWriter("dataset/15-clusters.csv");
		for (String line : lines) {
			String[] split = line.split("\\s+");
			String deliminator = "";
			for (String string : split) {
				out.print(deliminator + string);
				deliminator = ",";
			}
			out.println();
		}
		out.close();
	}
}

package skyband.random;

import static org.junit.Assert.*;

import java.awt.Color;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Test;

import skyband.Tuple;

public class FanShapeRandomGeneratorTest {
	@Test
	public void testGenerateFanShapeRandomData() throws Exception {
		FanShapeRandomGenerator f = new FanShapeRandomGenerator();
		List<Tuple> tuples = f.generateFanRandomTuple(10000, 0, 5000, 0, 5000);
		PrintWriter csv = new PrintWriter("dataset/fan.csv");
		PrintWriter dat = new PrintWriter("dataset/fan.dat");

		final XYSeriesCollection dataset = new XYSeriesCollection();
		File fig = new File("evaluations/fanshape.png");
		final XYSeries series = new XYSeries("random");
		for (Tuple t : tuples) {
			series.add(t.getValue(0), t.getValue(1));
			csv.println(t.getValue(0) + ","+ t.getValue(1));
			dat.println(t.getValue(0) + "\t"+ t.getValue(1));
		}
		csv.close();
		dat.close();
		dataset.addSeries(series);
		JFreeChart chart = ChartFactory.createScatterPlot("Fan shape random", "x", "y", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		// chart.getCategoryPlot().setDomainGridlinePaint(Color.black);
		// chart.getCategoryPlot().setRangeGridlinePaint(Color.black);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setDomainGridlinePaint(Color.BLACK);
		plot.setRangeGridlinePaint(Color.BLACK);
		plot.setBackgroundPaint(Color.white);
		int width = 1024; /* Width of the image */
		int height = 768; /* Height of the image */
		ChartUtilities.saveChartAsPNG(fig, chart, width, height);
	}
}

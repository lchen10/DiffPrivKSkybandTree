package result;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Test;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.FontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class ExampleChartTest {

	public static void saveChartAsPDF(File file, JFreeChart chart, int width, int height, FontMapper mapper)
			throws Exception {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		writeChartAsPDF(out, chart, width, height, mapper);
		out.close();
	}

	public static void writeChartAsPDF(OutputStream out, JFreeChart chart, int width, int height, FontMapper mapper)
			throws Exception {
		Rectangle pagesize = new Rectangle(width, height);
		Document document = new Document(pagesize, 50, 50, 50, 50);
		try {
			PdfWriter writer = PdfWriter.getInstance(document, out);
			document.addAuthor("JFreeChart");
			document.addSubject("Demonstration");
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tp = cb.createTemplate(width, height);
			Graphics2D g2 = tp.createGraphics(width, height, mapper);
			Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
			chart.draw(g2, r2D, null);
			g2.dispose();
			cb.addTemplate(tp, 0, 0);
		} catch (DocumentException de) {
			System.err.println(de.getMessage());
		}
		document.close();
	}
	
	private void resizeNumberTick(ValueAxis axis){
		double max = axis.getUpperBound();
		double min = axis.getLowerBound();
		axis.setAutoRange(false);		
		double interval = (max - min) / 3.0;
		axis.setRange(min - (interval*0.02), max*1.04);
		Font font = new Font("Dialog", Font.PLAIN, 60);
		axis.setTickLabelFont(font);
		NumberTickUnit rUnit = new NumberTickUnit(interval);
		NumberAxis naxis = (NumberAxis) axis;
		naxis.setTickUnit(rUnit);
		DecimalFormat format = new DecimalFormat() {
			@Override
			public StringBuffer format(double number, StringBuffer toAppendTo,
					FieldPosition pos) {
				if (number >= 1000000) {
					toAppendTo.append((int) (number / 1000000));
					toAppendTo.append(" M");
				} else if (number >= 1000) {
					toAppendTo.append((int) (number / 1000));
					toAppendTo.append(" K");
				} else {
					toAppendTo.append((int) number);
				}
				return toAppendTo;
			}

			@Override
			public StringBuffer format(long number, StringBuffer toAppendTo,
					FieldPosition fieldPosition) {
				if (number >= 1000000) {
					toAppendTo.append((int) (number / 1000000));
					toAppendTo.append(" M");
				} else if (number >= 1000) {
					toAppendTo.append((int) (number / 1000));
					toAppendTo.append(" K");
				} else {
					toAppendTo.append((int) number);
				}
				return toAppendTo;
			}

		};
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(0);
		naxis.setNumberFormatOverride(format);
		naxis.setLowerMargin(5);
		naxis.setUpperMargin(5);
	}

	@Test
	public void testChart() throws Exception {

		int[] ks = new int[] { 20, 40, 60, 80, 100, 150, 200 };
//		String[] datasets = new String[] { "15-clusters.dat", "normal10k.csv", "anti10k.csv", "correlated10k.csv",
//				"covtype.csv", "nba.csv", "fan.csv" };
		
		String[] datasets = new String[] { "kdtree-k-200-eps-2.0"
				 };

//		String[] datasets = new String[] { "fan-uniform.csv" };

		double xmax = 1000000;
		double ymax = 1000000;
		
		for (String ds : datasets) {
			List<String> trueLines = Files.readAllLines(Paths.get("evaluations/examples/" + ds+".csv"));
			String filename = ds;
			final XYSeries oriseries = new XYSeries(filename);
			final XYSeriesCollection ori = new XYSeriesCollection();
			for (String kline : trueLines) {
				String[] split = kline.split(",");
				oriseries.add(xmax- Double.parseDouble(split[0]), ymax-Double.parseDouble(split[1]));
			}
			ori.addSeries(oriseries);
			JFreeChart chart = ChartFactory.createScatterPlot("", "", "", ori, PlotOrientation.VERTICAL, false,
					true, false);
			// chart.getCategoryPlot().setDomainGridlinePaint(Color.black);
			// chart.getCategoryPlot().setRangeGridlinePaint(Color.black);
			XYPlot plot = (XYPlot) chart.getPlot();
			// plot.setDomainGridlinePaint(Color.BLACK);
			// plot.setRangeGridlinePaint(Color.BLACK);
			plot.setBackgroundPaint(Color.white);
			int width = 1024; /* Width of the image */
			int height = 768; /* Height of the image */
			
			resizeNumberTick(plot.getDomainAxis());
			resizeNumberTick(plot.getRangeAxis());
			
			
			saveChartAsPDF(new File("evaluations/examples/" + filename + ".pdf"), chart, width, height,
					new DefaultFontMapper());

		}

	}
}

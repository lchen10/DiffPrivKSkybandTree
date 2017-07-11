package result;

import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.junit.Test;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.FontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class NBAMultiDimFMeasureChartTest {

	public static void saveChartAsPDF(File file, JFreeChart chart, int width,
			int height, FontMapper mapper) throws Exception {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		writeChartAsPDF(out, chart, width, height, mapper);
		out.close();
	}

	public static void writeChartAsPDF(OutputStream out, JFreeChart chart,
			int width, int height, FontMapper mapper) throws Exception {
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

	@Test
	public void testChart() throws Exception {

		int[] ks = new int[] { 20, 40, 60, 80, 100, 150, 200 };

		double[] epslist = new double[] { 0.1, 0.5, 1, };

		for (double eps : epslist) {
			List<String> lines = Files.readAllLines(Paths
					.get("evaluations/multidim10k/corr/results-eps-" + eps + ".csv"));

			String ds = "";
			XYSeriesCollection ori = new XYSeriesCollection();
			XYSeries ksybandtree = new XYSeries("KSkyband-Tree");
			int index = 0;

			for (String line : lines) {
				String[] split = line.split(",");
				if (split.length == 1 && split[0].length() > 0) {
					ds = split[0];
					continue;
				}

				if (split.length > 1) {
					if (split[0].equals("k")) {
						continue;
					}

					ksybandtree.add(index, Double.parseDouble(split[1]));
					index++;
				}

				if (split.length == 1) {

					ori.addSeries(ksybandtree);
					String filename = ds + "-" + eps;
					filename = filename.replace(".", "");
					JFreeChart chart = ChartFactory.createXYLineChart("",
							"k", "F-1", ori, PlotOrientation.VERTICAL, false,
							true, false);
					// chart.getCategoryPlot().setDomainGridlinePaint(Color.black);
					// chart.getCategoryPlot().setRangeGridlinePaint(Color.black);
					XYPlot plot = (XYPlot) chart.getPlot();
//					plot.setDomainGridlinePaint(Color.BLACK);
//					plot.setRangeGridlinePaint(Color.BLACK);
					plot.setBackgroundPaint(Color.white);
					XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
					plot.setRenderer(renderer);
					int seriesCount = plot.getSeriesCount();
					Color[] colors = new Color[]{Color.blue, Color.red, Color.green};
					for (int i = 0; i < seriesCount; i++) {
						plot.getRenderer().setSeriesStroke(i,
								new BasicStroke(4));
						plot.getRenderer().setSeriesPaint(i, colors[i]);
					}

					SymbolAxis axis = new SymbolAxis("k", new String[] { "20",
							"40", "60", "80", "100", "150", "200", });
					axis.setTickUnit(new NumberTickUnit(1));
					axis.setAutoRange(false);
					axis.setRange(-1, 7);
					axis.setGridBandsVisible(false);
//					axis.setLowerMargin(20);
					plot.setDomainAxis(axis);
					NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
					rangeAxis.setTickUnit(new NumberTickUnit(0.2));
					rangeAxis.setAutoRange(false);
					rangeAxis.setRange(0, 1);
					Font font= new Font("Dialog", Font.PLAIN, 35); 
					rangeAxis.setLabelFont(font);
					axis.setLabelFont(font);
					rangeAxis.setTickLabelFont(font);
					axis.setTickLabelFont(font);
					
//					rangeAxis.setUpperMargin(0.5);

					float scaleFactor = 2F; // use to rescale the datapoints
											// smaller
					Shape[] shapes = DefaultDrawingSupplier
							.createStandardSeriesShapes();
					for (int i = 0; i <= seriesCount; i++) {
						Shape shape = shapes[i];
						System.out.println(shape.getClass().getName());
						if (shape.getClass() == Rectangle2D.Double.class) {
							Rectangle2D.Double rect = (Rectangle2D.Double) shape;
							rect.height = rect.height * scaleFactor;
							rect.width = rect.width * scaleFactor;
							rect.x = rect.x * scaleFactor;
							rect.y = rect.y * scaleFactor;
						} else if (shape.getClass() == Ellipse2D.Double.class) {
							Ellipse2D.Double ellipse = (Ellipse2D.Double) shape;
							ellipse.height = ellipse.height * scaleFactor;
							ellipse.width = ellipse.width * scaleFactor;
							ellipse.x = ellipse.x * scaleFactor;
							ellipse.y = ellipse.y * scaleFactor;
						} else if (shape.getClass() == Polygon.class) {
							Polygon poly = (Polygon) shape;
							int[] xp = poly.xpoints;
							int[] yp = poly.ypoints;
							for (int j = 0; j < poly.npoints; j++) {
								poly.xpoints[j] = Math
										.round((float) poly.xpoints[j]
												* scaleFactor);
								poly.ypoints[j] = Math
										.round((float) poly.ypoints[j]
												* scaleFactor);
							}
						}
						renderer.setSeriesShape(i, shape);
					}
					int width = 1024; /* Width of the image */
					int height = 768; /* Height of the image */
					RectangleInsets chartRectangle = new RectangleInsets(28F,
							30F, 30F, 30F);
					// RectangleInsets chartRectangle = new
					// RectangleInsets(TOP,LEFT,BOTTOM,RIGHT);
					chart.setPadding(chartRectangle);

					saveChartAsPDF(new File("evaluations/fmeasure/multidim10k/" + filename
							+ ".pdf"), chart, width, height,
							new DefaultFontMapper());

					ori = new XYSeriesCollection();
					ksybandtree = new XYSeries("KSkyband-Tree");
					index = 0;
				}

			}

		}

	}
}

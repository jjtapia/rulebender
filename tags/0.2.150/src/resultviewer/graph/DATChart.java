package resultviewer.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DecimalFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import resultviewer.data.DATFileData;

public class DATChart {

	/* 
	 * plot chart using JFreeChart for the input FileNode 
	 * @parameter dataType: "linear", "log"
	 */
	public static JFreeChart plotXYLineChart(DATFileData fileData, String dataType) {
		JFreeChart chart = null;
		if (fileData == null)
			return chart;
		XYSeriesCollection seriesCollection = null;
		if (dataType.equals("linear")) {
			//linear
			seriesCollection = fileData.getSeriesCollection();
		} else if (dataType.equals("log")) {
			//log
			seriesCollection = fileData.getLogSeriesCollection();
		}

		// XYLineChart
		chart = ChartFactory.createXYLineChart(fileData.getFileName(), fileData
				.getXAxisName(), "Concentration", seriesCollection,
				PlotOrientation.VERTICAL, true, true, false);

		XYPlot xyPlot = chart.getXYPlot();
		DecimalFormat myformat = new DecimalFormat();
		myformat.applyPattern("0.00E00"); // format of numbers
		
		DecimalFormat myformat_log = new DecimalFormat();
		myformat_log.applyPattern("00E0"); // format of numbers
		
		// x axis
		if (dataType.equals("log")) {
			//log
			LogAxis xAxis = new LogAxis(fileData
					.getXAxisName());
			xAxis.setNumberFormatOverride(myformat_log);
			xyPlot.setDomainAxis(xAxis);
		}
		
		// y axis
		NumberAxis yAxis = (NumberAxis) xyPlot.getRangeAxis();
		yAxis.setNumberFormatOverride(myformat);
		
		

		XYLineAndShapeRenderer xylinerenderer = (XYLineAndShapeRenderer) xyPlot
		.getRenderer();
		
		/* color of lines */
		/*
		for (int i = 0; i < seriesCollection.getSeriesCount(); i++) {
			xylinerenderer.setSeriesPaint(i, Color.GRAY);
		}
		*/

		/* stroke of lines */
		for (int i = 0; i < seriesCollection.getSeriesCount(); i++) {
			xylinerenderer.setSeriesStroke(i, new BasicStroke(1f));
		}
		return chart;
	}

	public static JFreeChart plotScatterChart(DATFileData fileData) {
		// TODO
		return null;

	}

	/*
	 * Mark the line of the selected species
	 */
	public static JFreeChart markSelectedLine(JFreeChart chart, String key) {
		XYPlot xyPlot = chart.getXYPlot();
		XYLineAndShapeRenderer xylinerenderer = (XYLineAndShapeRenderer) xyPlot
				.getRenderer();
		
		/*
		for (int i = 0; i < xyPlot.getSeriesCount(); i++) {
			xylinerenderer.setSeriesPaint(i, Color.GRAY);
		}
		*/
		
		// compute the index based on the key
		int index = -1;
		for (int j = 0; j < xyPlot.getDataset().getSeriesCount(); j++) {
			if (xyPlot.getDataset().getSeriesKey(j).equals(key)) {
				index = j;
				break;
			}
		}
		
		if (index != -1) {

			// redraw the marked line
//			xylinerenderer.setSeriesPaint(index, Color.RED); // Color

			for (int i = 0; i < xyPlot.getSeriesCount(); i++) {
				xylinerenderer.setSeriesStroke(i, new BasicStroke(1.5f));
			}
			xylinerenderer.setSeriesStroke(index, new BasicStroke(3.0f)); // Stroke
		}
		return chart;
	}
	
	/*
	 * Reset all the line in the chart be original stroke
	 */
	public static JFreeChart resetChart(JFreeChart chart) {
		XYPlot xyPlot = chart.getXYPlot();
		XYLineAndShapeRenderer xylinerenderer = (XYLineAndShapeRenderer) xyPlot
				.getRenderer();
		
		/*
		for (int i = 0; i < xyPlot.getSeriesCount(); i++) {
			xylinerenderer.setSeriesPaint(i, Color.GRAY);
		}
		*/

		for (int i = 0; i < xyPlot.getSeriesCount(); i++) {
			xylinerenderer.setSeriesStroke(i, new BasicStroke(1.5f));
		}

		return chart;
	}

}

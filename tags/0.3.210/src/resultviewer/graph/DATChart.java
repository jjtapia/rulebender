package resultviewer.graph;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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

	public static JFreeChart plotChart(DATFileData fileData, String xAxisType,
			String yAxisType, String chartType) {
		if (chartType.equals("line")) {
			return plotXYLineChart(fileData, xAxisType, yAxisType);
		} else if (chartType.equals("point")) {
			return plotScatterChart(fileData, xAxisType, yAxisType);
		} else if (chartType.equals("line & point")){
			return plotLineAndScatterChart(fileData, xAxisType, yAxisType);
		}
		else {
			return null;
		}
	}

	/*
	 * plot chart using JFreeChart for the input FileNode
	 * 
	 * @parameter dataType: "linear", "log"
	 */
	private static JFreeChart plotXYLineChart(DATFileData fileData,
			String xAxisType, String yAxisType) {
		JFreeChart chart = null;
		if (fileData == null)
			return chart;
		XYSeriesCollection seriesCollection = fileData.getSeriesCollection();

		// XYLineChart
		if (seriesCollection.getSeries().size() > 30
				&& fileData.getFileName().endsWith(".cdat")) {
			chart = ChartFactory.createXYLineChart(fileData.getFileName(),
					fileData.getXAxisName(), "Concentration", seriesCollection,
					PlotOrientation.VERTICAL, false, true, false);
		} else {
			chart = ChartFactory.createXYLineChart(fileData.getFileName(),
					fileData.getXAxisName(), "Concentration", seriesCollection,
					PlotOrientation.VERTICAL, true, true, false);
		}

		XYPlot xyPlot = chart.getXYPlot();
		DecimalFormat myformat = new DecimalFormat();
		myformat.applyPattern("0.00E00"); // format of numbers

		DecimalFormat myformat_log = new DecimalFormat();
		myformat_log.applyPattern("0.0E00"); // format of numbers

		// x axis
		if (xAxisType.equals("linear")) {
			NumberAxis xAxis = new NumberAxis(fileData.getXAxisName());
			xAxis.setNumberFormatOverride(myformat);
			xyPlot.setRangeAxis(xAxis);
		} else if (xAxisType.equals("log")) {
			// log
			LogAxis xAxis = new LogAxis(fileData.getXAxisName());
			xAxis.setNumberFormatOverride(myformat_log);
			xyPlot.setDomainAxis(xAxis);
		}

		// y axis
		if (yAxisType.equals("linear")) {
			// linear
			NumberAxis yAxis = new NumberAxis("Concentration");
			yAxis.setNumberFormatOverride(myformat);
			xyPlot.setRangeAxis(yAxis);
		} else if (yAxisType.equals("log")) {
			// log
			LogAxis yAxis = new LogAxis("Concentration");
			yAxis.setNumberFormatOverride(myformat_log);
			xyPlot.setRangeAxis(yAxis);
		}

		XYLineAndShapeRenderer xylinerenderer = (XYLineAndShapeRenderer) xyPlot
				.getRenderer();

		// stroke of lines
		for (int i = 0; i < seriesCollection.getSeriesCount(); i++) {
			xylinerenderer.setSeriesStroke(i, new BasicStroke(1.5f));
		}
		return chart;
	}

	private static JFreeChart plotScatterChart(DATFileData fileData,
			String xAxisType, String yAxisType) {
		JFreeChart chart = null;
		if (fileData == null)
			return chart;
		XYSeriesCollection seriesCollection = fileData.getSeriesCollection();

		// XYLineChart
		if (seriesCollection.getSeries().size() > 30
				&& fileData.getFileName().endsWith(".cdat")) {
			chart = ChartFactory.createScatterPlot(fileData.getFileName(),
					fileData.getXAxisName(), "Concentration", seriesCollection,
					PlotOrientation.VERTICAL, false, true, false);
		} else {
			chart = ChartFactory.createScatterPlot(fileData.getFileName(),
					fileData.getXAxisName(), "Concentration", seriesCollection,
					PlotOrientation.VERTICAL, true, true, false);
		}

		XYPlot xyPlot = chart.getXYPlot();
		DecimalFormat myformat = new DecimalFormat();
		myformat.applyPattern("0.00E00"); // format of numbers

		DecimalFormat myformat_log = new DecimalFormat();
		myformat_log.applyPattern("0.0E00"); // format of numbers

		// x axis
		if (xAxisType.equals("linear")) {
			NumberAxis xAxis = new NumberAxis(fileData.getXAxisName());
			xAxis.setNumberFormatOverride(myformat);
			xyPlot.setRangeAxis(xAxis);
		} else if (xAxisType.equals("log")) {
			// log
			LogAxis xAxis = new LogAxis(fileData.getXAxisName());
			xAxis.setNumberFormatOverride(myformat_log);
			xyPlot.setDomainAxis(xAxis);
		}

		// y axis
		if (yAxisType.equals("linear")) {
			// linear
			NumberAxis yAxis = new NumberAxis("Concentration");
			yAxis.setNumberFormatOverride(myformat);
			xyPlot.setRangeAxis(yAxis);
		} else if (yAxisType.equals("log")) {
			// log
			LogAxis yAxis = new LogAxis("Concentration");
			yAxis.setNumberFormatOverride(myformat_log);
			xyPlot.setRangeAxis(yAxis);
		}

		// shape and stroke
		XYLineAndShapeRenderer xylinerenderer = (XYLineAndShapeRenderer) xyPlot
				.getRenderer();

		for (int i = 0; i < seriesCollection.getSeriesCount(); i++) {
			// set line visible
			xylinerenderer.setSeriesLinesVisible(i, false);
			// set line stroke
			xylinerenderer.setSeriesStroke(i, new BasicStroke(1.5f));
			// set shape outline stroke
			xylinerenderer.setSeriesOutlineStroke(i, new BasicStroke(1.0f));
		}

		return chart;

	}
	
	private static JFreeChart plotLineAndScatterChart(DATFileData fileData,
			String xAxisType, String yAxisType) {
		JFreeChart chart = null;
		if (fileData == null)
			return chart;
		XYSeriesCollection seriesCollection = fileData.getSeriesCollection();

		// XYLineChart
		if (seriesCollection.getSeries().size() > 30
				&& fileData.getFileName().endsWith(".cdat")) {
			chart = ChartFactory.createScatterPlot(fileData.getFileName(),
					fileData.getXAxisName(), "Concentration", seriesCollection,
					PlotOrientation.VERTICAL, false, true, false);
		} else {
			chart = ChartFactory.createScatterPlot(fileData.getFileName(),
					fileData.getXAxisName(), "Concentration", seriesCollection,
					PlotOrientation.VERTICAL, true, true, false);
		}

		XYPlot xyPlot = chart.getXYPlot();
		DecimalFormat myformat = new DecimalFormat();
		myformat.applyPattern("0.00E00"); // format of numbers

		DecimalFormat myformat_log = new DecimalFormat();
		myformat_log.applyPattern("0.0E00"); // format of numbers

		// x axis
		if (xAxisType.equals("linear")) {
			NumberAxis xAxis = new NumberAxis(fileData.getXAxisName());
			xAxis.setNumberFormatOverride(myformat);
			xyPlot.setRangeAxis(xAxis);
		} else if (xAxisType.equals("log")) {
			// log
			LogAxis xAxis = new LogAxis(fileData.getXAxisName());
			xAxis.setNumberFormatOverride(myformat_log);
			xyPlot.setDomainAxis(xAxis);
		}

		// y axis
		if (yAxisType.equals("linear")) {
			// linear
			NumberAxis yAxis = new NumberAxis("Concentration");
			yAxis.setNumberFormatOverride(myformat);
			xyPlot.setRangeAxis(yAxis);
		} else if (yAxisType.equals("log")) {
			// log
			LogAxis yAxis = new LogAxis("Concentration");
			yAxis.setNumberFormatOverride(myformat_log);
			xyPlot.setRangeAxis(yAxis);
		}

		// shape and stroke
		XYLineAndShapeRenderer xylinerenderer = (XYLineAndShapeRenderer) xyPlot
				.getRenderer();

		for (int i = 0; i < seriesCollection.getSeriesCount(); i++) {
			// set line visible
			xylinerenderer.setSeriesLinesVisible(i, true);
			// set line stroke
			xylinerenderer.setSeriesStroke(i, new BasicStroke(1.5f));
			// set shape outline stroke
			xylinerenderer.setSeriesOutlineStroke(i, new BasicStroke(1.0f));
		}

		return chart;

	}

	/*
	 * Mark the line of the selected species
	 */
	public static JFreeChart markSelectedLine(JFreeChart chart, String key) {
		XYPlot xyPlot = chart.getXYPlot();
		XYLineAndShapeRenderer xylinerenderer = (XYLineAndShapeRenderer) xyPlot
				.getRenderer();

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
			for (int i = 0; i < xyPlot.getSeriesCount(); i++) {
				xylinerenderer.setSeriesStroke(i, new BasicStroke(1.5f));
				xylinerenderer.setSeriesOutlineStroke(i, new BasicStroke(1.0f));
			}
			xylinerenderer.setSeriesStroke(index, new BasicStroke(3.0f)); // Stroke
			xylinerenderer.setSeriesOutlineStroke(index, new BasicStroke(3.0f));

		}
		else {
			//reset
			for (int i = 0; i < xyPlot.getSeriesCount(); i++) {
				xylinerenderer.setSeriesStroke(i, new BasicStroke(1.5f));
				xylinerenderer.setSeriesOutlineStroke(i, new BasicStroke(1.0f));
			}
			
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

		for (int i = 0; i < xyPlot.getSeriesCount(); i++) {
			xylinerenderer.setSeriesStroke(i, new BasicStroke(1.5f));
			xylinerenderer.setSeriesOutlineStroke(i, new BasicStroke(1.0f));
		}

		return chart;
	}

}

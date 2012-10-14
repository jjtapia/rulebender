package resultviewer.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import resultviewer.data.DATComparisonData;

public class DATComparisonChart
{

  private static Paint[] colors = new Paint[] { Color.red, Color.blue,
      Color.green, Color.yellow, Color.magenta, Color.cyan, Color.pink,
      Color.gray, Color.orange };

  private static Paint[] colors_dark = new Paint[] {
      Color.red.darker().darker(), Color.blue.darker().darker(),
      Color.green.darker().darker(), Color.yellow.darker().darker(),
      Color.magenta.darker().darker(), Color.cyan.darker().darker(),
      Color.pink.darker().darker(), Color.gray.darker().darker(),
      Color.orange.darker().darker() };

  private static DATComparisonData fileData;


  /**
   * 
   * @param chart
   *          JFreeChart object
   * @param xAxisType
   *          plotting type of x axis, "linear", "log"
   * @param yAxisType
   *          plotting type of y axis, "linear", "log"
   * @return
   */
  public static JFreeChart plotChart(JFreeChart chart, String xAxisType,
      String yAxisType)
  {

    XYPlot xyPlot = chart.getXYPlot();
    DecimalFormat myformat = new DecimalFormat();
    myformat.applyPattern("0.00E00"); // format of numbers

    DecimalFormat myformat_log = new DecimalFormat();
    myformat_log.applyPattern("0.0E00"); // format of numbers

    // x axis
    if (xAxisType.equals("linear"))
    {
      NumberAxis xAxis = new NumberAxis(xyPlot.getDomainAxis().getLabel());
      xAxis.setNumberFormatOverride(myformat);
      xyPlot.setDomainAxis(xAxis);
    }
    else if (xAxisType.equals("log"))
    {
      // log
      LogAxis xAxis = new LogAxis(xyPlot.getDomainAxis().getLabel());
      xAxis.setNumberFormatOverride(myformat_log);
      if (fileData.isAllValueLargerThanZero_X() == false)
      {
        xAxis.setSmallestValue(fileData.getMinX());
      }
      xyPlot.setDomainAxis(xAxis);
    }

    // y axis
    if (yAxisType.equals("linear"))
    {
      // linear
      NumberAxis yAxis = new NumberAxis("Concentration");
      yAxis.setNumberFormatOverride(myformat);
      xyPlot.setRangeAxis(yAxis);
    }
    else if (yAxisType.equals("log"))
    {
      // log
      LogAxis yAxis = new LogAxis("Concentration");
      yAxis.setNumberFormatOverride(myformat_log);
      if (fileData.isAllValueLargerThanZero_Y() == false)
      {
        yAxis.setSmallestValue(fileData.getMinY());
      }
      xyPlot.setRangeAxis(yAxis);
    }

    return chart;
  }


  /**
   * 
   * @param fileData
   *          DATComparisonData object
   * @param chartType
   *          list of plotting types
   * @return
   */
  public static JFreeChart plotChart(DATComparisonData fileData,
      ArrayList<String> chartType)
  {
    DATComparisonChart.fileData = fileData;
    JFreeChart chart = null;
    if (fileData == null)
      return chart;
    XYSeriesCollection seriesCollection = fileData.getSeriesCollection();

    // XYLineChart
    if (seriesCollection.getSeriesCount() <= 60)
    {
      // with legend
      chart = ChartFactory.createXYLineChart(fileData.getFileName(),
          fileData.getXAxisName(), "Concentration", seriesCollection,
          PlotOrientation.VERTICAL, true, true, false);
    }
    else
    {
      // without legend
      chart = ChartFactory.createXYLineChart(fileData.getFileName(),
          fileData.getXAxisName(), "Concentration", seriesCollection,
          PlotOrientation.VERTICAL, false, true, false);
    }

    XYPlot xyPlot = chart.getXYPlot();

    // data set
    for (int i = 0; i < fileData.getDATDataCount(); i++)
    {
      xyPlot.setDataset(i, fileData.getSeriesCollection(i));

      // XYLineAndShapeRenderer(boolean lines, boolean shapes)
      XYLineAndShapeRenderer xylinerenderer = null;

      if (chartType.get(i).equals("line"))
      {
        xylinerenderer = new XYLineAndShapeRenderer(true, false);
      }
      else if (chartType.get(i).equals("point"))
      {
        xylinerenderer = new XYLineAndShapeRenderer(false, true);

      }
      else if (chartType.get(i).equals("line & point"))
      {
        xylinerenderer = new XYLineAndShapeRenderer(true, true);

      }
      for (int j = 0; j < seriesCollection.getSeriesCount(); j++)
      {
        // stroke
        xylinerenderer.setSeriesStroke(j, new BasicStroke(1.5f));

        if (i % 2 == 0)
        {
          // original color
          int color_index = j % colors.length;
          xylinerenderer.setSeriesPaint(j, colors[color_index]);
        }
        else
        {
          // darker color
          int color_index = j % colors_dark.length;
          xylinerenderer.setSeriesPaint(j, colors_dark[color_index]);
        }
      }
      xyPlot.setRenderer(i, xylinerenderer);
    }

    return chart;
  }


  /**
   * Mark the line of the selected series.
   * 
   * @param chart
   * @param cur_index
   *          index of current data set
   * @param key
   *          key of the selected series
   * @return
   */
  public static JFreeChart markSelectedLine(JFreeChart chart, int cur_index,
      String key)
  {
    XYPlot xyPlot = chart.getXYPlot();
    XYLineAndShapeRenderer xylinerenderer = (XYLineAndShapeRenderer) xyPlot
        .getRenderer(cur_index);

    // compute the index based on the key
    int index = -1;
    for (int j = 0; j < xyPlot.getDataset(cur_index).getSeriesCount(); j++)
    {
      if (xyPlot.getDataset(cur_index).getSeriesKey(j).equals(key))
      {
        index = j;
        break;
      }
    }

    if (index != -1)
    {

      // redraw the marked line
      for (int i = 0; i < xyPlot.getDataset(cur_index).getSeriesCount(); i++)
      {
        xylinerenderer.setSeriesStroke(i, new BasicStroke(1.5f));
        xylinerenderer.setSeriesOutlineStroke(i, new BasicStroke(1.0f));
      }
      xylinerenderer.setSeriesStroke(index, new BasicStroke(3.0f)); // Stroke
      xylinerenderer.setSeriesOutlineStroke(index, new BasicStroke(3.0f));

    }
    else
    {
      // reset
      for (int i = 0; i < xyPlot.getDataset(cur_index).getSeriesCount(); i++)
      {
        xylinerenderer.setSeriesStroke(i, new BasicStroke(1.5f));
        xylinerenderer.setSeriesOutlineStroke(i, new BasicStroke(1.0f));
      }

    }
    return chart;
  }


  /**
   * Reset chart.
   * 
   * @param chart
   * @param cur_index
   *          index of current data set
   * @return
   */
  public static JFreeChart resetChart(JFreeChart chart, int cur_index)
  {
    XYPlot xyPlot = chart.getXYPlot();
    XYLineAndShapeRenderer xylinerenderer = (XYLineAndShapeRenderer) xyPlot
        .getRenderer(cur_index);

    for (int i = 0; i < xyPlot.getDataset(cur_index).getSeriesCount(); i++)
    {
      xylinerenderer.setSeriesStroke(i, new BasicStroke(1.5f));
      xylinerenderer.setSeriesOutlineStroke(i, new BasicStroke(1.0f));
    }

    return chart;
  }
}

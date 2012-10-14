/**
 * ContactMapLayeredPane.java
 * @author Adam M. Smith
 * April 29, 2011
 * 
 * This class contains the main contact map visualization and its
 * context view.  The size of the context view is static, but the 
 * overall size of the window is set in the constructor and can
 * be changed with a method call after instantiation. 
 */
package visualizationviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.geom.Rectangle2D;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import prefuse.Display;
import prefuse.Visualization;

public class SpeciesBrowserLayeredPane extends JLayeredPane
{
  // The overview window size
  private final int OVERVIEW_WIDTH = 200;
  private final int OVERVIEW_HEIGHT = 125;

  // The width of the borders in pixels
  private final int BORDER_WIDTH = 1;

  // The size of the main visualization window.
  private Dimension currentSize;

  // The JPanels that hold the visualization and the overview
  private JPanel speciesBrowserJPanel;
  private JPanel speciesBrowserOverviewJPanel;

  // The overview has a rectangle that represents the current viewable area
  // in the main visualization. The RectanglePanel is the rectangle,
  // and it is accessed via the SelectBoxControl.
  private RectanglePanel speciesBrowserRectanglePanel;
  private SelectBoxControl speciesBrowserSelectBoxControl;

  // The border object that the two JPanels share.
  private Border border;


  /**
   * Constructor
   * 
   * @param size
   *          - The Dimension object describing the size (in pixels) of the main
   *          visualization.
   */
  public SpeciesBrowserLayeredPane(Dimension size)
  {
    // Set the value of the local size variable.
    currentSize = size;

    // Instantiate the border object.
    border = new LineBorder(Color.GRAY, BORDER_WIDTH);

    // Instantiate the JPanel for the overview and set its border
    speciesBrowserOverviewJPanel = new JPanel(new BorderLayout());
    speciesBrowserOverviewJPanel.setBorder(border);

    // Instantiate the JPanel for the main visualization and set its border.
    speciesBrowserJPanel = new JPanel(new BorderLayout());
    speciesBrowserJPanel.setBorder(border);

    // Add the JPanels to the JLayeredPane (this object)
    this.add(speciesBrowserJPanel, new Integer(0));
    this.add(speciesBrowserOverviewJPanel, new Integer(1));

    // Update the sizes of the JPanels and Displays
    resizeSpeciesBrowser(size);

  }


  /**
   * Use this to set the Display object to use for the contact map.
   * 
   * @param d
   *          - The prefuse.Display object for the visualization.
   */
  public void setSpeciesBrowser(Display d)
  {
    if (speciesBrowserJPanel.getComponentCount() > 0)
    {
      speciesBrowserJPanel.removeAll();
    }

    if (d != null)
    {
      speciesBrowserJPanel.add(d, BorderLayout.CENTER);
    }
  }


  public void setSpeciesBrowserOverview(Display od)
  {
    if (speciesBrowserOverviewJPanel.getComponentCount() > 0)
    {
      speciesBrowserOverviewJPanel.removeAll();
    }

    if (od != null && speciesBrowserJPanel.getComponentCount() == 1)
    {
      // absolute layout
      speciesBrowserOverviewJPanel.setLayout(null);

      Display dis_focus = (Display) speciesBrowserJPanel.getComponent(0);

      // compute the size of select box
      int x, y, width, height;
      double ratio; // scale of overview over focus

      double[] result = computeSelectBox(speciesBrowserOverviewJPanel, od,
          dis_focus, "component_graph");
      x = (int) result[0];
      y = (int) result[1];
      width = (int) result[2];
      height = (int) result[3];
      ratio = result[4];

      // get insets of overviewPanel
      // Insets insets = cMapOverviewJPanel.getInsets();
      // get the size of overviewPanel
      // Dimension size_panel = cMapOverviewJPanel.getSize();

      // create select box
      speciesBrowserRectanglePanel = new RectanglePanel(width, height);
      // set bounds for select box
      speciesBrowserRectanglePanel.setBounds(x, y, width, height);
      // set select box be transparent
      speciesBrowserRectanglePanel.setOpaque(false);
      // add select box to panel
      speciesBrowserOverviewJPanel.add(speciesBrowserRectanglePanel);

      // set bounds for overview display
      // od.setBounds(insets.left, insets.top, size_panel.width/2,
      // size_panel.height);
      // od.setBounds(BORDER_WIDTH, BORDER_WIDTH,
      // OVERVIEW_WIDTH-BORDER_WIDTH*2, OVERVIEW_HEIGHT-BORDER_WIDTH);
      // add overview display to panel
      speciesBrowserOverviewJPanel.add(od);
      // cMapOverviewJPanel.revalidate();

      // add mouse listener for select box
      speciesBrowserSelectBoxControl = new SelectBoxControl(x, y, width,
          height, speciesBrowserRectanglePanel, dis_focus, ratio);
      speciesBrowserRectanglePanel
          .addMouseListener(speciesBrowserSelectBoxControl);
      speciesBrowserRectanglePanel
          .addMouseMotionListener(speciesBrowserSelectBoxControl);
    }

    resizeSpeciesBrowser();

  }


  public void updateSpeciesBrowserSelectBox()
  {
    if (speciesBrowserJPanel.getComponentCount() == 1
        && speciesBrowserOverviewJPanel.getComponentCount() == 2)
    {
      double[] result = computeSelectBox(speciesBrowserOverviewJPanel,
          (Display) speciesBrowserOverviewJPanel.getComponent(1),
          (Display) speciesBrowserJPanel.getComponent(0), "component_graph");
      /*
       * x = (int)result[0]; y = (int)result[1]; width = (int)result[2]; height
       * = (int)result[3]; double ratio = result[4];
       */
      speciesBrowserSelectBoxControl.updateInfo((int) result[0],
          (int) result[1], (int) result[2], (int) result[3], result[4]);
    }

  }


  public Dimension getSpeciesBrowserSize()
  {
    return new Dimension(speciesBrowserJPanel.getSize().width
        - border.getBorderInsets(speciesBrowserJPanel).left
        - border.getBorderInsets(speciesBrowserJPanel).right,
        speciesBrowserJPanel.getSize().height
            - border.getBorderInsets(speciesBrowserJPanel).top
            - border.getBorderInsets(speciesBrowserJPanel).bottom);
  }


  public Dimension getSpeciesBrowserOverviewSize()
  {
    return new Dimension(speciesBrowserOverviewJPanel.getSize().width
        - border.getBorderInsets(speciesBrowserOverviewJPanel).left
        - border.getBorderInsets(speciesBrowserOverviewJPanel).right,
        speciesBrowserOverviewJPanel.getSize().height
            - border.getBorderInsets(speciesBrowserOverviewJPanel).top
            - border.getBorderInsets(speciesBrowserOverviewJPanel).bottom);
  }


  public void resizeSpeciesBrowser()
  {
    resizeSpeciesBrowser(currentSize);
  }


  public void resizeSpeciesBrowser(Dimension size)
  {
    currentSize = size;

    if (speciesBrowserJPanel != null && speciesBrowserOverviewJPanel != null)
    {
      // TODO why would there be more than 1 component in the main view?

      speciesBrowserJPanel.setBounds(0, 0, currentSize.width,
          currentSize.height);
      speciesBrowserOverviewJPanel.setBounds(0, size.height - OVERVIEW_HEIGHT,
          OVERVIEW_WIDTH - BORDER_WIDTH, OVERVIEW_HEIGHT - BORDER_WIDTH);

      if (speciesBrowserJPanel.getComponentCount() == 1
          && speciesBrowserOverviewJPanel.getComponentCount() > 1)
      {
        ((Display) speciesBrowserJPanel.getComponent(0)).setSize(new Dimension(
            currentSize.width - BORDER_WIDTH * 2, currentSize.height
                - BORDER_WIDTH * 2));
        ((Display) speciesBrowserOverviewJPanel.getComponent(1)).setBounds(
            BORDER_WIDTH, BORDER_WIDTH, OVERVIEW_WIDTH - BORDER_WIDTH * 3,
            OVERVIEW_HEIGHT - BORDER_WIDTH * 2);
        ((Display) speciesBrowserOverviewJPanel.getComponent(1))
            .setSize(new Dimension(OVERVIEW_WIDTH - BORDER_WIDTH * 3,
                OVERVIEW_HEIGHT - BORDER_WIDTH * 2));

      }
    }
  }


  /**
   * Compute select box based on linear relationship between overview display
   * and focus display
   * 
   * @param overviewPanel
   *          JPanel for the overview display
   * @param overviewDis
   *          overview display
   * @param focusDis
   *          focus display
   * @param graphName
   *          name of the visualization in display
   * @return [x, y, width, height, ratio(scale of overview over focus)] of
   *         select box
   */
  private double[] computeSelectBox(JPanel overviewPanel, Display overviewDis,
      Display focusDis, String graphName)
  {
    double[] result = new double[5];

    // get insets of overviewPanel
    Insets insets = overviewPanel.getInsets();

    // compute the size of select box
    int x, y, width, height;

    // focus display width height
    Display dis_focus = focusDis;
    double scale_focus = dis_focus.getScale();
    int dis_focus_width = dis_focus.getVisibleRect().width;
    int dis_focus_height = dis_focus.getVisibleRect().height;

    // compute ratio
    double scale_overview = overviewDis.getScale();
    double ratio = scale_overview / scale_focus;

    // set width and height for select box
    width = (int) (dis_focus_width * ratio);
    height = (int) (dis_focus_height * ratio);

    // compute the position of select box

    // focus display x y
    double dis_focus_x = dis_focus.getDisplayX();
    double dis_focus_y = dis_focus.getDisplayY();

    // focus vis x y
    Visualization vis_focus = dis_focus.getVisualization();
    Rectangle2D visbounds_focus = vis_focus.getBounds(graphName);
    double vis_focus_x = visbounds_focus.getX();
    double vis_focus_y = visbounds_focus.getY();

    // compute distance
    int distance_x = (int) ((dis_focus_x - vis_focus_x) * ratio);
    int distance_y = (int) ((dis_focus_y - vis_focus_y) * ratio);

    // overview dis x y
    double dis_overview_x = overviewDis.getDisplayX();
    double dis_overview_y = overviewDis.getDisplayY();

    // overview vis x y
    Visualization vis_overview = overviewDis.getVisualization();
    Rectangle2D visbounds_overview = vis_overview.getBounds(graphName);
    double vis_overview_x = visbounds_overview.getX() * ratio;
    double vis_overview_y = visbounds_overview.getY() * ratio;

    // set x and y for select box upper-left corner
    x = (int) vis_overview_x + (insets.left - (int) dis_overview_x)
        + distance_x;
    y = (int) vis_overview_y + (insets.top - (int) dis_overview_y) + distance_y;

    result[0] = x;
    result[1] = y;
    result[2] = width;
    result[3] = height;
    result[4] = ratio;
    return result;
  }

}

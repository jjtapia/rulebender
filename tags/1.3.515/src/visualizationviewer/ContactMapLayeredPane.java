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
import prefuse.visual.VisualItem;

public class ContactMapLayeredPane extends JLayeredPane {
	// The overview window size
	private final int OVERVIEW_WIDTH = 150;
	private final int OVERVIEW_HEIGHT = 100;

	// The width of the borders in pixels
	private final int BORDER_WIDTH = 1;

	// The size of the main visualization window.
	private Dimension currentSize;

	// The JPanels that hold the visualization and the overview
	private JPanel cMapJPanel;
	private JPanel cMapOverviewJPanel;

	// The overview has a rectangle that represents the current viewable area
	// in the main visualization. The RectanglePanel is the rectangle,
	// and it is accessed via the SelectBoxControl.
	private RectanglePanel cMapRectanglePanel;
	private SelectBoxControl cMapSelectBoxControl;

	// The border object that the two JPanels share.
	private Border border;

	/**
	 * Constructor
	 * 
	 * @param size
	 *            - The Dimension object describing the size (in pixels) of the
	 *            main visualization.
	 */
	public ContactMapLayeredPane(Dimension size) {
		// Set the value of the local size variable.
		currentSize = size;

		// Instantiate the border object.
		border = new LineBorder(Color.GRAY, BORDER_WIDTH);

		// Instantiate the JPanel for the overview and set its border
		cMapOverviewJPanel = new JPanel(new BorderLayout());
		cMapOverviewJPanel.setBorder(border);

		// Instantiate the JPanel for the main visualization and set its border.
		cMapJPanel = new JPanel(new BorderLayout());
		cMapJPanel.setBorder(border);

		// Add the JPanels to the JLayeredPane (this object)
		this.add(cMapJPanel, new Integer(0));
		this.add(cMapOverviewJPanel, new Integer(1));

		// Update the sizes of the JPanels and Displays
		resizeCMap(size);
		updateCMapSelectBox();
	}

	/**
	 * Use this to set the Display object to use for the contact map.
	 * 
	 * @param d
	 *            - The prefuse.Display object for the visualization.
	 */
	public void setCMap(Display d) {
		if (cMapJPanel.getComponentCount() > 0) {
			cMapJPanel.removeAll();
		}

		if (d != null) {
			cMapJPanel.add(d, BorderLayout.CENTER);
		}
	}

	public void setCMapOverview(Display od) {

		// system.out.println("------------------Setting Overview----------------");

		if (cMapOverviewJPanel.getComponentCount() > 0) {
			cMapOverviewJPanel.removeAll();
		}

		if (od != null && cMapJPanel.getComponentCount() == 1) {
			// absolute layout
			cMapOverviewJPanel.setLayout(null);

			Display dis_focus = (Display) cMapJPanel.getComponent(0);

			// create select box
			cMapRectanglePanel = new RectanglePanel(0, 0);
			cMapOverviewJPanel.add(cMapRectanglePanel);

			// add overview display to panel
			cMapOverviewJPanel.add(od);

			// add mouse listener for select box
			cMapSelectBoxControl = new SelectBoxControl(0, 0, 0, 0,
					cMapRectanglePanel, dis_focus, 0);
			cMapRectanglePanel.addMouseListener(cMapSelectBoxControl);
			cMapRectanglePanel.addMouseMotionListener(cMapSelectBoxControl);

			updateCMapSelectBox();
		}

		cMapOverviewJPanel.revalidate();

		resizeCMap();

		updateCMapSelectBox();
	}

	/**
	 * Call to update the size and location of the CMap select box.
	 */
	public void updateCMapSelectBox() {
		// system.out.println("------------------Updating Select Box----------------");

		if (cMapJPanel.getComponentCount() == 1
				&& cMapOverviewJPanel.getComponentCount() == 2) {
			double[] result = computeSelectBox(cMapOverviewJPanel,
					(Display) cMapOverviewJPanel.getComponent(1),
					(Display) cMapJPanel.getComponent(0), "component_graph");
			/*
			 * x = (int)result[0]; y = (int)result[1]; width = (int)result[2];
			 * height = (int)result[3]; double ratio = result[4];
			 */

			// system.out.println("Computed: \n\tx: "+ result[0] +
			// "\n\ty: " + result[1] +
			// "\n\twidth: " + result[2] +
			// "\n\theight: " + result[3]);

			cMapSelectBoxControl.updateInfo((int) result[0], (int) result[1],
					(int) result[2], (int) result[3], result[4]);

			// ((Display)cMapOverviewJPanel.getComponent(1)).repaint();
			// cMapOverviewJPanel.repaint();
		}

	}

	public Dimension getCMapSize() {
		return new Dimension(cMapJPanel.getSize().width
				- border.getBorderInsets(cMapJPanel).left
				- border.getBorderInsets(cMapJPanel).right,
				cMapJPanel.getSize().height
						- border.getBorderInsets(cMapJPanel).top
						- border.getBorderInsets(cMapJPanel).bottom);
	}

	public Dimension getCMapOverviewSize() {
		return new Dimension(cMapOverviewJPanel.getSize().width
				- border.getBorderInsets(cMapOverviewJPanel).left
				- border.getBorderInsets(cMapOverviewJPanel).right,
				cMapOverviewJPanel.getSize().height
						- border.getBorderInsets(cMapOverviewJPanel).top
						- border.getBorderInsets(cMapOverviewJPanel).bottom);
	}

	public void resizeCMap() {
		resizeCMap(currentSize);
	}

	public void resizeCMap(Dimension size) {
		currentSize = size;

		if (cMapJPanel != null && cMapOverviewJPanel != null) {
			cMapJPanel.setBounds(0, 0, currentSize.width, currentSize.height);
			cMapOverviewJPanel.setBounds(0, size.height - OVERVIEW_HEIGHT,
					OVERVIEW_WIDTH - BORDER_WIDTH, OVERVIEW_HEIGHT
							- BORDER_WIDTH);

			if (cMapJPanel.getComponentCount() == 1
					&& cMapOverviewJPanel.getComponentCount() > 1) {
				((Display) cMapJPanel.getComponent(0)).setSize(new Dimension(
						currentSize.width - BORDER_WIDTH * 2,
						currentSize.height - BORDER_WIDTH * 2));
				((Display) cMapOverviewJPanel.getComponent(1)).setBounds(
						BORDER_WIDTH, BORDER_WIDTH, OVERVIEW_WIDTH
								- BORDER_WIDTH * 3, OVERVIEW_HEIGHT
								- BORDER_WIDTH * 2);
				((Display) cMapOverviewJPanel.getComponent(1))
						.setSize(new Dimension(OVERVIEW_WIDTH - BORDER_WIDTH
								* 3, OVERVIEW_HEIGHT - BORDER_WIDTH * 2));

			}
		}

		updateCMapSelectBox();
	}

	/**
	 * Compute select box based on linear relationship between overview display
	 * and focus display
	 * 
	 * @param overviewPanel
	 *            JPanel for the overview display
	 * @param overviewDis
	 *            overview display
	 * @param focusDis
	 *            focus display
	 * @param graphName
	 *            name of the visualization in display
	 * @return [x, y, width, height, ratio(scale of overview over focus)] of
	 *         select box
	 */
	private double[] computeSelectBox(JPanel overviewPanel,
			Display overviewDis, Display focusDis, String graphName) {
		// system.out.println("-----------------------COMPUTING----------------------------");

		double[] result = new double[5];

		// get insets of overviewPanel
		Insets insets = overviewPanel.getInsets();

		// for the size of the select box
		int x, y, width, height;

		// focus display width height
		Display dis_focus = focusDis;
		double scale_focus = dis_focus.getScale();
		int dis_focus_width = dis_focus.getVisibleRect().width;
		int dis_focus_height = dis_focus.getVisibleRect().height;

		// system.out.println("\tfocus scale: " + scale_focus +
		// "\n\tfocus width: " + dis_focus_width +
		// "\n\tfocus height: " + dis_focus_height);

		// compute ratio

		// TODO bug here, the overviewDis.getScale is returning 1, when the
		// overview window should have a smaller scale.
		double scale_overview = overviewDis.getScale();
		double ratio = scale_overview / scale_focus;

		// system.out.println("\toverview scale: " + scale_overview +
		// "\n\tratio: " + ratio);

		// set width and height for select box
		width = (int) (dis_focus_width * ratio);
		height = (int) (dis_focus_height * ratio);

		// system.out.println("\tselect box width: " + width +
		// "\n\tselect box height: " + height);

		// compute the position of select box

		// focus display x y
		double dis_focus_x = dis_focus.getDisplayX();
		double dis_focus_y = dis_focus.getDisplayY();

		// focus vis x y
		Visualization vis_focus = dis_focus.getVisualization();
		Rectangle2D visbounds_focus = vis_focus.getBounds(graphName);
		double vis_focus_x = visbounds_focus.getX();
		double vis_focus_y = visbounds_focus.getY();

		// system.out.println("\n\tfocus dis x: " + dis_focus_x +
		// "\n\tfocus dis y: " + dis_focus_y +
		// "\n\tfocus vis x: " + vis_focus_x +
		// "\n\tfocus vis y: " + vis_focus_y);

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
		y = (int) vis_overview_y + (insets.top - (int) dis_overview_y)
				+ distance_y;

		result[0] = x;
		result[1] = y;
		result[2] = width;
		result[3] = height;
		result[4] = ratio;
		return result;
	}

	public Visualization getContactMapVisualization() {
		if (cMapJPanel.getComponentCount() > 0) {
			return ((Display) cMapJPanel.getComponent(0)).getVisualization();
		} else {
			return null;
		}
	}

}

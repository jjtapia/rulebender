package visualizationviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.visual.VisualItem;

public class InfluenceGraphLayeredPane extends JLayeredPane {

	private final int OVERVIEW_WIDTH = 200;
	private final int OVERVIEW_HEIGHT = 100;
	private final int BORDER_WIDTH = 1;

	private Dimension currentSize;

	private JPanel iGraphJPanel;
	private JPanel iGraphOverviewJPanel;

	private RectanglePanel iGraphRectanglePanel;
	private SelectBoxControl iGraphSelectBoxControl;

	private Border border;

	public InfluenceGraphLayeredPane(Dimension size) {
		super();

		currentSize = size;

		border = new LineBorder(Color.GRAY, BORDER_WIDTH);

		iGraphOverviewJPanel = new JPanel();
		iGraphOverviewJPanel.setBorder(border);
		iGraphOverviewJPanel.setBackground(Color.WHITE);

		iGraphJPanel = new JPanel(new BorderLayout());
		iGraphJPanel.setBorder(border);
		iGraphJPanel.setBackground(Color.WHITE);

		this.add(iGraphJPanel, new Integer(0));
		this.add(iGraphOverviewJPanel, new Integer(1));
	}

	public void setIGraph(Display d) {
		if (iGraphJPanel.getComponentCount() > 0) {
			// This should allow the visualization to be garbage collected.
			iGraphJPanel.removeAll();
			// iGraphJPanel.revalidate();
		}

		if (d != null) {
			iGraphJPanel.add(d, BorderLayout.CENTER);
			// iGraphJPanel.revalidate();
		}

	}

	public void setIGraphOverview(Display od) {
		if (iGraphOverviewJPanel.getComponentCount() > 0) {
			iGraphOverviewJPanel.removeAll();
			// iGraphOverviewJPanel.revalidate();
		}

		if (od != null && iGraphJPanel.getComponentCount() == 1) {
			// absolute layout
			iGraphOverviewJPanel.setLayout(null);

			Display dis_focus = (Display) iGraphJPanel.getComponent(0);

			// compute the size of select box
			int x, y, width, height;
			double ratio; // scale of overview over focus

			double[] result = computeSelectBox(iGraphOverviewJPanel, od,
					dis_focus, "igraph");
			x = (int) result[0];
			y = (int) result[1];
			width = (int) result[2];
			height = (int) result[3];
			ratio = result[4];

			// get insets of overviewPanel
			// Insets insets = iGraphOverviewJPanel.getInsets();
			// get the size of overviewPanel
			// Dimension size_panel = iGraphOverviewJPanel.getSize();

			// create select box
			iGraphRectanglePanel = new RectanglePanel(width, height);
			// set bounds for select box
			iGraphRectanglePanel.setBounds(x, y, width, height);
			// set select box be transparent
			iGraphRectanglePanel.setOpaque(false);
			// add select box to panel
			iGraphOverviewJPanel.add(iGraphRectanglePanel);

			// set bounds for overview display
			// od.setBounds(insets.left, insets.top, size_panel.width,
			// size_panel.height);
			// add overview display to panel
			iGraphOverviewJPanel.add(od);
			// iGraphOverviewJPanel.revalidate();

			// add mouse listener for select box
			iGraphSelectBoxControl = new SelectBoxControl(x, y, width, height,
					iGraphRectanglePanel, dis_focus, ratio);
			iGraphRectanglePanel.addMouseListener(iGraphSelectBoxControl);
			iGraphRectanglePanel.addMouseMotionListener(iGraphSelectBoxControl);
		}

		resizeIGraph();
	}

	public void updateIGraphSelectBox() {

		if (iGraphJPanel.getComponentCount() == 1
				&& iGraphOverviewJPanel.getComponentCount() == 2) {
			double[] result = computeSelectBox(iGraphOverviewJPanel,
					(Display) iGraphOverviewJPanel.getComponent(1),
					(Display) iGraphJPanel.getComponent(0), "igraph");

			iGraphSelectBoxControl.updateInfo((int) result[0], (int) result[1],
					(int) result[2], (int) result[3], result[4]);
		}
	}

	public Dimension getIGraphSize() {
		return iGraphJPanel.getSize();
	}

	public Dimension getIGraphOverviewSize() {
		return iGraphOverviewJPanel.getSize();
	}

	private void resizeIGraph() {
		resizeIGraph(currentSize);
	}

	public void resizeIGraph(Dimension size) {
		currentSize = size;

		// TODO this was == 2 before, but I cannot think of any reason
		// why there would be two things in that JPanel.
		if (iGraphJPanel != null && iGraphOverviewJPanel != null) {
			iGraphJPanel.setBounds(0, 0, currentSize.width, currentSize.height);
			iGraphOverviewJPanel.setBounds(0, currentSize.height
					- OVERVIEW_HEIGHT, OVERVIEW_WIDTH - BORDER_WIDTH,
					OVERVIEW_HEIGHT - BORDER_WIDTH);

			if (iGraphJPanel.getComponentCount() == 1
					&& iGraphOverviewJPanel.getComponentCount() > 1) {
				((Display) iGraphJPanel.getComponent(0)).setSize(new Dimension(
						size.width - BORDER_WIDTH * 2, size.height
								- BORDER_WIDTH * 2));
				((Display) iGraphOverviewJPanel.getComponent(1)).setBounds(
						BORDER_WIDTH, BORDER_WIDTH, OVERVIEW_WIDTH
								- BORDER_WIDTH * 3, OVERVIEW_HEIGHT
								- BORDER_WIDTH * 2);
				((Display) iGraphOverviewJPanel.getComponent(1))
						.setSize(new Dimension(OVERVIEW_WIDTH - BORDER_WIDTH
								* 3, OVERVIEW_HEIGHT - BORDER_WIDTH * 2));
			}
		}

		updateIGraphSelectBox();
	}

	/**
	 * Compute select box based on linear relationship between overview display
	 * and focus display
	 * 
	 * TODO this should be a part of the rectangle control or rectangle panel
	 * class, but I don't have the time to refactor that right now.
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
		y = (int) vis_overview_y + (insets.top - (int) dis_overview_y)
				+ distance_y;

		result[0] = x;
		result[1] = y;
		result[2] = width;
		result[3] = height;
		result[4] = ratio;
		return result;
	}

	public Visualization getInfluenceGraphVisualization() {
		if (iGraphJPanel.getComponentCount() > 0) {
			return ((Display) iGraphJPanel.getComponent(0)).getVisualization();
		} else {
			return null;
		}
	}
}

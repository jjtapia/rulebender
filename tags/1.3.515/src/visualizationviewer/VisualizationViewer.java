package visualizationviewer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.TableModel;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import visualizationviewer.annotation.MultiLineCellRenderer;
import visualizationviewer.annotation.TableMouseListener;

class VisualizationViewer extends JFrame 
{
	public static String CMAP = "Contact Map";
	public static String IGRAPH = "Influence Graph";
	public static String SPECIESBROWSER = "Species Browser";
	
	// Static and private for singleton pattern.  
	// This will be the shared instance.
	private static VisualizationViewer viewer;
	
	private JTabbedPane tabs;
		
	private JSplitPane mainPanel,
					   cMapAnnotationSplit;
			
	// The panel that holds the annotation.
	// Goes into the globalAnnotationSplit
	private JPanel annotation;
	
	// A LayeredPane for each visualization
	private ContactMapLayeredPane cMapLayeredPane;
	private InfluenceGraphLayeredPane iGraphLayeredPane;
	private SpeciesBrowserLayeredPane speciesBrowserLayeredPane;
	
	private JPanel cMapJP, iGraphJP, speciesBrowserJP;
	
	private VisualizationViewerController control;
	
	private Border border;
	
	private final Dimension cMapSize = new Dimension(425, 400);
	private final Dimension iGraphSize = new Dimension(600, 350);
	private final Dimension speciesBrowserSize = new Dimension(600, 400);
	
	/**
	 * Private constructor for singleton pattern.  The constructor
	 * instantiates all of the gui elements and packs them into
	 * the frame.  
	 * 
	 * @param control_in 	This argument is the controller that will be sending
	 * 						events and commands to the viewer.?	 */
	private VisualizationViewer(VisualizationViewerController control_in)
	{
		// Sent to JFrame constructor.
		super("Visualization Viewer");
	
		// Setup the controller
		control = control_in;
		
		// Create a border for the JPanels
		border = new LineBorder(Color.WHITE);
		
		// Create the Contact Map and Influence Graph visualization panels.
		// These contain both the visualization and the contact window.
		cMapLayeredPane = new ContactMapLayeredPane(cMapSize);
		iGraphLayeredPane = new InfluenceGraphLayeredPane(iGraphSize);
		speciesBrowserLayeredPane = new SpeciesBrowserLayeredPane(speciesBrowserSize);
		
		// Create the JPanel that holds the visualization, give it a layout,
		// set the background color, and give it a size.
		cMapJP = new JPanel();
		cMapJP.setLayout(new BorderLayout());
		cMapJP.setBackground(Color.WHITE);
		cMapJP.setPreferredSize(cMapSize);

		cMapJP.addComponentListener(new ComponentListener() 
		{
			
			public void componentShown(ComponentEvent e) 
			{}
			
			public void componentResized(ComponentEvent e) 
			{
				//System.out.println("CMapJP resized");
				resizeCMap();
			}
			
			public void componentMoved(ComponentEvent e) {}
			
			public void componentHidden(ComponentEvent arg0) {}
		});
		
		
		iGraphJP = new JPanel();
		iGraphJP.setLayout(new BorderLayout());
		iGraphJP.setBackground(Color.WHITE);
		iGraphJP.addComponentListener(new ComponentListener() {
			
			public void componentShown(ComponentEvent e) 
			{}
			
			public void componentResized(ComponentEvent e) 
			{
				//System.out.println("iGraphJP resized");
				resizeIGraph();
			}
			
			public void componentMoved(ComponentEvent e) {}
			
			public void componentHidden(ComponentEvent arg0) {}
		});
		
		speciesBrowserJP = new JPanel();
		speciesBrowserJP.setLayout(new BorderLayout());
		speciesBrowserJP.setBackground(Color.WHITE);
		speciesBrowserJP.addComponentListener(new ComponentListener() {
			
			public void componentShown(ComponentEvent e) 
			{}
			
			public void componentResized(ComponentEvent e) 
			{
				//System.out.println("iGraphJP resized");
				resizeSpeciesBrowser();
			}
			
			public void componentMoved(ComponentEvent e) {}
			
			public void componentHidden(ComponentEvent arg0) {}
		});
		
		// Add the visualizations to the JPanels.
		cMapJP.add(cMapLayeredPane, BorderLayout.CENTER);
		iGraphJP.add(iGraphLayeredPane, BorderLayout.CENTER);
		speciesBrowserJP.add(speciesBrowserLayeredPane, BorderLayout.CENTER);
		
		// The annotation box
		annotation = new JPanel(new BorderLayout());
		annotation.setBorder(border);
		annotation.setPreferredSize(new Dimension(150, 275));
		annotation.setBackground(Color.WHITE);
			
		// Create the contact map and observable split panel
		cMapAnnotationSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		cMapAnnotationSplit.setLeftComponent(cMapJP);
		cMapAnnotationSplit.setRightComponent(annotation);
		
		// Add everything to the main panel.
		mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainPanel.setTopComponent(cMapAnnotationSplit);
		mainPanel.setBottomComponent(iGraphJP);
		
		/* Now Set up the species browser tab */
		
		// Species Browser
		/*speciesBrowserJPanel = new JPanel(new BorderLayout());
		speciesBrowserJPanel.setBorder(border);
		speciesBrowserJPanel.setPreferredSize(new Dimension(400, 217));
		speciesBrowserJPanel.setBackground(Color.WHITE);
		
		// Species Browser overview
		speciesBrowserOverviewJPanel = new JPanel();
		speciesBrowserOverviewJPanel.setBorder(border);
		speciesBrowserJPanel.setPreferredSize(new Dimension(400, 125));
		speciesBrowserOverviewJPanel.setBackground(Color.WHITE);
		 
		
		mainBrowserPanel = new JPanel();
		mainBrowserPanel.setLayout(new BoxLayout(mainBrowserPanel, BoxLayout.Y_AXIS));
		mainBrowserPanel.add(speciesBrowserJPanel);
		mainBrowserPanel.add(speciesBrowserOverviewJPanel);
		*/

		/*
		 *  Add the tabs to the tabbed pane
		 */
		
		tabs = new JTabbedPane();
		tabs.add(mainPanel, "Model Visualization");
		//tabs.add(mainBrowserPanel, "Species Browser");
		tabs.add(speciesBrowserJP, "Species Browser");
		
		this.addComponentListener(new ComponentListener() {
			
			public void componentShown(ComponentEvent e) {}
			
			public void componentResized(ComponentEvent e) 
			{
				control.windowResized();
			}
			
			public void componentMoved(ComponentEvent e) {}
			
			public void componentHidden(ComponentEvent arg0) {}
		});
		
		// Add the panel to the frame.
		getContentPane().add(tabs);
		
		// Set the size of the frame.
		this.setPreferredSize(new Dimension(650, 700));
		
		// Pack it.  It is ready to be visibly enabled.
		this.pack();
	}
	
	/**
	 * This method is used instead of the constructor.  Only one 
	 * instance of the VisualizationViewer can be created.
	 * @return the VisualizationViewer instance. 
	 */
	protected static synchronized VisualizationViewer getViewer(VisualizationViewerController controller_in)
	{
		if(viewer == null)
		{
			viewer = new VisualizationViewer(controller_in);
			
		}
		return viewer;
	}	
	
	
	/**
	 * Give the VisualizationViewer a Display to use for
	 * the contact map.  
	 * 
	 * @param d A Display instance 
	 */
	protected void setCMap(Display d)
	{
		cMapLayeredPane.setCMap(d);
		
		//TODO could be part of the flicker
		this.update(this.getGraphics());//
	}	
	
	/**
	 * Give the VisualizationViewer a Display to use for
	 * the influence graph.  
	 * 
	 * @param d A Display instance 
	 */
	protected void setIGraph(Display d)
	{
		iGraphLayeredPane.setIGraph(d);
		
		//TODO could be part of the flicker
		this.update(this.getGraphics());
	}
	
	/**
	 * Give the VisualizationViewer a Display to use for
	 * the species browser.  
	 * 
	 * @param d A Display instance 
	 */
	protected void setSpeciesBrowser(Display d)
	{
		speciesBrowserLayeredPane.setSpeciesBrowser(d);
		
		/*
		if(speciesBrowserJPanel.getComponentCount() > 0)
		{
			// This should allow the visualization to be garbage collected.
			speciesBrowserJPanel.removeAll();
			//speciesBrowserJPanel.revalidate();
		}
		if(d != null)
		{
			speciesBrowserJPanel.add(d, BorderLayout.CENTER);
			//speciesBrowserJPanel.revalidate();
		}
		*/
		
		tabs.setSelectedComponent(speciesBrowserJP);
		
		//TODO could be part of the flicker
		this.update(this.getGraphics());
		
	}
	
	/**
	 * Give the VisualizationViewer a Display to use for the over
	 * of the cMap
	 * @param cMapOverview
	 */
	protected void setCMapOverview(Display od) 
	{
		cMapLayeredPane.setCMapOverview(od);
		
		this.update(this.getGraphics());
		//overview.updateUI();
	}
	
	public void updateCMapSelectBox()
	{
		cMapLayeredPane.updateCMapSelectBox();
	}
	
	/**
	 * Give the VisualizationViewer a Display to use for the over
	 * of the iGraph
	 * @param iGraphOverview
	 */
	protected void setIGraphOverview(Display od) 
	{
		iGraphLayeredPane.setIGraphOverview(od);
		
		//TODO could be part of the flicker
		this.update(this.getGraphics());
		//overview.updateUI();
	}
	
	public void updateIGraphSelectBox()
	{
		iGraphLayeredPane.updateIGraphSelectBox();
	}

	/**
	 * Give the VisualizationViewer a Display to use for the over
	 * of the speciesBrowser.
	 * @param speciesBrowserOverview
	 */
	protected void setSpeciesBrowserOverview(Display od) 
	{
		speciesBrowserLayeredPane.setSpeciesBrowserOverview(od);
		
		/*
		if(speciesBrowserOverviewJPanel.getComponentCount() > 0)
		{
			speciesBrowserOverviewJPanel.removeAll();
			//speciesBrowserOverviewJPanel.revalidate();
		}
		
		if (speciesBrowserOverview != null && speciesBrowserJPanel.getComponentCount() == 1) {

			// absolute layout
			speciesBrowserOverviewJPanel.setLayout(null);

			Display dis_focus = (Display) speciesBrowserJPanel.getComponent(0);

			// compute the size of select box
			int x, y, width, height;
			double ratio; // scale of overview over focus

			double[] result = computeSelectBox(speciesBrowserOverviewJPanel,
					speciesBrowserOverview, dis_focus, "component_graph");
			x = (int) result[0];
			y = (int) result[1];
			width = (int) result[2];
			height = (int) result[3];
			ratio = result[4];

			// get insets of overviewPanel
			Insets insets = speciesBrowserOverviewJPanel.getInsets();
			// get the size of overviewPanel
			Dimension size_panel = speciesBrowserOverviewJPanel.getSize();

			// create select box
			speciesBrowserRectanglePanel = new RectanglePanel(width, height);
			// set bounds for select box
			speciesBrowserRectanglePanel.setBounds(x, y, width, height);
			// set select box be transparent
			speciesBrowserRectanglePanel.setOpaque(false);
			// add select box to panel
			speciesBrowserOverviewJPanel.add(speciesBrowserRectanglePanel);

			// set bounds for overview display
			speciesBrowserOverview.setBounds(insets.left, insets.top,
					size_panel.width, size_panel.height);
			// add overview display to panel
			speciesBrowserOverviewJPanel.add(speciesBrowserOverview);
			// speciesBrowserOverviewJPanel.revalidate();

			// add mouse listener for select box
			speciesBrowserSelectBoxControl = new SelectBoxControl(x, y, width,
					height, speciesBrowserRectanglePanel, dis_focus, ratio);
			speciesBrowserRectanglePanel.addMouseListener(speciesBrowserSelectBoxControl);
			speciesBrowserRectanglePanel.addMouseMotionListener(speciesBrowserSelectBoxControl);
		}
		*/
		
		//TODO could be part of the flicker
		this.update(this.getGraphics());
		//overview.updateUI();
	}
	
	public void updateSpeciesBrowserSelectBox()
	{
		speciesBrowserLayeredPane.updateSpeciesBrowserSelectBox();
		/*
		
		if(speciesBrowserJPanel.getComponentCount() == 1 && speciesBrowserOverviewJPanel.getComponentCount() == 2)
		{
			
			double[] result = computeSelectBox(speciesBrowserOverviewJPanel,
					(Display) speciesBrowserOverviewJPanel.getComponent(1),
					(Display) speciesBrowserJPanel.getComponent(0),
					"component_graph");
			
			speciesBrowserSelectBoxControl.updateInfo((int) result[0], (int) result[1], (int) result[2], (int) result[3], result[4]);
		}
		*/
		
	}
	
	/**
	 * Give the panel a TabelModel object to display
	 * @param tm
	 */
	protected void setAnnotationPanel(TableModel tm) 
	{	
		if (annotation.getComponentCount() == 0 && tm == null) 
		{
			return;
		}
		
		if (annotation.getComponentCount() > 0) 
		{
			annotation.removeAll();
		}
		
		// create a table with tablemodel
		JTable table = new JTable(tm);
		// When this property is true the table uses the entire height of the container, 
		// even if the table doesn't have enough rows to use the whole vertical space. 
		//table.setFillsViewportHeight(true);
		// set table row height and be able to wrap lines
		table.setDefaultRenderer(String.class, new MultiLineCellRenderer());
		table.addMouseListener(new TableMouseListener());
		
		// add scrollable table
		// The scroll pane automatically places the table header at the top of the viewport. 
		JScrollPane pane = new JScrollPane(table);
		annotation.add(pane, BorderLayout.CENTER);
		annotation.updateUI();
	}
	
	/**
	 * Compute select box based on linear relationship between overview display and focus display
	 * 
	 * @param overviewPanel JPanel for the overview display
	 * @param overviewDis overview display
	 * @param focusDis focus display
	 * @param graphName name of the visualization in display
	 * @return [x, y, width, height, ratio(scale of overview over focus)] of select box
	 */
	/*
	private double[] computeSelectBox(JPanel overviewPanel, Display overviewDis, Display focusDis, String graphName) {
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
		width = (int)(dis_focus_width * ratio);
		height = (int)(dis_focus_height * ratio);
		
		// compute the position of select box
		
		// focus display x y
		double dis_focus_x = dis_focus.getDisplayX();
		double dis_focus_y = dis_focus.getDisplayY();
		
		// focus vis x y
		Visualization vis_focus= dis_focus.getVisualization();
		Rectangle2D visbounds_focus = vis_focus.getBounds(graphName);
		double vis_focus_x = visbounds_focus.getX();
		double vis_focus_y = visbounds_focus.getY();
		
		// compute distance
		int distance_x = (int)((dis_focus_x - vis_focus_x) * ratio);
		int distance_y = (int)((dis_focus_y - vis_focus_y) * ratio);
		
		// overview dis x y
		double dis_overview_x = overviewDis.getDisplayX();
		double dis_overview_y = overviewDis.getDisplayY();	
		
		// overview vis x y
		Visualization vis_overview= overviewDis.getVisualization();
		Rectangle2D visbounds_overview = vis_overview.getBounds(graphName);
		double vis_overview_x = visbounds_overview.getX() * ratio;
		double vis_overview_y = visbounds_overview.getY() * ratio;
				
		// set x and y for select box upper-left corner
		x = (int)vis_overview_x + (insets.left - (int)dis_overview_x) + distance_x;
		y = (int)vis_overview_y + (insets.top - (int)dis_overview_y) + distance_y;	
		
		result[0] = x;
		result[1] = y;
		result[2] = width;
		result[3] = height;
		result[4] = ratio;
		return result;
	}
*/
	/*
	 * Getters for size of display jpanels
	 */
	
	public Dimension getCMapSize() 
	{
		return cMapLayeredPane.getCMapSize();
	}
	
	public Dimension getIGraphSize() 
	{
		return iGraphLayeredPane.getIGraphSize();
	}
	
	public Dimension getSpeciesBrowserSize() 
	{
		return speciesBrowserLayeredPane.getSpeciesBrowserSize();
	}
	
	public Dimension getCMapOverviewSize() 
	{	
		return cMapLayeredPane.getCMapOverviewSize();
	}
	
	public Dimension getIGraphOverviewSize() 
	{
		return iGraphLayeredPane.getIGraphOverviewSize();
	}
	
	public Dimension getSpeciesBrowserOverviewSize() 
	{
		return speciesBrowserLayeredPane.getSpeciesBrowserOverviewSize();
	}
	
	/*
	public void resizeSpeciesBrowserOverviewDisplay() 
	{
		Dimension size = speciesBrowserOverviewJPanel.getSize();
		
		if(speciesBrowserOverviewJPanel.getComponentCount() == 2)
		{
			((Display)speciesBrowserOverviewJPanel.getComponent(1)).setSize(size);
		}
	}
	*/
	/*
	public void resizeCMapOverviewDisplay() 
	{		
		cMapLayeredPane.resizeCMapDisplay();
	}
	
	public void resizeIGraphOverviewDisplay() 
	{		
		iGraphLayeredPane.resizeIGraphOverviewDisplay();
	}
	*/
	/*
	public void resizeSpeciesBrowserDisplay() 
	{
		Dimension size = speciesBrowserJPanel.getSize();
		
		if(speciesBrowserJPanel.getComponentCount() == 1)
		{
			((Display)speciesBrowserJPanel.getComponent(0)).setSize(size);
		}
	}
	*/
	public void resizeCMap() 
	{		
		cMapLayeredPane.resizeCMap(cMapJP.getSize());
	}
	
	public void resizeIGraph() 
	{		
		iGraphLayeredPane.resizeIGraph(iGraphJP.getSize());
	}
	
	public void resizeSpeciesBrowser() 
	{		
		speciesBrowserLayeredPane.resizeSpeciesBrowser(speciesBrowserJP.getSize());
	}

	/**
	 * Given rule text, pass this call to the iGraphLayeredPane
	 * @param ruleText
	 * @return
	 */
	public Visualization getInfluenceGraphVisualization() 
	{
		if(iGraphLayeredPane != null)
			return iGraphLayeredPane.getInfluenceGraphVisualization();
		else
			return null;
	}
	
	/**
	 * Given node text, pass this call to the cMapLayeredPane
	 * @param nodeText
	 * @return
	 */
	public Visualization getContactMapVisualization() 
	{
		if(cMapLayeredPane != null)
			return cMapLayeredPane.getContactMapVisualization();
		else
			return null;
	}
}
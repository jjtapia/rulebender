package visualizationviewer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import editor.BNGEditor;

import prefuse.Display;
import prefuse.Visualization;

class VisualizationViewer extends JFrame 
{
	public static String CMAP = "Contact Map";
	public static String IGRAPH = "Influence Graph";
	public static String SPECIESBROWSER = "Species Browser";
	
	// Static and private for singleton pattern.  
	// This will be the shared instance.
	
	private static VisualizationViewer viewer;
	
	// This is the panel that holds all of the components. 
	private JPanel mainPanel;
	
	// The SplitPane between the visualizations and the lower 
	// annotation/observation panels. 
	private JSplitPane outermostSplit;
	
	// The tabs that hold the visualizations.
	private JTabbedPane visualizationTabs;
	
	// The split pane that goes on the bottom of the 
	// outermost split. 
	private JSplitPane globalAnnotationSplit;
	
	// The panel that holds the overview visualization.
	// Goes into the globalAnnotationSplit
	private JPanel overview;
	
	// The panel that holds the annotation.
	// Goes into the globalAnnotationSplit
	private JPanel annotation;
	
	// A JPanel for each visualization
	private JPanel cMapJPanel,
				   iGraphJPanel,
				   speciesBrowserJPanel;

	// An JPanel for each overview visualization
	private JPanel cMapOverviewJPanel, 
						  iGraphOverviewJPanel,
						  speciesBrowserOverviewJPanel;

	private RectanglePanel cMapRectanglePanel,
						   iGraphRectanglePanel,
						   speciesBrowserRectanglePanel;
	
	private SelectBoxControl cMapSelectBoxControl,
							 iGraphSelectBoxControl,
							 speciesBrowserSelectBoxControl;
	
	private VisualizationViewerController control;
	
	private CompoundBorder border;
	
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
	
		control = control_in;
		
		// Two borders that will become parts of the compound border.
		Border raisedbevel = new BevelBorder(BevelBorder.RAISED);
	    Border loweredbevel = new BevelBorder(BevelBorder.LOWERED);
		
	    // The border comprised of a lowered and raised bevel
	    border = new CompoundBorder(loweredbevel, raisedbevel);
		
	    // Set up the frame.  Bottom up.
	    
	    // The overviews
	    cMapOverviewJPanel = new JPanel();
	    iGraphOverviewJPanel = new JPanel();
	    speciesBrowserOverviewJPanel = new JPanel();  
	    
	    // The overview container
	    overview = new JPanel();
	    overview.setLayout(new CardLayout());
		overview.add("CMapO", cMapOverviewJPanel);
		overview.add("IGraphO", iGraphOverviewJPanel);
		overview.add("SpeciesBrowserO", speciesBrowserOverviewJPanel);
		overview.setBorder(border);
		
		// The annotation box
		annotation = new JPanel(new BorderLayout());
				
		// Splitpane for the annotation and overview
		globalAnnotationSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overview, annotation);
		globalAnnotationSplit.setDividerLocation(200);
		globalAnnotationSplit.setOneTouchExpandable(true);

		// The visualization panels
		cMapJPanel = new JPanel(new BorderLayout());
		iGraphJPanel = new JPanel(new BorderLayout());
		speciesBrowserJPanel = new JPanel(new BorderLayout());
		
		// The tabs for the visualizations.
		visualizationTabs = new JTabbedPane();
		visualizationTabs.addTab(CMAP, cMapJPanel);
		visualizationTabs.addTab(IGRAPH, iGraphJPanel);
		visualizationTabs.addTab(SPECIESBROWSER, speciesBrowserJPanel);
		
		
		visualizationTabs.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				tabFocusAndChangeAction();	
			}

			public void focusLost(FocusEvent arg0) {
				
			}
			
		});
		
		visualizationTabs.addChangeListener(new ChangeListener(){

			// When the selected state changes
			public void stateChanged(ChangeEvent e) 
			{
				tabFocusAndChangeAction();
			}
		});
		
		this.addComponentListener(new ComponentListener() {
			
			public void componentShown(ComponentEvent e) {}
			
			public void componentResized(ComponentEvent e) 
			{
				control.windowResized();
				
			}
			
			public void componentMoved(ComponentEvent e) {}
			
			public void componentHidden(ComponentEvent arg0) {}
		});
		
		// Set up the splitpane for the visualizations and the supplemental views.
		outermostSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, visualizationTabs, globalAnnotationSplit);
		outermostSplit.setOneTouchExpandable(true);
		outermostSplit.setDividerLocation(400);
		// set left/top and right/bottom resize ratio
		// when the split pane gets bigger
		outermostSplit.setResizeWeight(0.7);
		
		// Add everything to the main panel.
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(outermostSplit, BorderLayout.CENTER);
		
		// Add the panel to the frame.
		getContentPane().add(mainPanel);
		
		// Set the size of the frame.
		this.setPreferredSize(new Dimension(600, 600));
		
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
		if(cMapJPanel.getComponentCount() > 0)
		{
			// This should allow the visualization to be garbage collected.
			cMapJPanel.removeAll();
			//cMapJPanel.revalidate();
		}
		
		if(d != null)
		{
			cMapJPanel.add(d, BorderLayout.CENTER);
			//cMapJPanel.revalidate();
			/*
			Visualization vis = d.getVisualization();
			Rectangle2D visbounds = vis.getBounds("component_graph");
			/*
			System.out.println("Visbounds x: "  + visbounds.getX() + " y: " + visbounds.getY());
			System.out.println("Visbounds minx: "  + visbounds.getMinX() + " miny: " + visbounds.getMinY());
			System.out.println("Visbounds maxx: "  + visbounds.getMaxX() + " maxy: " + visbounds.getMaxY());
			System.out.println("Visbounds centerx: "  + visbounds.getCenterX() + " centery: " + visbounds.getCenterY());
			System.out.println("Visbounds width: " + visbounds.getWidth() + " height: " + visbounds.getHeight());
			
			System.out.println("Scale: " + d.getScale());
			
			System.out.println("DisplayX: " + d.getDisplayX());
			System.out.println("DisplayY: " + d.getDisplayY());
			
			Dimension preSize = d.getPreferredSize();
			//System.out.println("PreferredSize: " + preSize.width + "x" + preSize.height);
			Dimension size = d.getSize();
			//System.out.println("Size: " + size.width + "x" + size.height);
			Rectangle visRec = d.getVisibleRect();
			//System.out.println("VisibleRect x: " + visRec.x + " y: " + visRec.y + " width: " + visRec.width + " height: " + visRec.height);
			 * */
		}
		
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
		if(iGraphJPanel.getComponentCount() > 0)
		{
			// This should allow the visualization to be garbage collected.
			iGraphJPanel.removeAll();
			//iGraphJPanel.revalidate();
		}
		
		if(d != null)
		{
			iGraphJPanel.add(d, BorderLayout.CENTER);
			//iGraphJPanel.revalidate();
		}
		
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
		
		//TODO could be part of the flicker
		this.update(this.getGraphics());
	}
	
	/**
	 * Give the VisualizationViewer a Display to use for the over
	 * of the cMap
	 * @param cMapOverview
	 */
	protected void setCMapOverview(Display cMapOverview) 
	{
		if(cMapOverviewJPanel.getComponentCount() > 0)
		{
			cMapOverviewJPanel.removeAll();
			//cMapOverviewJPanel.revalidate();
		}

		if(cMapOverview != null && cMapJPanel.getComponentCount() == 1)
		{
			// absolute layout
			cMapOverviewJPanel.setLayout(null);
			
			Display dis_focus = (Display)cMapJPanel.getComponent(0);
			
			// compute the size of select box
			int x, y, width, height;
			double ratio; // scale of overview over focus
			
			double[] result = computeSelectBox(cMapOverviewJPanel, cMapOverview, dis_focus, "component_graph");
			x = (int)result[0];
			y = (int)result[1];
			width = (int)result[2];
			height = (int)result[3];
			ratio = result[4];
			
			// get insets of overviewPanel
			Insets insets = cMapOverviewJPanel.getInsets();
			// get the size of overviewPanel
			Dimension size_panel = cMapOverviewJPanel.getSize();
			
			// create select box
	        cMapRectanglePanel = new RectanglePanel(width, height);
	        // set bounds for select box
	        cMapRectanglePanel.setBounds(x, y, width, height);
	        // set select box be transparent
	        cMapRectanglePanel.setOpaque(false);
	        // add select box to panel
	        cMapOverviewJPanel.add(cMapRectanglePanel);
	        
	        // set bounds for overview display
			cMapOverview.setBounds(insets.left, insets.top, size_panel.width, size_panel.height);
			// add overview display to panel
			cMapOverviewJPanel.add(cMapOverview);
			//cMapOverviewJPanel.revalidate();	

			// add mouse listener for select box	
			cMapSelectBoxControl = new SelectBoxControl(x, y, width, height, cMapRectanglePanel, dis_focus, ratio);
			cMapRectanglePanel.addMouseListener(cMapSelectBoxControl);
			cMapRectanglePanel.addMouseMotionListener(cMapSelectBoxControl);
		}
		
		this.update(this.getGraphics());
	}
	
	public void updateCMapSelectBox()
	{

		if(cMapJPanel.getComponentCount() == 1 && cMapOverviewJPanel.getComponentCount() == 2)
		{
			double[] result = computeSelectBox(cMapOverviewJPanel, 
										      (Display) cMapOverviewJPanel.getComponent(1), 
										      (Display) cMapJPanel.getComponent(0), 
										      "component_graph");
			/*
			x = (int)result[0];
			y = (int)result[1];
			width = (int)result[2];
			height = (int)result[3];
			double ratio = result[4];
			*/
			cMapSelectBoxControl.updateInfo((int) result[0], (int) result[1], (int) result[2], (int) result[3], result[4]);
		}
	}
	

	/**
	 * Give the VisualizationViewer a Display to use for the over
	 * of the iGraph
	 * @param iGraphOverview
	 */
	protected void setIGraphOverview(Display iGraphOverview) 
	{
		if(iGraphOverviewJPanel.getComponentCount() > 0)
		{
			iGraphOverviewJPanel.removeAll();
			//iGraphOverviewJPanel.revalidate();
		}
		
		if(iGraphOverview != null && iGraphJPanel.getComponentCount() == 1)
		{
			// absolute layout
			iGraphOverviewJPanel.setLayout(null);
			
			Display dis_focus = (Display)iGraphJPanel.getComponent(0);
			
			// compute the size of select box
			int x, y, width, height;
			double ratio; // scale of overview over focus
			
			double[] result = computeSelectBox(iGraphOverviewJPanel, iGraphOverview, dis_focus, "igraph");
			x = (int)result[0];
			y = (int)result[1];
			width = (int)result[2];
			height = (int)result[3];
			ratio = result[4];
			
			// get insets of overviewPanel
			Insets insets = iGraphOverviewJPanel.getInsets();
			// get the size of overviewPanel
			Dimension size_panel = iGraphOverviewJPanel.getSize();
			
			// create select box
	        iGraphRectanglePanel = new RectanglePanel(width, height);
	        // set bounds for select box
	        iGraphRectanglePanel.setBounds(x, y, width, height);
	        // set select box be transparent
	        iGraphRectanglePanel.setOpaque(false);
	        // add select box to panel
	        iGraphOverviewJPanel.add(iGraphRectanglePanel);
	        
	        // set bounds for overview display
	        iGraphOverview.setBounds(insets.left, insets.top,
					size_panel.width, size_panel.height);
			// add overview display to panel
			iGraphOverviewJPanel.add(iGraphOverview);
			//iGraphOverviewJPanel.revalidate();	

			// add mouse listener for select box	
			iGraphSelectBoxControl = new SelectBoxControl(x, y, width, height, iGraphRectanglePanel, dis_focus, ratio);
			iGraphRectanglePanel.addMouseListener(iGraphSelectBoxControl);
			iGraphRectanglePanel.addMouseMotionListener(iGraphSelectBoxControl);		
		}
		
		this.update(this.getGraphics());
		//overview.updateUI();
	}
	

	public void updateIGraphSelectBox()
	{

		if(iGraphJPanel.getComponentCount() == 1 && iGraphOverviewJPanel.getComponentCount() == 2)
		{
			double[] result = computeSelectBox(iGraphOverviewJPanel, 
											  (Display) iGraphOverviewJPanel.getComponent(1),
											  (Display) iGraphJPanel.getComponent(0),
											  "igraph");
			
			iGraphSelectBoxControl.updateInfo((int) result[0], (int) result[1], (int) result[2], (int) result[3], result[4]);
		}
	}
	
	/**
	 * Give the VisualizationViewer a Display to use for the over
	 * of the speciesBrowser.
	 * @param speciesBrowserOverview
	 */
	protected void setSpeciesBrowserOverview(Display speciesBrowserOverview) 
	{
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
			
		this.update(this.getGraphics());
		//overview.updateUI();
	}
	
	public void updateSpeciesBrowserSelectBox()
	{

		if(speciesBrowserJPanel.getComponentCount() == 1 && speciesBrowserOverviewJPanel.getComponentCount() == 2)
		{
			
			double[] result = computeSelectBox(speciesBrowserOverviewJPanel,
					(Display) speciesBrowserOverviewJPanel.getComponent(1),
					(Display) speciesBrowserJPanel.getComponent(0),
					"component_graph");
			
			speciesBrowserSelectBoxControl.updateInfo((int) result[0], (int) result[1], (int) result[2], (int) result[3], result[4]);
		}
		
	}
	
	public Dimension getCMapSize() 
	{
		return new Dimension(cMapJPanel.getSize().width  - border.getBorderInsets(cMapJPanel).left
														 - border.getBorderInsets(cMapJPanel).right,
							 cMapJPanel.getSize().height - border.getBorderInsets(cMapJPanel).top
														 - border.getBorderInsets(cMapJPanel).bottom);
	}
	
	public Dimension getIGraphSize() 
	{
		return iGraphJPanel.getSize();
	}
	
	public Dimension getSpeciesBrowserSize() 
	{
		return speciesBrowserJPanel.getSize();
	}
	
	public Dimension getCMapOverviewSize() 
	{	
		return new Dimension(cMapOverviewJPanel.getSize().width  - border.getBorderInsets(cMapOverviewJPanel).left
																 - border.getBorderInsets(cMapOverviewJPanel).right,
							 cMapOverviewJPanel.getSize().height - border.getBorderInsets(cMapOverviewJPanel).top
							 									 - border.getBorderInsets(cMapOverviewJPanel).bottom);
	}
	
	public Dimension getIGraphOverviewSize() 
	{
		return iGraphOverviewJPanel.getSize();
	}
	
	public Dimension getSpeciesBrowserOverviewSize() 
	{
		return speciesBrowserOverviewJPanel.getSize();
	}
	
	/**
	 * Set the displayed tab to the contact map.
	 */
	protected void focusCMap()
	{
		visualizationTabs.setSelectedComponent(cMapJPanel);
	}

	/**
	 * Set the displayed tab to the influence graph.
	 */
	protected void focusIGraph()
	{
		visualizationTabs.setSelectedComponent(iGraphJPanel);
	}
	
	/**
	 * Set the displayed tab to the species graph.
	 * 
	 */
	protected void focusSpeciesBrowser()
	{
		visualizationTabs.setSelectedComponent(speciesBrowserJPanel);
	}
	
	/**
	 * Returns a text value to distinguish which tab is currently selected by the user.
	 * 
	 * @return The text value that represents the selected tab.
	 */
	protected String getSelectedComponent()
	{
		if(visualizationTabs.getSelectedComponent() == cMapJPanel)
			return CMAP;
		
		else if(visualizationTabs.getSelectedComponent() == iGraphJPanel)
			return IGRAPH;
		
		else
			return SPECIESBROWSER;
	}
	
	/**
	 * Actions when visualization tabs got focused or been changed
	 */
	protected void tabFocusAndChangeAction() {
		// If the new state is the cmap
		if (visualizationTabs.getSelectedComponent() == cMapJPanel)
		{
			control.cMapSelected();
			
			((CardLayout) overview.getLayout()).show(overview, "CMapO");
			
			
			// update overview twice
			// must be twice because some values are unavailable for the first time
			control.updateCMapOverview();
			control.updateCMapOverview();
			
		}
			
		if (visualizationTabs.getSelectedComponent() == iGraphJPanel)
		{
			control.iGraphSelected();
			((CardLayout) overview.getLayout()).show(overview, "IGraphO");
			
			
			// update overview twice
			// must be twice because some values are unavailable for the first time
			control.updateIGraphOverview();
			control.updateIGraphOverview();
			
		}
		
		if (visualizationTabs.getSelectedComponent() == speciesBrowserJPanel)
		{
			control.speciesBrowserSelected();
			((CardLayout) overview.getLayout()).show(overview, "SpeciesBrowserO");
			
			
			// update overview twice
			// must be twice because some values are unavailable for the first time
			control.updateSpeciesBrowserOverview(null);
			control.updateSpeciesBrowserOverview(null);
			
		}
		
		control.updateAnnotationTable(null);
	}
	
	/**
	 * Give the annotation panel a TabelModel object to display
	 * @param tm
	 */
	protected void setAnnotationPanel(TableModel tm) {
		if (annotation.getComponentCount() == 0 && tm == null) {
			return;
		}
		
		if (annotation.getComponentCount() > 0) {
			annotation.removeAll();
		}
		
		// create a table with tablemodel
		JTable table = new JTable(tm);
		// set table row height and be able to wrap lines
		table.setDefaultRenderer(String.class, new MultiLineCellRenderer());
		table.addMouseListener(new TableMouseListener());
		
		// set column width for rule
		TableColumn column = null;
		for (int i = 0; i < table.getColumnCount(); i++) {
			// find the column with name Rule
			if (table.getColumnName(i).equals("Rule")) {
				// get the column object
				column = table.getColumnModel().getColumn(i);
				// get width of annotation panel
				int width = (int) (annotation.getBounds().width * 0.8);
				// set preferred width
		        column.setPreferredWidth(width);
		    }
		}
		
		// add table header
		JTableHeader header = table.getTableHeader();
		annotation.add(header, BorderLayout.NORTH);
		
		// add scrollable table
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

	public void resizeSpeciesBrowserOverviewDisplay() 
	{
		Dimension size = speciesBrowserOverviewJPanel.getSize();
		
		if(speciesBrowserOverviewJPanel.getComponentCount() == 2)
		{
			((Display)speciesBrowserOverviewJPanel.getComponent(1)).setSize(size);
		}
	}
	
	public void resizeCMapOverviewDisplay() 
	{		
		Dimension size = cMapOverviewJPanel.getSize();
		
		if(cMapOverviewJPanel.getComponentCount() == 2)
		{
			((Display)cMapOverviewJPanel.getComponent(1)).setSize(size);
		}
	}
	
	public void resizeIGraphOverviewDisplay() 
	{		
		Dimension size = iGraphOverviewJPanel.getSize();
		
		if(iGraphOverviewJPanel.getComponentCount() == 2)
		{
			((Display)iGraphOverviewJPanel.getComponent(1)).setSize(size);
		}
	}
	
	
	public void resizeSpeciesBrowserDisplay() 
	{
		Dimension size = speciesBrowserJPanel.getSize();
		
		if(speciesBrowserJPanel.getComponentCount() == 1)
		{
			((Display)speciesBrowserJPanel.getComponent(0)).setSize(size);
		}
	}
	
	public void resizeCMapDisplay() 
	{		
		Dimension size = cMapJPanel.getSize();
		
		if(cMapJPanel.getComponentCount() == 2)
		{
			((Display)cMapJPanel.getComponent(1)).setSize(size);
		}
	}
	
	public void resizeIGraphDisplay() 
	{		
		Dimension size = iGraphJPanel.getSize();
		
		if(iGraphJPanel.getComponentCount() == 2)
		{
			((Display)iGraphJPanel.getComponent(1)).setSize(size);
		}
	}
	
	
	
}
package rulebender.core.prefuse.networkviewer.contactmap;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.controls.ControlAdapter;
import prefuse.controls.PanControl;
import prefuse.data.Graph;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.StrokeLib;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTupleSet;
import prefuse.visual.expression.InGroupPredicate;
import rulebender.core.prefuse.networkviewer.CustomizedZoomToFitControl;
import rulebender.simulate.ResultsFileUtility;
import rulebender.simulationjournaling.model.SMClickControlDelegate;

public class CMAPNetworkViewer 
{
	// These strings all define a 'group' that is a part of the visualization.
	// It is the internal string ID that prefuse uses for these collections
	// of visual items.

	// This is the name of the 'group' for the graph itself.
	// The edges automatically have component_graph.edges
	// and the nodes have component_graph.nodes.
	private static String COMPONENT_GRAPH;

	// A const for the string value of the category of aggregate
	public static final String AGG_CAT_LABEL = "molecule";

	// String const for aggregate decorators group.
	private static String AGG_DEC;

	// Group for Aggregates
	private static String AGG;
	
	// This is a label for aggregates of component with states
	private static String AGG_COMP = "aggregates_component";

	// Filepath to the BNGL source
	private String m_filePath;
	
	// Filepath to the position file
	private String m_posPath;
	
	// The visualization itself. We give the data to this, set
	// its renderers and whatnot.
	private Visualization vis;

	private Display mainDisplay;
	
	private ControlAdapter clickControlDelegate;

	private HoverTooltip tooltipDelegate;

	private boolean panControlEnabled = true;

	private boolean zoomControlEnabled = true;

	private boolean draggableAggregates = true;
	
	private boolean isGrayscale = false;

	/**
	 * Constructor accepts a graph structure.
	 * @param mainDisplaySize 
	 */
	public CMAPNetworkViewer(Dimension mainDisplaySize) 
	{
		vis = new Visualization();
		
		// create the visual group for the object that is selected.
		vis.addFocusGroup("selected");
		vis.addFocusGroup("different");
		
		mainDisplay = new Display();
		mainDisplay.setSize(mainDisplaySize);
	}
	
	/**
	 * Determine whether we should be in grayscale mode or not
	 * 
	 * @param isGrey - whether or not grayscale is enabled
	 */
	public void setGreyscale(boolean isGray) {
		isGrayscale = isGray;
	} //setGreyscale
	
	public void build()
	{
		setUpRenderers();

		setUpActions(isGrayscale);
		
		setUpDisplay();
		
	}

	/**
	 * Set a Graph to the visualization
	 * 
	 * @param g
	 * @param gString
	 * @return The VisualTupleSet that represents the graph.
	 */
	public VisualTupleSet addGraph(Graph g, String gString) {
		COMPONENT_GRAPH = gString;
		return vis.add(gString, g);
	}

	/**
	 * Set an aggregateTable for the molecules to the visualization.
	 * 
	 * @param atString
	 * @return
	 */
	public AggregateTable addAggregateTable(String atString) {
		AGG = atString;
		return vis.addAggregates(atString);
		
	}

	/**
	 * 
	 * @param aGG_DEC2
	 * @param aGG2
	 */
	public void addDecorators(String group, String source) {
		AGG_DEC = group;
		AGG = source;
		vis.addDecorators(AGG_DEC, AGG);
	}

	/**
	 * returns the visualization object.
	 */
	public Visualization getVisualization() {
		return vis;
	} //getVisualization

	public void setFilepath(String path) {
		m_filePath = path;
	} //setFilepath
	
	public String getFilepath() {
		return m_filePath;
	} //getFilepath
	
	public String getMostRecentSimulation() {
		String mostRecentSimulationFilepath = null;
		
		// Get an iFile for the BNGL file location
		IWorkspace workspace = ResourcesPlugin.getWorkspace();  
		IPath location = Path.fromOSString(m_filePath);
	    IFile iFile = workspace.getRoot().getFileForLocation(location);
		
	    // Use that iFile to get the results directory
		String resultsDir = ResultsFileUtility.getSimulationResultsDirectoryForIFile(iFile);
		
		// Jump up to the parent
		resultsDir = getParentDirectory(resultsDir);
		System.out.println(resultsDir);
		
		// Find the .gdat file
		mostRecentSimulationFilepath = findMostRecentResultsRun(resultsDir);
				
		return mostRecentSimulationFilepath;
	} //getMostRecentSimulation
	
	public String getParentDirectory(String filePath) {
		File d = new File(filePath);
		filePath = d.getParent().toString();		
		return filePath;
	} //getParentDirectory
	
	public String findMostRecentResultsRun(String filePath) {
		File d = new File(filePath);
		File newest = null;
		Date date;
		Date newestTimestamp = null;
		
		try {
			// Look for the newest directory that isn't
			for (File child : d.listFiles()) {
				if (child.isDirectory()) {
					try {
						date = new SimpleDateFormat("dd-MMMM-yy_HH-mm-ss", Locale.ENGLISH).parse(child.getName().toString());
						System.out.println(date);
					} catch (Exception e) {
						try {
							date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).parse(child.getName().toString());
							System.out.println(date);
						} catch (Exception e2) {
							System.err.println("Unknown date format");
							break;
						} //try-catch
					} //try-catch
				
					// Compare current date against most recent
					if (newestTimestamp == null) {
						newestTimestamp = date;
						newest = child;
					} else {
						if (date.after(newestTimestamp)) {
							newestTimestamp = date;
							newest = child;	
						} //if
					} //if-else
					
				} //if
			} //for
		} catch (NullPointerException npe) {
			return "";
		}
		
		// Look for the gdat file in that directory
		for (File child : newest.listFiles()) {
			String path = child.toString();
			if (path.substring(path.length()-5, path.length()).equals(".gdat")) {
				return path;
			} //if
		} //for
		
		return "";
	} //findMostRecentResultsRun
	
	public void setPosPath(String path) {
		m_posPath = path;
	} //setPosPath
	
	/**
	 * Set the click control delegate that will handle the clicks.
	 * 
	 * @param c
	 */
	public void setClickControl(ControlAdapter c) {
		clickControlDelegate = c;
	}
	
	public ControlAdapter getClickControl() {
		return clickControlDelegate;
	} //getClickControl;

	/**
	 * Set whether or not zoom controlling should be allowed
	 * 
	 * @param b
	 */
	public void setZoomControlEnabled(boolean b) {
		zoomControlEnabled = b;
	}

	/**
	 * Set whether or not pan controlling should be allowed
	 * 
	 * @param b
	 */
	public void setPanControlEnabled(boolean b) {
		panControlEnabled = b;
	}

	/**
	 * Set the tooltip delegate object.
	 * 
	 * @param ht
	 *            A HoverTooltip implementation.
	 */
	public void setTooltipDelegate(HoverTooltip ht) {
		tooltipDelegate = ht;
	}

	/**
	 * Set whether or not the aggregates should be draggable.
	 * 
	 * @param b
	 */
	public void setDraggableAggregates(boolean b) {
		draggableAggregates = b;
	}
	

	/**
	 * Sets up the renderers,actions and controls.
	 * 
	 * @return Display The display object that you embed in a JPanel
	 */
	public void setUpDisplay() {
		

		// Create a object to hold the visualization
		mainDisplay.setVisualization(vis);
		
		// Turn on prettiness and antialiasing
		mainDisplay.setHighQuality(true);

		// drag individual items around
		// Control the aggregates with a drag.
		if (draggableAggregates == true) {
			AggregateDragControl aggDrag = new AggregateDragControl(true, "color", vis, COMPONENT_GRAPH);
			aggDrag.addAction("bubbleColor");
			aggDrag.addAction("complayout");
			aggDrag.addAction("compartmentlayout");
			mainDisplay.addControlListener(aggDrag);
		}

		// pan with left-click drag on background
		if (panControlEnabled)
			mainDisplay.addControlListener(new PanControl());

		// Sets something in or out of the focus group after
		// a certain number of clicks.
		// d.addControlListener(new FocusControl(1));

		// click interactivity
		if (clickControlDelegate != null)
			mainDisplay.addControlListener(clickControlDelegate);

		// tooltip
		if (tooltipDelegate != null)
			mainDisplay.addControlListener(tooltipDelegate);

		// Zoom with the mouse wheel
		if (zoomControlEnabled) {
			//mainDisplay.addControlListener(new WheelZoomControl());
			mainDisplay.addControlListener(new ReverseWheelZoomControl());
			mainDisplay.addControlListener(new CustomizedZoomToFitControl());
		}
		

		vis.run("color");
		// start up the animated layout
		vis.run("layout");
//		vis.run("complayout");
//		vis.run("compartmentlayout");
	}

	/*
	 * Set up the renderers
	 * 
	 * 
	 * We create renderers for the nodes and edges, and then add them to a
	 * renderer factory, which decides which renderer to use for each visual
	 * item based on whether it is a node or edge. Can do more things with
	 * predicates and create new renderers for advanced rendering.
	 */
	private void setUpRenderers() {
		// The LabelRenderer will display text (and an option image) in a shape.
		ComponentRenderer lr = new ComponentRenderer(VisualItem.LABEL);

		// The PolygonRenderer draws arbitrary polygons. For aggregates.
		Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
		((PolygonRenderer) polyR).setCurveSlack(0.05f);

		// The SelfReferencingRenderer is a small subclass of the EdgeRenderer.
		// If the two items comprising the edge are the same object, then
		// an ellipse is drawn, else the parent class is used as is normal.
		SelfReferenceRenderer srr = new SelfReferenceRenderer();
		srr.setArrowType(Constants.EDGE_ARROW_FORWARD);
		srr.setArrowHeadSize(15, 30);
		
		// The factory decides which renderer to use for a visual item.
		DefaultRendererFactory rf = new DefaultRendererFactory(lr);

		// Set the default renderer for edges
		rf.setDefaultEdgeRenderer(srr);

		// Add the aggregate renderer for the items in the group 'aggregates'.
		rf.add("ingroup('compartments')", polyR);
		rf.add("ingroup('" + AGG + "')", polyR);
		rf.add("ingroup('" + AGG_COMP + "')", polyR);
		rf.add("ingroup('bubbles')", polyR);
		
		// Add the aggregate decorater renderer
		rf.add(new InGroupPredicate(AGG_DEC), new LabelRenderer(AGG_CAT_LABEL));

		// Tell the visualization to use our factory.
		vis.setRendererFactory(rf);

		// Define the render order
		mainDisplay.setItemSorter(new CMapItemSorter());
	}

	private void setUpActions(boolean grayscale) {
		// The DataColorAction chooses a color given a palette based on the
		// data column that is passed to the constructor.
		
		int[] palette;
		
		// normal component, state with bonds, component with states, state without bonds,
		// component with state change, hub node
		
		if (!grayscale) {
			palette = new int[] { 
				ColorLib.rgba(254, 224, 139, 150),   // 196, 196, 196
				ColorLib.rgba(166, 217, 106, 150),   // 161, 161, 161
				ColorLib.rgba(253, 174, 97, 150),    // 175, 175, 175
				ColorLib.rgba(240, 240, 240, 150),   // 240, 240, 240
				ColorLib.rgba(153, 112, 171, 150),   // 141, 141, 141
				ColorLib.rgba(189, 189, 189, 150)    // 189, 189, 189
			};
		
		} else {
		
			// Same color mapping as above, but with greyscale design
			palette = new int[] {
				ColorLib.rgba(196, 196, 196, 150),
				ColorLib.rgba(161, 161, 161, 150),
				ColorLib.rgba(175, 175, 175, 150),
				ColorLib.rgba(240, 240, 240, 150),
				ColorLib.rgba(141, 141, 141, 150),
				ColorLib.rgba(189, 189, 189, 150)
			};
		
		} //if-else
		
		// Color the component nodes.
		ComponentColorAction fill = new ComponentColorAction(COMPONENT_GRAPH + ".nodes", VisualItem.FILLCOLOR, palette);
		
		// Color the nodes red if they're in the class of nodes not found in the selected small multiple
		if (grayscale) {
			fill.add("ingroup('different')", ColorLib.rgb(255, 0, 0));
		} //if

		// Color the text
		ColorAction text = new ColorAction(COMPONENT_GRAPH + ".nodes",
				VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0));
		
		// FontAction for node text
		FontAction textFont = new FontAction(COMPONENT_GRAPH + ".nodes", FontLib.getFont("Arial", Font.PLAIN, 12));

		// Change the color of the strokes.
		ColorAction nodeStroke = new ColorAction(COMPONENT_GRAPH + ".nodes", VisualItem.STROKECOLOR, ColorLib.rgb(20, 20, 20));
		nodeStroke.add("ingroup('selected')", ColorLib.rgb(225, 100, 100));
		
		// Color the nodestrokes red if they're in the class of nodes not found in the selected small multiple
		if (grayscale) {
			nodeStroke.add("ingroup('different')", ColorLib.rgb(255, 0, 0));
		} //if
		
		//TODO If the node is selected, use a different color.

		// Color the egdes
		ColorAction edgeStroke = new ColorAction(COMPONENT_GRAPH + ".edges", VisualItem.STROKECOLOR, ColorLib.rgb(105, 105, 105));

		//TODO if the edge is selected, use a different color
		edgeStroke.add("ingroup('selected')", ColorLib.rgb(225, 100, 100));
		
		// Color the edges red if they're in the class of nodes not found in the selected small multiple
		if (grayscale) {
			edgeStroke.add("ingroup('different')", ColorLib.rgb(255, 0, 0));
		} //if
		
		// Use these to change the size of the strokes.
		StrokeAction nodeStrokea = new StrokeAction(COMPONENT_GRAPH + ".nodes",
				StrokeLib.getStroke(1.0f));
		StrokeAction edgeStrokea = new StrokeAction(COMPONENT_GRAPH + ".edges",
				StrokeLib.getStroke(3.0f));
		StrokeAction aggStrokea = new StrokeAction(AGG,
				StrokeLib.getStroke(1.5f));

		// stroke color the aggregates
		ColorAction aStroke = new ColorAction(AGG, VisualItem.STROKECOLOR,
				ColorLib.rgb(10, 10, 10));
		
		// TODO If the aggregate is selected, use a different color.
		aStroke.add("ingroup('selected')", ColorLib.rgb(225, 100, 100));
		
		// Color the aggregates red if they're in the class of nodes not found in the selected small multiple
		if (grayscale) {
			aStroke.add("ingroup('different')", ColorLib.rgb(255, 0, 0));
		} //if

		// Draw the decorators.
		ColorAction decText = new ColorAction(AGG_DEC, VisualItem.TEXTCOLOR,
				ColorLib.rgb(20, 20, 20));
		FontAction decFont = new FontAction(AGG_DEC, FontLib.getFont("Tahoma",
				Font.BOLD, 20));

		// Fill the aggregates (molecules) with gray.
		ColorAction aFill = new ColorAction(AGG, VisualItem.FILLCOLOR,
				ColorLib.rgb(240, 240, 240));
		
		
		// stroke color the for compartment aggregates
	//	ColorAction aStroke_compartment = new ColorAction("compartments", VisualItem.STROKECOLOR,
	//			ColorLib.rgba(10, 10, 10, 100));
		// Fill the aggregates (compartments)
		
		//TODO Color the selected compartments. 
		
		ColorAction aFill_compartment = new ColorAction("compartments", VisualItem.FILLCOLOR,
				ColorLib.rgba(103, 169, 207, 40));
		
		// edge fill color, for arrows
		ColorAction edgeFill = new ColorAction(COMPONENT_GRAPH + ".edges",
				VisualItem.FILLCOLOR, ColorLib.rgba(105, 105, 105, 50));

		// create an action list containing all color assignments
		ActionList color = new ActionList();

		// Add everything to the list.
		color.add(fill);
		color.add(text);
		color.add(textFont);
		color.add(nodeStroke);
		color.add(edgeStroke);
		color.add(nodeStrokea);
		color.add(edgeStrokea);
		color.add(aggStrokea);
		color.add(aStroke);
		color.add(aFill);
		color.add(decText);
		color.add(decFont);
//		color.add(aStroke_compartment);
		color.add(aFill_compartment);
		color.add(edgeFill);
		color.add(new RepaintAction());

		// layout
//		ActionList layout = new ActionList(3500);
		ActionList layout = new ActionList();
		
		// Create the force directed layout that uses invisible edges in force
		// calculations as well as the visible ones.

		ForceDirectedLayoutMagic f;
		f = new ForceDirectedLayoutMagic(
				COMPONENT_GRAPH, true, true);
		f.setMagicEdges(true);
		f.getForceSimulator().setSpeedLimit(3);
		
		// Pass in filepath to force simulator
		// Check to see if a position file is given.  If so, pass in the position file.  Otherwise, pass in the BNGL source path.
		if (m_posPath == null) {
			f.setPositionFilepath(m_filePath);
		} else {
			f.setPositionFilepath(m_posPath);			
		} //if-else

		/*		
		// set bounds based on graph size		
		Rectangle2D bounds;
		System.out.println("graph size: " + vis.size(COMPONENT_GRAPH));
		if (vis.size(COMPONENT_GRAPH) > 100) 
		{
			bounds = new Rectangle2D.Double(-1200, -1200, 2400, 2400);
		}
		else 
		{
			bounds = new Rectangle2D.Double(-600, -600, 1200, 1200);
		}
		f.setEnforceBounds(bounds);
		*/
		//f.setReferrer((VisualItem)vis.items().next());
		
		// Currently the anchor is only used for runonce mode
		//f.setLayoutAnchor(new Point2D.Double(500, 600));
		
		layout.add(f);
		
		
		ComponentLayout cl = new ComponentLayout(COMPONENT_GRAPH);
		layout.add(cl);
		
		// I am probably doing twice as much work by adding this here and to the
		// color action list,
		// but otherwise I am having trouble getting it to update.
	
		layout.add(new AggregateLayout(AGG));
		layout.add(new LabelLayout(AGG_DEC));
		layout.add(new RepaintAction());
		
		ActionList complayout = new ActionList();
		// to layout state nodes beside component nodes

		complayout.add(cl);
		complayout.add(new AggregateLayout(AGG));
		complayout.add(new LabelLayout(AGG_DEC));
		complayout.add(new RepaintAction());
		
		ActionList compartmentlayout = new ActionList();
		// to layout state nodes beside component nodes

		compartmentlayout.add(new AggregateLayout(AGG));
		compartmentlayout.add(new LabelLayout(AGG_DEC));
		compartmentlayout.add(new AggregateLayout("compartments"));
//		compartmentlayout.add(new BubbleSetLayout("compartments", COMPONENT_GRAPH));
		complayout.add(new RepaintAction());

		vis.putAction("color", color);
		vis.putAction("layout", layout);
		vis.putAction("complayout", complayout);
		vis.putAction("compartmentlayout", compartmentlayout);
	}
	
	public Display getDisplay()
	{
		return mainDisplay;
	}
} // Close NetworkViewer


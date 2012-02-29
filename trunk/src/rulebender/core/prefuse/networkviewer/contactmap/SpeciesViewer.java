package rulebender.core.prefuse.networkviewer.contactmap;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.awt.Dimension;
import java.awt.Font;


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

public class SpeciesViewer 
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

	// The visualization itself. We give the data to this, set
	// its renderers and whatnot.
	private Visualization vis;

	private Display mainDisplay;
	
	private ControlAdapter clickControlDelegate;

	private HoverTooltip tooltipDelegate;

	private boolean panControlEnabled = true;

	private boolean zoomControlEnabled = true;

	private boolean draggableAggregates = true;

	/**
	 * Constructor accepts a graph structure.
	 */
	public SpeciesViewer(Dimension mainDimension) {
		vis = new Visualization();
		mainDisplay = new Display();
		mainDisplay.setSize(mainDimension);
	}
	
	public void build()
	{
		setUpRenderers();

		setUpActions();
		
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
	}

	/**
	 * Set the click control delegate that will handle the clicks.
	 * 
	 * @param c
	 */
	public void setClickControl(ControlAdapter c) {
		clickControlDelegate = c;
	}

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
			AggregateDragControl aggDrag = new AggregateDragControl(true,
					"color", vis, COMPONENT_GRAPH);
			aggDrag.addAction("bubbleColor");
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
			mainDisplay.addControlListener(new ReverseWheelZoomControl());
			mainDisplay.addControlListener(new CustomizedZoomToFitControl());
		}
		
		vis.run("color");
		// start up the animated layout
		vis.run("layout");
//		vis.run("complayout");
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

		// The factory decides which renderer to use for a visual item.
		DefaultRendererFactory rf = new DefaultRendererFactory(lr);

		// Set the default renderer for edges
		rf.setDefaultEdgeRenderer(srr);

		// Add the aggregate renderer for the items in the group 'aggregates'.
		rf.add("ingroup('" + AGG + "') OR ingroup('bubbles') OR ingroup('" + AGG_COMP + "')", polyR);

		// Add the aggregate decorater renderer
		rf.add(new InGroupPredicate(AGG_DEC), new LabelRenderer(AGG_CAT_LABEL));

		// Tell the visualization to use our factory.
		vis.setRendererFactory(rf);

		// Define the render order
		mainDisplay.setItemSorter(new CMapItemSorter());
	}

	private void setUpActions() {
		// The DataColorAction chooses a color given a palette based on the
		// data column that is passed to the constructor.
		
		// component, state, component with states, state without bonds, component with state change
		int[] palette = new int[] { ColorLib.rgba(254, 224, 139, 150),
				ColorLib.rgba(166, 217, 106, 150),
				ColorLib.rgba(253, 174, 97, 150),
				ColorLib.rgba(240, 240, 240, 150),
				ColorLib.rgba(153, 112, 171, 150)};
		ComponentColorAction fill = new ComponentColorAction(COMPONENT_GRAPH
				+ ".nodes", VisualItem.FILLCOLOR, palette);

		// ColorActions color the given group a certain color.
		ColorAction text = new ColorAction(COMPONENT_GRAPH + ".nodes",
				VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0));
		// FontAction for node text
		FontAction textFont = new FontAction(COMPONENT_GRAPH + ".nodes", FontLib.getFont("Arial",
				Font.PLAIN, 12));

		// Change the color of the strokes.
		ColorAction nodeStroke = new ColorAction(COMPONENT_GRAPH + ".nodes",
				VisualItem.STROKECOLOR, ColorLib.rgb(20, 20, 20));

		ColorAction edgeStroke = new ColorAction(COMPONENT_GRAPH + ".edges",
				VisualItem.STROKECOLOR, ColorLib.rgb(105, 105, 105));

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

		// Draw the decorators.
		ColorAction decText = new ColorAction(AGG_DEC, VisualItem.TEXTCOLOR,
				ColorLib.rgb(20, 20, 20));
		FontAction decFont = new FontAction(AGG_DEC, FontLib.getFont("Tahoma",
				Font.BOLD, 20));

		// Fill the aggregates (molecules) with gray.
		ColorAction aFill = new ColorAction(AGG, VisualItem.FILLCOLOR,
				ColorLib.rgb(240, 240, 240));

		// create an action list containing all color assignments
		ActionList color = new ActionList();

		// Add everything to the list.
		color.add(new AggregateLayout(AGG));
		color.add(new LabelLayout(AGG_DEC));
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
		
		// set bounds based on graph size		
		Rectangle2D bounds;
//		System.out.println("graph size: " + vis.size(COMPONENT_GRAPH));
		if (vis.size(COMPONENT_GRAPH) > 200) {
			bounds = new Rectangle2D.Double(-1200, -1200, 2400, 2400);
		}
		else {
			bounds = new Rectangle2D.Double(-600, -600, 1200, 1200);
		}
		f.setEnforceBounds(bounds);
		
//		System.out.println("numItems: " + vis.size(COMPONENT_GRAPH));
//		System.out.println("iterator has next: " + vis.items().hasNext());
		
		f.setReferrer((VisualItem)vis.items().next());
		
		// Currently the anchor is only used for runonce mode
		f.setLayoutAnchor(new Point2D.Double(500, 600));

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

		vis.putAction("layout", layout);
		vis.putAction("color", color);
		vis.putAction("complayout", complayout);
	}
	
	public Display getDisplay()
	{
		return mainDisplay;
	}
} // Close NetworkViewer


package networkviewer;

import java.awt.geom.Point2D;

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
import prefuse.controls.WheelZoomControl;
import prefuse.data.Graph;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
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

@SuppressWarnings("serial")
public class NetworkViewer extends Display {
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
	
	// The visualization itself.  We give the data to this, set 
	// its renderers and whatnot.
	private Visualization vis;
	
	private ControlAdapter clickControlDelegate;
	
	private HoverTooltip tooltipDelegate; 
	
	private boolean panControlEnabled = true;
	
	private boolean zoomControlEnabled = true;
	
	private boolean draggableAggregates = true;
	
	/**
	 * Constructor accepts a graph structure.
	 */
	public NetworkViewer()
	{
		vis = new Visualization();
	}
	
	/**
	 * Set a Graph to the visualization
	 * 
	 * @param g
	 * @param gString
	 * @return The VisualTupleSet that represents the graph.
	 */
	public VisualTupleSet addGraph(Graph g, String gString) 
	{
		COMPONENT_GRAPH = gString;
		return vis.add(gString, g);
	}
	
	/**
	 * Set an aggregateTable for the molecules to the visualization.
	 * @param atString
	 * @return
	 */
	public AggregateTable addAggregateTable(String atString)
	{
		AGG = atString;
		return vis.addAggregates(atString);
	}
	
	/**
	 * 
	 * @param aGG_DEC2
	 * @param aGG2
	 */
	public void addDecorators(String group, String source) 
	{
		AGG_DEC = group;
		AGG = source;
		vis.addDecorators(AGG_DEC, AGG);
	}

	/**
	 * returns the visualization object.
	 */
	public Visualization getVisualization()
	{
		return vis;
	}
	
	/**
	 * Set the click control delegate that will handle the 
	 * clicks.
	 * @param c
	 */
	public void setClickControl(ControlAdapter c)
	{
		clickControlDelegate = c;
	}
	
	/**
	 * Set whether or not zoom controlling should be allowed
	 * @param b
	 */
	public void setZoomControlEnabled(boolean b)
	{
		zoomControlEnabled = b;
	}
	
	/**
	 * Set whether or not pan controlling should be allowed
	 * @param b
	 */
	public void setPanControlEnabled(boolean b)
	{
		panControlEnabled = b;
	}
	
	/**
	 * Set the tooltip delegate object.
	 * 
	 * @param ht A HoverTooltip implementation.
	 */
	public void setTooltipDelegate(HoverTooltip ht)
	{
		tooltipDelegate = ht;
	}
	
	/**
	 * Set whether or not the aggregates should be draggable.
	 * @param b
	 */
	public void setDraggableAggregates(boolean b)
	{
		draggableAggregates = b;
	}
	
	/**
	 * Sets up the renderers,actions and controls.
	 * 
	 * @return Display  The display object that you embed in a JPanel
	 */
	public Display getDisplay()
	{
		setUpRenderers();
		
		setUpActions();
		
		// As a Display
		
		// Create a  object to hold the visualization
		this.setVisualization(vis);
		this.setSize(500, 600);
		
		// Turn on prettiness and antialiasing
		this.setHighQuality(true);
		
		/*
		 * I'm pretty sure I only need one of the following drag controls
		 */
		// drag individual items around
		//d.addControlListener(new DragControl());
		// Control the aggregates with a drag.
		if(draggableAggregates == true)
		{
			AggregateDragControl aggDrag = new AggregateDragControl(true, "color");
			aggDrag.addAction("bubbleColor");
			this.addControlListener(aggDrag);
		}
        
        // pan with left-click drag on background
        if(panControlEnabled)        
        	this.addControlListener(new PanControl()); 
        
        // Sets something in or out of the focus group after 
        // a certain number of clicks.
        //  d.addControlListener(new FocusControl(1));
        
        // click interactivity
        if(clickControlDelegate != null)
        	this.addControlListener(clickControlDelegate);
        
        // tooltip
        if(tooltipDelegate != null)
        	this.addControlListener(tooltipDelegate);
        
        // Zoom with the mouse wheel 
        if(zoomControlEnabled)
        	this.addControlListener(new WheelZoomControl());
        // zoom with right-click drag
        //d.addControlListener(new ZoomControl());	  
       
        vis.run("color");
        // start up the animated layout
        vis.run("layout");
        
        return this;
	}
	
	/*
	 * Set up the renderers
	 * 
	 *
	 * We create renderers for the nodes and edges, and then
	 * add them to a renderer factory, which decides which renderer to use
	 * for each visual item based on whether it is a node or edge.  Can do
	 * more things with predicates and create new renderers for advanced
	 * rendering.
	 */
	private void setUpRenderers()
	{
		// The LabelRenderer will display text (and an option image) in a shape.
		ComponentRenderer lr = new ComponentRenderer(VisualItem.LABEL);
		
		// The PolygonRenderer draws arbitrary polygons.  For aggregates.
		Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
	    ((PolygonRenderer)polyR).setCurveSlack(0.05f);   
		
		// The SelfReferencingRenderer is a small subclass of the EdgeRenderer.
		// If the two items comprising the edge are the same object, then 
		// an ellipse is drawn, else the parent class is used as is normal.
		SelfReferenceRenderer srr = new SelfReferenceRenderer();
		
		
		// The factory decides which renderer to use for a visual item.
		DefaultRendererFactory rf = new DefaultRendererFactory(lr);
		
		// Set the default renderer for edges
		rf.setDefaultEdgeRenderer(srr);
		
		// Add the aggregate renderer for the items in the group 'aggregates'.
		rf.add("ingroup('"+AGG+"') OR ingroup('bubbles')", polyR);
		
		// Add the aggregate decorater renderer
		rf.add(new InGroupPredicate(AGG_DEC), new LabelRenderer(AGG_CAT_LABEL));
		
		// Tell the visualization to use our factory.
		vis.setRendererFactory(rf);
		
		// Define the render order
		this.setItemSorter(new CMapItemSorter());
	}

	private void setUpActions() 
	{
		// The DataColorAction chooses a color given a palette based on the
		// data column that is passed to the constructor.
		ColorAction fill = new ColorAction(COMPONENT_GRAPH+".nodes", VisualItem.FILLCOLOR, ColorLib.rgb(180, 180, 180));
		
		// This predicate tells our fill ColorAction to use a different color for nodes with states.
		Predicate pred = (Predicate) ExpressionParser.parse("states != NULL");		
		fill.add(pred, ColorLib.rgb(255, 100, 100));
		
		// ColorActions color the given group a certain color.
		ColorAction text = new ColorAction(COMPONENT_GRAPH+".nodes",VisualItem.TEXTCOLOR, ColorLib.rgb(0,0,0));
		
		// Change the color of the strokes.
		ColorAction nodeStroke = new ColorAction(COMPONENT_GRAPH+".nodes",VisualItem.STROKECOLOR, ColorLib.rgb(20, 20, 20));
		
		ColorAction edgeStroke = new ColorAction(COMPONENT_GRAPH+".edges",VisualItem.STROKECOLOR, ColorLib.rgb(105, 105, 105));
		
		// Use these to change the size of the strokes.
		StrokeAction nodeStrokea = new StrokeAction(COMPONENT_GRAPH+".nodes", StrokeLib.getStroke(1.0f));
		StrokeAction edgeStrokea = new StrokeAction(COMPONENT_GRAPH+".edges", StrokeLib.getStroke(3.0f));
		StrokeAction aggStrokea = new StrokeAction(AGG, StrokeLib.getStroke(1.5f));
		
		// stroke color the aggregates
		ColorAction aStroke = new ColorAction(AGG, VisualItem.STROKECOLOR, ColorLib.rgb(10, 10, 10));
	    	    
	    // Draw the decorators.
	    ColorAction decText = new ColorAction(AGG_DEC, VisualItem.TEXTCOLOR, ColorLib.rgb(0,0,0)); 
	    FontAction decFont = new FontAction(AGG_DEC, FontLib.getFont("Tahoma", Font.BOLD, 20));
	    
	    // Fill the aggregates (molecules) with gray.
	    ColorAction aFill = new ColorAction(AGG,VisualItem.FILLCOLOR, ColorLib.rgb(240, 240, 240));
	          
		// create an action list containing all color assignments
	    ActionList color = new ActionList();
	   
	    // Add everything to the list.
	    color.add(new AggregateLayout(AGG));
	    color.add(new LabelLayout(AGG_DEC));
	    color.add(fill);
	    color.add(text);
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
	    
	    ActionList layout = new ActionList(2500);
	   
	    // Create the force directed layout that uses invisible edges in force 
	    // calculations as well as the visible ones.
	    ForceDirectedLayoutMagic f = new ForceDirectedLayoutMagic(COMPONENT_GRAPH, true, false);
	    f.setMagicEdges(true);
	    f.getForceSimulator().setSpeedLimit(9);
	    // Currently the anchor is only used for runonce mode, and runonce mode doesn't work...
	    f.setLayoutAnchor(new  Point2D.Double(500,600));
	    
	    layout.add(f);
	   	    
	    // TODO
	    // I am probably doing twice as much work by adding this here and to the color action list,
	    // but otherwise I am having trouble getting it to work. 
	    layout.add(new AggregateLayout(AGG));
	    layout.add(new LabelLayout(AGG_DEC));
	    layout.add(new RepaintAction());
	    
	   vis.putAction("layout", layout);
	   vis.putAction("color", color);
	}
} // Close NetworkViewer



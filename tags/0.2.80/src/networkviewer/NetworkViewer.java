package networkviewer;

import java.awt.Font;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

import editor.contactmap.Rule;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.layout.RandomLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.controls.ControlAdapter;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.StrokeLib;
import prefuse.util.force.Spring;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTupleSet;
import prefuse.visual.expression.InGroupPredicate;

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
		LabelRenderer lr = new LabelRenderer(VisualItem.LABEL);
		
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
	}

	private void setUpActions() 
	{
		// A color palette.  We define a color action later that depends on it.
		int[] palette = new int[] {
	            ColorLib.rgb(255,180,180), ColorLib.rgb(190,190,255), ColorLib.rgb(190,255,190), ColorLib.rgb(190,190,0), ColorLib.rgb(190,0,255)
	        };
		
		// The DataColorAction chooses a color given a palette based on the
		// data column that is passed to the constructor.
		//DataColorAction fill = new DataColorAction(COMPONENT_GRAPH+".nodes",COMP_PARENT_LABEL, Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		ColorAction fill = new ColorAction(COMPONENT_GRAPH+".nodes",VisualItem.FILLCOLOR, ColorLib.rgb(180, 180, 180));
		// For focus nodes. Not working
		//fill.add("_fixed", ColorLib.rgb(255,100,100));
		
		// ColorActions color the given group a certain color.
		//ColorAction text = new ColorAction(COMPONENT_GRAPH+".nodes",VisualItem.TEXTCOLOR, ColorLib.rgb(255, 255, 255));
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
	    //aStroke.add("_hover", ColorLib.rgb(255,100,100));
	    
	    /* 
	     * Decorator Stuff
	     */
	    ColorAction decText = new ColorAction(AGG_DEC, VisualItem.TEXTCOLOR, ColorLib.rgb(0,0,0)); 
	    FontAction decFont = new FontAction(AGG_DEC, FontLib.getFont("Tahoma", Font.BOLD, 20));
	    
	    // Color palette for the aggregates.
	    // Eventually I will need to dynamically do this for 
	    // any number of aggs
	    int[] AGGpalette = new int[] {
	        ColorLib.rgba(255,180,180,150),
	        ColorLib.rgba(190,195,255,150),
	        ColorLib.rgba(190,255,190,150),
	        ColorLib.rgba(190,190,0,150),
	        ColorLib.rgba(190,0,255,150)
	    };
	    
	    // Fill color the aggregates based on the molecule they represent.
	    //ColorAction aFill = new DataColorAction(AGG, AGG_CAT_LABEL,
	      //      Constants.NOMINAL, VisualItem.FILLCOLOR, AGGpalette);
	    
	    ColorAction aFill = new ColorAction(AGG,VisualItem.FILLCOLOR, ColorLib.rgb(240, 240, 240));
	          
	    		
		// create an action list containing all color assignments
	    ActionList color = new ActionList();
	    //I'm not sure why this has to be here, but it works perfectly now.
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
	    
	    // I will probably have to write a custom layout to put the 
	    // components into the molecules (Not sure how I will render
	    // those yet).
	    ActionList layout = new ActionList(2500);
	    
	    ForceDirectedLayoutMagic f = new ForceDirectedLayoutMagic(COMPONENT_GRAPH, true, false);
	    //f.setIterations(50);
	    //f.setMaxTimeStep(1000);
	    f.setMagicEdges(true);
	    f.getForceSimulator().setSpeedLimit(9);
	    f.setLayoutAnchor(new  Point2D.Double(500,600));
	   
	    // This didn't do anything 
	    //Iterator<Spring> springIt = f.getForceSimulator().getSprings();
	    
	    //while(springIt.hasNext())
	   // {
	    //	springIt.next().length = 400;
	  //  }
	    
	    layout.add(f);
	    //layout.add(new RandomLayout(COMPONENT_GRAPH));
	    
	    // TODO
	    // I am probably doing twice as much work by adding this here and to the color action list,
	    // but it works for now. 
	    layout.add(new AggregateLayout(AGG));
	    layout.add(new LabelLayout(AGG_DEC));
	    layout.add(new RepaintAction());
	    
	   vis.putAction("layout", layout);
	   vis.putAction("color", color);
	}
	
	/**
	 * Just a quick way to write out a log.  Sometimes I don't trust the console window
	 * in eclipse, so I write it out to a file when I am having doubts.
	 */
	public static void writeLog(String msg)
	{
		
		File file;
		FileWriter fstream;
		BufferedWriter out;
		 try{
			    // Create file 
			 file = new File("/Users/ams292/Desktop/out.txt");
			 System.out.println("File object created");
			 if(!file.exists())
				 file.createNewFile();
			 
			  fstream = new FileWriter(file);
			    
			    out = new BufferedWriter(fstream);
			        
			    out.write(msg);
			    //Close the output stream
			    out.close();
		    }
		 catch (Exception e)
		 {
			 //Catch exception if any
		      System.err.println("Error: " + e.getMessage());
		 }
		
	}
} // Close NetworkViewer



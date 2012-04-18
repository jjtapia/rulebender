package editor.contactmap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import collinsbubbleset.layout.BubbleSetLayout;

import networkviewer.AggregateBubbleLayout;
import networkviewer.AggregateLayout;
import networkviewer.JMenuItemRuleHolder;
import networkviewer.LabelLayout;
import networkviewer.VisualRule;


import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.layout.RandomLayout;
import prefuse.controls.ControlAdapter;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.StrokeLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class CMapClickControlDelegate extends ControlAdapter
{
	// A const for the string value of the category of aggregate
	public static final String AGG_CAT_LABEL = "molecule";
	
	private AggregateTable bubbleTable;
	private Visualization vis;
	
	private VisualRule activeRule;
	
	public CMapClickControlDelegate(Visualization v)
	{
		// Set the local reference to the visualization that this controller is attached to. 
		vis = v;
		
		// Create the bubbletable that is going to be used for aggregates.
		bubbleTable = vis.addAggregates("bubbles");

		// Add the shape column to the table.
        bubbleTable.addColumn(VisualItem.POLYGON, float[].class);
        bubbleTable.addColumn("type", String.class);
        
        //For collins
        bubbleTable.addColumn("SURFACE", ArrayList.class);
        bubbleTable.addColumn("aggregate_threshold", double.class);
        bubbleTable.addColumn("aggreagate_negativeEdgeInfluenceFactor", double.class);
        bubbleTable.addColumn("aggregate_nodeInfluenceFactor", double.class);
        bubbleTable.addColumn("aggreagate_negativeNodeInfluenceFactor", double.class);
        bubbleTable.addColumn("aggregate_edgeInfluenceFactor", double.class);
        
	    // Set the bubble stroke size
	    StrokeAction aggStrokea = new StrokeAction("bubbles", StrokeLib.getStroke(0f));
	    
	    // Set the color of the stroke.
	    ColorAction aStroke = new ColorAction("bubbles", VisualItem.STROKECOLOR, ColorLib.rgb(10, 10, 10));
	    
	    
	 // A color palette.  We define a color action later that depends on it.
		int[] palette = new int[] {
	            ColorLib.rgba(255,180,180,150), ColorLib.rgba(190,190,255, 150)
	        };
		
	    
	    // Set the fill color for the bubbles.
	    //ColorAction aFill = new ColorAction("bubbles",VisualItem.FILLCOLOR, ColorLib.rgb(240, 50, 40));
	    DataColorAction aFill = new DataColorAction("bubbles","type",Constants.NOMINAL,VisualItem.FILLCOLOR, palette);
	    
	    
	    // create an action list containing all color assignments
	    ActionList color = new ActionList();
	    
	    color.add(new AggregateBubbleLayout("bubbles"));
	    color.add(new BubbleSetLayout("bubbles", "component_graph"));
	    color.add(aggStrokea);
	    color.add(aStroke);
	    color.add(aFill);
	    //color.add(new RepaintAction());
	    
	    ActionList layout = new ActionList();
	     
	    //layout.add(new AggregateBubbleLayout("bubbles"));
	    layout.add(new BubbleSetLayout("bubbles", "component_graph"));
	    layout.add(new RepaintAction());
	    
	    vis.putAction("bubbleLayout", layout);
	    vis.putAction("bubbleColor", color);
	}
	
	/**
	 * Called when no VisualItem is hit.
	 */
	public void mouseClicked(MouseEvent e)
	{
		//super.mouseClicked(e);
		
		System.out.println("Click: ("+e.getX() + ", " + e.getY() + ")");
	}
	
    public void itemClicked(VisualItem item, MouseEvent e) 
    {
    	// left click
    	if(e.getButton() == MouseEvent.BUTTON1)
    	{
    		if((item instanceof NodeItem) ) 
    		{
    			System.out.println("You left clicked node " +  
    					item.getString(VisualItem.LABEL) + 
    					" at position (" + item.getX() + 
    					", " + item.getY() + ")");
    		}
    		else if(item instanceof EdgeItem)
    		{
    			System.out.println("Click: ("+e.getX() + ", " + e.getY() + ")"+" edge " +  
    					item.get("id"));
    			edgeLeftClicked(item, e);  
    		}
    		else if(item instanceof AggregateItem)
    		{
    			System.out.println("Click: ("+e.getX() + ", " + e.getY() + ")");
    			
    			System.out.println("Aggregate should contain:");
    			
    			Iterator it =  ((AggregateItem) item).items();
    			while (it.hasNext())
    			{
    				VisualItem vt = (VisualItem) it.next();
    				//DEBUG
    				if(vt instanceof EdgeItem)
    					System.out.println("\tItem (" + vt.getX() + ", " + vt.getY() + ") is Edge " + vt.get("id"));
    				else if(vt instanceof NodeItem)
    					System.out.println("\tItem (" + vt.getX() + ", " + vt.getY() + ") is Node " + vt.getString(VisualItem.LABEL));
    				
    			}
    			
    		}
    		else
    		{
    			System.out.println("Click: ("+e.getX() + ", " + e.getY() + ")");
		    }
		       
    	}    	
    	// Right Click
    	else if(e.getButton() == MouseEvent.BUTTON3)
    	{
    		System.out.println("Right Clicked");
    		
    		// Clear the bubble if there is one.
    		if(activeRule!=null)
			{
    			activeRule.setVisible(false);
    			activeRule = null;
			}
    		
    		if(item instanceof NodeItem)
    		{
    			JPopupMenu popupMenu = new JPopupMenu(); 
    			popupMenu.add("Right click on " + item.getString(VisualItem.LABEL));
    			popupMenu.show(e.getComponent(), e.getX(), e.getY());
    		}
    	}
    }
   	  
  /**
   * Called on the left click of an edge.
   * @param item
   */
	private void edgeLeftClicked(VisualItem item, MouseEvent event)
   	{
		// Get the edge object that corresponds to the edgeitem
   		Edge edge = (Edge) item.getSourceTuple();
   		
   		// get the rules that can make that edge.
   		ArrayList<VisualRule> rules = (ArrayList<VisualRule>) edge.get("rules"); 
   		
   		// Make a popupMenu that will have all of the rules added to it.
   		JPopupMenu popup = new JPopupMenu();
   		
   		// For each of the rules that could make the bond.
   		for(VisualRule rule: rules)
   		{
   			// Create a JMenuItem that corresponds to a rule.
   			JMenuItemRuleHolder menuItem = new JMenuItemRuleHolder(rule);
   			
   			// Add an actionlistener to the menu item.
   			menuItem.addActionListener(new ActionListener() 
   			{
   				// When the menuItem is clicked, fire this.
				public void actionPerformed(ActionEvent e) 
				{
					JMenuItemRuleHolder source = (JMenuItemRuleHolder)(e.getSource());
	   		       
	   		       // Build the rule if it is not built already.
	   		       	if(!(source.getRule().isBuilt()))
	   		       		source.getRule().pack("component_graph", bubbleTable);
	   		       	
	   		       	// Clear the current activeRule.
	   		       	if(activeRule != null)
	   		       	{
	   		       		activeRule.setVisible(false);
	   		       		activeRule = null;
	   		       	}
	   		       	
	   		       	// Set the active rule
	   		       	activeRule = source.getRule();
	   		     
	   		       	// Set the bubbles to visible
	   		       	activeRule.setVisible(true);
	   		     
	   		       	// Run the actions.  Apparently the layout does not need
	   		       	// to be run here...
	   		       	vis.run("bubbleLayout");
	   		       	vis.run("bubbleColor");
				}
   			});
   			
   			// Add the JMenuItem to the popup menu.
   			popup.add(menuItem);
   			
   		}
   		// After adding all of the rules, show the popup.
   		popup.show(event.getComponent(), event.getX(), event.getY());
   		
   	  }
}

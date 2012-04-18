package editor.contactmap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

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
		
	}
	
    public void itemClicked(VisualItem item, MouseEvent e) 
    {	
    	// left click
    	if(e.getButton() == MouseEvent.BUTTON1)
    	{
    		if((item instanceof NodeItem) ) 
    		{
    			//System.out.println("You left clicked node " +  item.getString(VisualItem.LABEL));
    		}
    		else if(item instanceof EdgeItem)
    		{
    			edgeLeftClicked(item, e);  
    		}
    		else if(item instanceof AggregateItem)
    		{
    			//System.out.println("You left clicked aggregate " + item.getString(AGG_CAT_LABEL));
    		}
    		else
    		{
    			//System.out.println("Background");
		    }
		       
    	}    	
    	// Right Click
    	else if(e.getButton() == MouseEvent.BUTTON3)
    	{
    		//System.out.println("Right Clicked");
    		
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
					
				}
   			});
   			
   			// Add the JMenuItem to the popup menu.
   			popup.add(menuItem);
   			
   		}
   		// After adding all of the rules, show the popup.
   		popup.show(event.getComponent(), event.getX(), event.getY());
   		
   	  }
}

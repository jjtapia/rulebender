package networkviewer;

import java.util.ArrayList;

import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;


/**
 * Holds information in the form of prefuse visual objects about a rule.  
 * 
 * After the visualization has launched, this also acts as a holder for the 
 * bubbleset aggregateItems.  Before the bubbles are viewed, the pack
 * method must be called.
 * 
 * @author ams292
 */
public class VisualRule 
{
	
	// The text of the rule
	private String ruleText;
	
	// The prefuse Node objects that are in the reactants.
	private ArrayList<Node> reactantComponents;
	
	// The prefuse Node objects that are in the products.
	private ArrayList<Node> productComponents;
	
	// The prefuse Node objects that are in the reactants.
	private ArrayList<Edge> reactantBonds;
	
	// The prefuse Node objects that are in the products.
	private ArrayList<Edge> productBonds;
	
	// The prefuse Edge objects that are added as a result of the rule.
	private ArrayList<Edge> addBonds;
	
	// The prefuse Edge objects that are removed as a result of the rule.
	private ArrayList<Edge> removeBonds;

	// The AggregateItem corresponding to the context bubble.
	private AggregateItem context;
	
	// The AggregateItem corresponding to the center bubble.
	private AggregateItem center;

	private Visualization vis;
	private String group;
	
	// Flag for whether or not the bubbles have been built.
	private boolean built = false;
	
	/**
	 * Constructor.  Takes the String name of the rule as a parameter.
	 * 
	 * @param text 
	 */
	public VisualRule(String text)
	{
		ruleText = text;
		reactantComponents = new ArrayList<Node>();
		productComponents = new ArrayList<Node>();
		reactantBonds = new ArrayList<Edge>();
		productBonds = new ArrayList<Edge>();
		addBonds = new ArrayList<Edge>();
		removeBonds = new ArrayList<Edge>();
	}
	
	/**
	 * Add a Node to the reactancts.
	 * @param n
	 */
	public void addReactantNode(Node n)
	{
		reactantComponents.add(n);
	}
	
	/**
	 * Add a Node to the products.
	 * @param n
	 */
	public void addProductNode(Node n)
	{
		productComponents.add(n);
	}
	
	public void addReactantBond(Edge e)
	{
		reactantBonds.add(e);
	}
	
	public void addProductBond(Edge e) 
	{
		productBonds.add(e);
	}
	
	/**
	 * Add an Edge to the addBonds.
	 * @param n
	 */
	public void addAddBond(Edge n)
	{
		addBonds.add(n);
	}
	

	/**
	 * Add an Edge to the removeBonds.
	 * @param n
	 */
	public void addRemoveBond(Edge e)
	{
		removeBonds.add(e);
	}

	/**
	 * Getter for the text of the rule.
	 * 
	 * @return ruleText
	 */
	public String getName() {
		return ruleText;
	}

	/**
	 * Get the context AggregateItem.
	 * 
	 * @return context
	 */
	public AggregateItem getContext() {
		return context;
	}

	/**
	 * Get the center AggregateItem
	 * 
	 * @return
	 */
	public AggregateItem getCenter() {
		return center;
	}

	/**
	 * Build the AggregateItems according to the node and edge data already 
	 * given. Creates a center and a context bubble. Takes the String name of 
	 * the group of which the nodes and edges are members, and the 
	 * AggregateTable instance that will hold the AggregateItems.
	 * 
	 * @param group
	 * @param bubbleTable
	 */
	public void pack(String group_, AggregateTable bubbleTable)
	{
		//System.out.println("\n\nPacking:");
		
		vis = bubbleTable.getVisualization();
		center = (AggregateItem) bubbleTable.addItem();
		context = (AggregateItem) bubbleTable.addItem();
		group = group_;
		center.set("type", "center");
		context.set("type", "context");
		
		// Add all of the bonds that are created.
		for(Edge e : addBonds)
		{
			if(vis.getVisualItem(group,e).getX() == 0 && vis.getVisualItem(group,e).getY() == 0)
			{
				vis.getVisualItem(group, e).setX((vis.getVisualItem(group, e).getBounds().getMaxX() - vis.getVisualItem(group, e).getBounds().getMinX())/2);
				vis.getVisualItem(group, e).setY((vis.getVisualItem(group, e).getBounds().getMaxY() - vis.getVisualItem(group, e).getBounds().getMinY())/2);
			}
				
			//System.out.println("Adding Node to center: (" + vis.getVisualItem(group, e.getSourceNode()).getX()+ ", " + vis.getVisualItem(group, e.getSourceNode()).getY() + ")");
			center.addItem(vis.getVisualItem(group, e.getSourceNode()));
			
			//System.out.println("Adding Node to center: (" + vis.getVisualItem(group, e.getTargetNode()).getX()+ ", " + vis.getVisualItem(group, e.getTargetNode()).getY() + ")");
			center.addItem(vis.getVisualItem(group, e.getTargetNode()));
			
			//System.out.println("Adding Edge to center: (" + vis.getVisualItem(group, e).getX()+ ", " 
			//		+ vis.getVisualItem(group, e).getY() + ")\n\t"+
			//		vis.getVisualItem(group, e).getBounds().toString());
			center.addItem(vis.getVisualItem(group, e));
		}
		
		// Add all of the bonds that are removed
		for(Edge e : removeBonds)
		{
			if(vis.getVisualItem(group,e).getX() == 0 && vis.getVisualItem(group,e).getY() == 0)
			{
				vis.getVisualItem(group, e).setX((vis.getVisualItem(group, e).getBounds().getMaxX() - vis.getVisualItem(group, e).getBounds().getMinX())/2);
				vis.getVisualItem(group, e).setY((vis.getVisualItem(group, e).getBounds().getMaxY() - vis.getVisualItem(group, e).getBounds().getMinY())/2);
			}
			
			//System.out.println("Adding Node to center: (" + vis.getVisualItem(group, e.getSourceNode()).getX()+ ", " + vis.getVisualItem(group, e.getSourceNode()).getY() + ")");
			center.addItem(vis.getVisualItem(group, e.getSourceNode()));
			
		//	System.out.println("Adding Node to center: (" + vis.getVisualItem(group, e.getTargetNode()).getX()+ ", " + vis.getVisualItem(group, e.getTargetNode()).getY() + ")");
			center.addItem(vis.getVisualItem(group, e.getTargetNode()));
			
			//System.out.println("Adding Edge to center: (" + vis.getVisualItem(group, e).getX()+ ", " 
			//		+ vis.getVisualItem(group, e).getY() + ")\n\t"+
			//		vis.getVisualItem(group, e).getBounds().toString());
			
			center.addItem(vis.getVisualItem(group, e));
		}

		// no edges in the bubbles for now.
		
		for(Edge e : reactantBonds)
		{
			if(vis.getVisualItem(group,e).getX() == 0 && vis.getVisualItem(group,e).getY() == 0)
			{
				vis.getVisualItem(group, e).setX((vis.getVisualItem(group, e).getBounds().getMaxX() - vis.getVisualItem(group, e).getBounds().getMinX())/2);
				vis.getVisualItem(group, e).setY((vis.getVisualItem(group, e).getBounds().getMaxY() - vis.getVisualItem(group, e).getBounds().getMinY())/2);
			}
			
			if(productBonds.contains(e))
			{
				//System.out.println("Adding Edge to context: (" + vis.getVisualItem(group, e).getX()+ ", " + vis.getVisualItem(group, e).getY() + ")");
				context.addItem(vis.getVisualItem(group, e));
			}
		}
		
		// Add the context
		// afaik the compononents cannot change in a rule, so all of the 
		// components in the reactants are part of the context.
		for(Node n : reactantComponents)
		{
			if(!center.containsItem(vis.getVisualItem(group,n)))
			{
				//System.out.println("Adding Node to context: (" + vis.getVisualItem(group, n).getX()+ ", " + vis.getVisualItem(group, n).getY() + ")");
				context.addItem(vis.getVisualItem(group, n));
			}
				
		}
		
		//TODO Still need to add the edges that are present in the reaction.
		// Will have to do this at construction of the VisualRule.
		built = true;
	}

	public boolean isBuilt() 
	{
		return built;
	}

	public void setVisible(boolean b)
	{	
		context.setVisible(b);
		center.setVisible(b);	
	}
}

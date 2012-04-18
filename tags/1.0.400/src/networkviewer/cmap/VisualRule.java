package networkviewer.cmap;

import java.util.ArrayList;

import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;


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
	
	// changed state nodes
	private ArrayList<Node> changedStates;
	
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
		changedStates = new ArrayList<Node>();
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
	
	public void addChangedStateNode(Node n) {
		changedStates.add(n);
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
		vis = bubbleTable.getVisualization();
		center = (AggregateItem) bubbleTable.addItem();
		context = (AggregateItem) bubbleTable.addItem();
		group = group_;
		center.set("type", "center");
		context.set("type", "context");
		
		// Add all of the bonds that are created.
		for(Edge e : addBonds)
		{	
			center.addItem(vis.getVisualItem(group, e.getSourceNode()));
			center.addItem(vis.getVisualItem(group, e.getTargetNode()));
			center.addItem(vis.getVisualItem(group, e));
			
			addBondToCenterOrContext(center, e);
			
		}
		
		// Add all of the bonds that are removed
		for(Edge e : removeBonds)
		{	 
			center.addItem(vis.getVisualItem(group, e.getSourceNode()));
			center.addItem(vis.getVisualItem(group, e.getTargetNode()));
			center.addItem(vis.getVisualItem(group, e));
			
			addBondToCenterOrContext(center, e);
		}
		
		for (Node n : changedStates) {
			center.addItem(vis.getVisualItem(group, n));
			addNodeToCenterOrContext(center, n);
		}
		
		// Add the bonds that are in both the reactants and products to the context.
		for(Edge e : reactantBonds)
		{	
			if(e != null && productBonds.contains(e))
			{
				context.addItem(vis.getVisualItem(group, e));
				
				addBondToCenterOrContext(context, e);
			}
		}
		
		// Add the context
		// afaik the compononents cannot change in a rule, so all of the 
		// components in the reactants are part of the context.
		for(Node n : reactantComponents)
		{
			if(!center.containsItem(vis.getVisualItem(group,n)))
			{
				context.addItem(vis.getVisualItem(group, n));
				
				addNodeToCenterOrContext(context, n);
			}
				
		}
		
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
	
	private void addNodeToCenterOrContext(AggregateItem aggregate, Node node) {
		Node comp_node = node;
		
		String type = node.getString("type");
		if (type != null && type.equals("state")) {
			comp_node = (Node)node.get("stateparent");
			aggregate.addItem(vis.getVisualItem(group, comp_node));
		}
		
		// add all state nodes of leftnode
		if (comp_node.get("states") != null) {
			ArrayList<Node> state_nodes = (ArrayList<Node>) comp_node
					.get("state_nodes");

			if (state_nodes != null) {

				for (int i = 0; i < state_nodes.size(); i++) {
					VisualItem stateitem = vis.getVisualItem(group,
							state_nodes.get(i));
					
					aggregate.addItem(stateitem);
				}
			}
		}
	}
	
	private void addBondToCenterOrContext(AggregateItem aggregate, Edge e) {
		Node leftnode = e.getSourceNode();
		// for state node, also add component node to center
		if (e.get("leftparent") != null) {
			aggregate.addItem(vis.getVisualItem(group, (Node)e.get("leftparent")));
			leftnode = (Node)e.get("leftparent");
		}
		
		// add all state nodes of leftnode
		if (leftnode.get("states") != null) {
			ArrayList<Node> state_nodes = (ArrayList<Node>) leftnode
					.get("state_nodes");

			if (state_nodes != null) {

				for (int i = 0; i < state_nodes.size(); i++) {
					VisualItem stateitem = vis.getVisualItem(group,
							state_nodes.get(i));
					
						aggregate.addItem(stateitem);
				}
			}
		}
		
		Node rightnode = e.getTargetNode();
		if (e.get("rightparent") != null) {
			aggregate.addItem(vis.getVisualItem(group, (Node)e.get("rightparent")));
			rightnode = (Node)e.get("rightparent");
		}
		
		// add all state nodes of rightnode
		if (rightnode.get("states") != null) {
			ArrayList<Node> state_nodes = (ArrayList<Node>) rightnode
					.get("state_nodes");

			if (state_nodes != null) {

				for (int i = 0; i < state_nodes.size(); i++) {
					VisualItem stateitem = vis.getVisualItem(group,
							state_nodes.get(i));
						aggregate.addItem(stateitem);
				}
			}
		}
	}
}

package rulebender.contactmap.prefuse;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.table.TableModel;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import rulebender.contactmap.models.Bond;
import rulebender.contactmap.models.BondAction;
import rulebender.contactmap.models.ContactMapModel;
import rulebender.contactmap.models.Compartment;
import rulebender.contactmap.models.CompartmentTable;
import rulebender.contactmap.models.Component;
import rulebender.contactmap.models.ComponentPattern;
import rulebender.contactmap.models.Molecule;
import rulebender.contactmap.models.MoleculePattern;
import rulebender.contactmap.models.Rule;
import rulebender.contactmap.models.RulePattern;
import rulebender.contactmap.models.State;
import rulebender.contactmap.view.ContactMapView;
import rulebender.core.prefuse.networkviewer.contactmap.CMAPNetworkViewer;
import rulebender.core.prefuse.networkviewer.contactmap.VisualRule;



/**
 * This class creates the data structure required for prefuse and 
 * interacts with the NetworkView class in order to show 
 * a visualization of the contact map data structure constructed 
 * by the parser. 
 * 
 * @author ams
 *
 */

public class ContactMapVisual
{
	// To use Prefuse, you first have to construct the data structures that it
	// uses for visualization e.g. the Graph object. This Graph object will 
	// hold the nodes and edges for our graph visualization.
	//
	Graph m_componentGraph;

	// Prefuse uses string identifiers for data structures and 
	// their data fields.  These Strings variables are all used 
	// as identifiers.
	
	// Each node stores a string label for the molecule it belongs to.
	String COMP_PARENT_LABEL = "molecule";
	// Each state node stores a string label for the component it belongs to.
	String STATE_PARENT_LABEL = "component";
	// This is the identifier for our primary Graph object
	String COMPONENT_GRAPH = "component_graph";
	// This is a label used with Aggregate objects.  They are used for visually grouping nodes.
	// In our case, they make the molecules
	String AGG_CAT_LABEL = "molecule";
	// This is an identifier for the aggregate labels
	String AGG_DEC = "aggregate_decorators";
	// This is a label for the aggregates themselves.
	String AGG = "aggregates";
	// This is a label for aggregates of component with states
	String AGG_COMP = "aggregates_component";
	// This is a label for component and its states: molecule.component
	String AGG_COMP_LABEL = "aggregates_component_label";
	
	// The visualization object is a primary data structure for prefuse.
	// We will give our Graph object to it, and also use it to access
	// and change properties of the visualization.
	Visualization m_vis;
	
	// The NetworkViewer is an object I created to encapsulate most of 
	// prefuse nastiness.  You will interact with this mostly.  I plan
	// on making it much easier to use once I get back.
	// NetworkViewer is a subclass of prefuse.Display which is a 
	// Component and can be added to a JPanel.
	CMAPNetworkViewer m_networkViewer;
	
	ContactMapModel m_model;
	
	Dimension m_mainDisplaySize;
	
	 // This is an index to the Node objects so that I can retrieve them 
    // based on the string value "<parent molecule index>.<component index>.<state index>".
    private Hashtable<String, Node> m_nodes;
    
    // This is so I can get an edge, based on an string value: bondID.<type> (either component or state)
    private Hashtable<String, Edge> m_edges;
    
    // Hash table for all the hub nodes of molecule-level rules.
    // The set contains all the involved nodes in a rule.
    private Hashtable<Set<Node>, Node> m_hubNodes;
    
	
	public ContactMapVisual(ContactMapView view, ContactMapModel model_in, Dimension cMapSize) 
	{

		m_model = model_in;
		m_mainDisplaySize = cMapSize;
		
		// Instantiate the NetworkViewer object.
		m_networkViewer = new CMAPNetworkViewer(m_mainDisplaySize); 
		
		// Instantiate the Graph
		/*
		 *  VisualItem.LABEL   [Integer]   |   COMP_PARENT_LABEL ('molecule') [String]   |   'rules'   [ArrayList<Rule>]  |
		 *  |   'states'     |
		 * 
		 */
		m_componentGraph = new Graph();
		
		// Ideally I wanted to remove all interaction with the Visualization object,
		// but I didn't quite finish it yet.  
		m_vis = m_networkViewer.getVisualization();
				
		// Graphs (and all other data structures in prefuse) are table-based data structures.  Each node is a row in the
		// table and the columns hold data about the node.  Here we add a 
		// column for the label of the node, and then a column for the
		// molecule of which the node is a member.
        
		// The Label for a node.  Constant value int from VisualItem
		m_componentGraph.addColumn(VisualItem.LABEL, String.class);
		
        // Node type: component, state
        m_componentGraph.addColumn("type", String.class);
        
        // Node id: moleculeIndex.componentIndex<.stateIndex>
        m_componentGraph.addColumn("ID", String.class);
		
		// The parent (molecule) for a node.  String.
        m_componentGraph.addColumn(COMP_PARENT_LABEL, String.class);
        
        // The molecule expression for a node.  String.
        m_componentGraph.addColumn("molecule_expression", String.class);
        
        // The parent (component) for a state node.  String.
        m_componentGraph.addColumn(STATE_PARENT_LABEL, String.class);
        
        // The VisualRule object that can create an edge.  Arraylist of VisualRule objects.
        m_componentGraph.addColumn("rules", ArrayList.class);
        
        // The states that the component can be in.  ArrayList of Strings.
        m_componentGraph.addColumn("states", ArrayList.class);
        
        // State nodes
        m_componentGraph.addColumn("state_nodes", ArrayList.class);
        
        //store the parent component node for a state node
        m_componentGraph.addColumn("stateparent", Node.class);
        
        // Store the parent component nodes for sourcenode and targetnode of an edge
        // if they are state nodes
        m_componentGraph.addColumn("leftparent", Node.class);
        m_componentGraph.addColumn("rightparent", Node.class);
        
        // add a column to decide which edge to show in different display mode (show states)
        m_componentGraph.addColumn("displaymode", String.class);
        
        // add a column to check if the node has edge linked to it
        m_componentGraph.addColumn("hasedge", boolean.class);
        
        // add a column to indicate a component has state change
        m_componentGraph.addColumn("statechange", boolean.class);
        
        // add a column to store annotation
        m_componentGraph.addColumn("annotation", TableModel.class);
        
        m_componentGraph.addColumn("model", Object.class);
        
        // Aggregate table for compartments
		AggregateTable at_compartment = m_networkViewer.addAggregateTable("compartments");

		// Aggregate tables are created by adding an aggregator group to the
		// NetworkViewer. This is also a table data structure and will be
		// used to keep track of the shape of the molecules.
		AggregateTable at = m_networkViewer.addAggregateTable(AGG);

		// Aggregate tables for components with states
		AggregateTable at_comp = m_networkViewer.addAggregateTable(AGG_COMP);
		
		// Add the graph to the visualization
		// Pass the Graph object, and the string label for it.
        m_networkViewer.addGraph(m_componentGraph, COMPONENT_GRAPH);
    	
        // Add the decorators to the visualization
        // Decorators are a way to add labels to objects.
        // Pass the string label for the decorators, and
        // the string value for the objects that they decorate.
		m_networkViewer.addDecorators(AGG_DEC, AGG);
		
		// This sets the decorator objects as not interactive.
		m_vis.setInteractive(AGG_DEC, null, false);
		m_vis.setInteractive(COMPONENT_GRAPH+".edges", null, false);
		
        // Create the aggregate table and add it to the visualization
        //at = vis.addAggregates(AGG);
		
		at_compartment.addColumn(VisualItem.POLYGON, float[].class);
		// Molecule Compartment
		at_compartment.addColumn("compartment", String.class);
		
		// For collins
		at_compartment.addColumn("SURFACE", ArrayList.class);
		at_compartment.addColumn("aggregate_threshold", double.class);
		at_compartment.addColumn("aggreagate_negativeEdgeInfluenceFactor", double.class);
		at_compartment.addColumn("aggregate_nodeInfluenceFactor", double.class);
		at_compartment.addColumn("aggreagate_negativeNodeInfluenceFactor", double.class);
		at_compartment.addColumn("aggregate_edgeInfluenceFactor", double.class);
		
		at_compartment.addColumn("type", String.class);

		// add a column to store annotation
		at_compartment.addColumn("annotation", TableModel.class);
		
        // Add a column to the table to hold the polygon information
        // that will be used to render the aggregates.  This float array
		// will hold the points that create the molecule boundary.
        at.addColumn(VisualItem.POLYGON, float[].class);
        // Add a column that will keep track of the types of aggregates.
        at.addColumn(AGG_CAT_LABEL, String.class); 
        // The molecule expression for a node.  String.
        at.addColumn("molecule_expression", String.class);
        // Molecule Compartment
        at.addColumn("compartment", String.class);  
        // add a column to store annotation
		at.addColumn("annotation", TableModel.class);
		
		at.addColumn("type", String.class);
		
        // Add a column to the talbe to hold the polygon information
        at_comp.addColumn(VisualItem.POLYGON, float[].class);
        // Add a column that will keep track of the types of aggregates. 
        at_comp.addColumn(AGG_COMP_LABEL, String.class);
        
        // Set how the clicks will be handled.
        // Here you can create your own interactions for the Graph.
        // If you do not want interactions, simply do not add a
        // clickcontroldelegate.  See the editor.contactmap.CMapClickControlDelegate
        // for how to implement it.
        CMapClickControlDelegate cctrldelegate = new CMapClickControlDelegate(view , m_networkViewer.getVisualization());
        m_networkViewer.setClickControl(cctrldelegate);

        // This is an index to the Node objects so that I can retrieve them 
        // based on the string value "<parent molecule index>.<component index>".
        m_nodes = new Hashtable<String, Node>();
        
        // This is so I can get an edge, based on an string value: bondID.type(component, state)
        m_edges = new Hashtable<String, Edge>();
        
        // This is how to get a hub node based on a Node set (all the involved nodes in a rule)
        m_hubNodes = new Hashtable<Set<Node>, Node>();
        
        // Now begins the construction of the data structure.  I am not sure
        // if you will be able to use Yao's parser and get the same structure,
        // but if you can, then you will be able to use this code below to create 
        // the visualizaiton.
		Molecule tmole;
		Component tcomp;
		State tstate;
		
		// store first node of each compartments to control layout
		Hashtable<String, Node> compartmentsFirstNode = new Hashtable<String, Node>();
		
		// This is so I can add invisible edges between all of the components in the 
		// molecule.  This is for the force directed layout.
		ArrayList<Node> otherCompsInMol;
		
		// In general, we construct the graph by making a node for each component
		// and then group the components in the same molecule into an aggregate. 
		// There are visible edges representing bonds, and then invisible edges
		// between components in the same molecule. 
		
		// initialize compartment aggregates
		ArrayList<AggregateItem> cmptAggList = new ArrayList<AggregateItem>();;
		initCompartmentAggregates(at_compartment, cmptAggList);
		
		// For each molecule.
		for(int i=0; i<m_model.getMolecules().size(); i++)
		{			
			// Get the molecule
			tmole = m_model.getMolecules().get(i);
			
			// If there are no components in the molecule, then the molecule
			// is rendered as if it were a component.
			if(tmole.getComponents().size()==0)
			{
				// Create the node
				Node n = m_componentGraph.addNode();
				
				// Set the label to some blank space to give the node some size.
				n.setString(VisualItem.LABEL, "       ");

				// Set the type as a component node.
				n.setString("type", "component");
				
				// Set its ID
			    n.setString("ID", ""+i);
			    
			    // Set the expression of the molecule
			    n.setString("molecule_expression", tmole.getExpression());

			    // Add it to the hashtable for future reference.
			    m_nodes.put(""+i, n);
			
			    // Set its compartment
				String compartment = tmole.getFirstCompartment();
				if (compartment != null) 
				{
					addItemToCompartmentAgg(cmptAggList, n, compartment);
				}
			    
				// Create the aggregate that forms the molecule.
				AggregateItem agg = (AggregateItem) at.addItem();
				agg.setString("type", "molecule");
				agg.setString(AGG_CAT_LABEL, tmole.getName());
				agg.setString("molecule_expression", tmole.getExpression());
				agg.setString("compartment", compartment);
				agg.addItem(m_vis.getVisualItem(COMPONENT_GRAPH, n));
				
				// add invisible edge inside compartment to control layout
				if (compartment != null) {
					if (compartmentsFirstNode.containsKey(compartment)) {
						Node cfn = compartmentsFirstNode.get(compartment);

						// add invisible edge
						Edge te = m_componentGraph.addEdge(cfn, n);
						te.set("type", "compartment_edge");
						EdgeItem ei = (EdgeItem) m_vis.getVisualItem(
								COMPONENT_GRAPH + ".edges", te);
						ei.setVisible(false);
					}
					else {
						// this is the first node
						compartmentsFirstNode.put(compartment, n);
					}
				}
			}
			
			// It has components
			else
			{	
				// for making invisible edges between components in the same molecule.
				otherCompsInMol = new ArrayList<Node>();
				
				// Get the aggregate so you can add nodes to it.
				AggregateItem aggregateForMolecule = (AggregateItem) at.addItem();
				aggregateForMolecule.setString("type", "molecule");
				aggregateForMolecule.setString(AGG_CAT_LABEL, tmole.getName());
				aggregateForMolecule.setString("molecule_expression", tmole.getExpression());
				// Set its compartment
				String compartment = tmole.getFirstCompartment();
				aggregateForMolecule.setString("compartment", compartment);
				
				//TODO I do not see the point of this at_compartment table....
				// Originally Wen added the convex hull compartments, but these are only
				// containing a single molecule and are named after it....
				
				
				// add another same aggregate in table at_compartment
				// Get the aggregate so you can add nodes to it.
				AggregateItem aggregateForMoleculeCompartment = (AggregateItem) at_compartment.addItem();
				aggregateForMoleculeCompartment.setString("type", "compartment");
				//DEBUG
				//System.out.println("365: Setting compartment POSSIBLE MOLE NAME: " + tmole.getName());
				aggregateForMoleculeCompartment.setString("compartment", tmole.getName());
				aggregateForMoleculeCompartment.setVisible(false);			
				
				// special component node represent Molecule()
				Node specialCompNode = m_componentGraph.addNode();
				specialCompNode.setString(VisualItem.LABEL, " ");
				specialCompNode.setString("type", "component");
				// Set its ID
				specialCompNode.setString("ID", ""+i);
			    // Add it to the hashtable
			    m_nodes.put(""+i, specialCompNode);
			    
			    // set invisible
			    VisualItem nItem = m_vis.getVisualItem(COMPONENT_GRAPH, specialCompNode);
			    nItem.setVisible(false);
				
			    // Add it to the compartment
				if (compartment != null) {
					addItemToCompartmentAgg(cmptAggList, specialCompNode, compartment);
				}
				
				// Add it to the aggregate
			    aggregateForMolecule.addItem(nItem);
			    aggregateForMoleculeCompartment.addItem(nItem);
				
			    otherCompsInMol.add(specialCompNode);
			    
				// For each component
				for(int j=0;j<tmole.getComponents().size();j++)
				{	
					// Get the component
					tcomp = tmole.getComponents().get(j);
				
					// Make a new node for it
					Node n = makeComponentNode(tmole, tcomp, i, j);
				    
				    // Add it to the compartment
				    if (compartment != null) {
						addItemToCompartmentAgg(cmptAggList, n, compartment);
					}
				    // Add it to the aggregate
				    aggregateForMolecule.addItem(m_vis.getVisualItem(COMPONENT_GRAPH,n));
				    aggregateForMoleculeCompartment.addItem(m_vis.getVisualItem(COMPONENT_GRAPH,n));
				    
		
				    // add invisible edge inside compartment to control layout
					if (compartment != null && j==0) {

						if (compartmentsFirstNode.containsKey(compartment)) {

							Node cfn = compartmentsFirstNode.get(compartment);

							// if the first node is a node without components,
							// then add a invisible edge to control layout
							if (!cfn.getString("ID").contains(".")) {
								// add invisible edge
								Edge te = m_componentGraph.addEdge(cfn, n);
								te.set("type", "compartment_edge");
								EdgeItem ei = (EdgeItem) m_vis.getVisualItem(
										COMPONENT_GRAPH + ".edges", te);
								ei.setVisible(false);
							}
						}
						else {
							// this is the first node
							compartmentsFirstNode.put(compartment, n);
						}
					}
					     
					// If it has states
					if(tcomp.getStates().size() != 0)	
					{
						n.set("states", new ArrayList<String>());
						n.set("state_nodes", new ArrayList<Node>());
						
						// set aggregates for component with states
						AggregateItem aggregateForComponent = (AggregateItem) at_comp.addItem();
						aggregateForComponent.setString(AGG_COMP_LABEL, tmole.getName()+"."+tcomp.getName());
						aggregateForComponent.addItem(m_vis.getVisualItem(COMPONENT_GRAPH,n));
						
						for(int k = 0; k < tcomp.getStates().size(); k++)
						{
							
							tstate = tcomp.getStates().get(k);
							
							((ArrayList<String>) n.get("states")).add(tstate.getName());
							
							// make a new node for tstate
							Node n_state = makeStateNode(tmole, tcomp, tstate, i, j, k, n);
							
							// Add it to the compartment
							if (compartment != null) {
								addItemToCompartmentAgg(cmptAggList, n_state, compartment);
							}
							// add it to the molecule
							aggregateForMolecule.addItem(m_vis.getVisualItem(COMPONENT_GRAPH,n_state));
							aggregateForMoleculeCompartment.addItem(m_vis.getVisualItem(COMPONENT_GRAPH,n_state));
							// add it to the component
							aggregateForComponent.addItem(m_vis.getVisualItem(COMPONENT_GRAPH,n_state));
							
							((ArrayList<Node>) n.get("state_nodes")).add(n_state);
							
							//set invisible
							NodeItem nitem = (NodeItem) m_vis.getVisualItem(COMPONENT_GRAPH+".nodes", n_state);
							nitem.setVisible(false);
							
							// add invisible edges connected to component
							Edge te = m_componentGraph.addEdge(n, n_state);
							te.set("type", "stateInvisible_edge");
					    	EdgeItem ei = (EdgeItem) m_vis.getVisualItem(COMPONENT_GRAPH+".edges", te);
								
					    	ei.setVisible(false);
						}  
					}  

				    // Add invisible edges for the force directed layout.
				    for(Node on : otherCompsInMol)
				    {
				    	Edge te = m_componentGraph.addEdge(n, on);
				    	te.set("type", "componentInvisible_edge");
				    	EdgeItem ei = (EdgeItem) m_vis.getVisualItem(COMPONENT_GRAPH+".edges", te);
							
				    	ei.setVisible(false);
				    }
				     
				    otherCompsInMol.add(n);
				}
			}
		} // Close for each molecule
		
		
		// create edge for each bond
		createEdgesForBonds();
		
		String previousRuleText="";
		
		// map rules
		for(int l = 0; l < m_model.getRules().size(); l++)
		{	
			// Get a reference to the rule
			Rule thisRule = m_model.getRules().get(l);
			
			// Create a visual rule.  Used for interaction.  (See editor.contactmap.CMapClickControlDelegate.java)
			VisualRule r_comp = new VisualRule(thisRule.getLabel(), thisRule.getExpression());
			VisualRule r_state = new VisualRule(thisRule.getLabel(), thisRule.getExpression());
			
			
			// Perform a check to see if this is the reverse rule for a bidirection.
			// This is a quick hack to fix this problem, but it works for now.  
			// The old parser did not produce
			if(!thisRule.getExpression().equals(previousRuleText))
			{
				// map rules to bonds
				identifyBonds(thisRule, r_comp, r_state);
			}
			
			// reactants, products, changed states
			identifyStateChange(thisRule, r_state);
			identifyReactants(thisRule, r_comp, r_state);
			identifyProducts(thisRule, r_comp, r_state);
			
			
			// is there any changed state?
			if (r_state.getChangedStateNodeCount() != 0) {
				continue;
			}
			
			
			// is there any reaction bond?
			if (r_comp.getReactionBondCount() != 0) {
				continue;
			}
			
			// is there any internal bond in reactants or products?
			if (r_comp.getInternalBondCount() != 0) {
				continue;
			}
			
			// map the rest of rules which can not be mapped to bonds or changed state
			identifyMoleLevelReaction(thisRule, r_comp);
			
			previousRuleText = thisRule.getExpression();
		}
					
		m_networkViewer.build();
	}
	
	private Node makeComponentNode(Molecule tmole, Component tcomp, int moleIndex, int compIndex) 
	{
		Node n = m_componentGraph.addNode();
		// Set its name
	    n.setString(VisualItem.LABEL, " " + tcomp.getName() + " ");
	    // Set its parent
	    n.setString(COMP_PARENT_LABEL,tmole.getName()); 
	    // Set molecule expression
	    n.setString("molecule_expression", tmole.getExpression());
	    // Set its type
	    n.setString("type", "component");
	    // Set its ID
	    n.setString("ID", moleIndex+"."+compIndex);
	    // Add it to the hashtable
	    m_nodes.put(moleIndex+"."+compIndex , n);
	    
	   // System.out.println("Molecule: (" + tmole.getName() + "," + moleIndex + ")\t\tComponent: (" + tcomp.getName() + "," + compIndex +")");
	    
	    return n;
	}
	
	private Node makeStateNode(Molecule tmole, Component tcomp, State tstate,
			int moleIndex, int compIndex, int stateIndex, Node compNode) 
	{
		Node n_state = m_componentGraph.addNode();
		// set its name
		n_state.setString(VisualItem.LABEL, tstate.getName());
		// set its type
		n_state.setString("type", "state");
		// set its ID
		n_state.set("ID", moleIndex + "." + compIndex + "." + stateIndex);
		// set its parent molecule name
		n_state.setString(COMP_PARENT_LABEL, tmole.getName());
		// Set molecule expression
		n_state.setString("molecule_expression", tmole.getExpression());
		// set its parent component name
		n_state.setString(STATE_PARENT_LABEL, tcomp.getName());
		// set its parent component node
		n_state.set("stateparent", compNode);
		// add it to the hashtable
		m_nodes.put(moleIndex + "." + compIndex + "." + stateIndex, n_state);

		return n_state;
	}
	
	private void createEdgesForBonds() 
	{
		// Create an edge for each bond.
		for(int currentBondIndex = 0; currentBondIndex < m_model.getBonds().size(); currentBondIndex++)
		{
			// Declare two type of Edges, to show the CMAP in two mode: no states, with states
			Edge e_comp, e_state;
			
			// Get the bond
			Bond tbond = m_model.getBonds().get(currentBondIndex);

			// create left and right nodes of the bond edge
			// both left and right components have no state strict
			Node leftnode = m_nodes.get(tbond.getMolecule1()+"."+tbond.getComponent1());
			Node rightnode = m_nodes.get(tbond.getMolecule2()+"."+tbond.getComponent2());
			
			leftnode.set("hasedge", true);
			rightnode.set("hasedge", true);
			
			Node leftparentnode = null, rightparentnode = null;
			
			//TODO For multiple components with same name bug:  This is where we are creating the edges.
			// We use the bonds that are in the data structure so I am pretty sure that it is in the parser.
			
			// Create the edge linking component
			e_comp = m_componentGraph.addEdge(leftnode, rightnode);
			// Put the edge into the hashtable.
			m_edges.put(Integer.toString(currentBondIndex) + "." + "component", e_comp);
			// instantiate the ArrayList of Rule objects that will be stored with the edge.
			e_comp.set("rules", new ArrayList<VisualRule>());
			// show this edge in "show states" mode
			e_comp.set("displaymode", "both");
			e_comp.set("type", "componentVisible_edge");
			
			//System.out.println("Check " + tbond.getMolecule1() + "." + tbond.getComponent1());
			
			// If the state of the first component is not -1 (there is one)
			if (tbond.getState1() != -1) 
			{
				// Set the leftparentNode as the leftnode.  The leftnode is the component level.
				leftparentnode = leftnode; 
				
				// Set the the leftnode as the state level node.
				leftnode = m_nodes.get(tbond.getMolecule1()+"."+tbond.getComponent1()+"."+tbond.getState1());
				
				// If there is a state node, then set the node as having an edge.
				if (leftnode != null)
				{
					leftnode.set("hasedge", true);	
				}
				// Do nothing. 
				else
				{
					//System.out.println("\tleft node null: " + tbond.getMolecule1()+"."+tbond.getComponent1()+"."+tbond.getState1());
				}
			}
			else
			{
				//System.out.println("\tDid not consider " + tbond.getMolecule1() + "." + tbond.getComponent1() + "(no state)");
			}
	
			
			//System.out.println("Check " + tbond.getMolecule2() + "." + tbond.getComponent2());
			
			// if the right node has a state 
			if (tbond.getState2() != -1) 
			{
				// Set the parent node as the component level node.
				rightparentnode = rightnode; 
				
				// Set the rightnode as the state level.
				rightnode = m_nodes.get(tbond.getMolecule2()+"."+tbond.getComponent2()+"."+tbond.getState2()); // state node
				
				// If there is a state level, then set the node as having an edge.
				if (rightnode != null)
				{
					rightnode.set("hasedge", true);
				}
				// Do nothing if there is no state level.
				else
				{
				}
			}
			// Do nothing if there is no state associated with the node.
			else
			{
				//System.out.println("\tDid not consider " + tbond.getMolecule2() + "." + tbond.getComponent2() + "(no state)");
			}
			
			// If either of the parent nodes are not null, then there is a bond that 
			// requires a state.
			if (leftparentnode != null || rightparentnode != null) 
			{
				// Create the node between the left and right state nodes. 
				// If there was state information for the node, then that node
				// is a state level node.  
				e_state = m_componentGraph.addEdge(leftnode, rightnode);
				
				// add component information
				e_state.set("leftparent", leftparentnode);
				e_state.set("rightparent", rightparentnode);
					
				// Put the edge into the hashtable.
				m_edges.put(Integer.toString(currentBondIndex) + "." + "state", e_state);
				
				// instantiate the ArrayList of Rule objects that will be stored with the edge.
				e_state.set("rules", new ArrayList<VisualRule>());
				// show this edge in "show states" mode
				// and disable e_comp
				e_state.set("displaymode", "state");
				e_state.set("type", "stateVisible_edge");
				e_comp.set("displaymode", "component");
				
				// set invisible
				EdgeItem eitem = (EdgeItem) m_vis.getVisualItem(COMPONENT_GRAPH+".edges", e_state);
				eitem.setVisible(false);
			}				
		}
	}
	
	private void identifyBonds(Rule thisRule, VisualRule r_comp, VisualRule r_state)
	{
		//DEBUG
		System.out.println("Processing Rule: " + thisRule.getLabel());
		System.out.println("Bond Actions: " + thisRule.getBondactions().size());
		
		// For the bonds created or destroyed by the rule
		for(BondAction ba : thisRule.getBondactions())
		{
			String bondid = Integer.toString(ba.getBondIndex());
			
			//DEBUG
			System.out.println("Bond ID: " + bondid);
			
			// Get the edge for this bond.
			Edge e_state = m_edges.get(bondid + "." + "state");
			Edge e_comp = m_edges.get(bondid + "." + "component");

			if (e_state != null) 
			{
				// Add the bond to the rule as either created or
				// destroyed by the rule.
				if (ba.getAction() > 0)
					r_state.addAddBond(e_state);
				else
					r_state.addRemoveBond(e_state);

				// Add the Visual rule to the Edge.
				//DEBUG
				System.out.println("\tAdding Visual Rule: " + r_state.getLabel());
				ArrayList<VisualRule> e_state_rules =  (ArrayList<VisualRule>) e_state.get("rules");
				System.out.println("\t\tExisting Visual Rules: " + e_state_rules.size());
				e_state_rules.add(r_state);
				System.out.println("\t\tNow Visual Rules: " + ((ArrayList<VisualRule>) e_state.get("rules")).size());
			}
			else
			{
				//DEBUG
				System.out.println("\te_state was null");
			}
			
			if (e_comp != null) 
			{
				// Add the bond to the rule as either created or
				// destroyed by the rule.
				if (ba.getAction() > 0)
					r_comp.addAddBond(e_comp);
				else
					r_comp.addRemoveBond(e_comp);

				//DEBUG
				System.out.println("\tAdding Visual Rule: " + r_comp.getLabel());
				
				// Add the Visual rule to the Edge.
				ArrayList<VisualRule> e_comp_rules =  (ArrayList<VisualRule>) e_comp.get("rules");
				System.out.println("\t\tExisting Visual Rules: " + e_comp_rules.size());
				e_comp_rules.add(r_state);	
				System.out.println("\t\tNow Visual Rules: " + ((ArrayList<VisualRule>) e_comp.get("rules")).size());
			}
			else
			{
				//DEBUG
				System.out.println("\te_state was null");
			}
		}
	}
	
	private void identifyReactants(Rule thisRule, VisualRule r_comp, VisualRule r_state) {
		// For all of the reactant patterns
		for(RulePattern rp : thisRule.getReactantpatterns())
		{
			for(Integer i : rp.getBonds())
			{
				r_comp.addReactantBond(m_edges.get(i+"."+"component"));
				r_state.addReactantBond(m_edges.get(i+"."+"state"));
			}
			
			for(MoleculePattern mp : rp.getMolepatterns()) {
				
				// molecule level, no components
				if (mp.getComppatterns().size() == 0) {
					r_comp.addReactantMoleNode(m_nodes.get(""+mp.getMoleIndex()));
					r_state.addReactantMoleNode(m_nodes.get(""+mp.getMoleIndex()));
				}
				
				// component level
				for(ComponentPattern cp : mp.getComppatterns()) {
					if (cp.getStateindex() != -1) {
						// has certain state
						r_state.addReactantCompNode(m_nodes.get(mp.getMoleIndex()+"."+cp.getCompIndex()+"."+cp.getStateindex()));
					}							

					r_comp.addReactantCompNode(m_nodes.get(mp.getMoleIndex()+"."+cp.getCompIndex()));
					r_state.addReactantCompNode(m_nodes.get(mp.getMoleIndex()+"."+cp.getCompIndex()));
				}

			}
		}
	}
	
	private void identifyProducts(Rule thisRule, VisualRule r_comp, VisualRule r_state) {
		// For all of the product patterns.
		for(RulePattern pp : thisRule.getProductpatterns())
		{
			for(Integer i : pp.getBonds())
			{
				r_comp.addProductBond(m_edges.get(i+"."+"component"));
				r_state.addProductBond(m_edges.get(i+"."+"state"));
			}
			
			for(MoleculePattern mp : pp.getMolepatterns()) {
				// molecule level, no components
				if (mp.getComppatterns().size() == 0) {
					r_comp.addProductMoleNode(m_nodes.get(""+mp.getMoleIndex()));
					r_state.addProductMoleNode(m_nodes.get(""+mp.getMoleIndex()));
				}
				
				// component level
				for(ComponentPattern cp : mp.getComppatterns()) {
					if (cp.getStateindex() != -1) {
						// has certain state
						r_state.addProductCompNode(m_nodes.get(mp.getMoleIndex()+"."+cp.getCompIndex()+"."+cp.getStateindex()));
					}						
					
					r_comp.addProductCompNode(m_nodes.get(mp.getMoleIndex()+"."+cp.getCompIndex()));
					r_state.addProductCompNode(m_nodes.get(mp.getMoleIndex()+"."+cp.getCompIndex()));
				}
			}
		}
	}
	
	//TODO really bad efficiency
	private void identifyStateChange(Rule thisRule, VisualRule r_state) {
		for (RulePattern pp : thisRule.getProductpatterns()) 
		{
			for (MoleculePattern pmp : pp.getMolepatterns()) 
			{
				int pmoleId = pmp.getMoleIndex();
				
				for (RulePattern rp : thisRule.getReactantpatterns()) 
				{
					for (MoleculePattern rmp : rp.getMolepatterns()) 
					{
						
						int rmoleId = rmp.getMoleIndex();

						// same molecule
						if (rmoleId == pmoleId) 
						{
							for(ComponentPattern rcp : rmp.getComppatterns())  
							{
								int rcompId = rcp.getCompIndex();
								for(ComponentPattern pcp : pmp.getComppatterns())  
								{
									int pcompId = pcp.getCompIndex();
									
									// same component
									if (rcompId == pcompId) 
									{
										int rstateId = rcp.getStateindex();
										int pstateId = pcp.getStateindex();
										
										// different state
										if (rstateId != pstateId) 
										{
											Node pstateNode = m_nodes.get(pmp.getMoleIndex()+"."+pcp.getCompIndex()+"."+pcp.getStateindex());
											Node rstateNode = m_nodes.get(rmp.getMoleIndex()+"."+rcp.getCompIndex()+"."+rcp.getStateindex());
											
											// rule direction includes forward
											if (r_state.getExpression().indexOf(">") != -1) 	
											{
												if (pstateNode.get("rules") == null) 
												{
													pstateNode.set("rules", new ArrayList<VisualRule>());
												}
												// add VisualRule to state node
												ArrayList<VisualRule> tmplist = (ArrayList<VisualRule>) pstateNode.get("rules");
												if (!tmplist.contains(r_state)) 
												{
													tmplist.add(r_state);
												}
											}
											
											// rule direction includes backward
											if (r_state.getExpression().indexOf("<") != -1) 
											{
												if (rstateNode.get("rules") == null) 
												{
													rstateNode.set("rules", new ArrayList<VisualRule>());
												}
												// add VisualRule to state node
												ArrayList<VisualRule> tmplist = (ArrayList<VisualRule>) rstateNode.get("rules");
												if (!tmplist.contains(r_state)) 
												{
													tmplist.add(r_state);
												}
											}
											
											// add state changed nodes
											r_state.addChangedStateNode(pstateNode);
											r_state.addChangedStateNode(rstateNode);
											
											pstateNode.set("statechange", true);
											
											// set component "statechange" = true
											Node compNode = m_nodes.get(pmp.getMoleIndex()+"."+pcp.getCompIndex());
											compNode.set("statechange", true);
										}
									}
								}
							}
						}
					}
				}
			}					
		}
	}
	
	private void identifyMoleLevelReaction(Rule thisRule, VisualRule r_comp) 
	{
		// hub node
		Node n;
		// get the hub node if already exists
		Set<Node> moleSet = new HashSet<Node>();
		
		// go over all molecules
		ArrayList<ArrayList<RulePattern>> patterns = new ArrayList<ArrayList<RulePattern>>(
				2);
		patterns.add(thisRule.getReactantpatterns());
		patterns.add(thisRule.getProductpatterns());

		// create NodeSet for current rule
		// aims to reduce edges
		// for two different rules, if they have same NodeSet, there will be only one hub node
		for (ArrayList<RulePattern> rpl : patterns) {
			for (RulePattern rp : rpl) {
				for (MoleculePattern mp : rp.getMolepatterns()) {
					if (mp.getComppatterns().size() != 1) {
						// molecule-level node
						Node moleNode = m_nodes.get("" + mp.getMoleIndex());
						if (moleNode != null) {
							moleSet.add(moleNode);
						}
					}
					else {
						ComponentPattern cp = mp.getComppatterns().get(0);
						Node compNode = null, stateNode = null;
						// component node
						compNode = m_nodes.get("" + mp.getMoleIndex() + "." + cp.getCompIndex());
						if (cp.getStateindex() != -1) {
							// state node
							stateNode = m_nodes.get(mp.getMoleIndex()+"."+cp.getCompIndex()+"."+cp.getStateindex());
							if (stateNode != null) {
								moleSet.add(stateNode);
							}
						}
						else {
							if (compNode != null) {
								moleSet.add(compNode);
							}
						}
					}
				}
			}
		}
		
		// get the hub node from hash table if exists
		n = m_hubNodes.get(moleSet);

		// node does not exist in hash table, create a new one
		if (n == null) {

			// make connection node
			n = m_componentGraph.addNode();
			// set its name
			n.setString(VisualItem.LABEL, " ");
			// set its type
			n.setString("type", "hub");
			// set VisualRule list
			ArrayList<VisualRule> tmplist = new ArrayList<VisualRule>();
			tmplist.add(r_comp);
			n.set("rules", tmplist);
			m_hubNodes.put(moleSet, n);
		}
		// exists, add current rule to the hub node
		else {
			ArrayList<VisualRule> tmplist = (ArrayList<VisualRule>)n.get("rules");
			tmplist.add(r_comp);
		}

		// add edges
		for (ArrayList<RulePattern> rpl : patterns) {
			for (RulePattern rp : rpl) {
				for (MoleculePattern mp : rp.getMolepatterns()) {

					// molecule level, no components or more than one components
					if (mp.getComppatterns().size() != 1) {

						Node moleNode = m_nodes.get("" + mp.getMoleIndex());
						// set visible
						VisualItem nItem = m_vis.getVisualItem(COMPONENT_GRAPH, moleNode);
						
					    nItem.setVisible(true);
						if (moleNode != null) {
							// create edge
							Edge e = m_componentGraph.addEdge(n, moleNode);
							e.set("type", "moleConnection");
						}

					}
					else {
						
						ComponentPattern cp = mp.getComppatterns().get(0);
						Node compNode = null, stateNode = null;
						compNode = m_nodes.get("" + mp.getMoleIndex() + "." + cp.getCompIndex());
						boolean hasState = false;
						
						if (cp.getStateindex() != -1) {
							// has certain state
							stateNode = m_nodes.get(mp.getMoleIndex()+"."+cp.getCompIndex()+"."+cp.getStateindex());
							hasState = true;
						}						
						
						if (compNode != null) {
							// connect to component node
							Edge e_comp = m_componentGraph.addEdge(n, compNode);
							e_comp.set("type", "moleConnection");
							e_comp.set("displaymode", "component");
							
							// connect to state node
							if (hasState && stateNode != null) {
								Edge e_state = m_componentGraph.addEdge(n, stateNode);
								e_state.set("type", "moleConnection");
								e_state.set("displaymode", "state");
								stateNode.set("hasedge", true);
								
								// set invisible
								EdgeItem eitem = (EdgeItem) m_vis.getVisualItem(COMPONENT_GRAPH+".edges", e_state);
								eitem.setVisible(false);
								
							}
							else {
								e_comp.set("displaymode", "both");
							}
						}
					}
				}
			}
		}
	}
	
	private void initCompartmentAggregates(AggregateTable at_compartment,  ArrayList<AggregateItem> aggList) {
		   
        CompartmentTable cmptTable = m_model.getCompartments();
        ArrayList<Compartment> cmptList = (ArrayList<Compartment>)cmptTable.getCompartmentsList(true);
        
        // no compartment info
        if (cmptList == null || cmptList.size() == 0) {
        	return ;
        }
        
        // create aggregate items for compartments
        for (int i = 0; i < cmptList.size(); i++) {
        	AggregateItem agg = (AggregateItem) at_compartment.addItem();
        	agg.setString("type", "compartment");
        	agg.set("compartment", cmptList.get(i).getName());
        	agg.setVisible(false);
        	aggList.add(agg);
        }
	}
	
	/**
	 * Adds a visual item to a compartment aggregate.
	 * @param aggList
	 * @param node
	 * @param cmptStr
	 */
	private void addItemToCompartmentAgg(ArrayList<AggregateItem> aggList, Node node, String cmptStr) 
	{
		// Get the a table of information about the compartments in the model.
		CompartmentTable cmptTable = m_model.getCompartments();
		
		// Get a list of all of the compartments.
		ArrayList<Compartment> cmptList = (ArrayList<Compartment>) cmptTable.getCompartmentsList(false);
		
		// For each compartment (in reverse order).
		for (int i = cmptList.size() - 1; i >= 0; i--)
		{
			// Get the current aggregate (compartment).
			AggregateItem agg = aggList.get(i);
			// Get the name of the compartment.
			String cmptName = cmptList.get(i).getName();
			
			// current molecule belongs to current compartment directly OR indirectly
			
			// If the passed in compartment name is the same as the current name, or if
			// the passed in compartment is a child of the current compartment.
			if (cmptStr.equals(cmptName) || cmptTable.isChild(cmptStr, cmptName)) 
			{
				// Add the node to the aggregate. 
				//DEBUG
				System.out.println("ADDING: " + node.getString("molecule"));
				agg.addItem(m_vis.getVisualItem(COMPONENT_GRAPH, node));

			}
		}
	}

	public Display getDisplay()
	{
		return m_networkViewer.getDisplay();
	}
	
	public CMAPNetworkViewer getCMAPNetworkViewer() {
		return this.m_networkViewer;
	}
}


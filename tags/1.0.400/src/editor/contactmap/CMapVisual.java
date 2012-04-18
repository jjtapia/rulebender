package editor.contactmap;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Hashtable;

import networkviewer.FitOverviewListener;
import networkviewer.cmap.CMAPNetworkViewer;
import networkviewer.cmap.VisualRule;

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
import editor.contactmap.cdata.Bond;
import editor.contactmap.cdata.BondAction;
import editor.contactmap.cdata.Compartment;
import editor.contactmap.cdata.CompartmentTable;
import editor.contactmap.cdata.Component;
import editor.contactmap.cdata.ComponentPattern;
import editor.contactmap.cdata.Molecule;
import editor.contactmap.cdata.MoleculePattern;
import editor.contactmap.cdata.Rule;
import editor.contactmap.cdata.RulePattern;
import editor.contactmap.cdata.State;



/**
 * This class creates the data structure required for prefuse and 
 * interacts with the NetworkView class in order to show 
 * a visualization of the contact map data structure constructed 
 * by the parser. 
 * 
 * @author ams
 *
 */

public class CMapVisual
{
	
	// To use Prefuse, you first have to construct the data structures that it
	// uses for visualization e.g. the Graph object. This Graph object will 
	// hold the nodes and edges for our graph visualization.
	//
	Graph comp_graph;

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
	Visualization vis;
	
	// The NetworkViewer is an object I created to encapsulate most of 
	// prefuse nastiness.  You will interact with this mostly.  I plan
	// on making it much easier to use once I get back.
	// NetworkViewer is a subclass of prefuse.Display which is a 
	// Component and can be added to a JPanel.
	CMAPNetworkViewer nv;
	
	
	CMapModel model;
	
	Display mainDisplay, overviewDisplay;
	
	Dimension mainDisplaySize, overviewDisplaySize;
	
	
	public CMapVisual(CMapModel model_in, Dimension cMapSize,
			Dimension cMapOverviewSize) 
	{
		
		model = model_in;
		mainDisplaySize = cMapSize;
		overviewDisplaySize = cMapOverviewSize;
		
		// Instantiate the NetworkViewer object.
		nv = new CMAPNetworkViewer(mainDisplaySize); 
		
		// Instantiate the Graph
		/*
		 *  VisualItem.LABEL   [Integer]   |   COMP_PARENT_LABEL ('molecule') [String]   |   'rules'   [ArrayList<Rule>]  |
		 *  |   'states'     |
		 *
		 * 
		 */
		comp_graph = new Graph();
		
		// Ideally I wanted to remove all interaction with the Visualization object,
		// but I didn't quite finish it yet.  
		vis = nv.getVisualization();
		
		// Graphs (and all other data structures in prefuse) are table-based data structures.  Each node is a row in the
		// table and the columns hold data about the node.  Here we add a 
		// column for the label of the node, and then a column for the
		// molecule of which the node is a member.
        
		// The Label for a node.  Constant value int from VisualItem
		comp_graph.addColumn(VisualItem.LABEL, String.class);
		
        // Node type: component, state
        comp_graph.addColumn("type", String.class);
        
        // Node id: moleculeIndex.componentIndex<.stateIndex>
        comp_graph.addColumn("ID", String.class);
		
		// The parent (molecule) for a node.  String.
        comp_graph.addColumn(COMP_PARENT_LABEL, String.class);
        
        // The parent (component) for a state node.  String.
        comp_graph.addColumn(STATE_PARENT_LABEL, String.class);
        
        // The Rule object that can create an edge.  Arraylist of Rule objects.
        comp_graph.addColumn("rules", ArrayList.class);
        
        // The states that the component can be in.  ArrayList of Strings.
        comp_graph.addColumn("states", ArrayList.class);
        
        // State nodes
        comp_graph.addColumn("state_nodes", ArrayList.class);
        
        //store the parent component node for a state node
        comp_graph.addColumn("stateparent", Node.class);
        
        // Store the parent component nodes for sourcenode and targetnode of an edge
        // if they are state nodes
        comp_graph.addColumn("leftparent", Node.class);
        comp_graph.addColumn("rightparent", Node.class);
        
        // add a column to decide which edge to show in different display mode (show states)
        comp_graph.addColumn("displaymode", String.class);
        
        // add a column to check if the node has edge linked to it
        comp_graph.addColumn("hasedge", boolean.class);
        
        // add a column to indicate a component has state change
        comp_graph.addColumn("statechange", boolean.class);
        
        // Aggregate table for compartments
		AggregateTable at_compartment = nv.addAggregateTable("compartments");

		// Aggregate tables are created by adding an aggregator group to the
		// NetworkViewer. This is also a table data structure and will be
		// used to keep track of the shape of the molecules.
		AggregateTable at = nv.addAggregateTable(AGG);

		// Aggregate tables for components with states
		AggregateTable at_comp = nv.addAggregateTable(AGG_COMP);
		
		// Add the graph to the visualization
		// Pass the Graph object, and the string label for it.
        nv.addGraph(comp_graph, COMPONENT_GRAPH);
    	
        // Add the decorators to the visualization
        // Decorators are a way to add labels to objects.
        // Pass the string label for the decorators, and
        // the string value for the objects that they decorate.
		nv.addDecorators(AGG_DEC, AGG);
		
		// This sets the decorator objects as not interactive.
		vis.setInteractive(AGG_DEC, null, false);
		vis.setInteractive(COMPONENT_GRAPH+".edges", null, false);
		
        // Create the aggregate table and add it to the visualization
        //at = vis.addAggregates(AGG);
		
		at_compartment.addColumn(VisualItem.POLYGON, float[].class);
		// Molecule Compartment
		at_compartment.addColumn("compartment", String.class);
		
		// For collins
		at_compartment.addColumn("SURFACE", ArrayList.class);
		at_compartment.addColumn("aggregate_threshold", double.class);
		at_compartment.addColumn("aggreagate_negativeEdgeInfluenceFactor",
				double.class);
		at_compartment.addColumn("aggregate_nodeInfluenceFactor", double.class);
		at_compartment.addColumn("aggreagate_negativeNodeInfluenceFactor",
				double.class);
		at_compartment.addColumn("aggregate_edgeInfluenceFactor", double.class);
		
        // Add a column to the table to hold the polygon information
        // that will be used to render the aggregates.  This float array
		// will hold the points that create the molecule boundary.
        at.addColumn(VisualItem.POLYGON, float[].class);
        // Add a column that will keep track of the types of aggregates.
        at.addColumn(AGG_CAT_LABEL, String.class); 
        // Molecule Compartment
        at.addColumn("compartment", String.class);      
        
        // Add a column to the talbe to hold the polygon information
        at_comp.addColumn(VisualItem.POLYGON, float[].class);
        // Add a column that will keep track of the types of aggregates. 
        at_comp.addColumn(AGG_COMP_LABEL, String.class);
        
        // Set how the clicks will be handled.
        // Here you can create your own interactions for the Graph.
        // If you do not want interactions, simply do not add a
        // clickcontroldelegate.  See the editor.contactmap.CMapClickControlDelegate
        // for how to implement it.
        CMapClickControlDelegate cctrldelegate = new CMapClickControlDelegate(nv.getVisualization());
        nv.setClickControl(cctrldelegate);

        // This is an index to the Node objects so that I can retrieve them 
        // based on the string value "<parent molecule index>.<component index>".
        Hashtable<String, Node> nodes = new Hashtable<String, Node>();
        
        // This is so I can get an edge, based on an string value: bondID.type(component, state)
        Hashtable<String, Edge> edges = new Hashtable<String, Edge>();
        
        // Now begins the construction of the data structure.  I am not sure
        // if you will be able to use Yao's parser and get the same structure,
        // but if you can, then you will be able to use this code below to create 
        // the visualizaiton.
		Molecule tmole;
		Component tcomp;
		State tstate;
		Bond tbond;
		Node tnode = null; // temp variable for nodes without components
		// store first node of each compartments to control layout
		Hashtable<String, Node> compartmentsFirstNode = new Hashtable<String, Node>();
		
		
		// This is so I can add invisible edges between all of the components in the 
		// molecule.  This is for the force directed layout.
		ArrayList<Node> otherCompsInMol;
		
		// In general, we construct the graph by making a node for each component
		// and then group the components in the same molecule into an aggregator. 
		// There are visible edges representing bonds, and then invisible edges
		// between components in the same molecule. 
		
		// initialize compartment aggregates
		ArrayList<AggregateItem> cmptAggList = new ArrayList<AggregateItem>();;
		initCompartmentAggregates(at_compartment, cmptAggList);
		
		// For each molecule.
		for(int i=0; i<model.getMolecules().size(); i++)
		{
			// Get the molecule
			tmole = model.getMolecules().get(i);
			
			// If there are no components in the molecule, then the molecule
			// is rendered as a component.
			if(tmole.getComponents().size()==0)
			{
				Node n = comp_graph.addNode();
				n.setString(VisualItem.LABEL, "       ");
				n.setString("type", "component");
				// Set its ID
			    n.setString("ID", ""+i);
			    // Add it to the hashtable
			    nodes.put(""+i, n);
				
			    // Set its compartment
				String compartment = tmole.getFirstCompartment();
				if (compartment != null) {
					addItemToCompartmentAgg(cmptAggList, n, compartment);
				}
			    
				// add aggregates
				AggregateItem agg = (AggregateItem) at.addItem(); 
				agg.setString(AGG_CAT_LABEL, tmole.getName());
				agg.setString("compartment", compartment);
				agg.addItem(vis.getVisualItem(COMPONENT_GRAPH, n));
				
				
				// add invisible edge inside compartment to control layout
				if (compartment != null) {
					if (compartmentsFirstNode.containsKey(compartment)) {
						Node cfn = compartmentsFirstNode.get(compartment);

						// add invisible edge
						Edge te = comp_graph.addEdge(cfn, n);
						te.set("type", "compartment_edge");
						EdgeItem ei = (EdgeItem) vis.getVisualItem(
								COMPONENT_GRAPH + ".edges", te);
						ei.setVisible(false);
					}
					else {
						// this is the first node
						compartmentsFirstNode.put(compartment, n);
					}
				}
				//else {
					// link all nodes without components together to control layout
					if (tnode == null) {
						tnode = n;
					}
					else {
						Edge te = comp_graph.addEdge(tnode, n);
						te.set("type", "linkSingleNode_edge");
				    	EdgeItem ei = (EdgeItem) vis.getVisualItem(COMPONENT_GRAPH+".edges", te);
				    	ei.setVisible(false);
				    	tnode = n;	    	
					}
				//}
			}
			
			// It has components
			else
			{	
				// for making invisible edges between components in the same molecule.
				otherCompsInMol = new ArrayList<Node>();
				
				// Get the aggregate so you can add nodes to it.
				AggregateItem aggregateForMolecule = (AggregateItem) at.addItem();
				aggregateForMolecule.setString(AGG_CAT_LABEL, tmole.getName());
				// Set its compartment
				String compartment = tmole.getFirstCompartment();
				aggregateForMolecule.setString("compartment", compartment);
				
				// add another same aggregate in table at_compartment
				// Get the aggregate so you can add nodes to it.
				AggregateItem aggregateForMoleculeCompartment = (AggregateItem) at_compartment.addItem();
				aggregateForMoleculeCompartment.setString("compartment", tmole.getName());
				aggregateForMoleculeCompartment.setVisible(false);			
				
				// For each component
				for(int j=0;j<tmole.getComponents().size();j++)
				{	
					// Get the component
					tcomp = tmole.getComponents().get(j);
				
					// Make a new node for it
					Node n = comp_graph.addNode();
					// Set its name
				    n.setString(VisualItem.LABEL, " " + tcomp.getName() + " ");
				    // Set its parent
				    n.setString(COMP_PARENT_LABEL,tmole.getName()); 
				    // Set its type
				    n.setString("type", "component");
				    // Set its ID
				    n.setString("ID", i+"."+j);
				    // Add it to the hashtable
				    nodes.put(i+"."+j , n);
				    
				    // Add it to the compartment
				    if (compartment != null) {
						addItemToCompartmentAgg(cmptAggList, n, compartment);
					}
				    // Add it to the aggregate
				    aggregateForMolecule.addItem(vis.getVisualItem(COMPONENT_GRAPH,n));
				    aggregateForMoleculeCompartment.addItem(vis.getVisualItem(COMPONENT_GRAPH,n));
				    
				    // add invisible edge inside compartment to control layout
					if (compartment != null && j==0) {

						if (compartmentsFirstNode.containsKey(compartment)) {

							Node cfn = compartmentsFirstNode.get(compartment);

							// if the first node is a node without components,
							// then add a invisible edge to control layout
							if (!cfn.getString("ID").contains(".")) {
								// add invisible edge
								Edge te = comp_graph.addEdge(cfn, n);
								te.set("type", "compartment_edge");
								EdgeItem ei = (EdgeItem) vis.getVisualItem(
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
						aggregateForComponent.addItem(vis.getVisualItem(COMPONENT_GRAPH,n));
						
						for(int k = 0; k < tcomp.getStates().size(); k++)
						{
							tstate = tcomp.getStates().get(k);
							
							((ArrayList<String>) n.get("states")).add(tstate.getName());
							
							// make a new node for tstate
							Node n_state = comp_graph.addNode();
							// set its name
							n_state.setString(VisualItem.LABEL, tstate.getName());
							// set its type
							n_state.setString("type", "state");
							// set its ID
							n_state.set("ID", i+"."+j+"."+k);
							// set its parent molecule name
							n_state.setString(COMP_PARENT_LABEL, tmole.getName());
							// set its parent component name
							n_state.setString(STATE_PARENT_LABEL, tcomp.getName());
							// set its parent component node
							n_state.set("stateparent", n);
							// add it to the hashtable
							nodes.put(i+"."+j+"."+k, n_state);
							// Add it to the compartment
							if (compartment != null) {
								addItemToCompartmentAgg(cmptAggList, n_state, compartment);
							}
							// add it to the molecule
							aggregateForMolecule.addItem(vis.getVisualItem(COMPONENT_GRAPH,n_state));
							aggregateForMoleculeCompartment.addItem(vis.getVisualItem(COMPONENT_GRAPH,n_state));
							// add it to the component
							aggregateForComponent.addItem(vis.getVisualItem(COMPONENT_GRAPH,n_state));
							
							((ArrayList<Node>) n.get("state_nodes")).add(n_state);
							
							//set invisible
							NodeItem nitem = (NodeItem) vis.getVisualItem(COMPONENT_GRAPH+".nodes", n_state);
							nitem.setVisible(false);
							
							// add invisible edges connected to component
							Edge te = comp_graph.addEdge(n, n_state);
							te.set("type", "stateInvisible_edge");
					    	EdgeItem ei = (EdgeItem) vis.getVisualItem(COMPONENT_GRAPH+".edges", te);
								
					    	ei.setVisible(false);
						}  
					}  

				    // Add invisible edges for the force directed layout.
				    for(Node on : otherCompsInMol)
				    {
				    	Edge te = comp_graph.addEdge(n, on);
				    	te.set("type", "componentInvisible_edge");
				    	EdgeItem ei = (EdgeItem) vis.getVisualItem(COMPONENT_GRAPH+".edges", te);
							
				    	ei.setVisible(false);
				    }
				     
				    otherCompsInMol.add(n);
				}
			}
		} // Close for each molecule
		
		
		// Create an edge for each bond.
		for(int b = 0; b < model.getBonds().size(); b++)
		{
			
			// Declare two type of Edges, to show the CMAP in two mode: no states, with states
			Edge e_comp, e_state;
			
			// Get the bond
			tbond = model.getBonds().get(b);

			// create left and right nodes of the bond edge
			// both left and right components have no state strict
			Node leftnode = nodes.get(tbond.getMolecule1()+"."+tbond.getComponent1());
			Node rightnode = nodes.get(tbond.getMolecule2()+"."+tbond.getComponent2());
			
			leftnode.set("hasedge", true);
			rightnode.set("hasedge", true);
			
			Node leftparentnode = null, rightparentnode = null;
			
			// Create the edge linking component
			e_comp = comp_graph.addEdge(leftnode, rightnode);
			// Put the edge into the hashtable.
			edges.put(Integer.toString(b) + "." + "component", e_comp);
			// instantiate the ArrayList of Rule objects that will be stored with the edge.
			e_comp.set("rules", new ArrayList<VisualRule>());
			// show this edge in "show states" mode
			e_comp.set("displaymode", "both");
			e_comp.set("type", "componentVisible_edge");
			
			// left component has state requirement
			if (tbond.getState1() != -1) {
				leftparentnode = leftnode; // component node
				leftnode = nodes.get(tbond.getMolecule1()+"."+tbond.getComponent1()+"."+tbond.getState1()); // state node
				if (leftnode != null)
					leftnode.set("hasedge", true);
			}
			// right component has state requirement
			if (tbond.getState2() != -1) {
				rightparentnode = rightnode; // component node
				rightnode = nodes.get(tbond.getMolecule2()+"."+tbond.getComponent2()+"."+tbond.getState2()); // state node
				if (rightnode != null)
					rightnode.set("hasedge", true);
			}
			
			if (leftparentnode != null || rightparentnode != null) {
				// Create the edge
				e_state = comp_graph.addEdge(leftnode, rightnode);
				
				// add component information
				e_state.set("leftparent", leftparentnode);
				e_state.set("rightparent", rightparentnode);
					
				// Put the edge into the hashtable.
				edges.put(Integer.toString(b) + "." + "state", e_state);
				
				// instantiate the ArrayList of Rule objects that will be stored with the edge.
				e_state.set("rules", new ArrayList<VisualRule>());
				// show this edge in "show states" mode
				// and disable e_comp
				e_state.set("displaymode", "state");
				e_state.set("type", "stateVisible_edge");
				e_comp.set("displaymode", "component");
				
				// set invisible
				EdgeItem eitem = (EdgeItem) vis.getVisualItem(COMPONENT_GRAPH+".edges", e_state);
				eitem.setVisible(false);
			}				
		}
		
		
		// For all of the rules
		for(int l = 0; l<model.getRules().size();l++)
		{	
			// Get a reference to the rule
			Rule thisRule = model.getRules().get(l);
			
			// Create a visual rule.  Used for interaction.  (See editor.contactmap.CMapClickControlDelegate.java)
			VisualRule r_comp = new VisualRule(model.getRules().get(l).getName());
			VisualRule r_state = new VisualRule(model.getRules().get(l).getName());
			
			// For the bonds created or destroyed by the rule
			for(BondAction ba : thisRule.getBondactions())
			{
				String bondid = Integer.toString(ba.getBondindex());
				// Get the edge for this bond.
				Edge e_state = edges.get(bondid + "." + "state");
				Edge e_comp = edges.get(bondid + "." + "component");

				if (e_state != null) {

					// Add the bond to the rule as either created or
					// destroyed by the rule.
					if (ba.getAction() > 0)
						r_state.addAddBond(e_state);
					else
						r_state.addRemoveBond(e_state);

					// Add the Visual rule to the Edge.
					((ArrayList<VisualRule>) e_state.get("rules")).add(r_state);
				}
				
				if (e_comp != null) {
					// Add the bond to the rule as either created or
					// destroyed by the rule.
					if (ba.getAction() > 0)
						r_comp.addAddBond(e_comp);
					else
						r_comp.addRemoveBond(e_comp);

					// Add the Visual rule to the Edge.
					((ArrayList<VisualRule>) e_comp.get("rules")).add(r_comp);
				}
				
				
			}
			
			// For all of the reactant patterns
			for(RulePattern rp : thisRule.getReactantpatterns())
			{
				for(Integer i : rp.getBonds())
				{
					r_comp.addReactantBond(edges.get(i+"."+"component"));
					r_state.addReactantBond(edges.get(i+"."+"state"));
				}
				
				for(MoleculePattern mp : rp.getMolepatterns())
					for(ComponentPattern cp : mp.getComppatterns()) {
						if (cp.getStateindex() != -1) {
							// certain state
							r_state.addReactantNode(nodes.get(mp.getMoleindex()+"."+cp.getCompindex()+"."+cp.getStateindex()));
						}							

						r_comp.addReactantNode(nodes.get(mp.getMoleindex()+"."+cp.getCompindex()));
						r_state.addReactantNode(nodes.get(mp.getMoleindex()+"."+cp.getCompindex()));
					}
			}
			
			// For all of the product patterns.
			for(RulePattern pp : thisRule.getProductpatterns())
			{
				for(Integer i : pp.getBonds())
				{
					r_comp.addProductBond(edges.get(i+"."+"component"));
					r_state.addProductBond(edges.get(i+"."+"state"));
				}
				
				for(MoleculePattern mp : pp.getMolepatterns())
					for(ComponentPattern cp : mp.getComppatterns()) {
						if (cp.getStateindex() != -1) {
							// certain state
							r_state.addProductNode(nodes.get(mp.getMoleindex()+"."+cp.getCompindex()+"."+cp.getStateindex()));
						}						
						
						r_comp.addProductNode(nodes.get(mp.getMoleindex()+"."+cp.getCompindex()));
						r_state.addProductNode(nodes.get(mp.getMoleindex()+"."+cp.getCompindex()));
					}
			}
			
			//TODO really bad efficiency
			for(RulePattern rp : thisRule.getReactantpatterns()) {
				for(MoleculePattern rmp : rp.getMolepatterns()) {
					int rmoleId = rmp.getMoleindex();
					for(RulePattern pp : thisRule.getProductpatterns()) {
						for(MoleculePattern pmp : pp.getMolepatterns()) {
							int pmoleId = pmp.getMoleindex();
							
							// same molecule
							if (rmoleId == pmoleId) {
								for(ComponentPattern rcp : rmp.getComppatterns())  {
									int rcompId = rcp.getCompindex();
									for(ComponentPattern pcp : pmp.getComppatterns())  {
										int pcompId = pcp.getCompindex();
										
										// same component
										if (rcompId == pcompId) {
											int rstateId = rcp.getStateindex();
											int pstateId = pcp.getStateindex();
											
											// different state
											if (rstateId != pstateId) {
												Node pstateNode = nodes.get(pmp.getMoleindex()+"."+pcp.getCompindex()+"."+pcp.getStateindex());
												if (pstateNode.get("rules") == null) {
													pstateNode.set("rules", new ArrayList<VisualRule>());
												}
												// add VisualRule to state node
												ArrayList<VisualRule> tmplist = (ArrayList<VisualRule>) pstateNode.get("rules");
												if (!tmplist.contains(r_state)) {
													tmplist.add(r_state);
												}
												
												// add state changed nodes
												r_state.addChangedStateNode(pstateNode);
												Node rstateNode = nodes.get(rmp.getMoleindex()+"."+rcp.getCompindex()+"."+rcp.getStateindex());
												r_state.addChangedStateNode(rstateNode);
												
												pstateNode.set("statechange", true);
												
												// set component "statechange" = true
												Node compNode = nodes.get(pmp.getMoleindex()+"."+pcp.getCompindex());
												compNode.set("statechange", true);
												
												// DEBUG
												System.out.println("state change: " + thisRule.getName());
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
					
			//TODO  I'm not sure what this is for, but I'm going to ignore it for now.
			//if(tbond.CanGenerate == true)
			//{
				/*if(tbond.state1 != -1)
				{
					outfile.print("struct"+model.getMolecules().get(tbond.molecule1).name+"_"+model.getMolecules().get(tbond.molecule1).components.get(tbond.component1).name+tbond.component1+":"+model.getMolecules().get(tbond.molecule1).components.get(tbond.component1).states.get(tbond.state1).name+"->");
				}
				else
				{
					outfile.print("struct"+model.getMolecules().get(tbond.molecule1).name+"_"+model.getMolecules().get(tbond.molecule1).components.get(tbond.component1).name+tbond.component1+"->");
				}
				if(tbond.state2 != -1)
				{
					outfile.println("struct"+model.getMolecules().get(tbond.molecule2).name+"_"+model.getMolecules().get(tbond.molecule2).components.get(tbond.component2).name+tbond.component2+":"+model.getMolecules().get(tbond.molecule2).components.get(tbond.component2).states.get(tbond.state2).name+"[arrowhead=none];");
				}
				else
				{
					outfile.println("struct"+model.getMolecules().get(tbond.molecule2).name+"_"+model.getMolecules().get(tbond.molecule2).components.get(tbond.component2).name+tbond.component2+"[arrowhead=none];");
				}*/
			
			// Add an edge between the nodes in the bond.
			
		//	comp_graph.addEdge(arg0, arg1)
			
		/*	}
			else
			{
				if(tbond.state1 != -1)
				{
					outfile.print("struct"+model.getMolecules().get(tbond.molecule1).name+"_"+model.getMolecules().get(tbond.molecule1).components.get(tbond.component1).name+tbond.component1+":"+model.getMolecules().get(tbond.molecule1).components.get(tbond.component1).states.get(tbond.state1).name+"->");
				}
				else
				{
					outfile.print("struct"+molecules.get(tbond.molecule1).name+"_"+molecules.get(tbond.molecule1).components.get(tbond.component1).name+tbond.component1+"->");
				}
				if(tbond.state2 != -1)
				{
					outfile.println("struct"+molecules.get(tbond.molecule2).name+"_"+molecules.get(tbond.molecule2).components.get(tbond.component2).name+tbond.component2+":"+molecules.get(tbond.molecule2).components.get(tbond.component2).states.get(tbond.state2).name+"[arrowhead=none,color=grey];");
				}
				else
				{
					outfile.println("struct"+molecules.get(tbond.molecule2).name+"_"+molecules.get(tbond.molecule2).components.get(tbond.component2).name+tbond.component2+"[arrowhead=none,color=grey];");
				}
			}*/
				
		
		
		
		nv.build();
		setUpOverviewDisplay();
		
		}  // Close for rules.		}	

	
	
	private void initCompartmentAggregates(AggregateTable at_compartment,  ArrayList<AggregateItem> aggList) {
		   
        CompartmentTable cmptTable = model.getCompartments();
        ArrayList<Compartment> cmptList = (ArrayList<Compartment>)cmptTable.getCompartmentsList(true);
        
        // no compartment info
        if (cmptList == null || cmptList.size() == 0) {
        	return ;
        }
        
        // create aggregate items for compartments
        for (int i = 0; i < cmptList.size(); i++) {
        	AggregateItem agg = (AggregateItem) at_compartment.addItem();
        	agg.set("compartment", cmptList.get(i).getName());
        	agg.setVisible(false);
        	aggList.add(agg);
        }
	}
	
	private void addItemToCompartmentAgg(ArrayList<AggregateItem> aggList, Node node, String cmptStr) {
		CompartmentTable cmptTable = model.getCompartments();
		ArrayList<Compartment> cmptList = (ArrayList<Compartment>)cmptTable.getCompartmentsList(false);
		for (int i = cmptList.size() - 1; i >= 0; i--) {
			AggregateItem agg = aggList.get(i);
			String cmptName = cmptList.get(i).getName();

			// current molecule belongs to current compartment directly
			// OR indirectly
			if (cmptStr.equals(cmptName) || cmptTable.isChild(cmptStr, cmptName)) {
//			if (cmptStr.equals(cmptName)) {
				agg.addItem(vis.getVisualItem(COMPONENT_GRAPH, node));
			}
		}
	}

	public Display getDisplay()
	{
		return nv.getDisplay();
	}
	
	public void setUpOverviewDisplay()
	{
		// Make a new display out of the same visualization 
		// that is used for the cmap.
		overviewDisplay = new Display(vis);
		
		// Set it to high quality.
		overviewDisplay.setHighQuality(true);
		
		// Set the size to match the available JPanel size 
		// that is passed in as a Dimension.
		overviewDisplay.setSize(overviewDisplaySize);
		
		// Add a listener that will zoom out/in to fit the items 
		// onto the screen. 
		overviewDisplay.addItemBoundsListener(new FitOverviewListener());
	}
	
	public Display getOverviewDisplay()
	{	
		return overviewDisplay;
	}	
}


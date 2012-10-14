package resultviewer.graph;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Hashtable;

import networkviewer.FitOverviewListener;
import networkviewer.cmap.SpeciesViewer;

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

import resultviewer.data.SBond;
import resultviewer.data.SComponent;
import resultviewer.data.SMolecule;
import resultviewer.data.Species;

public class SpeciesGraph {
	// To use Prefuse, you first have to construct the data structures that it
	// uses for visualization e.g. the Graph object. This Graph object will
	// hold the nodes and edges for our graph visualization.
	//
	Graph comp_graph;

	// Prefuse uses string identifiers for data structures and
	// their data fields. These Strings variables are all used
	// as identifiers.

	// Each node stores a string label for the molecule it belongs to.
	String COMP_PARENT_LABEL = "molecule";
	// Each state node stores a string label for the component it belongs to.
	String STATE_PARENT_LABEL = "component";
	// This is the identifier for our primary Graph object
	String COMPONENT_GRAPH = "component_graph";
	// This is a label used with Aggregate objects. They are used for visually
	// grouping nodes.
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

	SpeciesViewer nv;

	Species species;

	private Dimension overviewDimension;

	public SpeciesGraph(int id, String speciesExp, Dimension mainDimension,
			Dimension overviewDimension) {
		// parse the species expression
		this.parseSpecies(id, speciesExp);

		this.overviewDimension = overviewDimension;

		// Instantiate the NetworkViewer object.
		nv = new SpeciesViewer(mainDimension);

		// create a graph
		this.createGraph();

		nv.build();
	}

	/**
	 * Parse a string into species data
	 * 
	 * @param id
	 *            species id
	 * @param speciesExp
	 *            string of species expression
	 */
	private void parseSpecies(int id, String speciesExp) {

		// System.out.println("Parsing species: " + speciesExp);
		// create species
		species = new Species(id, speciesExp);

		// create molecules

		// if there are more than one molecule
		String[] molecules = speciesExp.trim().split("\\.");

		if (molecules.length == 0) {
			// if there's only one molecule
			molecules = new String[1];
			molecules[0] = speciesExp.trim();
		}

		for (int i = 0; i < molecules.length; i++) {
			String curMolecule = molecules[i];

			// get the name of the molecule
			String moleculeName = curMolecule.substring(0,
					curMolecule.indexOf("("));

			// create a molecule using the name
			SMolecule mole = new SMolecule(i + 1, moleculeName);

			// parse the components
			String componentExp = curMolecule.substring(
					curMolecule.indexOf("(") + 1, curMolecule.lastIndexOf(")"));
			String[] components = componentExp.trim().split("\\,");

			for (int j = 0; j < components.length; j++) {
				String curComponent = components[j];
				String componentWithoutBond = curComponent;
				// get the component without bond
				if (componentWithoutBond.indexOf("!") != -1) {
					componentWithoutBond = componentWithoutBond.substring(0,
							componentWithoutBond.indexOf("!"));
				}

				// get the name of the components
				String componentName = componentWithoutBond;
				if (componentWithoutBond.indexOf("~") != -1) {
					componentName = componentWithoutBond.substring(0,
							componentWithoutBond.indexOf("~"));
				}

				// create a component
				SComponent comp = new SComponent(j + 1, componentName);
				if (componentWithoutBond.indexOf("~") != -1) {

					// add states
					String stateslist = componentWithoutBond
							.substring(componentWithoutBond.indexOf("~") + 1);
					String[] states = stateslist.trim().split("\\~");
					for (int k = 0; k < states.length; k++) {
						String state = states[k];
						if (state.indexOf("!") != -1) {
							state = state.substring(0, state.indexOf("!"));
						}
						comp.addState(state);
					}
				} else {
					// no states
					comp.setStates(null);
				}

				// add component 'c' to molecule 'm'
				mole.addComponent(comp);

				// add bonds
				for (int t = 0; t < curComponent.length(); t++) {
					if (curComponent.charAt(t) == '!') {
						t++;

						// get bond id string
						String bondIdStr = "";
						while (Character.isDigit(curComponent.charAt(t))) {
							bondIdStr += Character.toString(curComponent
									.charAt(t));
							t++;
							if (t >= curComponent.length())
								break;
						}

						t--;

						// pattern
						if (bondIdStr.equals("")) {
							continue;
						}

						// get the integer of bond id
						int bondId = Integer.parseInt(bondIdStr);

						// get state name if there is
						String substr = curComponent.substring(0, t);
						int curExclamation = substr.lastIndexOf("!");
						int curTilde = substr.lastIndexOf("~");

						if (species.getBondById(bondId) != null) {
							// if the bond has already has the left part,
							// set the right part
							SBond bond = species.getBondById(bondId);

							// has state
							if (curTilde != -1 && curTilde + 1 < curExclamation) {
								String state = curComponent.substring(
										curTilde + 1, curExclamation);

								// add right part of bond
								bond.setRightPart(mole.getId(), comp.getId(),
										comp.getStateId(state));
							}
							// no state
							else {
								// add right part of bond
								bond.setRightPart(mole.getId(), comp.getId(),
										-1);
							}
						} else {
							// if the bond does not exist,
							// create a bond and set the left part
							SBond bond;

							// has state
							if (curTilde != -1 && curTilde + 1 < curExclamation) {
								String state = curComponent.substring(
										curTilde + 1, curExclamation);

								bond = new SBond(bondId, mole.getId(),
										comp.getId(), comp.getStateId(state));
							}
							// no state
							else {
								bond = new SBond(bondId, mole.getId(),
										comp.getId(), -1);
							}

							species.addBond(bond);
						}
					}
				}
			}
			// add molecule 'm' to species
			species.addMolecule(mole);
		}

	}

	/**
	 * Create graph for species after parsing the speciesExp.
	 */
	private void createGraph() {
		ArrayList<SMolecule> molecules = species.getMolecules();
		ArrayList<SBond> bonds = species.getBonds();

		// Instantiate the Graph
		comp_graph = new Graph();

		vis = nv.getVisualization();

		// Graphs (and all other data structures in prefuse) are table-based
		// data structures. Each node is a row in the
		// table and the columns hold data about the node. Here we add a
		// column for the label of the node, and then a column for the
		// molecule of which the node is a member.

		// The Label for a node
		comp_graph.addColumn(VisualItem.LABEL, String.class);

		// Node type: component, state
		comp_graph.addColumn("type", String.class);

		// Node id: moleculeIndex.componentIndex<.stateIndex>
		comp_graph.addColumn("ID", String.class);

		// The parent (molecule) for a node
		comp_graph.addColumn(COMP_PARENT_LABEL, String.class);

		// The parent (component) for a state node. String.
		comp_graph.addColumn(STATE_PARENT_LABEL, String.class);

		// The states that the component can be in. ArrayList of Strings.
		comp_graph.addColumn("states", ArrayList.class);

		// State nodes
		comp_graph.addColumn("state_nodes", ArrayList.class);

		// store the parent component node for a state node
		comp_graph.addColumn("stateparent", Node.class);

		// Store the parent component nodes for sourcenode and targetnode of an
		// edge
		// if they are state nodes
		comp_graph.addColumn("leftparent", Node.class);
		comp_graph.addColumn("rightparent", Node.class);

		// add a column to decide which edge to show in different display mode
		// (show states)
		comp_graph.addColumn("displaymode", String.class);

		// add a column to check if the node has edge linked to it
		comp_graph.addColumn("hasedge", boolean.class);

		// add a column to indicate a component has state change
		comp_graph.addColumn("statechange", boolean.class);

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

		// Create the aggregate table and add it to the visualization
		// at = vis.addAggregates(AGG);

		// Add a column to the table to hold the polygon information
		// that will be used to render the aggregates. This float array
		// will hold the points that create the molecule boundary.
		at.addColumn(VisualItem.POLYGON, float[].class);

		// Add a column that will keep track of the types of aggregates.
		at.addColumn(AGG_CAT_LABEL, String.class);

		// Add a column to the talbe to hold the polygon information
		at_comp.addColumn(VisualItem.POLYGON, float[].class);

		// Add a column that will keep track of the types of aggregates.
		at_comp.addColumn(AGG_COMP_LABEL, String.class);

		nv.setClickControl(new SpeciesGraphClickControlDelegate(nv
				.getVisualization()));

		// Delegation is also used for tooltips, but I do not want them
		// and so I commented them out.
		// Set how the tooltips will work
		// nv.setTooltipDelegate(new CMapHoverTooltipDelegate());

		// This is so I can get the aggregate for a specific molecule based on
		// the string
		// value of its index in the molecules arraylist
		Hashtable<String, AggregateItem> aggregates = new Hashtable<String, AggregateItem>();

		// This is an index to the Node objects so that I can retrieve them
		// based on the string value
		// "<parent molecule index>.<component index>".
		Hashtable<String, Node> nodes = new Hashtable<String, Node>();

		// This is so I can get an edge, based on an string value:
		// bondID.type(component, state)
		Hashtable<String, Edge> edges = new Hashtable<String, Edge>();

		// Now begins the construction of the data structure. I am not sure
		// if you will be able to use Yao's parser and get the same structure,
		// but if you can, then you will be able to use this code below to
		// create
		// the visualizaiton.
		SMolecule tmole;
		SComponent tcomp;
		SBond tbond;

		// This is so I can add invisible edges between all of the components in
		// the
		// molecule. This is for the force directed layout.
		ArrayList<Node> otherCompsInMol;

		// In general, we construct the graph by making a node for each
		// component
		// and then group the components in the same molecule into an
		// aggregator.
		// There are visible edges representing bonds, and then invisible edges
		// between components in the same molecule.

		/*
		 * 
		 * // add the expression of the species Node exp = comp_graph.addNode();
		 * if (species.getId() != -1) { // species
		 * exp.setString(VisualItem.LABEL, Integer.toString(species.getId())+
		 * " " + species.getExpression()); } else { // not a species, just
		 * expression exp.setString(VisualItem.LABEL, species.getExpression());
		 * }
		 */

		for (int i = 0; i < molecules.size(); i++) {
			// Get the molecule
			tmole = molecules.get(i);

			// If there are no components in the molecule, then the molecule
			// is rendered as a component.
			if (tmole.getComponents().size() == 0) {
				Node n = comp_graph.addNode();
				n.setString(VisualItem.LABEL, "       ");
				n.setString("type", "component");
				AggregateItem agg = (AggregateItem) at.addItem();
				agg.setString(AGG_CAT_LABEL, tmole.getName());
				agg.addItem(vis.getVisualItem(COMPONENT_GRAPH, n));
			}

			// It has components
			else {
				otherCompsInMol = new ArrayList<Node>();

				// Try to get the aggregate item
				AggregateItem aggregateForMolecule = aggregates.get(tmole
						.getId() + "");

				// This won't happen more than once.
				// If it is null, then create a new one and add it to the
				// aggregate table.
				// if (aggregateForMolecule == null)
				// {
				aggregateForMolecule = (AggregateItem) at.addItem();
				aggregateForMolecule.setString(AGG_CAT_LABEL, tmole.getName());
				// }

				// For each component
				for (int j = 0; j < tmole.getComponents().size(); j++) {

					// Get the component
					tcomp = tmole.getComponents().get(j);

					// Make a new node for it
					Node n = comp_graph.addNode();
					// Set its name
					n.setString(VisualItem.LABEL, " " + tcomp.getName() + " ");
					// Set its parent
					n.setString(COMP_PARENT_LABEL, tmole.getName());
					// Set its type
					n.setString("type", "component");
					// Set its ID
					n.setString("ID", tmole.getId() + "." + tcomp.getId());
					// Add it to the hashtable
					nodes.put(tmole.getId() + "." + tcomp.getId(), n);
					// Add it to the aggregate
					aggregateForMolecule.addItem(vis.getVisualItem(
							COMPONENT_GRAPH, n));

					// If it has states
					if (tcomp.getStates() != null
							&& tcomp.getStates().size() != 0) {
						n.set("states", new ArrayList<String>());
						n.set("state_nodes", new ArrayList<Node>());

						// set aggregates for component with states
						AggregateItem aggregateForComponent = (AggregateItem) at_comp
								.addItem();
						aggregateForComponent.setString(AGG_COMP_LABEL,
								tmole.getName() + "." + tcomp.getName());
						aggregateForComponent.addItem(vis.getVisualItem(
								COMPONENT_GRAPH, n));

						for (int k = 0; k < tcomp.getStates().size(); k++) {
							String tstate = tcomp.getStates().get(k);

							((ArrayList<String>) n.get("states")).add(tstate);

							// make a new node for tstate
							Node n_state = comp_graph.addNode();
							// set its name
							n_state.setString(VisualItem.LABEL, tstate);
							// set its type
							n_state.setString("type", "state");
							// set its ID
							n_state.set("ID",
									tmole.getId() + "." + tcomp.getId() + "."
											+ k);
							// set its parent molecule name
							n_state.setString(COMP_PARENT_LABEL,
									tmole.getName());
							// set its parent component name
							n_state.setString(STATE_PARENT_LABEL,
									tcomp.getName());
							// set its parent component node
							n_state.set("stateparent", n);
							// add it to the hashtable
							nodes.put(tmole.getId() + "." + tcomp.getId() + "."
									+ k, n_state);
							// add it to the aggregate
							aggregateForMolecule.addItem(vis.getVisualItem(
									COMPONENT_GRAPH, n_state));
							aggregateForComponent.addItem(vis.getVisualItem(
									COMPONENT_GRAPH, n_state));

							((ArrayList<Node>) n.get("state_nodes"))
									.add(n_state);

							// set invisible
							NodeItem nitem = (NodeItem) vis.getVisualItem(
									COMPONENT_GRAPH + ".nodes", n_state);
							nitem.setVisible(true);

							// add invisible edges connected to component
							Edge te = comp_graph.addEdge(n, n_state);
							EdgeItem ei = (EdgeItem) vis.getVisualItem(
									COMPONENT_GRAPH + ".edges", te);

							ei.setVisible(false);
						}
					}

					for (Node on : otherCompsInMol) {
						Edge te = comp_graph.addEdge(n, on);
						EdgeItem ei = (EdgeItem) vis.getVisualItem(
								COMPONENT_GRAPH + ".edges", te);

						ei.setVisible(false);
					}

					otherCompsInMol.add(n);
				}
			}
		} // Close for each molecule

		// For all of the bonds

		for (int l = 0; l < bonds.size(); l++) {
			tbond = bonds.get(l);
			Edge e_comp, e_state;

			// create left and right nodes of the bond edge
			// both left and right components have no state strict
			Node leftnode = nodes
					.get(tbond.getLeftM() + "." + tbond.getLeftC());
			Node rightnode = nodes.get(tbond.getRightM() + "."
					+ tbond.getRightC());

			if (rightnode == null) {
				continue;
			}

			leftnode.set("hasedge", true);
			rightnode.set("hasedge", true);

			Node leftparentnode = null, rightparentnode = null;

			// Create the edge linking component
			e_comp = comp_graph.addEdge(leftnode, rightnode);
			// Put the edge into the hashtable.
			edges.put(tbond.getId() + ".component", e_comp);
			// show this edge in "show states" mode
			e_comp.set("displaymode", "both");

			// left component has state requirement
			if (tbond.getLeftS() != -1) {
				leftparentnode = leftnode; // component node
				leftnode = nodes.get(tbond.getLeftM() + "." + tbond.getLeftC()
						+ "." + tbond.getLeftS()); // state node
				if (leftnode != null)
					leftnode.set("hasedge", true);
			}
			// right component has state requirement
			if (tbond.getRightS() != -1) {
				rightparentnode = rightnode; // component node
				rightnode = nodes.get(tbond.getRightM() + "."
						+ tbond.getRightC() + "." + tbond.getRightS()); // state
																		// node
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
				edges.put(tbond.getId() + ".state", e_state);

				// show this edge in "show states" mode
				// and disable e_comp
				e_state.set("displaymode", "state");
				e_comp.set("displaymode", "component");

				// set invisible
				EdgeItem eitem = (EdgeItem) vis.getVisualItem(COMPONENT_GRAPH
						+ ".edges", e_comp);
				eitem.setVisible(false);
			}
		}

	}

	/**
	 * 
	 * @return prefuse.Display object
	 */
	public Display getDisplay() {
		return nv.getDisplay();
	}

	/**
	 * 
	 * @return prefuse.Display object for overview
	 */
	public Display getOverviewDisplay() {
		Display overviewDisplay = new Display(nv.getVisualization());
		overviewDisplay.setHighQuality(true);
		overviewDisplay.setSize(overviewDimension);
		overviewDisplay.addItemBoundsListener(new FitOverviewListener());
		return overviewDisplay;
	}
}

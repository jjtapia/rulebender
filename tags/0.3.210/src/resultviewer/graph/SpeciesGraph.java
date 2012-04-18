package resultviewer.graph;

import java.util.ArrayList;
import java.util.Hashtable;

import networkviewer.NetworkViewer;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

import resultviewer.data.Bond;
import resultviewer.data.Component;
import resultviewer.data.Molecule;
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

	// The visualization object is a primary data structure for prefuse.
	// We will give our Graph object to it, and also use it to access
	// and change properties of the visualization.
	Visualization vis;

	// The NetworkViewer is an object I created to encapsulate most of
	// prefuse nastiness. You will interact with this mostly. I plan
	// on making it much easier to use once I get back.
	// NetworkViewer is a subclass of prefuse.Display which is a
	// Component and can be added to a JPanel.
	NetworkViewer nv;

	Species species;

	public SpeciesGraph(int id, String speciesExp) {
		// parse the species expression
		this.parseSpecies(id, speciesExp);
		
		// Instantiate the NetworkViewer object.
		nv = new NetworkViewer();

		// create a graph
		this.createGraph();
	}

	/*
	 * Parse a string into species data
	 */
	private void parseSpecies(int id, String speciesExp) {

		System.out.println("Parsing species: " + speciesExp);
		// create species
		species = new Species(id, speciesExp);

		/* create molecules */

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
			String moleculeName = curMolecule.substring(0, curMolecule
					.indexOf("("));

			// create a molecule using the name
			Molecule mole = new Molecule(i + 1, moleculeName);

			// parse the components
			String componentExp = curMolecule.substring(curMolecule
					.indexOf("(") + 1, curMolecule.lastIndexOf(")"));
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
				Component comp = new Component(j + 1, componentName);
				if (componentWithoutBond.indexOf("~") != -1) {

					// add states
					String[] states = componentWithoutBond.substring(
							componentWithoutBond.indexOf("~")).trim().split(
							"\\~");
					for (int k = 0; k < states.length; k++) {
						comp.addState(states[k]);
					}
				} else {
					// no states
					comp.setStates(null);
				}

				// add component 'c' to molecule 'm'
				mole.addComponent(comp);

				/* add bonds */
				for (int t = 0; t < curComponent.length(); t++) {
					if (curComponent.charAt(t) == '!') {
						t++;
						String bondIdStr = "";
						while (Character.isDigit(curComponent.charAt(t))) {
							bondIdStr += Character.toString(curComponent
									.charAt(t));
							t++;
							if (t >= curComponent.length())
								break;
						}
						
						//pattern
						if (bondIdStr.equals("")) {
							continue;
						}
						// get the integer of bond id
						int bondId = Integer.parseInt(bondIdStr);
						if (species.getBondById(bondId) != null) {
							// if the bond has already has the left part,
							// set the right part
							Bond bond = species.getBondById(bondId);
							bond.setRightPart(mole.getId(), comp.getId());
						} else {
							// if the bond does not exist,
							// create a bond and set the left part
							Bond bond = new Bond(bondId, mole.getId(), comp
									.getId());
							species.addBond(bond);
						}
					}
				}
			}
			// add molecule 'm' to species
			species.addMolecule(mole);
		}

	}

	/*
	 * Create graph for species after parsing the speciesExp
	 */
	private void createGraph() {
		ArrayList<Molecule> molecules = species.getMolecules();
		ArrayList<Bond> bonds = species.getBonds();

		// Instantiate the Graph
		comp_graph = new Graph();

		// Ideally I wanted to remove all interaction with the Visualization
		// object,
		// but I didn't quite finish it yet.
		vis = nv.getVisualization();
		

		// Graphs (and all other data structures in prefuse) are table-based
		// data structures. Each node is a row in the
		// table and the columns hold data about the node. Here we add a
		// column for the label of the node, and then a column for the
		// molecule of which the node is a member.

		// The Label for a node
		comp_graph.addColumn(VisualItem.LABEL, String.class);

		// The parent (molecule) for a node
		comp_graph.addColumn(COMP_PARENT_LABEL, String.class);


		// Aggregate tables are created by adding an aggregator group to the
		// NetworkViewer. This is also a table data structure and will be
		// used to keep track of the shape of the molecules.
		AggregateTable at = nv.addAggregateTable(AGG);

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

		// Set how the clicks will be handled.
		//
		// Here you can create your own interactions for the Graph.
		// If you do not want interactions, simply do not add a
		// clickcontroldelegate. See the
		// editor.contactmap.CMapClickControlDelegate
		// for how to implement it.
		
		
		nv.setClickControl(new SpeciesGraphClickControlDelegate(nv.getVisualization()));

		// Delegation is also used for tooltips, but I do not want them
		// and so I commented them out.
		// Set how the tooltips will work
		// nv.setTooltipDelegate(new CMapHoverTooltipDelegate());

		 // This is so I can get the aggregate for a specific molecule based on the string
        // value of its index in the molecules arraylist
        Hashtable<String, AggregateItem> aggregates = new Hashtable<String, AggregateItem>();
        
        // This is an index to the Node objects so that I can retrieve them 
        // based on the string value "<parent molecule index>.<component index>".
        Hashtable<String, Node> nodes = new Hashtable<String, Node>();
        
        // This is so I can get an edge, based on an integer value.
        Hashtable<Integer, Edge> edges = new Hashtable<Integer, Edge>();

		// Now begins the construction of the data structure. I am not sure
		// if you will be able to use Yao's parser and get the same structure,
		// but if you can, then you will be able to use this code below to
		// create
		// the visualizaiton.
		Molecule tmole;
		Component tcomp;
		Bond tbond;

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

		// print out the name of molecules
		for (int i = 0; i < molecules.size(); i++) {
			System.out.println("Molecule " + molecules.get(i).getId() + ": "
					+ molecules.get(i).getName());
		}
		
		/*
		
		// add the expression of the species
		Node exp = comp_graph.addNode();
		if (species.getId() != -1) {
			// species
		exp.setString(VisualItem.LABEL, Integer.toString(species.getId())+ " " + species.getExpression());
		}
		else {
			// not a species, just expression
			exp.setString(VisualItem.LABEL, species.getExpression());
		}
		*/

		for (int i = 0; i < molecules.size(); i++) {
			// Get the molecule
			tmole = molecules.get(i);

			// If there are no components in the molecule, then the molecule
			// is rendered as a component.
			if (tmole.getComponents().size() == 0) {
				Node n = comp_graph.addNode();
				n.setString(VisualItem.LABEL, tmole.getName());
			}

			// It has components
			else {
				otherCompsInMol = new ArrayList<Node>();

				// Try to get the aggregate item
				AggregateItem aggregateForMolecule = aggregates.get(tmole
						.getId()
						+ "");

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

					// TODO For now just ignore the states.
					// If it does not have any states
					// if(tcomp.states.size() == 0)
					// {
					// Make a new node for it
					Node n = comp_graph.addNode();
					// Set its name
					n.setString(VisualItem.LABEL, tcomp.getName());
					// Set its parent
					n.setString(COMP_PARENT_LABEL, tmole.getName());
					nodes.put(tmole.getId() + "." + tcomp.getId(), n);
					aggregateForMolecule.addItem(vis.getVisualItem(
							COMPONENT_GRAPH, n));
					// }

					for (Node on : otherCompsInMol) {
						Edge te = comp_graph.addEdge(n, on);
						EdgeItem ei = (EdgeItem) vis.getVisualItem(
								COMPONENT_GRAPH + ".edges", te);

						ei.setVisible(false);
					}

					otherCompsInMol.add(n);

					// If it has states
					// TODO
				}
			}
		} // Close for each molecule

		// DEBUG
		System.out.println("Nodes: ");
		for (String i : nodes.keySet()) {
			System.out.println("\tKey: " + i + " Value: " + nodes.get(i));
		}

		// For all of the bonds
		for (int l = 0; l < bonds.size(); l++) {
			tbond = bonds.get(l);
			Edge e;
			// Create the edge
			e = comp_graph.addEdge(nodes.get(tbond.getLeftM() + "."
					+ tbond.getLeftC()), nodes.get(tbond.getRightM() + "."
					+ tbond.getRightC()));
			
			edges.put(tbond.getId(), e);
		}
		
	}

	public Display getDisplay() {
		return nv.getDisplay();
	}

}

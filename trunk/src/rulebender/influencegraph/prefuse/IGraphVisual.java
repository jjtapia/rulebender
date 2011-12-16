package rulebender.influencegraph.prefuse;

import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.StrokeLib;
import prefuse.visual.VisualItem;
import rulebender.contactmap.models.CMapModel;
import rulebender.core.prefuse.networkviewer.CustomizedZoomToFitControl;
import rulebender.core.prefuse.networkviewer.contactmap.ReverseWheelZoomControl;
import rulebender.core.prefuse.networkviewer.influencegraph.IMAPLayout;
import rulebender.core.prefuse.networkviewer.influencegraph.RuleInfluenceRenderer;
import rulebender.core.prefuse.overview.FitOverviewListener;
import rulebender.influencegraph.models.InfluenceGraphModel;

public class IGraphVisual 
{
	// Graph structure
	Graph iGraph;

	// Visualization object
	Visualization vis;

	// Arraylist to hold all of the node objects for creating edges.
	ArrayList<Node> nodeList = new ArrayList<Node>();

	InfluenceGraphModel model;

	Display mainDisplay;

	public IGraphVisual(InfluenceGraphModel model_in, Dimension mainDisplaySize_in) 
	{
		model = model_in;
		// directed graph
		iGraph = new Graph(true);
		// iGraph = new Graph();
		vis = new Visualization();
		mainDisplay = new Display();

		vis.addGraph("igraph", iGraph);
		
		vis.addFocusGroup("selected");

		setUpGraph();
		setUpRenderers();
		setUpActions();
		setUpMainDisplay(mainDisplaySize_in);
	}

	private void setUpGraph() {
		// index of the rule
		iGraph.addColumn(VisualItem.LABEL, String.class);
		// name of the rule
		iGraph.addColumn("rulename", String.class);
		// -1 none, 0 possible, 1 definite
		iGraph.addColumn("activation", Integer.class);
		// -1 none, 0 possible, 1 definite
		iGraph.addColumn("inhibition", Integer.class);
		// indicate the direction of the reaction (rule can be bi-direction)
		iGraph.addColumn("forward", Integer.class);
		// whether to highlight, used for click control
		iGraph.addColumn("highlight", boolean.class);

		// Create all of the Prefuse Nodes for the IRuleNode objects
		for (int i = 0; i < model.getRuleNodes().size(); i++) {
			// direction of the rule
			int direction = (model.getRuleNodes().get(i).isForward() ? 1 : 0);
			
			Node currentNode = iGraph.addNode();
			nodeList.add(currentNode);
			
			currentNode.set("forward", direction);
			
			// label of the rule
			String label = "";
						
			// create label based on actual rule label defined by user in BNGL model
			label = model.getRuleNodes().get(i).getLabel();
			if (label == "") {
				// create label based on definition index
				int index = model.getRuleNodes().get(i).getIruleindex() + 1;
				label += "Rule" + index;
				if (direction == 0) {
					label += "'";
				}
			}
			else {
				// create label based on definition index
				int index = model.getRuleNodes().get(i).getIruleindex() + 1;
				label += "(R" + index;
				if (direction == 0) {
					label += "'";
				}
				label += ")";
			}
			
			currentNode.set(VisualItem.LABEL, label);
			// rule name
			String rulename = parseTwoDirectionRule(model.getRuleNodes().get(i)
					.getName(), direction);
			currentNode.set("rulename", rulename);

		}

		// Set up the edges
		for (int i = 0; i < model.getInfluences().size(); i++) {
			// edge for activation
			if (model.getInfluences().get(i).getActivation() != -1) {
				Edge e_a = iGraph.addEdge(
						nodeList.get(model.getInfluences().get(i)
								.getStartrulenodeindex()),
						nodeList.get(model.getInfluences().get(i)
								.getEndrulenodeindex()));
				e_a.set("activation", model.getInfluences().get(i).getActivation());
				e_a.set("inhibition", -1);
			}
			
			// edge for inhibition
			if (model.getInfluences().get(i).getInhibition() != -1) {
				Edge e_i = iGraph.addEdge(
						nodeList.get(model.getInfluences().get(i)
								.getStartrulenodeindex()),
						nodeList.get(model.getInfluences().get(i)
								.getEndrulenodeindex()));
				e_i.set("activation", -1);
				e_i.set("inhibition", model.getInfluences().get(i).getInhibition());
			}
		}
	}

	private String parseTwoDirectionRule(String rulename, int forward) {
		
		String res = "";
		// forward
		if (forward == 1) {
			String constraints = "";
			// two direction , only keep forward
			if (rulename.indexOf("<") != -1) {
				// delete <
				res = rulename.replace("<", "");
				// delete second rate
				if (res.indexOf("exclude")!=-1 || res.indexOf("include")!=-1) {
					constraints = res.substring(res.lastIndexOf(" ")).trim();
					res = res.substring(0, res.lastIndexOf(" ")).trim();
				}
				if (res.lastIndexOf(",") != 0) {
					res = res.substring(0, res.lastIndexOf(","));
				}
				// add constraints if has
				res += " " + constraints;
				res = res.trim();
			} else {
				// change nothing
				res = rulename;
			}
		}
		// two direction, only keep backward
		else if (forward == 0) {
			String secondrate = "";
			String constraints = "";
			if (rulename.indexOf(">") != -1) {
				// keep the second half of the rule, includes product and rates
				res += rulename.substring(rulename.indexOf(">") + 1).trim();

				//delete constrains
				if (res.indexOf("exclude")!=-1 || res.indexOf("include")!=-1) {
					constraints = res.substring(res.lastIndexOf(" ")).trim();
					res = res.substring(0, res.lastIndexOf(" ")).trim();
				}
				
				// delete rate
				if (res.lastIndexOf(",") != -1) {
					secondrate = res.substring(res.lastIndexOf(",") + 1,
							res.length()).trim();
					res = res.substring(0, res.lastIndexOf(",")).trim();
					if (res.lastIndexOf(" ") != -1) {
						res = res.substring(0, res.lastIndexOf(" ")).trim();
					}
				}
			}
			// add direction
			res += " -> ";
			// add first half of the rule, include product
			if (rulename.indexOf("<") != -1) {
				res += rulename.substring(0, rulename.indexOf("<")).trim();
			}
			// add second rate
			res += " " + secondrate + " " + constraints;
			res = res.trim();
		}

		return res;
	}

	private void setUpRenderers() {
		// The LabelRenderer will display text (and an option image) in a shape.
		// LabelRenderer lr = new LabelRenderer(VisualItem.LABEL);
		RuleRenderer lr = new RuleRenderer(VisualItem.LABEL);
		// The SelfReferencingRenderer is a small subclass of the EdgeRenderer.
		// If the two items comprising the edge are the same object, then
		// an ellipse is drawn, else the parent class is used as is normal.

		RuleInfluenceRenderer rir = new RuleInfluenceRenderer(
				Constants.EDGE_TYPE_CURVE);

		// The factory decides which renderer to use for a visual item.
		DefaultRendererFactory rf = new DefaultRendererFactory(lr);

		// Set the default renderer for edges
		rf.setDefaultEdgeRenderer(rir);

		// Tell the visualization to use our factory.
		vis.setRendererFactory(rf);
	}

	private void setUpActions() {

		// The DataColorAction chooses a color given a palette based on the
		// data column that is passed to the constructor.
		ColorAction fill = new ColorAction("igraph.nodes",
				VisualItem.FILLCOLOR, ColorLib.rgb(225, 225, 225));

		// ColorActions color the given group a certain color.
		ColorAction text = new ColorAction("igraph.nodes",
				VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0));

		// FontAction for node text
		int fontsize = vis.getGroup("igraph.nodes").getTupleCount();
		fontsize *= 0.8;
		if (fontsize < 18)
			fontsize = 18;
		FontAction textFont = new FontAction("igraph.nodes", FontLib.getFont(
				"Arial", Font.PLAIN, fontsize));

		// Change the color of the strokes.
		ColorAction nodeStroke = new ColorAction("igraph.nodes",
				VisualItem.STROKECOLOR, ColorLib.rgb(100, 100, 100));

		// Change the color of the stroke for the selected nodes
		nodeStroke.add("ingroup('selected')", ColorLib.rgb(225, 100, 100));
		
		int alpha = 200;
		ColorAction edgeStroke = new ColorAction("igraph.edges",
				VisualItem.STROKECOLOR, ColorLib.rgba(105, 105, 105, alpha));

		// This predicate tells our fill ColorAction to use a different color
		// for nodes with states.
		Predicate activate1 = (Predicate) ExpressionParser
				.parse("activation == 1");
		edgeStroke.add(activate1, ColorLib.rgba(77, 146, 33, alpha));
		Predicate activate0 = (Predicate) ExpressionParser
				.parse("activation == 0");
		edgeStroke.add(activate0, ColorLib.rgba(77, 146, 33, alpha));
		Predicate inhibit1 = (Predicate) ExpressionParser
				.parse("inhibition == 1");
		edgeStroke.add(inhibit1, ColorLib.rgba(197, 27, 125, alpha));
		Predicate inhibitn0 = (Predicate) ExpressionParser
				.parse("inhibition == 0");
		edgeStroke.add(inhibitn0, ColorLib.rgba(197, 27, 125, alpha));

		// Use these to change the size of the strokes.
		StrokeAction nodeStrokea = new StrokeAction("igraph.nodes",
				StrokeLib.getStroke(1.0f));

		// For selected
		nodeStrokea.add("ingroup('selected')", StrokeLib.getStroke(4.0f));
		
		StrokeAction edgeStrokea = new StrokeAction("igraph.edges",
				StrokeLib.getStroke(3.0f));

		float dashes[] = { 5.0f, 10.0f };

		Predicate activate0dashes = (Predicate) ExpressionParser
				.parse("activation == 0");
		edgeStrokea.add(activate0dashes, StrokeLib.getStroke(3.0f, dashes));
		Predicate inhibitn0dashes = (Predicate) ExpressionParser
				.parse("inhibition == 0");
		edgeStrokea.add(inhibitn0dashes, StrokeLib.getStroke(3.0f, dashes));
		
		// edge fill color, for arrows
		ColorAction edgeFill = new ColorAction("igraph.edges",
				VisualItem.FILLCOLOR, ColorLib.rgba(105, 105, 105, alpha));

		// This predicate tells our fill ColorAction to use a different color
		// for nodes with states.
		edgeFill.add(activate1, ColorLib.rgba(77, 146, 33, alpha));
		edgeFill.add(activate0, ColorLib.rgba(77, 146, 33, alpha));
		edgeFill.add(inhibit1, ColorLib.rgba(197, 27, 125, alpha));
		edgeFill.add(inhibitn0, ColorLib.rgba(197, 27, 125, alpha));

		// create an action list containing all color assignments
		ActionList color = new ActionList();

		color.add(fill);
		color.add(text);
		color.add(textFont);
		color.add(nodeStroke);
		color.add(edgeStroke);
		color.add(nodeStrokea);
		color.add(edgeStrokea);
		color.add(edgeFill);
		color.add(new RepaintAction());

		ActionList layout = new ActionList();

		IMAPLayout c = new IMAPLayout("igraph");
		int radius = iGraph.getNodeCount() * 20;
		if (radius < 200)
			radius = 200;
		c.setRadius(radius);

		layout.add(c);
		layout.add(new RepaintAction());

		vis.putAction("layout", layout);
		vis.putAction("color", color);
	}

	public void setUpMainDisplay(Dimension mainDisplaySize_in) 
	{

		// Add the vis object to the display
		mainDisplay.setVisualization(vis);

		// Set the size
		mainDisplay.setSize(mainDisplaySize_in);

		// Turn on antialiasing
		mainDisplay.setHighQuality(true);

		mainDisplay.addControlListener(new DragControl());

		mainDisplay.addControlListener(new PanControl());

		mainDisplay.addControlListener(new InfluenceGraphClickControlDelegate(
				vis));

		// tooltip
		// mainDisplay.addControlListener(tooltipDelegate);

		// Zoom with the mouse wheel
		mainDisplay.addControlListener(new ReverseWheelZoomControl());
		mainDisplay.addControlListener(new CustomizedZoomToFitControl());

		vis.run("color");
		// start up the animated layout
		vis.run("layout");
	}

	public Display getDisplay() {
		return mainDisplay;
	}
}

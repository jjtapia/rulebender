package rulebender.prefuse.contactmap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


import rulebender.linkedviews.LinkHub;
import rulebender.linkedviews.LinkedViewsReceiverInterface;
import rulebender.prefuse.collinsbubbleset.layout.BubbleSetLayout;
import rulebender.prefuse.networkviewer.PrefuseTooltip;
import rulebender.prefuse.networkviewer.contactmap.ComponentTooltip;
import rulebender.prefuse.networkviewer.contactmap.JMenuItemRuleHolder;
import rulebender.prefuse.networkviewer.contactmap.VisualRule;


import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.controls.ControlAdapter;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;
import prefuse.util.StrokeLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import rulebender.prefuse.PngSaveFilter;
//import visualizationviewer.VisualizationViewerController;

public class CMapClickControlDelegate extends ControlAdapter implements LinkedViewsReceiverInterface 
{
	public static final String AGG_CAT_LABEL = "molecule";
	
	// For the node tooltips.
	PrefuseTooltip activeTooltip;

	private AggregateTable bubbleTable;
	private Visualization vis;

	private VisualRule activeRule;

	private String displaymode_states = "Show States";
	private String displaymode_compartments = "Show Compartments";

//	private VisualizationViewerController visviewer;

	public CMapClickControlDelegate(Visualization v) 
	{
		
	//	visviewer = VisualizationViewerController.loadVisualizationViewController();
		
		// Set the local reference to the visualization that this controller is
		// attached to.
		vis = v;

		// Create the bubbletable that is going to be used for aggregates.
		bubbleTable = vis.addAggregates("bubbles");

		// Add the shape column to the table.
		bubbleTable.addColumn(VisualItem.POLYGON, float[].class);
		bubbleTable.addColumn("type", String.class);

		// For collins
		bubbleTable.addColumn("SURFACE", ArrayList.class);
		bubbleTable.addColumn("aggregate_threshold", double.class);
		bubbleTable.addColumn("aggreagate_negativeEdgeInfluenceFactor",
				double.class);
		bubbleTable.addColumn("aggregate_nodeInfluenceFactor", double.class);
		bubbleTable.addColumn("aggreagate_negativeNodeInfluenceFactor",
				double.class);
		bubbleTable.addColumn("aggregate_edgeInfluenceFactor", double.class);

		// Set the bubble stroke size
		StrokeAction aggStrokea = new StrokeAction("bubbles", StrokeLib
				.getStroke(0f));

		// Set the color of the stroke.
		ColorAction aStroke = new ColorAction("bubbles",
				VisualItem.STROKECOLOR, ColorLib.rgb(10, 10, 10));

		// A color palette. We define a color action later that depends on it.
		int[] palette = new int[] { ColorLib.rgba(255, 180, 180, 150),
				ColorLib.rgba(190, 190, 255, 150), 
				ColorLib.rgba(244, 202, 228, 150),
				ColorLib.rgba(179, 226, 205, 150)};

		// Set the fill color for the bubbles.
		// ColorAction aFill = new ColorAction("bubbles",VisualItem.FILLCOLOR,
		// ColorLib.rgb(240, 50, 40));
		DataColorAction aFill = new DataColorAction("bubbles", "type",
				Constants.NOMINAL, VisualItem.FILLCOLOR, palette);

		// create an action list containing all color assignments
		ActionList color = new ActionList();

		color.add(new BubbleSetLayout("bubbles", "component_graph"));
		color.add(aggStrokea);
		color.add(aStroke);
		color.add(aFill);
		// color.add(new RepaintAction());

		ActionList layout = new ActionList();

		// layout.add(new AggregateBubbleLayout("bubbles"));
		layout.add(new BubbleSetLayout("bubbles", "component_graph"));
		layout.add(new RepaintAction());

		vis.putAction("bubbleLayout", layout);
		vis.putAction("bubbleColor", color);
		
		// Tell the linkHub to let us know when a rule is selected.
		LinkHub.getLinkHub().registerLinkedViewsListener(this);
	
	}

	/**
	 * Called when no VisualItem is hit.
	 */
	public void mouseClicked(MouseEvent e) 
	{
		// super.mouseClicked(e);

		// Right click
		if (e.getButton() == MouseEvent.BUTTON3 || (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown())) 
		{
			JPopupMenu popupMenu = new JPopupMenu();
			// save as
			JMenuItem saveAsMenuItem = new JMenuItem("Save as...");
			popupMenu.add(saveAsMenuItem);

			// states display mode
			JMenuItem displaymodeStatesMenuItem = new JMenuItem(displaymode_states);
			popupMenu.add(displaymodeStatesMenuItem);
			
			// compartments display mode
			// display mode
			JMenuItem displaymodeCompartmentsMenuItem = new JMenuItem(displaymode_compartments);
			popupMenu.add(displaymodeCompartmentsMenuItem);

			popupMenu.show(e.getComponent(), e.getX(), e.getY());

			// add listener
			saveAsMenuItem.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					// file filter
					chooser.addChoosableFileFilter(new PngSaveFilter());

					int option = chooser.showSaveDialog(null);
					if (option == JFileChooser.APPROVE_OPTION) {
						if (chooser.getSelectedFile() != null) {
							File theFileToSave = chooser.getSelectedFile();
							OutputStream output;
							try {
								output = new FileOutputStream(theFileToSave);
								// save png
								vis.getDisplay(0).saveImage(output, "PNG", 1.0);
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							}

						}
					}
				}

			});

			// add listener
			displaymodeStatesMenuItem.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					AbstractButton aButton = (AbstractButton) e.getSource();

					Iterator iter = vis.items("component_graph");

					// show states
					if (aButton.getText().equals("Show States")) {
						while (iter.hasNext()) {
							VisualItem item = (VisualItem) iter.next();
							// show state nodes
							if (item instanceof NodeItem) {
								String type = item.getString("type");
								if (type != null && type.equals("state")) {
									item.setVisible(true);
								}
							}
							// show edges linked to state nodes
							else if (item instanceof EdgeItem) {
								String displaymode = item
										.getString("displaymode");

								if (displaymode != null) {
									// edge linked to state nodes
									if (displaymode.equals("both")
											|| displaymode.equals("state")) {
										item.setVisible(true);
									}
									// edge linked to component nodes
									else if (displaymode.equals("component")) {
										item.setVisible(false);
									}
								}
							}
						}
						displaymode_states = "Hide States";

					} else {
						while (iter.hasNext()) {
							VisualItem item = (VisualItem) iter.next();

							// hide the state nodes
							if (item instanceof NodeItem) {
								String type = item.getString("type");
								if (type != null && type.equals("state")) {
									item.setVisible(false);
								}
							}
							// show edges linked to component nodes
							else if (item instanceof EdgeItem) {
								String displaymode = item
										.getString("displaymode");

								if (displaymode != null) {
									// edge linked to component nodes
									if (displaymode.equals("both")
											|| displaymode.equals("component")) {
										item.setVisible(true);
									}
									// edge linked to state nodes
									else if (displaymode.equals("state")) {
										item.setVisible(false);
									}
								}
							}
						}
						displaymode_states = "Show States";
					}

					// apply actions
					vis.run("color");
					vis.run("complayout");
					vis.run("compartmentlayout");
					vis.run("bubbleColor");
					vis.run("bubbleLayout");

				}
			});
			
			
			// add listener
			displaymodeCompartmentsMenuItem.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					AbstractButton aButton = (AbstractButton) e.getSource();

					Iterator iter = vis.items("compartments");

					// show states
					if (aButton.getText().equals("Show Compartments")) {
						while (iter.hasNext()) {
							VisualItem item = (VisualItem) iter.next();
							item.setVisible(true);
						}
						displaymode_compartments = "Hide Compartments";

					} else {
						while (iter.hasNext()) {
							VisualItem item = (VisualItem) iter.next();
							item.setVisible(false);
						}
						displaymode_compartments = "Show Compartments";
					}

					// apply actions
					vis.run("color");
					vis.run("complayout");
					vis.run("compartmentlayout");
					vis.run("bubbleColor");
					vis.run("bubbleLayout");

				}
			});
		} // Closes right click
		
		// Left Click.  This check has to come after the right click because the condition is also
		// true in the case of a control click.
		else if (e.getButton() == MouseEvent.BUTTON1) 
		{
			// Clear the selections.  
			//clearSelection();
			LinkHub.getLinkHub().clearSelectionFromContactMap();
		} 
	}
	
	public void itemClicked(VisualItem item, MouseEvent e) 
	{
		// Right Click
		if (e.getButton() == MouseEvent.BUTTON3 || (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown())) 
		{
			// Clear the bubble if there is one.
			if (activeRule != null) 
			{
				activeRule.setVisible(false);
				activeRule = null;
			}

			if (item instanceof NodeItem) 
			{
				nodeRightClicked(item, e);
			}
			
			else if (item instanceof EdgeItem) 
			{
				edgeRightClicked(item, e);
			}
		}
		// left click.  This check has to come after the right click because the condition is also
		// true in the case of a control click.
		else if (e.getButton() == MouseEvent.BUTTON1) 
		{
			if ((item instanceof NodeItem))
				nodeLeftClicked(item, e);

			else if (item instanceof EdgeItem)
				edgeLeftClicked(item, e);
			
			else if (item instanceof AggregateItem) {
				aggregateLeftClicked(item, e);
			}
		}
	}

	/**
	 * Left click on node
	 * 
	 * @param item
	 * @param event
	 */
	private void nodeLeftClicked(VisualItem item, MouseEvent event) 
	{
		//TODO organize clear selections. 
		clearSelection(true);

		setVisualItemAsSelected(item);
		
		if(item.getString("type").equals("state"))
		{
			LinkHub.getLinkHub().stateSelectedInContactMap(item);
		}
		else if(item.getString("type").equals("component"))
		{
			LinkHub.getLinkHub().componentSelectedInContactMap(item);
		}
		else if(item.getString("type").equals("hub"))
		{
			LinkHub.getLinkHub().hubSelectedInContactMap(item);
		}
	}
	
	/**
	 * Right click on node
	 * 
	 * @param item
	 * @param event
	 */
	private void nodeRightClicked(VisualItem item, MouseEvent event) 
	{	
		String type = item.getString("type");
		// state node
		if (type.equals("state")) 
		{
			// show associated rules
			// Get the edge object that corresponds to the edgeitem
			Node state_node = (Node) item.getSourceTuple();

			// get the rules that can make that state node.
			ArrayList<VisualRule> rules = (ArrayList<VisualRule>) state_node.get("rules");

			if (rules == null || rules.size() == 0) {
					
				showTooltip(new ComponentTooltip((Display) event.getSource(),
						"State: " + (String) item.get(VisualItem.LABEL),
						""), item,
						event);			
			}
			else 
			{
				JPopupMenu popup = new JPopupMenu();
				
				// For each of the rules that could make state node.

				for (VisualRule rule : rules) 
				{
					// Create a JMenuItem that corresponds to a rule.
					JMenuItemRuleHolder menuItem = new JMenuItemRuleHolder(rule);

					// Add an actionlistener to the menu item.
					menuItem.addActionListener(new ActionListener() {
						// When the menuItem is clicked, fire this.
						public void actionPerformed(ActionEvent e) 
						{
							//JMenuItemRuleHolder source = (JMenuItemRuleHolder) (e.getSource());
							VisualRule sourceRule = ((JMenuItemRuleHolder) e.getSource()).getRule();

							selectRule(sourceRule, true);
							/*
							 JMenuItemRuleHolder source = (JMenuItemRuleHolder) (e.getSource());

							// Build the rule if it is not built already.
							if (!(source.getRule().isBuilt()))
								source.getRule().pack("component_graph",
										bubbleTable);

							// Clear the current activeRule.
							if (activeRule != null) {
								activeRule.setVisible(false);
								activeRule = null;
							}

							// Set the active rule
							activeRule = source.getRule();

							// Set the bubbles to visible
							activeRule.setVisible(true);

							// Run the actions. Apparently the layout does not need
							// to be run here...
							vis.run("bubbleLayout");
							vis.run("bubbleColor");
							*/
						}
					});

					// Add the JMenuItem to the popup menu.
					popup.add(menuItem);
				}

				// After adding all of the rules, show the popup.
				popup.show(event.getComponent(), event.getX(), event.getY());
				
			}
			
			//LinkHub.getLinkHub().stateSelectedInContactMap(item);
		}
		// component node
		else if (type.equals("component"))
		{

			String states = "[";
			ArrayList<String> stateList = (ArrayList) item.get("states");
			if (stateList != null) 
			{
				for (int i = 0; i < stateList.size() - 1; i++) {
					states = states + stateList.get(i) + ", ";
				}
				states = states + stateList.get(stateList.size() - 1);
			}

			states += "]";

			String statesStr = "";
			if (!states.equals("[]")) 
			{
				statesStr = "States: " + states;
			}	
			
			showTooltip(new ComponentTooltip((Display) event.getSource(),
					"Component:" + (String) item.get(VisualItem.LABEL),
					statesStr), item, event);
			
			//LinkHub.getLinkHub().componentSelectedInContactMap(item);
		} 
		else if (type.equals("hub")) 
		{
			Node hub_node = (Node) item.getSourceTuple();

			// get the rules that can make that hub node.
			ArrayList<VisualRule> rules = (ArrayList<VisualRule>) hub_node.get("rules");

			if (rules != null || rules.size() != 0) 
			{
				JPopupMenu popup = new JPopupMenu();

				// For each of the rules that could make the hub node.

				for (VisualRule rule : rules) 
				{
					// Create a JMenuItem that corresponds to a rule.
					JMenuItemRuleHolder menuItem = new JMenuItemRuleHolder(rule);

					// Add an actionlistener to the menu item.
					menuItem.addActionListener(new ActionListener()
					{
						// When the menuItem is clicked, fire this.
						public void actionPerformed(ActionEvent e) 
						{
							//JMenuItemRuleHolder source = (JMenuItemRuleHolder) (e.getSource());
							VisualRule sourceRule = ((JMenuItemRuleHolder) e.getSource()).getRule();

							selectRule(sourceRule, true);
							
							/*
							JMenuItemRuleHolder source = (JMenuItemRuleHolder) (e
									.getSource());

							// Build the rule if it is not built already.
							if (!(source.getRule().isBuilt()))
								source.getRule().pack("component_graph",
										bubbleTable);

							// Clear the current activeRule.
							if (activeRule != null) {
								activeRule.setVisible(false);
								activeRule = null;
							}

							// Set the active rule
							activeRule = source.getRule();

							// Set the bubbles to visible
							activeRule.setVisible(true);

							// Run the actions. Apparently the layout does not
							// need
							// to be run here...
							vis.run("bubbleLayout");
							vis.run("bubbleColor");
							
							*/
						}
					});

					// Add the JMenuItem to the popup menu.
					popup.add(menuItem);
				}

				// After adding all of the rules, show the popup.
				popup.show(event.getComponent(), event.getX(), event.getY());
			}
			
			//LinkHub.getLinkHub().hubSelectedInContactMap(item);
		}
	}

	/**
	 * Called on the left click of an edge.
	 * 
	 * @param item
	 */
	private void edgeLeftClicked(VisualItem item, MouseEvent event) 
	{	
		//TODO organize clear selections.
		clearSelection(true);
		LinkHub.getLinkHub().clearSelectionFromContactMap();
		setVisualItemAsSelected(item);
		
		LinkHub.getLinkHub().edgeSelectedInContactMap(item);
	}
	
	/**
	 * Called on the right click of an edge.
	 * 
	 * @param item
	 */
	private void edgeRightClicked(VisualItem item, MouseEvent event) 
	{
		// Get the edge object that corresponds to the edgeitem
		Edge edge = (Edge) item.getSourceTuple();

		// get the rules that can make that edge.
		ArrayList<VisualRule> rules = (ArrayList<VisualRule>) edge.get("rules");

		// Make a popupMenu that will have all of the rules added to it.
		JPopupMenu popup = new JPopupMenu();

		if (rules == null || rules.size() == 0)
			popup.add(new JMenuItem("No associated rules."));
		else 
		{
			// For each of the rules that could make the bond.

			for (VisualRule rule : rules) {
				// Create a JMenuItem that corresponds to a rule.
				JMenuItemRuleHolder menuItem = new JMenuItemRuleHolder(rule);

				// Add an actionlistener to the menu item.
				menuItem.addActionListener(new ActionListener() {
					// When the menuItem is clicked, fire this.
					public void actionPerformed(ActionEvent e) {
						//JMenuItemRuleHolder source = (JMenuItemRuleHolder) (e.getSource());
						VisualRule sourceRule = ((JMenuItemRuleHolder) e.getSource()).getRule();

						selectRule(sourceRule, true);
					}
				});

				// Add the JMenuItem to the popup menu.
				popup.add(menuItem);
			}

			// After adding all of the rules, show the popup.
			popup.show(event.getComponent(), event.getX(), event.getY());

			//LinkHub.getLinkHub().edgeSelectedInContactMap(item);
		}
	}
	
	/**
	 * clear the active rule and refresh the screen if the bool is true.
	 */
	private void clearSelection(boolean refresh)
	{		
		if (activeTooltip != null) 
		{
			activeTooltip.stopShowingImmediately();
		}
			
		// empty annotation table
		//TODO organize this functionality better.
		// This is commented out because it is clearing the table after it is 
		// set from the igraphClickControlDelegate.
		// It should not need to be cleared anyway if it is going to be replaced.
		//cmapAnnotation.updateAnnotationTable(null, null, null, null);

		// Clear the current activeRule.
		if (activeRule != null) {
			activeRule.setVisible(false);
			activeRule = null;
		}
		
		// Clear any selected visual items.
		TupleSet selectedSet = vis.getFocusGroup("selected");
		if(selectedSet != null)
		{	selectedSet.clear();
			vis.run("color");
		}
		
		// Run the actions. Apparently the layout does not need
		// to be run here...
		if (refresh)
		{
			vis.run("bubbleLayout");
			vis.run("bubbleColor");
		}
	}
	
	/**
	 * Selects the given visual rule.  The passing on boolean is because
	 * this method is called both as a result of local clicks and from
	 * messages from the linkhub.  If it is passed back to the linkhub
	 * it will be an infinite loop.
	 * @param rule
	 */
	private void selectRule(VisualRule sourceRule, boolean passingOn)
	{	
		// keep the annotation panel when a rule is selected
		clearSelection(false);
		
		// Build the rule if it is not built already.
		if (!(sourceRule.isBuilt()))
			sourceRule.pack("component_graph",
					bubbleTable);

		// Set the active rule
		activeRule = sourceRule;

		// Set the bubbles to visible
		activeRule.setVisible(true);

		// Run the actions. Apparently the layout does not need
		// to be run here...
		vis.run("bubbleLayout");
		vis.run("bubbleColor");
		
		if(passingOn)
			LinkHub.getLinkHub().ruleSelectedInContactMap(activeRule);
	}
	
	/**
	 * Called on the left click of an aggregate item.
	 * @param item
	 * @param event
	 */
	private void aggregateLeftClicked(VisualItem item, MouseEvent event) 
	{
		// TODO This is kind of messy.  The rule is cleared here, and needs to be
		// cleared elsewhere, but other visualizations should do it from the 
		// moleculeSelected event.
		clearSelection(true);
		
		// Set the aggregate as selected. 
		setVisualItemAsSelected(item);
		
		// if it is a molecule
		// System.out.println("Type: " + item.get("compartment"));
		
		if(item.get("compartment") != null)
			LinkHub.getLinkHub().compartmentSelectedInContactMap(item);
		else
			LinkHub.getLinkHub().moleculeSelectedInContactMap(item);
	}
	
	
	/**
	 * Called when the user enters an item.
	 * TODO Memory leak.  Every time an item is entered this is called.
	 * It creates 3 new EnterColorActionObjects, adds them to a new color list,
	 * and then adds that new list to the visualization. 
	 */
	
	
	public void itemEntered(VisualItem item, MouseEvent event) 
	{
		/*
		ActionList color = new ActionList();
		// aggregate stroke color, edge stroke color, highlight color
		int[] palette = { ColorLib.rgb(10, 10, 10), ColorLib.rgb(105, 105, 105), ColorLib.rgb(230, 10, 10) };
		EnterColorAction aStrokeColor = new EnterColorAction("aggregates",
				VisualItem.STROKECOLOR, palette, item);
		EnterColorAction nStrokeColor = new EnterColorAction("component_graph.nodes",
				VisualItem.STROKECOLOR, palette, item);
		EnterColorAction eStrokeColor = new EnterColorAction("component_graph.edges",
				VisualItem.STROKECOLOR, palette, item);

		color.add(aStrokeColor);
		color.add(nStrokeColor);
		color.add(eStrokeColor);
		color.add(new RepaintAction());
		vis.putAction("entercolor", color);
		vis.run("entercolor");
		*/
	}

	/**
	 * Called when the user exits an item. If there is a tooltip, then the
	 * tooltip is removed.
	 * 
	 */
	public void itemExited(VisualItem item, MouseEvent e) {
		if (activeTooltip != null) {
			activeTooltip.stopShowing();
		}
		
		vis.run("color");
	}

	/**
	 * Displays the tooltip.
	 * 
	 * @param ptt
	 * @param item
	 * @param e
	 */
	protected void showTooltip(PrefuseTooltip ptt, VisualItem item,
			java.awt.event.MouseEvent e) {
		if (activeTooltip != null) {
			activeTooltip.stopShowingImmediately();
		}

		activeTooltip = ptt;

		activeTooltip.startShowing((int) e.getX() + 10, (int) e.getY());
	}

	// The overview window is updated automatically since the Display objects comes from
	// the same Visualization object. 
	public void mouseDragged(MouseEvent e) 
	{
		//visviewer.updateCMapSelectBox();
    }
	
	public void mouseWheelMoved(MouseWheelEvent e) 
	{
		
		// System.out.println("----------------------ZOOM--------------------------");
		//visviewer.updateCMapSelectBox();
		//visviewer.updateIGraphSelectBox();
    }

	public void itemDragged(VisualItem item, MouseEvent e) 
	{
		//visviewer.updateCMapSelectBox();
	}

	public void itemMoved(VisualItem item, MouseEvent e) 
	{
		//visviewer.updateCMapSelectBox();
	}

	public void itemWheelMoved(VisualItem item, MouseWheelEvent e) 
	{
		//visviewer.updateCMapSelectBox();
	}

	/**
	 * For setting a VisualItem as the member of the selected group.
	 * @param item
	 */
	private void setVisualItemAsSelected(VisualItem item)
	{
		// System.out.println("Changing selected item");
		
		TupleSet focused = vis.getFocusGroup("selected");
		
		focused.clear();
		
		focused.addTuple(item);
		
		vis.run("color");
	}
		
	/**
	 * go from text to visual item and call selectRule
	 * 
	 * @see rulebender.linkedviews.LinkedViewsReceiverInterface#ruleSelected(java.lang.String)
	 */
	public void ruleSelectedInInfluenceGraph(VisualItem ruleItem) 
	{
		clearSelection(true);
		
		String ruleText = ruleItem.getString("rulename");
		
		selectRuleFromText(ruleText);
		
	}

	private void selectRuleFromText(String ruleText)
	{
		// Rules from the influence graph will be of the form r -> p.
		// They can come from unidirectional rules r->p, or if it is 
		// bidirectional then it could be r <-> p or p <-> r.
		
		// This strips the rates out of the rule.
		ruleText = ruleText.substring(0,ruleText.lastIndexOf(")")+1);
		
		// r -> p
		String forward = ruleText;
		
		// r <-> p
		String bi_forward = ruleText.replace("->","<->");
		
		// p <-> r
		String bi_reverse = bi_forward.substring(bi_forward.indexOf('>')+1, bi_forward.length()) + 
		"<->" + bi_forward.substring(0, bi_forward.indexOf('<'));
		
		// remove the whitespace
		forward = forward.replace(" ", "");
		bi_forward = bi_forward.replace(" ", "");
		bi_reverse = bi_reverse.replace(" ", "");
		
		//System.out.println("\n\nContact Map RuleSelected Listener: \nPassed in Rule:\n\t" + ruleText + "\n");
		//System.out.println("bi ->\"" + bi_forward +"\"");
		//System.out.println("bi <-\"" + bi_reverse+"\"");
		
		// Now we have to get the visual rule by looking in each edge.
		Iterator<VisualItem> iter = vis.items("component_graph");
		
		// For each VisualItem
		while (iter.hasNext()) 
		{	
			// Get the next one.
			VisualItem curEdge = (VisualItem) iter.next();
			
			// only looking at EdgeItem objects
			// There are many INVISIBLE edges that are used to hold the force directed layout together
			// so there are many more EdgeItems than you would think. We check here to make sure that 
			// the VisualItem is an edge and that it is visible.
			//
			// TODO that may not be enough.
			// The rules arraylist is still null for some edges in invivo_reduced_model.bngl
			
			//if (curEdge instanceof EdgeItem && ((EdgeItem)curEdge).isVisible())
			//{
				
				if (curEdge.get("rules") == null)
				{
					//DEBUG 
					//System.out.println("*A visible edge has null rules*");
					continue;
				}
				
				// get the rules that can make that edge.
				ArrayList<VisualRule> rules = (ArrayList<VisualRule>) curEdge.get("rules");
				
		
				// DEBUG  checking on the size of the rules.
				//System.out.println("\tEdge with " + rules.size()+ " rules.");
				
				// For all of the rules associated with the edge
				for(VisualRule rule : rules)
				{
					// Get the text for the rule
					String potentialRule = rule.getName();
					
					// Strip the rates.
					potentialRule = potentialRule.substring(0,potentialRule.lastIndexOf(")")+1);
					
					// remove the whitespace
					potentialRule = potentialRule.replace(" ", "");
					
					// DEBUG 
					//System.out.print("\t\t" + potentialRule);
					
					// If the potential rule matches the bidirectional, forward, or reverse rule
					if (potentialRule.equals(forward) || potentialRule.equals(bi_forward) || potentialRule.equals(bi_reverse)) 
					{
						//DEBUG
						//System.out.println(" <- Match");
						
						// Select the rule
						selectRule(rule, false);
					}
					else 
					{
						//DEBUG
						//System.out.println(" <- NO");
					}
				}		
			//}
		} 

	}
	
	public void clearSelectionFromContactMap() 
	{
		clearSelection(true);
		//cmapAnnotation.updateAnnotationTable(null, null, null, null);
	}

	public void clearSelectionFromInfluenceGraph() 
	{
		clearSelection(true);
	}

	public void moleculeSelectedInContactMap(VisualItem moleculeItem) 
	{
		// Do Handled Locally.
	}

	public void ruleSelectedInContactMap(VisualRule ruleItem) 
	{
		// Handled Locally	
	}

	public void componentSelectedInContactMap(VisualItem moleculeItem) 
	{
		// Handled Locally
	}

	public void edgeSelectedInContactMap(VisualItem edge) 
	{
		// Handled Locally		
	}

	public void stateSelectedInContactMap(VisualItem stateItem) 
	{
		// Handled Locally	
	}
	
	public void hubSelectedInContactMap(VisualItem hubItem) 
	{
		// Handled Locally
	}

	//TODO doesn't work
	public void moleculeSelectedInText(String moleculeText) 
	{
		clearSelection(true);
		
		// Now we have to get the visual rule by looking in each edge.
		Iterator<VisualItem> iter = vis.items("component_graph");
		
		// For each VisualItem
		while (iter.hasNext()) 
		{	
			// Get the next one.
			VisualItem curItem = (VisualItem) iter.next();
				
			if (curItem.get(VisualItem.LABEL) == null)
			{
				//DEBUG 
				// System.out.println("*NULL*");
				continue;
			}
			
			String label = ((String) curItem.get(VisualItem.LABEL)).trim();
			moleculeText = moleculeText.trim();
			
			if (moleculeText.equals(label)) 
			{
				// Select the rule
				setVisualItemAsSelected(curItem);
			}
		}		
	}
	
	public void ruleSelectedInText(String ruleText) 
	{
		clearSelection(true);
	 	// System.out.println("Contact Map Selecting Rule From Text: " + ruleText);
	 	selectRuleFromText(ruleText);
	}

	public void compartmentSelectedInContactMap(VisualItem compartment) 
	{
		// handled locally	
	}

	public void clearSelectionFromText() 
	{
		// TODO Auto-generated method stub	
	}

	public void componentSelectedInText(String componentText) 
	{
		// TODO Auto-generated method stub	
	}

	public void stateSelectedInText(String stateText) 
	{
		// TODO Auto-generated method stub	
	}

	public void compartmentSelectedInText(VisualItem compartment) 
	{
		// TODO Auto-generated method stub	
	}
}
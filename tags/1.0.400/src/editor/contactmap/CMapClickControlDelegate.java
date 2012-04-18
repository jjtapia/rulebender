package editor.contactmap;

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
import javax.swing.table.TableModel;

import collinsbubbleset.layout.BubbleSetLayout;

import networkviewer.PrefuseTooltip;
import networkviewer.cmap.ComponentTooltip;
import networkviewer.cmap.EnterColorAction;
import networkviewer.cmap.JMenuItemRuleHolder;
import networkviewer.cmap.VisualRule;

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
import prefuse.util.ColorLib;
import prefuse.util.StrokeLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import resultviewer.graph.PngSaveFilter;
import visualizationviewer.VisualizationViewerController;

public class CMapClickControlDelegate extends ControlAdapter {
	// A const for the string value of the category of aggregate
	public static final String AGG_CAT_LABEL = "molecule";

	// For the node tooltips.
	PrefuseTooltip activeTooltip;

	private AggregateTable bubbleTable;
	private Visualization vis;

	private VisualRule activeRule;

	private String displaymode_states = "Show States";
	private String displaymode_compartments = "Show Compartments";
	
	private VisualizationViewerController visviewer;
	

	public CMapClickControlDelegate(Visualization v) {
		visviewer = VisualizationViewerController.loadVisualizationViewController();
		
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
				ColorLib.rgba(190, 190, 255, 150) };

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
	}

	/**
	 * Called when no VisualItem is hit.
	 */
	public void mouseClicked(MouseEvent e) {
		// super.mouseClicked(e);

	if (e.getButton() == MouseEvent.BUTTON3 || (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown())) {
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
		} // Close right click
	
	// Left Click
	else if (e.getButton() == MouseEvent.BUTTON1) {
		if (activeTooltip != null) {
			activeTooltip.stopShowingImmediately();
		}

		if (activeRule != null)
			activeRule.setVisible(false);
		activeRule = null;
		
		// empty annotation table
		visviewer.updateAnnotationTable(null);

		// Run the actions.
		vis.run("bubbleLayout");
		vis.run("bubbleColor");
	}
	
	}

	public void itemClicked(VisualItem item, MouseEvent e) {
		
		// Right Click
		if (e.getButton() == MouseEvent.BUTTON3 || (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown())) {
			// Clear the bubble if there is one.
			if (activeRule != null) {
				activeRule.setVisible(false);
				activeRule = null;
			}

			if (item instanceof NodeItem) {
				nodeRightClicked(item, e);
			}
			
			else if (item instanceof EdgeItem) {
				edgeRightClicked(item, e);
			}
		}
		
		// left click.  Check is second because button1 triggers left click even if mod key is down.
		else if (e.getButton() == MouseEvent.BUTTON1) {
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
	private void nodeLeftClicked(VisualItem item, MouseEvent event) {
		String type = item.getString("type");
		// state node
		if (type.equals("state")) {
		
			// show details in the table in the annotation panel
			String[] names = {"Type", "Name", "Molecule", "Component"};
			Object[][] data = {{"State", ((String) item.get(VisualItem.LABEL)).trim(), item.getString("molecule").trim(), item.getString("component").trim()}};
			TableModel tm = new CMapTableModel(names, data);
			visviewer.updateAnnotationTable(tm);
		}
		// component node
		else {

			String states = "[";
			ArrayList<String> stateList = (ArrayList) item.get("states");
			if (stateList != null) {
				for (int i = 0; i < stateList.size() - 1; i++) {
					states = states + stateList.get(i) + ", ";
				}
				states = states + stateList.get(stateList.size() - 1);
			}

			states += "]";	
			
			if (((String) item.get(VisualItem.LABEL)).trim().equals("")) {
				return;
			}
			
			// show details in the table in the annotation panel
			String[] names = {"Type", "Name", "Molecule", "States"};
			Object[][] data = {{"Component", ((String) item.get(VisualItem.LABEL)).trim(), item.getString("molecule").trim(), states.trim()}};
			TableModel tm = new CMapTableModel(names, data);
			visviewer.updateAnnotationTable(tm);		
		}
		
	}
	
	/**
	 * Right click on node
	 * 
	 * @param item
	 * @param event
	 */
	private void nodeRightClicked(VisualItem item, MouseEvent event) {
		String type = item.getString("type");
		// state node
		if (type.equals("state")) {

			// show associated rules
			// Get the edge object that corresponds to the edgeitem
			Node state_node = (Node) item.getSourceTuple();

			// get the rules that can make that edge.
			ArrayList<VisualRule> rules = (ArrayList<VisualRule>) state_node.get("rules");

			if (rules == null || rules.size() == 0) {
					
				showTooltip(new ComponentTooltip((Display) event.getSource(),
						"State: " + (String) item.get(VisualItem.LABEL),
						""), item,
						event);			
			}
			else {
				JPopupMenu popup = new JPopupMenu();
				
				// For each of the rules that could make the bond.

				for (VisualRule rule : rules) {
					// Create a JMenuItem that corresponds to a rule.
					JMenuItemRuleHolder menuItem = new JMenuItemRuleHolder(rule);

					// Add an actionlistener to the menu item.
					menuItem.addActionListener(new ActionListener() {
						// When the menuItem is clicked, fire this.
						public void actionPerformed(ActionEvent e) {
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

							// Run the actions. Apparently the layout does not need
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
			
			// show details in the table in the annotation panel
			String[] names = {"Type", "Name", "Molecule", "Component"};
			Object[][] data = {{"State", (String) item.get(VisualItem.LABEL), item.getString("molecule"), item.getString("component")}};
			TableModel tm = new CMapTableModel(names, data);
			visviewer.updateAnnotationTable(tm);
		}
		// component node
		else {

			String states = "[";
			ArrayList<String> stateList = (ArrayList) item.get("states");
			if (stateList != null) {
				for (int i = 0; i < stateList.size() - 1; i++) {
					states = states + stateList.get(i) + ", ";
				}
				states = states + stateList.get(stateList.size() - 1);
			}

			states += "]";

			String statesStr = "";
			if (!states.equals("[]")) {
				statesStr = "States: " + states;
			}	
			
			showTooltip(new ComponentTooltip((Display) event.getSource(),
					"Component:" + (String) item.get(VisualItem.LABEL),
					statesStr), item, event);
					
			
			// show details in the table in the annotation panel
			String[] names = {"Type", "Name", "Molecule", "States"};
			Object[][] data = {{"Component", (String) item.get(VisualItem.LABEL), item.getString("molecule"), states}};
			TableModel tm = new CMapTableModel(names, data);
			visviewer.updateAnnotationTable(tm);	
		}
		
	}

	/**
	 * Called on the left click of an edge.
	 * 
	 * @param item
	 */
	private void edgeLeftClicked(VisualItem item, MouseEvent event) {
		// Get the edge object that corresponds to the edgeitem
		Edge edge = (Edge) item.getSourceTuple();

		// get the rules that can make that edge.
		ArrayList<VisualRule> rules = (ArrayList<VisualRule>) edge.get("rules");

		// show details in the table in the annotation panel
		String[] names = { "Rule", "Rate1", "Rate2", "Constraints"};
		Object[][] data = new Object[rules.size()][];

		// set the rest of rows be rules
		for (int i = 0; i < rules.size(); i++) {
			data[i] = new Object[names.length];
			// get current visualruel
			VisualRule rule = rules.get(i);
			// whole rule
			// A() + B() <-> C() rate1, rate2 <exclude>
			String wholerule = rule.getName().trim();

			String rulename = "", rate1 = "", rate2 = "", constraints = "";
			
			// constraints
			if (wholerule.indexOf("exclude")!=-1 || wholerule.indexOf("exclude")!=-1) {
				constraints = wholerule.substring(
						wholerule.lastIndexOf(" ") + 1).trim();
				wholerule = wholerule.substring(0,
						wholerule.lastIndexOf(" ")).trim();
			}
			
			// two directions, has rate2
			if (wholerule.contains("<")) {
				rate2 = wholerule.substring(wholerule.lastIndexOf(",") + 1)
						.trim();
				wholerule = wholerule.substring(0, wholerule.lastIndexOf(","))
						.trim();
			}
			// rate1
			// some model don't have )
			// some model don't have space
			// the model don't have ) should have space
			if (wholerule.lastIndexOf(")") != -1) {
				rate1 = wholerule.substring(wholerule.lastIndexOf(")") + 1)
						.trim();
				rulename = wholerule.substring(0,
						wholerule.lastIndexOf(")") + 1).trim();
			} else {
				rate1 = wholerule.substring(wholerule.lastIndexOf(" ") + 1)
						.trim();
				rulename = wholerule.substring(0,
						wholerule.lastIndexOf(" ") + 1).trim();
			}

			// rulename
			data[i][0] = rulename;
			// rate1
			data[i][1] = rate1;
			// rate2
			data[i][2] = rate2;
			// constraints
			data[i][3] = constraints;
		}

		// update table
		TableModel tm = new CMapTableModel(names, data);
		visviewer.updateAnnotationTable(tm);
	}
	
	/**
	 * Called on the right click of an edge.
	 * 
	 * @param item
	 */
	private void edgeRightClicked(VisualItem item, MouseEvent event) {
		// Get the edge object that corresponds to the edgeitem
		Edge edge = (Edge) item.getSourceTuple();

		// get the rules that can make that edge.
		ArrayList<VisualRule> rules = (ArrayList<VisualRule>) edge.get("rules");

		// Make a popupMenu that will have all of the rules added to it.
		JPopupMenu popup = new JPopupMenu();

		if (rules.size() == 0)
			popup.add(new JMenuItem("No associated rules."));
		else {
			// For each of the rules that could make the bond.

			for (VisualRule rule : rules) {
				// Create a JMenuItem that corresponds to a rule.
				JMenuItemRuleHolder menuItem = new JMenuItemRuleHolder(rule);

				// Add an actionlistener to the menu item.
				menuItem.addActionListener(new ActionListener() {
					// When the menuItem is clicked, fire this.
					public void actionPerformed(ActionEvent e) {
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

						// Run the actions. Apparently the layout does not need
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
			
			// show details in the table in the annotation panel
			String[] names = {"Rule", "Rate1", "Rate2"};
			Object[][] data = new Object[rules.size()][];
			
			// set the rest of rows be rules
			for (int i = 0; i < rules.size(); i++) {
				data[i] = new Object[names.length];
				// get current visualruel
				VisualRule rule = rules.get(i);
				// whole rule
				// A() + B() <-> C() rate1, rate2
				String wholerule = rule.getName().trim();
				
				String rulename = "", rate1 = "", rate2 = "";
				// two directions, has rate2
				if (wholerule.contains("<")) {
					rate2 = wholerule.substring(wholerule.lastIndexOf(",") + 1).trim();
					wholerule = wholerule.substring(0, wholerule.lastIndexOf(",")).trim();
				}
				// rate1
				rate1 = wholerule.substring(wholerule.lastIndexOf(" ") + 1).trim();
				rulename = wholerule.substring(0, wholerule.lastIndexOf(" ")).trim();
				
				// rulename
				data[i][0] = rulename;
				// rate1
				data[i][1] = rate1;
				// rate2
				data[i][2] = rate2;
			}
			
			// update table
			TableModel tm = new CMapTableModel(names, data);
			visviewer.updateAnnotationTable(tm);

		}
	}
	
	/**
	 * Called on the left click of an aggregate item.
	 * @param item
	 * @param event
	 */
	private void aggregateLeftClicked(VisualItem item, MouseEvent event) {
		// show details in the table in the annotation panel
		String[] names = {"Molecule", "Name", "UniProt", "HPRD", "Reactome", "UCSD-Nature", "InterPro", "PROSITE", "KEGG", "ChEBI", "PubChem"};
		if (!item.canGetString(AGG_CAT_LABEL)) {
			return;
		}
		String moleName = ((String) item.get(AGG_CAT_LABEL)).trim();
		Object[][] data = {{"Molecule", moleName, "UniProt", "HPRD", "Reactome", "UCSD-Nature", "InterPro", "PROSITE", "KEGG", "ChEBI", "PubChem"}};
		TableModel tm = new CMapTableModel(names, data);
		visviewer.updateAnnotationTable(tm);
	}
	
	
	/**
	 * Called when the user enters an item.
	 * 
	 */
	public void itemEntered(VisualItem item, MouseEvent event) {
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
	
	// add mouse listeners to update overview window
	
	public void mouseDragged(MouseEvent e) {
		visviewer.updateCMapSelectBox();
    }
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		visviewer.updateCMapSelectBox();
    }

	public void itemDragged(VisualItem item, MouseEvent e) {
		visviewer.updateCMapSelectBox();
	}

	public void itemMoved(VisualItem item, MouseEvent e) {
		visviewer.updateCMapSelectBox();
	}

	public void itemWheelMoved(VisualItem item, MouseWheelEvent e) {
		visviewer.updateCMapSelectBox();
	}
}
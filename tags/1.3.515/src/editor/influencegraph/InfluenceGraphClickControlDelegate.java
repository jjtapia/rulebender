package editor.influencegraph;

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

import link.LinkHub;
import link.LinkedViewsReceiverInterface;

import networkviewer.PrefuseTooltip;
import networkviewer.cmap.VisualRule;
import networkviewer.igraph.RuleTooltip;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.controls.ControlAdapter;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;
import prefuse.util.StrokeLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import resultviewer.graph.PngSaveFilter;
import visualizationviewer.VisualizationViewerController;

//import visualizationviewer.annotation.AnnotationTableModel;

public class InfluenceGraphClickControlDelegate extends ControlAdapter
		implements LinkedViewsReceiverInterface {

	private Visualization vis;

	// For the node tooltips.
	private PrefuseTooltip activeTooltip;

	private VisualizationViewerController visviewer;

	private String displaymode_activate = "Hide Activation";
	private String displaymode_inhibit = "Hide Inhibition";

	// if the current running color action is "highlightColor" action, then true
	private boolean isHighlightAction = false;

	public InfluenceGraphClickControlDelegate(Visualization v) {
		// Set the local reference to the visualization that this controller is
		// attached to.
		vis = v;

		visviewer = VisualizationViewerController
				.loadVisualizationViewController();

		LinkHub.getLinkHub().registerLinkedViewsListener(this);
	}

	/**
	 * Called when no VisualItem is hit.
	 */
	public void mouseClicked(MouseEvent e) {
		// super.mouseClicked(e);

		// right click
		if (e.getButton() == MouseEvent.BUTTON3
				|| (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown())) {
			JPopupMenu popupMenu = new JPopupMenu();
			// save as
			JMenuItem saveAsMenuItem = new JMenuItem("Save as...");
			popupMenu.add(saveAsMenuItem);

			// display mode - activation
			JMenuItem activateItem = new JMenuItem(displaymode_activate);
			popupMenu.add(activateItem);

			// display mode - inhibition
			JMenuItem inhibitItem = new JMenuItem(displaymode_inhibit);
			popupMenu.add(inhibitItem);

			// show menu
			popupMenu.show(e.getComponent(), e.getX(), e.getY());

			// add listener for save as
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

			// add listener for activation display mode
			activateItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AbstractButton aButton = (AbstractButton) e.getSource();

					Iterator iter = vis.items("igraph");

					// show states
					if (aButton.getText().equals("Show Activation")) {
						while (iter.hasNext()) {
							VisualItem item = (VisualItem) iter.next();
							// show activation edges
							if (item instanceof EdgeItem) {
								int activation = (Integer) item
										.get("activation");
								if (activation != -1) {
									item.setVisible(true);
								}
							}
						}
						displaymode_activate = "Hide Activation";

					} else {
						while (iter.hasNext()) {
							VisualItem item = (VisualItem) iter.next();
							// show activation edges
							if (item instanceof EdgeItem) {
								int activation = (Integer) item
										.get("activation");
								if (activation != -1) {
									item.setVisible(false);
								}
							}
						}
						displaymode_activate = "Show Activation";
					}

					// run actions
					if (isHighlightAction) {
						vis.run("transparentColor");
						vis.run("highlightColor");
					} else {
						vis.run("color");
					}
				}
			});

			// add listener for inhibition display mode
			inhibitItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AbstractButton aButton = (AbstractButton) e.getSource();

					Iterator iter = vis.items("igraph");

					// show states
					if (aButton.getText().equals("Show Inhibition")) {
						while (iter.hasNext()) {
							VisualItem item = (VisualItem) iter.next();
							// show activation edges
							if (item instanceof EdgeItem) {
								int activation = (Integer) item
										.get("inhibition");
								if (activation != -1) {
									item.setVisible(true);
								}
							}
						}
						displaymode_inhibit = "Hide Inhibition";

					} else {
						while (iter.hasNext()) {
							VisualItem item = (VisualItem) iter.next();
							// show activation edges
							if (item instanceof EdgeItem) {
								int activation = (Integer) item
										.get("inhibition");
								if (activation != -1) {
									item.setVisible(false);
								}
							}
						}
						displaymode_inhibit = "Show Inhibition";
					}

					// run actions
					if (isHighlightAction) {
						vis.run("transparentColor");
						vis.run("highlightColor");
					} else {
						vis.run("color");
					}
				}
			});
		}

		// Left Click
		else if (e.getButton() == MouseEvent.BUTTON1) {
			clearSelection();
			LinkHub.getLinkHub().clearSelectionFromInfluenceGraph();
		}
	}

	/**
	 * Run this to color all of the nodes and edges, and remove the annotation.
	 */
	public void clearSelection() {
		vis.getFocusGroup("selected").clear();

		vis.run("color");
		isHighlightAction = false;

		// empty annotation table
		// visviewer.updateAnnotationTable(null);
	}

	public void itemClicked(VisualItem item, MouseEvent event) {

		// Right Click
		if (event.getButton() == MouseEvent.BUTTON3
				|| (event.getButton() == MouseEvent.BUTTON1 && event
						.isControlDown())) {
			if (item instanceof NodeItem)
				showTooltip(new RuleTooltip((Display) event.getSource(),
						(String) item.get("rulename")), item, event);
		}
		// left click
		else if (event.getButton() == MouseEvent.BUTTON1) {
			if (item instanceof NodeItem) {
				setVisualItemAsSelected(item, true);

				/*
				 * // show tooltip showTooltip(new RuleTooltip((Display)
				 * event.getSource(), (String) item.get("rulename")), item,
				 * event);
				 */

				// highlight selected items and increase transparency for other
				// items
				vis.run("transparentColor");

				Iterator iter = vis.items();
				while (iter.hasNext()) {
					VisualItem cur = (VisualItem) iter.next();
					// not EdgeItem, return
					if (!(cur instanceof EdgeItem)) {
						continue;
					}
					// EdgeItem
					VisualItem src = ((EdgeItem) cur).getSourceItem();
					VisualItem tar = ((EdgeItem) cur).getTargetItem();
					if (src == item || tar == item) {
						cur.set("highlight", true);
					} else {
						cur.set("highlight", false);
					}
				}
				// set highlight action
				setHighlightAction("highlightColor");

				// run action
				vis.run("highlightColor");
				isHighlightAction = true;

				LinkHub.getLinkHub().ruleSelectedInInfluenceGraph(item);
			}
		}
	}

	/**
	 * Called when the user enters an item. Show the label of the nodes.
	 */
	public void itemEntered(VisualItem item, MouseEvent event) {
		if (item instanceof NodeItem)
			showTooltip(new RuleTooltip((Display) event.getSource(),
					(String) item.get(VisualItem.LABEL)), item, event);
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

		activeTooltip.startShowing((int) e.getX() + 10, (int) e.getY() + 30);
	}

	private void setHighlightAction(String actionName) {
		int hightlight_alpha = 255;
		int alpha = 25;
		ColorAction edgeStroke = new ColorAction("igraph.edges",
				VisualItem.STROKECOLOR, ColorLib.rgba(105, 105, 105, alpha));

		// hightlight color
		Predicate highlight_activate1 = (Predicate) ExpressionParser
				.parse("activation == 1 && highlight == true");
		edgeStroke.add(highlight_activate1,
				ColorLib.rgba(77, 146, 33, hightlight_alpha));
		Predicate highlight_activate0 = (Predicate) ExpressionParser
				.parse("activation == 0 && highlight == true");
		edgeStroke.add(highlight_activate0,
				ColorLib.rgba(77, 146, 33, hightlight_alpha));
		Predicate highlight_inhibit1 = (Predicate) ExpressionParser
				.parse("inhibition == 1 && highlight == true");
		edgeStroke.add(highlight_inhibit1,
				ColorLib.rgba(197, 27, 125, hightlight_alpha));
		Predicate highlight_inhibitn0 = (Predicate) ExpressionParser
				.parse("inhibition == 0 && highlight == true");
		edgeStroke.add(highlight_inhibitn0,
				ColorLib.rgba(197, 27, 125, hightlight_alpha));

		// not highlight color
		Predicate activate1 = (Predicate) ExpressionParser
				.parse("activation == 1 && highlight == false");
		edgeStroke.add(activate1, ColorLib.rgba(77, 146, 33, alpha));
		Predicate activate0 = (Predicate) ExpressionParser
				.parse("activation == 0 && highlight == false");
		edgeStroke.add(activate0, ColorLib.rgba(77, 146, 33, alpha));
		Predicate inhibit1 = (Predicate) ExpressionParser
				.parse("inhibition == 1 && highlight == false");
		edgeStroke.add(inhibit1, ColorLib.rgba(197, 27, 125, alpha));
		Predicate inhibitn0 = (Predicate) ExpressionParser
				.parse("inhibition == 0 && highlight == false");
		edgeStroke.add(inhibitn0, ColorLib.rgba(197, 27, 125, alpha));

		// edge fill color, for arrows
		ColorAction edgeFill = new ColorAction("igraph.edges",
				VisualItem.FILLCOLOR, ColorLib.rgba(105, 105, 105, alpha));
		// hightlight arrow color
		edgeFill.add(highlight_activate1,
				ColorLib.rgba(77, 146, 33, hightlight_alpha));
		edgeFill.add(highlight_activate0,
				ColorLib.rgba(77, 146, 33, hightlight_alpha));
		edgeFill.add(highlight_inhibit1,
				ColorLib.rgba(197, 27, 125, hightlight_alpha));
		edgeFill.add(highlight_inhibitn0,
				ColorLib.rgba(197, 27, 125, hightlight_alpha));

		// not highlight arrow color
		edgeFill.add(activate1, ColorLib.rgba(77, 146, 33, alpha));
		edgeFill.add(activate0, ColorLib.rgba(77, 146, 33, alpha));
		edgeFill.add(inhibit1, ColorLib.rgba(197, 27, 125, alpha));
		edgeFill.add(inhibitn0, ColorLib.rgba(197, 27, 125, alpha));

		// highlight stroke
		StrokeAction edgeStrokea = new StrokeAction("igraph.edges",
				StrokeLib.getStroke(3.0f));

		edgeStrokea.add(highlight_activate1, StrokeLib.getStroke(5.0f));
		edgeStrokea.add(highlight_inhibit1, StrokeLib.getStroke(5.0f));

		float dashes[] = { 5.0f, 10.0f };

		// highlight dash
		Predicate highlight_activate0dashes = (Predicate) ExpressionParser
				.parse("activation == 0 && highlight == true");
		edgeStrokea.add(highlight_activate0dashes,
				StrokeLib.getStroke(5.0f, dashes));
		Predicate highlight_inhibitn0dashes = (Predicate) ExpressionParser
				.parse("inhibition == 0 && highlight == true");
		edgeStrokea.add(highlight_inhibitn0dashes,
				StrokeLib.getStroke(5.0f, dashes));

		// not highlight dash

		Predicate activate0dashes = (Predicate) ExpressionParser
				.parse("activation == 0 && highlight == false");
		edgeStrokea.add(activate0dashes, StrokeLib.getStroke(3.0f, dashes));
		Predicate inhibitn0dashes = (Predicate) ExpressionParser
				.parse("inhibition == 0 && highlight == false");
		edgeStrokea.add(inhibitn0dashes, StrokeLib.getStroke(3.0f, dashes));

		// set actionlist
		ActionList highlightColor = new ActionList();
		highlightColor.add(edgeStroke);
		highlightColor.add(edgeStrokea);
		highlightColor.add(edgeFill);
		highlightColor.add(new RepaintAction());
		vis.putAction(actionName, highlightColor);
	}

	public void mouseDragged(MouseEvent e) {
		visviewer.updateIGraphSelectBox();
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		visviewer.updateIGraphSelectBox();
	}

	public void itemDragged(VisualItem item, MouseEvent e) {
		visviewer.updateIGraphSelectBox();
	}

	public void itemMoved(VisualItem item, MouseEvent e) {
		visviewer.updateIGraphSelectBox();
	}

	public void itemWheelMoved(VisualItem item, MouseWheelEvent e) {
		visviewer.updateIGraphSelectBox();
	}

	/**
	 * For setting a VisualItem as the member of the selected group.
	 * 
	 * @param item
	 */
	private void setVisualItemAsSelected(VisualItem item, boolean single) {
		TupleSet focused = vis.getFocusGroup("selected");

		if (single)
			focused.clear();

		focused.addTuple(item);

		vis.run("color");
	}

	/**
	 * required for RuleSelectedReceiverInterface.
	 * 
	 * 
	 * @see link.LinkedViewsReceiverInterface#ruleSelected(java.lang.String)
	 */
	public void ruleSelectedInContactMap(VisualRule rule) {
		// Text <-> contact map is a straight forward representation, There are
		// no rules in the contact map that do not directly appear
		// in the text and vice-versa. For all concerns, the contact map and
		// text can be thought of as having the same set of textual rules.

		// Text/Contact Map -> Influence Graph: Rules will only be forward or
		// reverse. In either case they could actually be from a bidirectional
		// rule.
		// r <-> p
		// and they could be represented elsewhere as
		// r -> p
		// for the forward reaction, and
		// p -> r
		// for the reverse

		// So when a rule is selected in the contact map, if it is bidirectional
		// then we need to look for r -> p or p->r
		// in the influence graph.
		// If it is unidirectional then we need to look for only the r->p.

		// TODO There could be multiple rules in the influence graph for each
		// rule in the contact map, so we need to be able to select multiple
		// rules in the influence graph.

		// DEBUG
		// System.out.println("\n\nInfluence Graph RuleSelected:\nTrying to find edges attached to rule "
		// + ruleText);

		// Remove the rates.
		String ruleText = rule.getName();

		selectRuleFromText(ruleText, true);
	}

	private void selectRuleFromText(String ruleText, boolean reset) {
		if (reset)
			clearSelection();

		ruleText = ruleText.substring(0, ruleText.lastIndexOf(")") + 1);

		// Set the forward rule and remove the white space
		String forward = ruleText.replace(" ", "");
		forward = forward.replace("<->", "->");

		String reverse = forward.substring(forward.indexOf('>') + 1,
				forward.length())
				+ "->" + forward.substring(0, forward.indexOf('-'));

		// Get the visual object(s (forward and backward)) for the rule
		// set it as part of the highlighted group
		// redraw
		Iterator iter = vis.items();

		while (iter.hasNext()) {
			VisualItem cur = (VisualItem) iter.next();
			// not EdgeItem, return
			if (!(cur instanceof EdgeItem)) {
				if (cur instanceof NodeItem) {
					String nodeName = ((String) cur.get("rulename")).replace(
							" ", "");
					;
					nodeName = nodeName.substring(0,
							nodeName.lastIndexOf(")") + 1);

					if (nodeName.equals(forward) || nodeName.equals(reverse))
						setVisualItemAsSelected(cur, false);
				}

				continue;
			}

			// EdgeItem
			VisualItem src = ((EdgeItem) cur).getSourceItem();
			VisualItem tar = ((EdgeItem) cur).getTargetItem();

			String srcName = ((String) src.get("rulename")).replace(" ", "");
			;
			srcName = srcName.substring(0, srcName.lastIndexOf(")") + 1);

			String tarName = ((String) tar.get("rulename")).replace(" ", "");
			tarName = tarName.substring(0, tarName.lastIndexOf(")") + 1);

			if (tarName.equals(forward) || srcName.equals(forward)
					|| tarName.equals(reverse) || srcName.equals(reverse)) {
				cur.set("highlight", true);
				// System.out.print("X");
			} else {
				cur.set("highlight", false);
				// System.out.print(".");
			}
		}

		// System.out.println();
		// set highlight action
		setHighlightAction("highlightColor");

		// highlight selected items and increase transparency for other
		// items
		vis.run("transparentColor");

		// run action
		vis.run("highlightColor");
		isHighlightAction = true;
	}

	// Called from the linkhub when a rule is deselected in the contact map.
	public void clearSelectionFromContactMap() {
		clearSelection();
	}

	public void clearSelectionFromInfluenceGraph() {
		// Handled Locally.
	}

	// TODO Eventually we should highlight the rules that have
	// the given molecule as a reactant or product, but for now
	// we will just clear the selection.
	public void moleculeSelectedInContactMap(VisualItem molecule) {
		clearSelection();
	}

	public void ruleSelectedInInfluenceGraph(VisualItem ruleItem) {
		// handled locally.
	}

	// TODO Eventually we should highlight the rules that have
	// the given molecule as a reactant or product, but for now
	// we will just clear the selection.
	public void componentSelectedInContactMap(VisualItem moleculeItem) {
		// TODO Auto-generated method stub
	}

	public void edgeSelectedInContactMap(VisualItem edge) {
		clearSelection();

		ArrayList rules = (ArrayList<VisualRule>) edge.get("rules");

		// TODO Hub edges do not have rules. I should get the source nodes
		// and see if they have rules.
		// there is a "type" value of "hub" for hub nodes.
		// and a "rules" arraylist.
		if (rules == null)
			return;

		Iterator<VisualRule> ruleIt = rules.iterator();

		while (ruleIt.hasNext()) {
			selectRuleFromText(ruleIt.next().getName(), false);
		}
	}

	public void stateSelectedInContactMap(VisualItem stateItem) {
		// TODO If there are rules associated with the state, select all of the
		// rule nodes.

	}

	public void hubSelectedInContactMap(VisualItem hubItem) {
		// TODO If there are rules associated with the hub,
		// highlight the rule nodes.
	}

	public void moleculeSelectedInText(String moleculeText) {
		// TODO Auto-generated method stub
	}

	public void ruleSelectedInText(String ruleText) {
		selectRuleFromText(ruleText, true);
	}

	public void compartmentSelectedInContactMap(VisualItem compartment) {
		// TODO Auto-generated method stub

	}

	public void clearSelectionFromText() {
		// TODO Auto-generated method stub

	}

	public void componentSelectedInText(String componentText) {
		// TODO Auto-generated method stub

	}

	public void stateSelectedInText(String stateText) {
		// TODO Auto-generated method stub

	}

	public void compartmentSelectedInText(VisualItem compartment) {
		// TODO Auto-generated method stub

	}
}
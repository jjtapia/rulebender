package editor.influencegraph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.table.TableModel;

import networkviewer.PrefuseTooltip;
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
import prefuse.util.ColorLib;
import prefuse.util.StrokeLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import resultviewer.graph.PngSaveFilter;
import visualizationviewer.VisualizationViewerController;

public class InfluenceGraphClickControlDelegate extends ControlAdapter {

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
		
		visviewer = VisualizationViewerController.loadVisualizationViewController();
	}

	/**
	 * Called when no VisualItem is hit.
	 */
	public void mouseClicked(MouseEvent e) {
		// super.mouseClicked(e);

		// right click
		if (e.getButton() == MouseEvent.BUTTON3 || (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown())) {
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
								int activation = (Integer)item.get("activation");
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
								int activation = (Integer)item.get("activation");
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
					}
					else {
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
								int activation = (Integer)item.get("inhibition");
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
								int activation = (Integer)item.get("inhibition");
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
					}
					else {
						vis.run("color");
					}
				}
			});
		}
		

		// Left Click
		else if (e.getButton() == MouseEvent.BUTTON1) {
			if (!(e.getSource() instanceof VisualItem)) {
				vis.run("color");
				isHighlightAction = false;
			}
			
			// empty annotation table
			visviewer.updateAnnotationTable(null);
		}

	}

	public void itemClicked(VisualItem item, MouseEvent event) 
	{
		// Right Click
		if (event.getButton() == MouseEvent.BUTTON3 || (event.getButton() == MouseEvent.BUTTON1 && event.isControlDown())) {
			if (item instanceof NodeItem)
				showTooltip(new RuleTooltip((Display) event.getSource(),
						(String) item.get("rulename")), item, event);
		}
		// left click
		else if (event.getButton() == MouseEvent.BUTTON1) {
			if (item instanceof NodeItem) {
				/*
				// show tooltip
				showTooltip(new RuleTooltip((Display) event.getSource(),
						(String) item.get("rulename")), item, event);
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
				
				// show details in the table in the annotation panel
				String[] names = {"ID", "Rule", "Rate", "Constraints"};
				Object[][] data = new Object[1][];
				
				data[0] = new Object[names.length];

				// whole rule
				// A() + B() <-> C() rate1, rate2
				String wholerule = (String) item.get("rulename");
				
				String rulename = "", rate1 = "", constraints = "";

				// constraints
				if (wholerule.indexOf("exclude")!=-1 || wholerule.indexOf("exclude")!=-1) {
					constraints = wholerule.substring(
							wholerule.lastIndexOf(" ") + 1).trim();
					wholerule = wholerule.substring(0,
							wholerule.lastIndexOf(" ")).trim();
				}
				
				//rate1
				// some model don't have )
				// some model don't have space
				// the model don't have ) should have space
				int index_rightP = wholerule.lastIndexOf(")");
				// last right parenthesis on the right half and belongs to the last molecule
				if (index_rightP != -1 && index_rightP > wholerule.lastIndexOf(">") && index_rightP > wholerule.lastIndexOf("+")) {
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
				
				//ID
				data[0][0] = item.getString(VisualItem.LABEL);
				// rulename
				data[0][1] = rulename;
				// rate1
				data[0][2] = rate1;
				
				// constraints
				data[0][3] = constraints;
				
				
				// update table
				TableModel tm = new IMapTableModel(names, data);
				visviewer.updateAnnotationTable(tm);
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
				VisualItem.STROKECOLOR, ColorLib.rgba(105, 105, 105,
						alpha));

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
				VisualItem.FILLCOLOR, ColorLib.rgba(105, 105, 105,
						alpha));
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
		edgeStrokea.add(activate0dashes,
				StrokeLib.getStroke(3.0f, dashes));
		Predicate inhibitn0dashes = (Predicate) ExpressionParser
				.parse("inhibition == 0 && highlight == false");
		edgeStrokea.add(inhibitn0dashes,
				StrokeLib.getStroke(3.0f, dashes));

		// set actionlist
		ActionList highlightColor = new ActionList();
		highlightColor.add(edgeStroke);
		highlightColor.add(edgeStrokea);
		highlightColor.add(edgeFill);
		highlightColor.add(new RepaintAction());
		vis.putAction(actionName, highlightColor);
	}
	
	// add mouse listeners to update overview window
	
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
	
	
}
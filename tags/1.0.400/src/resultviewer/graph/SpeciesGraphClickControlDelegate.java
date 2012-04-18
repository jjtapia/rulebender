package resultviewer.graph;

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

import editor.contactmap.CMapTableModel;

import networkviewer.PrefuseTooltip;
import networkviewer.cmap.EnterColorAction;

import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.controls.ControlAdapter;
import prefuse.util.ColorLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import visualizationviewer.VisualizationViewerController;

public class SpeciesGraphClickControlDelegate extends ControlAdapter {

	private Visualization vis;

	// For the node tooltips.
	PrefuseTooltip activeTooltip;

	// store the current display mode, "Show States" or "Hide States"
	private String displaymode = "Hide States";

	private VisualizationViewerController visviewer;

	public SpeciesGraphClickControlDelegate(Visualization v) {
		this.vis = v;

		visviewer = VisualizationViewerController
				.loadVisualizationViewController();
	}

	/**
	 * Called when no VisualItem is hit and right click.
	 */
	public void mouseClicked(MouseEvent e) 
	{
		// Right Click
		if (e.getButton() == MouseEvent.BUTTON3 || (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown())) {
			
			JPopupMenu popupMenu = new JPopupMenu();
			// save as
			JMenuItem saveAsMenuItem = new JMenuItem("Save as...");
			popupMenu.add(saveAsMenuItem);

			// display mode
			JMenuItem displaymodeMenuItem = new JMenuItem(displaymode);
			popupMenu.add(displaymodeMenuItem);

			popupMenu.show(e.getComponent(), e.getX(), e.getY());

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
			displaymodeMenuItem.addActionListener(new ActionListener() {

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
						displaymode = "Hide States";

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
						displaymode = "Show States";
					}

					// apply actions
					vis.run("color");
					vis.run("complayout");

				}
			});
		}

		else if (e.getButton() == MouseEvent.BUTTON1) {
			// left click
			//System.out.println("Click: (" + e.getX() + ", " + e.getY() + ")");

			// empty annotation table
			visviewer.updateAnnotationTable(null);
		}

	}

	/**
	 * Called when VisualItem is hit.
	 */
	public void itemClicked(VisualItem item, MouseEvent event) {
		// left click
		if (event.getButton() == MouseEvent.BUTTON1) {

			if (!(item instanceof NodeItem)) {
				return;
			}

			String type = item.getString("type");

			if (type != null && type.equals("component")) {

				String states = "[";
				ArrayList<String> stateList = (ArrayList) item.get("states");
				if (stateList != null) {
					for (int i = 0; i < stateList.size() - 1; i++) {
						states = states + stateList.get(i) + ", ";
					}
					states = states + stateList.get(stateList.size() - 1);
				}

				states += "]";

				// show details in the table in the annotation panel
				String[] names = { "Type", "Name", "Molecule", "States" };
				Object[][] data = { { "Component",
						(String) item.get(VisualItem.LABEL),
						item.getString("molecule"), states } };
				TableModel tm = new CMapTableModel(names, data);
				visviewer.updateAnnotationTable(tm);
			} else if (type.equals("state")) {
				// show details in the table in the annotation panel
				String[] names = { "Type", "Name", "Molecule", "Component" };
				Object[][] data = { { "State",
						(String) item.get(VisualItem.LABEL),
						item.getString("molecule"), item.getString("component") } };
				TableModel tm = new CMapTableModel(names, data);
				visviewer.updateAnnotationTable(tm);
			}

		}
	}

	/**
	 * Called when mouse entered VisualItem
	 */
	public void itemEntered(VisualItem item, MouseEvent event) {

		ActionList color = new ActionList();
		// aggregate stroke color, edge stroke color, highlight color
		int[] palette = { ColorLib.rgb(10, 10, 10),
				ColorLib.rgb(105, 105, 105), ColorLib.rgb(230, 10, 10) };
		EnterColorAction aStrokeColor = new EnterColorAction("aggregates",
				VisualItem.STROKECOLOR, palette, item);
		EnterColorAction nStrokeColor = new EnterColorAction(
				"component_graph.nodes", VisualItem.STROKECOLOR, palette, item);
		EnterColorAction eStrokeColor = new EnterColorAction(
				"component_graph.edges", VisualItem.STROKECOLOR, palette, item);

		color.add(aStrokeColor);
		color.add(nStrokeColor);
		color.add(eStrokeColor);
		color.add(new RepaintAction());
		vis.putAction("entercolor", color);
		vis.run("entercolor");
	}

	/**
	 * Called when mouse exited VisualItem
	 */
	public void itemExited(VisualItem item, MouseEvent event) {
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
		visviewer.updateSpeciesBrowserSelectBox();
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		visviewer.updateSpeciesBrowserSelectBox();
	}

	public void itemDragged(VisualItem item, MouseEvent e) {
		visviewer.updateSpeciesBrowserSelectBox();
	}

	public void itemMoved(VisualItem item, MouseEvent e) {
		visviewer.updateSpeciesBrowserSelectBox();
	}

	public void itemWheelMoved(VisualItem item, MouseWheelEvent e) {
		visviewer.updateSpeciesBrowserSelectBox();
	}
}
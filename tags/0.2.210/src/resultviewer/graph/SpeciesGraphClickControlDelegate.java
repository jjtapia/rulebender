package resultviewer.graph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.visual.AggregateItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;



public class SpeciesGraphClickControlDelegate extends ControlAdapter {

	private Visualization vis;
	
	public SpeciesGraphClickControlDelegate(Visualization v) {
		this.vis = v;
	}
	/**
	 * Called when no VisualItem is hit.
	 */
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			// left click
			System.out.println("Click: (" + e.getX() + ", " + e.getY() + ")");
		}

		// Right Click
		else if (e.getButton() == MouseEvent.BUTTON3) {
			System.out.println("Right Clicked");

			JPopupMenu popupMenu = new JPopupMenu();
			// save as
			JMenuItem saveAsMenuItem = new JMenuItem("Save as...");
			popupMenu.add(saveAsMenuItem);
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
			
			saveAsMenuItem.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					// file filter
					chooser.addChoosableFileFilter(new PngSaveFilter());				    
				    
					int option = chooser.showSaveDialog(null);
					if (option == JFileChooser.APPROVE_OPTION) {
						if(chooser.getSelectedFile()!=null){  
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
		}
	}

	public void itemClicked(VisualItem item, MouseEvent e) {
		// left click
		if (e.getButton() == MouseEvent.BUTTON1) {
			if ((item instanceof NodeItem)) {
				System.out.println("You left clicked node "
						+ item.getString(VisualItem.LABEL) + " at position ("
						+ item.getX() + ", " + item.getY() + ")");
			} else if (item instanceof EdgeItem) {
				System.out.println("Click: (" + e.getX() + ", " + e.getY()
						+ ")" + " edge ");
			} else if (item instanceof AggregateItem) {
				System.out.println("Click: (" + e.getX() + ", " + e.getY()
						+ ")");

				System.out.println("Aggregate should contain:");

				Iterator it = ((AggregateItem) item).items();
				while (it.hasNext()) {
					VisualItem vt = (VisualItem) it.next();
					// DEBUG
					if (vt instanceof EdgeItem)
						System.out.println("\tItem (" + vt.getX() + ", "
								+ vt.getY() + ") is Edge " + vt.get("id"));
					else if (vt instanceof NodeItem)
						System.out.println("\tItem (" + vt.getX() + ", "
								+ vt.getY() + ") is Node "
								+ vt.getString(VisualItem.LABEL));

				}

			} else {
				System.out.println("Click: (" + e.getX() + ", " + e.getY()
						+ ")");
			}

		}
	}
}
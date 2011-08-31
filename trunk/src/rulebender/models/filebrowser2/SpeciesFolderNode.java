package rulebender.models.filebrowser2;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * SpeciesFolder Node. Contains SpeciesNode children.
 * 
 */
public class SpeciesFolderNode extends TreeNode {
	// name of species folder
	private String cFolder;
	// string of components
	private ArrayList<String> components;

	/**
	 * 
	 * @param folder
	 *            name of species folder
	 * @param components
	 *            list of string to create children
	 */
	public SpeciesFolderNode(String folder, ArrayList<String> components) {
		this(null, folder, components);
	}

	/**
	 * 
	 * @param parent
	 *            parent node
	 * @param folder
	 *            name of species folder
	 * @param components
	 *            list of string to create children
	 */
	public SpeciesFolderNode(ITreeNode parent, String folder,
			ArrayList<String> components) {
		super("SpeciesFolderNode", parent);
		this.cFolder = folder;
		this.components = components;
	}

	/**
	 * @return name of species folder
	 */
	public String getName() {
		return cFolder;
	}

	/**
	 * 
	 * @return list of string to create children
	 */
	public ArrayList<String> getComponents() {
		return this.components;
	}

	/**
	 * @return image
	 */
	public Image getImage() {
		return null;
	}

	/**
	 * create children (Species Node)
	 */
	protected void createChildren(List children) {
		for (int i = 0; i < components.size(); i++) {
			children.add(new SpeciesNode(this, components.get(i)));
		}
	}
}

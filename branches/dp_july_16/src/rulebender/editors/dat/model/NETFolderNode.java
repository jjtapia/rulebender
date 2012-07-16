package rulebender.editors.dat.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

/**
 * 
 * NET file folder node. 
 * Contains NETItemNode children.
 * 
 */
public class NETFolderNode extends TreeNode {
	private String cFolder;
	private ArrayList<String> components;

	/**
	 * 
	 * @param folder
	 *            name of folder
	 * @param components
	 *            list of components
	 */
	public NETFolderNode(String folder, ArrayList<String> components) {
		this(null, folder, components);
	}

	/**
	 * 
	 * @param parent
	 *            parent node
	 * @param folder
	 *            name of folder
	 * @param components
	 *            list of components
	 */
	public NETFolderNode(ITreeNode parent, String folder,
			ArrayList<String> components) {
		super("NETFolderNode", parent);
		this.cFolder = folder;
		this.components = components;
	}

	/**
	 * @return name of the folder
	 */
	public String getName() {
		return cFolder;
	}

	/**
	 * 
	 * @return list of components
	 */
	public ArrayList<String> getComponents() {
		return this.components;
	}

	/**
	 * @return folder image
	 */
	public Image getImage() {
		return null;
	}

	/**
	 * create children nodes based on a list of children components
	 */
	protected void createChildren(List children) {
		for (int i = 0; i < components.size(); i++) {
			children.add(new NETItemNode(this, components.get(i)));
		}
	}
}

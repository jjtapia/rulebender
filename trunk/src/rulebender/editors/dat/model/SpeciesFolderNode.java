package rulebender.editors.dat.model;

import java.util.List;

import org.eclipse.swt.graphics.Image;

/**
 * 
 * SpeciesFolder Node. Contains SpeciesNode children.
 * 
 */
public class SpeciesFolderNode extends TreeNode {
	// name of species folder
	private final String cFolder;
	// string of components
	private final List<String> components;
	// string of components
	private List<String> componentsID = null;

	/**
	 * 
	 * @param folder
	 *          name of species folder
	 * @param components
	 *          list of string to create children
	 */
	public SpeciesFolderNode(String folder, List<String> components) {
		this(null, folder, components);
	}

	/**
	 * 
	 * @param parent
	 *          parent node
	 * @param folder
	 *          name of species folder
	 * @param components
	 *          list of string to create children
	 */
	public SpeciesFolderNode(ITreeNode parent, String folder,
	    List<String> components) {
		super("SpeciesFolderNode", parent);
		this.cFolder = folder;
		this.components = components;
	}

	/**
	 * @return name of species folder
	 */
	@Override
	public String getName() {
		return cFolder;
	}

	/**
	 * 
	 * @return list of string to create children
	 */
	public List<String> getComponents() {
		return this.components;
	}

	/**
	 * @return image
	 */
	@Override
	public Image getImage() {
		return null;
	}

	/**
	 * Gets the components id.
	 * 
	 * @return the components id
	 */
	public List<String> getComponentsID() {
		return componentsID;
	}

	/**
	 * Sets the components id.
	 * 
	 * @param componentsID
	 *          the new components id
	 */
	public void setComponentsID(List<String> componentsID) {
		this.componentsID = componentsID;
	}

	/**
	 * create children (Species Node)
	 */
	@Override
	protected void createChildren(List children) {
		for (int i = 0; i < components.size(); i++) {
			if (componentsID != null && componentsID.size() > i) {
				children.add(new SpeciesNode(this, components.get(i), componentsID
				    .get(i)));
			} else {
				children
				    .add(new SpeciesNode(this, components.get(i), components.get(i)));
			}
		}
	}
}

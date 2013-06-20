package rulebender.editors.dat.model;

import java.util.List;

import org.eclipse.swt.graphics.Image;

/**
 * 
 * Species Node
 * 
 */
public class SpeciesNode extends TreeNode {
	private final String id;
	// species name (id expression)
	private final String name;
	// species expression
	private String expression = "";

	/**
	 * 
	 * @param parent
	 *          parent node
	 * @param name
	 *          species name (id expression)
	 */
	public SpeciesNode(ITreeNode parent, String name, String id) {
		super("SpeciesNode", parent);
		this.id = id;
		this.name = name;
		this.expression = name;
	}

	/**
	 * @return species name (id expression)
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * 
	 * @return species expression
	 */
	public String getExpression() {
		return this.expression;
	}

	/**
	 * @return image
	 */
	@Override
	public Image getImage() {
		return null;
	}

	/**
	 * Empty.
	 */
	@Override
	protected void createChildren(List children) {
	}

	/**
	 * Whether has children.
	 * 
	 * @return false
	 */
	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public String toString() {
		return this.getName();
	}

}

package rulebender.editors.dat.model;

import java.util.List;

import org.eclipse.swt.graphics.Image;

/**
 * 
 * Observable node. Contains one SpeciesFolderNode child.
 * 
 */
public class ObservableNode extends TreeNode {
	// observable name
	private String name = "";
	// observable expression
	private String expression = "";
	private List<String> patterns;
	// list of components string
	private final List<String> component;

	/**
	 * 
	 * @param parent
	 *          parent node
	 * @param name
	 *          information to create node (id type name expression)
	 * @param component
	 *          information to create children
	 */
	public ObservableNode(ITreeNode parent, String name, List<String> patterns,
	    List<String> component) {
		super("ObservableNode", parent);
		this.name = name;

		// info contains: id type name expression
		// String[] tmp = name.trim().split(" ");
		// if (tmp.length == 1) {
		// this.name = tmp[0];
		// } else if (tmp.length >= 4) {
		// this.name = tmp[2];
		// this.expression = tmp[3];
		// for (int i = 4; i < tmp.length; i++) {
		// this.expression += " " + tmp[4];
		// }
		// }

		expression = patterns.get(0);

		for (int i = 1; i < patterns.size(); i++) {
			expression += " " + patterns.get(i);
		}

		this.component = component;
	}

	/**
	 * @return observable name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return observable expression
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
	 * create SpeciesFolderNode children
	 */
	@Override
	protected void createChildren(List children) {
		if (component != null) {
			children.add(new SpeciesFolderNode(this, expression, component));
		}
	}

	/**
	 * whether has children
	 * 
	 * @return true
	 */
	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public String toString() {
		return this.getName();
	}

}

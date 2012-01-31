package rulebender.navigator.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.graphics.Image;

/**
 * 
 * Observable node. Contains one SpeciesFolderNode child.
 * 
 */
public class ObservableNode extends TreeNode {
	// observable id
	private int id;
	// observable type
	private String type = "";
	// observable name
	private String name = "";
	// observable expression
	private String expression = "";
	// list of components string
	private ArrayList<String> component;

	/**
	 * 
	 * @param parent
	 *            parent node
	 * @param info
	 *            information to create node (id type name expression)
	 * @param component
	 *            information to create children
	 */
	public ObservableNode(ITreeNode parent, String info,
			ArrayList<String> component) {
		super("ObservableNode", parent);
		// info contains: id type name expression
		String[] tmp = info.trim().split(" ");
		if (tmp.length == 1) {
			this.name = tmp[0];
		} else if (tmp.length >= 4) {
			this.id = Integer.parseInt(tmp[0]);
			this.type = tmp[1];
			this.name = tmp[2];
			this.expression = tmp[3];
			for (int i = 4; i < tmp.length; i++) {
				this.expression += " " + tmp[4];
			}
		}

		this.component = component;
	}

	/**
	 * 
	 * @return observable id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * 
	 * @return observable type
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * @return observable name
	 */
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
	public Image getImage() {
		return null;
	}

	/**
	 * create SpeciesFolderNode children
	 */
	protected void createChildren(List children) {
		if (component != null)
			children.add(new SpeciesFolderNode(this, expression, component));
	}

	/**
	 * whether has children
	 * 
	 * @return true
	 */
	public boolean hasChildren() {
		return true;
	}

	public String toString() {
		return this.getName();
	}

}

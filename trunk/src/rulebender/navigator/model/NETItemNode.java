package rulebender.navigator.model;

import java.util.List;
import org.eclipse.swt.graphics.Image;

import prefuse.Display;

/**
 * 
 * NET item node (block label)
 * 
 */
public class NETItemNode extends TreeNode {
	private String name; // name of block
	private int offset; // offset of the block label in whole content
	private int length; // length of the block label

	/**
	 * 
	 * @param parent
	 *            parent node
	 * @param item
	 *            string of item, contains "name"\t"offset"\t"length"
	 */
	public NETItemNode(ITreeNode parent, String item) {
		super("NETItemNode", parent);
		String[] tmp = item.split("\t");
		this.name = tmp[0];
		this.offset = Integer.parseInt(tmp[1]);
		this.length = Integer.parseInt(tmp[2]);
	}

	/**
	 * @return name of the block
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return offset of block label in whole content
	 */
	public int getOffset() {
		return this.offset;
	}

	/**
	 * 
	 * @return length of block label
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * @return image
	 */
	public Image getImage() {
		return null;
	}

	/**
	 * Empty
	 */
	protected void createChildren(List children) {
	}

	/**
	 * No Children.
	 * 
	 * @return false
	 */
	public boolean hasChildren() {
		return false;
	}

	public String toString() {
		return this.getName();
	}

}

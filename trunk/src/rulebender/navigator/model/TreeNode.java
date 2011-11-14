package rulebender.navigator.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

/**
 * Default or adapter implementation for ITreeNode
 */
public abstract class TreeNode implements ITreeNode {
	// parent node
	protected ITreeNode fParent;
	// list of children
	protected List fChildren;
	// type of this node
	protected String nodeType;

	/**
	 * 
	 * @param nodeType
	 *            type of node
	 * @param parent
	 *            parent node
	 */
	public TreeNode(String nodeType, ITreeNode parent) {
		this.nodeType = nodeType;
		fParent = parent;
	}

	/**
	 * @return image of the node
	 */
	public Image getImage() {
		return null;
	}

	/**
	 * @return whether has children
	 */
	public boolean hasChildren() {
		return true;
	}

	/**
	 * @return parent node
	 */
	public ITreeNode getParent() {
		return fParent;
	}

	/**
	 * @return list of children nodes
	 */
	public List getChildren() {
		if (fChildren != null) {
			return fChildren;
		}

		fChildren = new ArrayList();
		createChildren(fChildren);
		return fChildren;
	}

	/**
	 * 
	 * @return type of node
	 */
	public String getNodeType() {
		return this.nodeType;
	}

	/**
	 * 
	 * @return array of children nodes
	 */
	public Object[] getChildrenArray() {
		if (fChildren == null)
			return null;

		else {
			Object[] childrenArray = new Object[fChildren.size()];
			for (int i = 0; i < fChildren.size(); i++) {
				childrenArray[i] = (Object) fChildren.get(i);
			}
			return childrenArray;
		}
	}

	/**
	 * Add a child node.
	 * 
	 * @param node
	 *            child node
	 */
	public void addChild(ITreeNode node) {
		if (fChildren == null) {
			fChildren = new ArrayList();
		}
		fChildren.add(node);
	}

	/**
	 * Remove a child node.
	 * 
	 * @param node
	 *            child node
	 */
	public void removeChild(ITreeNode node) {
		if (fChildren != null) {
			fChildren.remove(node);
		}
	}

	/**
	 * subclasses should override this method and add the child nodes
	 * 
	 * @param children
	 *            list of children nodes
	 */
	protected abstract void createChildren(List children);

}

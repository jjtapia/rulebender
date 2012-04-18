package resultviewer.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

/* Default or adapter implementation for ITreeNode */
public abstract class TreeNode implements ITreeNode
{
	protected ITreeNode fParent;
	protected List fChildren;
	protected String nodeType;
		
	public TreeNode(String nodeType, ITreeNode parent) {
		this.nodeType = nodeType;
		fParent = parent;
	}
	
	public Image getImage() {
		return null; /* TODO */
	}
	
	public boolean hasChildren() {		
		return true;
	}
 
	public ITreeNode getParent() {		
		return fParent;
	}
	
	public List getChildren() 
	{
		if( fChildren != null ) {		
			return fChildren;
		}
		
		fChildren = new ArrayList();
		createChildren(fChildren);	
		return fChildren;
	}
	
	public String getNodeType() {
		return this.nodeType;
	}
	
	public Object[] getChildrenArray() {
		if (fChildren == null)
			return null;
		
		else {
			Object[] childrenArray = new Object[fChildren.size()];
			for (int i = 0; i < fChildren.size(); i++) {
				childrenArray[i] = (Object)fChildren.get(i);
			}
			return childrenArray;
		}
	}
	
	/* subclasses should override this method and add the child nodes */
	protected abstract void createChildren(List children);

}


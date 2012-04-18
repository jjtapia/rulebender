package resultviewer.tree;

import java.util.List;
import org.eclipse.swt.graphics.Image;

import prefuse.Display;

/* Species node */
public class NETItemNode extends TreeNode
{
	private String name;
	private int offset;
	private int length;
	
	
	public NETItemNode(ITreeNode parent, String item)
	{
		super("NETItemNode", parent);
		String[] tmp = item.split("\t");
		this.name = tmp[0];
		this.offset = Integer.parseInt(tmp[1]);
		this.length = Integer.parseInt(tmp[2]);
	}
	
	public String getName() {
		return name;
	}
	
	public int getOffset() {
		return this.offset;
	}
	
	public int getLength() {
		return this.length;
	}
 
	public Image getImage() {
		return null; /* TODO: Return File image */
	}
	
	protected void createChildren(List children) {		
	}
	
	public boolean hasChildren() {		
		return false;
	}
	
	public String toString() {
		return this.getName();
	}

}


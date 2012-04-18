package resultviewer.tree;

import java.util.List;
import org.eclipse.swt.graphics.Image;

import prefuse.Display;

/* Species node */
public class SpeciesNode extends TreeNode
{
	private int id;
	private String name;
	private String expression;
	
	public SpeciesNode(ITreeNode parent, String name)
	{
		super("SpeciesNode", parent);
		this.name = name;
		String[] tmp = name.split(" ");
		this.id = Integer.parseInt(tmp[0]);
		this.expression = tmp[1];
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getExpression() {
		return this.expression;
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


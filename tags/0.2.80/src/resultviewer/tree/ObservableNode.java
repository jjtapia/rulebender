package resultviewer.tree;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.graphics.Image;

import prefuse.Display;

/* Observable node*/
public class ObservableNode extends TreeNode
{
	private int id;
	private String type;
	private String name;
	private String expression;
	private ArrayList<String> component;
	
	public ObservableNode(ITreeNode parent, String info, ArrayList<String> component)
	{
		super("ObservableNode", parent);
		// info contains: id name expression
		String[] tmp = info.trim().split(" ");
		this.id = Integer.parseInt(tmp[0]);
		this.type = tmp[1];
		this.name = tmp[2];
		this.expression = tmp[3];
		this.component = component;				
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getType(){
		return this.type;
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
		children.add(new SpeciesFolderNode(this, expression, component));
	}
	
	public boolean hasChildren() {		
		return true;
	}
	
	public String toString() {
		return this.getName();
	}

}


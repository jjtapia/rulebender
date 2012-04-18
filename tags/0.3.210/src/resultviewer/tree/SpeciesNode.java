package resultviewer.tree;

import java.util.List;
import org.eclipse.swt.graphics.Image;

import prefuse.Display;

/* Species node */
public class SpeciesNode extends TreeNode
{
	private int id;
	private String name;
	private String expression = "";
	
	public SpeciesNode(ITreeNode parent, String name)
	{
		super("SpeciesNode", parent);
		this.name = name;
		String[] tmp = name.split(" ");
		this.id = Integer.parseInt(tmp[0]);
		if (tmp.length > 1) {
			this.expression = tmp[1];
		}
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
	
	/*
	 *  match a string to a species expression
	 *  molecules have to be in the same order
	 */
	
	/*
	public boolean matchExpression(String other) {
		if (this.expression.equalsIgnoreCase(other)) {
			return true;
		}
		else {
			// A().B()
			String[] otherMoleList = other.split("\\.");
			String[] curMoleList = this.expression.split("\\.");
			if (otherMoleList.length != curMoleList.length) {
				return false;
			}
			for(int i = 0; i < curMoleList.length; i++) {
				// match each molecule
				String curMole = curMoleList[i];
				String otherMole = otherMoleList[i];
				
				// match the name
				String curMoleName = curMole.substring(0, curMole.indexOf("("));
				String otherMoleName = otherMole.substring(0, otherMole.indexOf("("));
				if (!curMoleName.equals(otherMoleName)) {
					return false;
				}
				
				// match the bound sites
				String curMoleSite = curMole.substring(curMole.indexOf("(") + 1, curMole.length()-1);
				String otherMoleSite = otherMole.substring(otherMole.indexOf("(") + 1, otherMole.length()-1);
				if (!curMoleSite.contains(otherMoleSite)) {
					return false;
				}
			}
			return true;
		}
	}
	*/
	
 
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


package resultviewer.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/* FOLDER NODE */
public class SpeciesFolderNode extends TreeNode
{
	private String cFolder;
	private ArrayList<String> components;
	
	public SpeciesFolderNode(String folder, ArrayList<String> components) {
		this(null, folder, components);
	}
	
	public SpeciesFolderNode(ITreeNode parent, String folder, ArrayList<String> components) {
		super("SpeciesFolderNode", parent);
		this.cFolder = folder;
		this.components = components;
	}
 
	public String getName() {		
		return cFolder;
	}
	
	public ArrayList<String> getComponents() {
		return this.components;
	}
 
	public Image getImage() {
		return null;    
	}
	
	protected void createChildren(List children) {
		for (int i = 0; i < components.size(); i++) {
			children.add(new SpeciesNode(this, components.get(i)));
		}
	}
}


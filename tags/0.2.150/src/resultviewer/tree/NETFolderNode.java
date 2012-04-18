package resultviewer.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

/* FOLDER NODE */
public class NETFolderNode extends TreeNode
{
	private String cFolder;
	private ArrayList<String> components;
	
	public NETFolderNode(String folder, ArrayList<String> components) {
		this(null, folder, components);
	}
	
	public NETFolderNode(ITreeNode parent, String folder, ArrayList<String> components) {
		super("NETFolderNode", parent);
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
		return null; /* TODO: Return Folder image */
	}
	
	protected void createChildren(List children) {
		for (int i = 0; i < components.size(); i++) {
			children.add(new NETItemNode(this, components.get(i)));
		}
	}
}


package resultviewer.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

/* FOLDER NODE */
public class ObservableFolderNode extends TreeNode
{
	private String cFolder;
	// info contains: id name expression
	private ArrayList<String> info;
	private ArrayList<String>[] components;
	
	public ObservableFolderNode(String folder, ArrayList<String> info, ArrayList<String>[] components) {
		this(null, folder, info, components);
	}
	
	public ObservableFolderNode(ITreeNode parent, String folder, ArrayList<String> info, ArrayList<String>[] components) {
		super("ObservableFolderNode", parent);
		this.cFolder = folder;
		this.info = info;
		this.components = components;
	}
 
	public String getName() {		
		return cFolder;
	}
 
	public Image getImage() {
		return null; /* TODO: Return Folder image */
	}
	
	protected void createChildren(List children) {
		for (int i = 0; i < info.size(); i++) {
			children.add(new ObservableNode(this, info.get(i), components[i]));
		}
	}
}


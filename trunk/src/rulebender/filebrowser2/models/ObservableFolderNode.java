package rulebender.filebrowser2.models;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

/**
 * 
 * Observable Folder Node.
 * Contains ObservableNode children.
 *
 */
public class ObservableFolderNode extends TreeNode
{
	private String cFolder;
	// info contains: id name expression
	private ArrayList<String> info;
	private ArrayList<String>[] components;
	
	/**
	 * 
	 * @param folder name of folder
	 * @param info information to create children (id name expression)
	 * @param components array of list of components string
	 */
	public ObservableFolderNode(String folder, ArrayList<String> info, ArrayList<String>[] components) {
		this(null, folder, info, components);
	}
	
	/**
	 * 
	 * @param parent parent node
	 * @param folder name of folder
	 * @param info information to create children (id name expression)
	 * @param components array of list of components string
	 */
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
		return null;
	}
	
	/**
	 * Create children (ObservableNode) based on info and components.
	 */
	protected void createChildren(List children) {
		for (int i = 0; i < info.size(); i++) {
			children.add(new ObservableNode(this, info.get(i), components[i]));
		}
	}
}


package rulebender.editors.dat.model;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Image;

/**
 * 
 * Observable Folder Node. Contains ObservableNode children.
 * 
 */
public class ObservableFolderNode extends TreeNode {
	private final String cFolder;
	// info contains: id name expression
	private final List<String> names;
	private final List<List<String>> patterns;
	private final List<List<String>> components;

	/**
	 * 
	 * @param folder
	 *          name of folder
	 * @param names
	 *          information to create children (id name expression)
	 * @param components
	 *          array of list of components string
	 */
	public ObservableFolderNode(String folder, List<String> names,
	    List<List<String>> patterns, List<List<String>> components) {
		this(null, folder, names, patterns, components);
	}

	/**
	 * 
	 * @param parent
	 *          parent node
	 * @param folder
	 *          name of folder
	 * @param names
	 *          information to create children (id name expression)
	 * @param components
	 *          array of list of components string
	 */
	public ObservableFolderNode(ITreeNode parent, String folder,
	    List<String> names, List<List<String>> patterns,
	    List<List<String>> components) {
		super("ObservableFolderNode", parent);
		this.cFolder = folder;
		this.names = names;
		this.components = components;
		this.patterns = patterns;
	}

	@Override
	public String getName() {
		return cFolder;
	}

	@Override
	public Image getImage() {
		return null;
	}

	/**
	 * Create children (ObservableNode) based on info and components.
	 */
	@Override
	protected void createChildren(List children) {
		Iterator<String> itNames = names.iterator();
		Iterator<List<String>> itComp = components.iterator();
		Iterator<List<String>> itPat = patterns.iterator();
		while (itNames.hasNext()) {
			children.add(new ObservableNode(this, itNames.next(), itPat.next(),
			    itComp.next()));
		}
		// for (int i = 0; i < info.size(); i++) {
		// children.add(new ObservableNode(this, info.get(i), components.get(i)));
		// }
	}
}

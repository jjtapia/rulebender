package rulebender.navigator.model;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * 
 * Folder Node.
 * Contains FileNode children.
 * 
 */
public class FolderNode extends TreeNode 
{	
	private File fFolder; // actual data object

	private static final Image folderImage = AbstractUIPlugin.imageDescriptorFromPlugin ("rulebender","/icons/views/fldr_obj.gif").createImage();
	private static final Image projectImage = AbstractUIPlugin.imageDescriptorFromPlugin ("rulebender","/icons/views/prj_obj.gif").createImage();
	/**
	 * 
	 * @param folder
	 *            current file (directory)
	 */
	public FolderNode(File folder) {
		this(null, folder);
	}

	/**
	 * 
	 * @param parent
	 *            parent node
	 * @param folder
	 *            current file (directory)
	 */
	public FolderNode(ITreeNode parent, File folder) {
		super("FolderNode", parent);
		fFolder = folder;
	}

	/**
	 * @return name of node
	 */
	public String getName() {
		return fFolder.getName();
	}

	/**
	 * 
	 * @return path of file
	 */
	public String getPath() {
		return fFolder.getPath();
	}

	/**
	 * @return fold image
	 */
	public Image getImage() 
	{
		if(this.getParent().getParent() == null)
		{
			return projectImage;
		}
		else
		{
			return folderImage;
		}
	}

	/**
	 * Create children nodes.
	 */
	protected void createChildren(List children) {
		File[] childFiles = fFolder.listFiles();
		Arrays.sort(childFiles, Collections.reverseOrder());
		for (int i = 0; i < childFiles.length; i++) {
			File childFile = childFiles[i];
			// directory
			if (childFile.isDirectory()) 
			{
				
				if (acceptFolder(childFile.getName())) {
					children.add(new FolderNode(this, childFile));
				}
			}
			// common files
			else {
				// accept all
				if (acceptFolder(childFile.getName())) {
					children.add(new FileNode(this, childFile));
				}
			}
		}
	}

	/**
	 * make a filter for folders
	 * 
	 * @param foldername
	 * @return
	 */
	private boolean acceptFolder(String foldername) 
	{
		if(foldername.substring(0, 1).equals("."))
		{
			return false;
		}
		
		return true;
	}

	/**
	 * 
	 * @param filename
	 * @return whether accept this file based on their types.
	 */
	private boolean acceptFile(String filename) {
		if (filename.endsWith(".net"))
			return true;
		if (filename.endsWith(".cdat"))
			return true;
		if (filename.endsWith(".gdat"))
			return true;
		if (filename.endsWith(".scan"))
			return true;
		if (filename.endsWith(".bngl"))
			return true;
		if (filename.endsWith(".log"))
			return true;
		if (filename.endsWith(".pl"))
			return true;
		if (filename.endsWith(".xml"))
			return true;
		if (filename.endsWith(".m"))
			return true;
		if (filename.endsWith(".rxn"))
			return true;
		if (filename.endsWith(".cfg"))
			return true;
		return false;
	}

	public boolean equals(Object other) {
		if (!(other instanceof FolderNode)) {
			return false;
		}

		if (((FolderNode) other).getPath().equals(this.getPath())) {
			return true;
		}

		return false;
	}

	public int hashCode() {
		String path = this.getPath();
		int hashcode = 0;
		for (int i = 0; i < path.length(); i++) {
			Character ch = path.charAt(i);
			hashcode += ch.hashCode();
		}
		return hashcode;
	}

}

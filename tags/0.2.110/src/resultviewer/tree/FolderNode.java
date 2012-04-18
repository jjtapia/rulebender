package resultviewer.tree;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;

/* FOLDER NODE */
public class FolderNode extends TreeNode
{
	private File fFolder; /* actual data object */
	
	public FolderNode(File folder) {
		this(null, folder);
	}
	
	public FolderNode(ITreeNode parent, File folder) {
		super("FolderNode", parent);
		fFolder = folder;
	}
 
	public String getName() {		
		return fFolder.getName();
	}
 
	public Image getImage() {
		return null; /* TODO: Return Folder image */
	}
	
	protected void createChildren(List children) {
		File[] childFiles = fFolder.listFiles();
		Arrays.sort(childFiles, Collections.reverseOrder());
		for (int i = 0; i < childFiles.length; i++) {
			File childFile = childFiles[i];
			if (childFile.isDirectory()) {
				if (acceptFolder(childFile.getName())) {
					children.add(new FolderNode(this, childFile));
				}
			} else {
				if (acceptFile(childFile.getName())) {
					children.add(new FileNode(this, childFile));
				}
			}
		}
	}
	
	//TODO: make a filter for folders
	private boolean acceptFolder(String foldername) {
		return true;
	}
	
	private boolean acceptFile(String filename) {
		if(filename.endsWith(".net")) 
	    	return true;
	   	if(filename.endsWith(".cdat"))
	   		return true;
	 	if(filename.endsWith(".gdat"))
	    	return true;
	 	if(filename.endsWith(".scan"))
	    	return true;
		return false;
	}	
	
}


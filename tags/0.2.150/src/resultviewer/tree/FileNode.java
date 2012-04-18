package resultviewer.tree;

import java.io.File;
import java.util.List;
import org.eclipse.swt.graphics.Image;

import resultviewer.data.DATFileData;
import resultviewer.data.FileData;
import resultviewer.data.NETFileData;

/* FILE NODE */
public class FileNode extends TreeNode
{
	private File fFile; /* actual data object */
	private FileData fData;
	
	public FileNode(ITreeNode parent, File file)
	{
		super("FileNode", parent);
		fFile = file;
	}
	
	public String getName() {
		String filename = fFile.getName();
		String suffix = null;
		if(filename.endsWith(".net")) 
	    	suffix = "NET";
	   	if(filename.endsWith(".cdat"))
	   		suffix = "CDAT";
	 	if(filename.endsWith(".gdat"))
	    	suffix = "GDAT";
	 	if(filename.endsWith(".scan"))
	    	suffix = "SCAN";
		return (suffix + ": " + filename);
	}
 
	public Image getImage() {
		return null; /* TODO: Return File image */
	}
	
	protected void createChildren(List children) {		
	}
	
	public boolean hasChildren() {		
		return false;
	}
	
	public FileData getFileData() {
		if (fData == null) {
			String filename = fFile.getName();
			if (filename.endsWith(".net")) {
				fData = new NETFileData(this.fFile);
			}
			else if (filename.endsWith(".cdat") || filename.endsWith(".gdat") || filename.endsWith(".scan")) {
				fData = new DATFileData(this.fFile);
			}
		}
		return fData;
	}
}


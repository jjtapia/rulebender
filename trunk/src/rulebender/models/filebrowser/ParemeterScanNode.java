package rulebender.models.filebrowser;

import org.eclipse.swt.graphics.Image;

public class ParemeterScanNode implements FileBrowserTreeNodeInterface 
{

	private FileBrowserTreeNodeInterface m_parent;

	private FileNode m_scanFile;
	
	public ParemeterScanNode(FileBrowserTreeNodeInterface parent, FileNode scanFile)
	{
		m_parent = parent;
		setScanFile(scanFile);
	}
	
	

	public FileBrowserTreeNodeInterface getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public FileBrowserTreeNodeInterface[] getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	
	/*---------------------------Accessors and Mutators----------------------*/
	
	public FileNode getScanFile() 
	{
		return m_scanFile;
	}

	public void setScanFile(FileNode scanFile) {
		this.m_scanFile = scanFile;
	}



	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}
}

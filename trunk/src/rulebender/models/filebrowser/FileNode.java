package rulebender.models.filebrowser;

import java.io.File;

import org.eclipse.swt.graphics.Image;

public class FileNode implements FileBrowserTreeNodeInterface {

	private FileBrowserTreeNodeInterface m_parent;
	private File m_file;
		
	public FileNode(FileBrowserTreeNodeInterface parent, File file)
	{
		m_parent = parent;
		m_file = file;
	}
	
	public FileBrowserTreeNodeInterface getParent() 
	{
		return m_parent;
	}

	public boolean hasChildren()
	{
		return false;
	}
	public FileBrowserTreeNodeInterface[] getChildren() 
	{
		return null;
	}

	public String getName() 
	{
		return "File: " + m_file.getName();
	}

	public Image getImage() 
	{
		return null;
	}

}

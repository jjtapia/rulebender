package rulebender.filebrowser.models;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;

public class ParameterScanCollectionNode implements FileBrowserTreeNodeInterface 
{
	private String m_name = "ParameterScans";

	private FileBrowserTreeNodeInterface m_parent;
	
	private Image m_image = null;
	
	private ArrayList<ParemeterScanNode> m_scans;
	
	public ParameterScanCollectionNode(FileBrowserTreeNodeInterface parent) 
	{
		m_parent = parent;
	}

	public FileBrowserTreeNodeInterface getParent() 
	{
		return m_parent;
	}

	public FileBrowserTreeNodeInterface[] getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() 
	{
		return m_name;
	}

	public Image getImage() 
	{
		return m_image;
	}

	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

}

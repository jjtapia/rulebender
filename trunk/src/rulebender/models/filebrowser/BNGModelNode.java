package rulebender.models.filebrowser;

import org.eclipse.swt.graphics.Image;

public class BNGModelNode implements FileBrowserTreeNodeInterface 
{
	private FileBrowserTreeNodeInterface m_parent;
	
	private String m_name;
	
	private FileNode m_bnglFileNode;
	
	private SimulationCollectionNode m_simulationCollectionNode; 
	
	private ParameterScanCollectionNode m_parameterScanCollectionNode;

	private Image m_image = null;
	
	public BNGModelNode(FileBrowserTreeNodeInterface parent, String name)
	{
		m_name = name;
		m_parent = parent;
	}

	public FileBrowserTreeNodeInterface getParent() 
	{
		return m_parent;
	}

	public boolean hasChildren() 
	{
		return true;
	}

	public Object[] getChildren() 
	{
		//FileBrowserTreeNode[] children = {m_bnglFileNode, m_simulationCollectionNode, m_parameterScanCollectionNode};
		Object[] children = {m_bnglFileNode};
		return children;
		
	}

	public String getName() 
	{
		return "Model: "+m_name;
	}

	public Image getImage() 
	{
		return m_image;
	}
	
	public void setBNGLFileNode(FileNode bnglFileNode)
	{
		m_bnglFileNode = bnglFileNode;
	}
}


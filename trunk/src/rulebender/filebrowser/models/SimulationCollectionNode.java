package rulebender.filebrowser.models;

import org.eclipse.swt.graphics.Image;
import java.util.ArrayList;

public class SimulationCollectionNode implements FileBrowserTreeNodeInterface 
{
	private FileBrowserTreeNodeInterface m_parent;
	
	private String m_name = "Simulations";
	
	private Image m_image = null;
	
	private ArrayList<SimulationNode> m_simulations;
	
	public SimulationCollectionNode(FileBrowserTreeNodeInterface parent)
	{
		m_parent = parent;
	}
	
	public FileBrowserTreeNodeInterface getParent() 
	{
		return m_parent;
	}

	public FileBrowserTreeNodeInterface[] getChildren() 
	{
		return (FileBrowserTreeNodeInterface[]) m_simulations.toArray();
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

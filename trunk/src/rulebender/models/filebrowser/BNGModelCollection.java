package rulebender.models.filebrowser;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;

public class BNGModelCollection implements FileBrowserTreeNodeInterface
{
	private ArrayList<BNGModelNode> m_models;
	private String m_name = "BNGL Models";
	private Image m_image = null;
	
	public BNGModelCollection()
	{
		
	}
	
	public ArrayList<BNGModelNode> getModels()
	{
		return m_models;
	}

	/**
	 * return null because this is the root.
	 */
	public FileBrowserTreeNodeInterface getParent() 
	{
		return null;
	}

	public boolean hasChildren()
	{
		return !m_models.isEmpty();
	}
	
	public Object[] getChildren() 
	{
		return m_models.toArray();
	}

	public String getName() 
	{
		return m_name;
	}

	public Image getImage() 
	{
		return m_image;
	}
	
	public void setModels(ArrayList<BNGModelNode> models)
	{
		m_models = models;
	}
	
	public void addModel(BNGModelNode model)
	{
		if(m_models == null)
		{
			m_models = new ArrayList<BNGModelNode>();
		}
		
		m_models.add(model);
	}
}

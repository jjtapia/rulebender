package rulebender.contactmap.properties;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import prefuse.visual.VisualItem;
import rulebender.core.prefuse.networkviewer.contactmap.VisualRule;
import rulebender.editors.bngl.IBNGLLinkedElement;

public class CompartmentPropertySource implements IPropertySource, IBNGLLinkedElement 
{

	private static final String PROPERTY_NAME = "rulebender.contactmap.properties.compartment";
	private static final String PROPERTY_MOLECULES_PREFIX = "rulebender.contactmap.properties.compartment.molecule_";

	private String m_sourcePath;
	
	private String m_name;

	private ArrayList<String> m_molecules;
	
    private IPropertyDescriptor[] m_propertyDescriptors;
    
	public CompartmentPropertySource(VisualItem item, String sourcePath) 
	{
		m_name = ((String) item.get("compartment"));
	
		if(m_name == null)
		{
			m_name = "None";		
		}
		
		m_sourcePath = sourcePath;
		
		//m_molecules = (ArrayList) item
	}

	@Override
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() 
	{
		if (m_propertyDescriptors == null) 
		{	
			ArrayList<IPropertyDescriptor> propertyDescriptors = new ArrayList<IPropertyDescriptor>();
			
            // Create a descriptor and set a category
			PropertyDescriptor nameDescriptor = new PropertyDescriptor(PROPERTY_NAME, "Compartment Name");
            nameDescriptor.setCategory("Details");
            propertyDescriptors.add(nameDescriptor);
            	
            PropertyDescriptor moleprop = null;
            
            if(m_molecules != null)
            {
	            for(int i = 0; i < m_molecules.size(); i++)
	            {
	            	moleprop = new PropertyDescriptor(PROPERTY_MOLECULES_PREFIX+"_"+i, "Name");
	            	
	            	moleprop.setCategory("Molecules");
	            	propertyDescriptors.add(moleprop);
	            }
            }
            
            m_propertyDescriptors = propertyDescriptors.toArray(new IPropertyDescriptor[]{});
		}
		
		return m_propertyDescriptors;
	}

	@Override
	public Object getPropertyValue(Object id) 
	{
		if(id.equals(PROPERTY_NAME))
		{
			return m_name;
		}
		else if (id instanceof String && ((String) id).contains(PROPERTY_MOLECULES_PREFIX))
		{
			String sid = (String) id;
			int num = Integer.parseInt(sid.substring(sid.indexOf("_")+1));
			
			return m_molecules.get(num);
		}
		return null;
	}

	@Override
	public boolean isPropertySet(Object id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLinkedBNGLPath() 
	{
		return m_sourcePath;
	}

	@Override
	public String getRegex() 
	{
		return m_name;
	}
}

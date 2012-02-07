package rulebender.contactmap.properties;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import prefuse.visual.VisualItem;
import rulebender.core.prefuse.networkviewer.contactmap.VisualRule;

public class StatePropertySource implements IPropertySource 
{

	private static final String PROPERTY_NAME = "rulebender.contactmap.properties.state";
	private static final String PROPERTY_COMPONENT = "rulebender.contactmap.properties.state.component";
	private static final String PROPERTY_MOLECULE = "rulebender.contactmap.properties.state.molecule";
	private static final String PROPERTY_RULES_PREFIX = "rulebender.contactmap.properties.state.rule";
	
	private String m_name;
	private String m_component;
	private String m_molecule;
	
	private ArrayList<VisualRule> m_rules;
	
    private IPropertyDescriptor[] m_propertyDescriptors;
    
	public StatePropertySource(VisualItem item) 
	{
		m_name = ((String) item.get(VisualItem.LABEL)).trim();
		m_component = item.getString("component").trim();
		m_molecule = item.getString("molecule");
		
		m_rules = (ArrayList<VisualRule>) item.get("rules");
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
			PropertyDescriptor nameDescriptor = new PropertyDescriptor(PROPERTY_NAME, "Name");
            nameDescriptor.setCategory("Details");
            propertyDescriptors.add(nameDescriptor);
            
			PropertyDescriptor componentDescriptor = new PropertyDescriptor(PROPERTY_COMPONENT, "Component");
			componentDescriptor.setCategory("Details");
			propertyDescriptors.add(componentDescriptor);
            
            PropertyDescriptor moleculeDescriptor = new PropertyDescriptor(PROPERTY_MOLECULE, "Molecule");
			moleculeDescriptor.setCategory("Details");
			propertyDescriptors.add(moleculeDescriptor);
            
         	//nameDescriptor.setCategory("Label");
            
            PropertyDescriptor ruleprop = null;
            
            if(m_rules != null)
            {
		        for(int i = 0; i < m_rules.size(); i++)
		        {
		        	ruleprop = new PropertyDescriptor(PROPERTY_RULES_PREFIX+"_"+i, m_rules.get(i).getLabel());
		        	
		        	ruleprop.setCategory("Rules");
		        	propertyDescriptors.add(ruleprop);
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
		else if(id.equals(PROPERTY_COMPONENT))
		{
			return m_component;
		}
		else if(id.equals(PROPERTY_MOLECULE))
		{
			return m_molecule;
		}
		else if (id instanceof String && ((String) id).contains(PROPERTY_RULES_PREFIX))
		{
			String sid = (String) id;
			int num = Integer.parseInt(sid.substring(sid.indexOf("_")+1));
			
			return m_rules.get(num).getName();
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
}

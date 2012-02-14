package rulebender.contactmap.properties;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import prefuse.data.Edge;
import prefuse.visual.VisualItem;
import rulebender.core.prefuse.networkviewer.contactmap.VisualRule;

public class EdgePropertySource implements IPropertySource 
{

	private static final String PROPERTY_RULES_PREFIX = "rulebender.contactmap.properties.edge.rule";

	private ArrayList<VisualRule> m_rules;
	
    private IPropertyDescriptor[] m_propertyDescriptors;
    
	public EdgePropertySource(VisualItem item) 
	{
		// Get the edge object that corresponds to the edgeitem
		Edge edge = (Edge) item.getSourceTuple();

		// get the rules that can make that edge.
		m_rules = (ArrayList<VisualRule>) edge.get("rules");
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
			
            PropertyDescriptor compprop = null;
            
            if(m_rules != null)
            {
	            for(int i = 0; i < m_rules.size(); i++)
	            {
	            	compprop = new PropertyDescriptor(PROPERTY_RULES_PREFIX+"_"+i, m_rules.get(i).getLabel());
	            	
	            	compprop.setCategory("Rules");
	            	propertyDescriptors.add(compprop);
	            }
            }
            
            m_propertyDescriptors = propertyDescriptors.toArray(new IPropertyDescriptor[]{});
		}
		
		return m_propertyDescriptors;
	}

	@Override
	public Object getPropertyValue(Object id) 
	{
		if (id instanceof String && ((String) id).contains(PROPERTY_RULES_PREFIX))
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

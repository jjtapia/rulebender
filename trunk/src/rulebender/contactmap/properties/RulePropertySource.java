package rulebender.contactmap.properties;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import prefuse.visual.AggregateItem;
import prefuse.visual.VisualItem;
import rulebender.core.prefuse.networkviewer.contactmap.VisualRule;

public class RulePropertySource implements IPropertySource 
{

	private static final String PROPERTY_LABEL = "rulebender.contactmap.properties.rule";
	private static final String PROPERTY_EXPRESSION = "rulebender.contactmap.properties.rule.expression";
	//private static final String PROPERTY_CENTER_PREFIX = "rulebender.contactmap.properties.rule.center_";
	//private static final String PROPERTY_CONTEXT_PREFIX = "rulebender.contactmap.properties.rule.context_";
	
	private String m_label;
	private String m_expression;

	//private ArrayList<String> m_context;
	//private ArrayList<String> m_center;
	
    private IPropertyDescriptor[] m_propertyDescriptors;
    
	public RulePropertySource(VisualRule sourceRule) 
	{
		m_label = sourceRule.getLabel();
		m_expression = sourceRule.getExpression();
		
		int index = m_expression.indexOf(":");
		if(index >= 0)
		{
			m_expression = m_expression.substring(index+1);
		}
			
		//m_context = new ArrayList<String>();
		//m_center = new ArrayList<String>();
		
		//TODO Maybe add the context and center to the properties.
		//AggregateItem context = sourceRule.getContext();
		//AggregateItem center = sourceRule.getCenter();
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
			   
            PropertyDescriptor moleculeDescriptor = new PropertyDescriptor(PROPERTY_EXPRESSION, "Rule Expression");
			moleculeDescriptor.setCategory("Details");
			propertyDescriptors.add(moleculeDescriptor);
			
            // Create a descriptor and set a category
			PropertyDescriptor nameDescriptor = new PropertyDescriptor(PROPERTY_LABEL, "Rule Label");
            nameDescriptor.setCategory("Details");
            propertyDescriptors.add(nameDescriptor);
         			
			/*
            PropertyDescriptor compprop = null;
            
            if(m_context != null)
            {
	            for(int i = 0; i < m_context.size(); i++)
	            {
	            	compprop = new PropertyDescriptor(PROPERTY_COMPONENTS_PREFIX+"_"+i, "Name");
	            	
	            	compprop.setCategory("Components");
	            	propertyDescriptors.add(compprop);
	            }
            }
            */
            m_propertyDescriptors = propertyDescriptors.toArray(new IPropertyDescriptor[]{});
		}
		
		return m_propertyDescriptors;
	}

	@Override
	public Object getPropertyValue(Object id) 
	{
		if(id.equals(PROPERTY_LABEL))
		{
			return m_label;
		}
		else if(id.equals(PROPERTY_EXPRESSION))
		{
			return m_expression;
		}
		/*
		else if (id instanceof String && ((String) id).contains(PROPERTY_COMPONENTS_PREFIX))
		{
			String sid = (String) id;
			int num = Integer.parseInt(sid.substring(sid.indexOf("_")+1));
			
			return m_context.get(num);
		}*/
		return null;
	}

	@Override
	public boolean isPropertySet(Object id) 
	{
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) 
	{	
	}

	@Override
	public void setPropertyValue(Object id, Object value) 
	{		
	}
}

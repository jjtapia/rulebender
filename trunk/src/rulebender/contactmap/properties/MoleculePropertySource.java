package rulebender.contactmap.properties;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import prefuse.visual.VisualItem;
import rulebender.editors.bngl.IBNGLLinkedElement;

public class MoleculePropertySource implements IPropertySource, IBNGLLinkedElement 
{

	private static final String PROPERTY_NAME = "rulebender.contactmap.properties.molecule";
	private static final String PROPERTY_EXPRESSION = "rulebender.contactmap.properties.molecule.expression";
	private static final String PROPERTY_COMPARTMENT = "rulebender.contactmap.properties.molecule.compartment";
	private static final String PROPERTY_COMPONENTS_PREFIX = "rulebender.contactmap.properties.molecule.component_";
	
	private String m_name;
	private String m_expression;
	private String m_compartment;

	private ArrayList<String> m_components;
	
    private IPropertyDescriptor[] m_propertyDescriptors;
	
    private String m_sourcePath;
    
	public MoleculePropertySource(VisualItem item, String sourcePath) 
	{
		m_name = ((String) item.get("molecule")).trim();
		m_expression = ((String) item.get("molecule_expression")).trim();
		m_compartment = "None";
		m_sourcePath = sourcePath;
		
		if(item.get("compartment") != null && !item.get("compartment").equals("")) 
		{
			m_compartment = (String) item.get("compartment");
		}
						
		//m_components = (ArrayList) item
	}

	@Override
	public Object getEditableValue() 
	{
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() 
	{
		if (m_propertyDescriptors == null) 
		{	
			ArrayList<IPropertyDescriptor> propertyDescriptors = new ArrayList<IPropertyDescriptor>();
			
            // Create a descriptor and set a category
			PropertyDescriptor nameDescriptor = new PropertyDescriptor(PROPERTY_NAME, "Molecule Name");
            nameDescriptor.setCategory("Details");
            propertyDescriptors.add(nameDescriptor);
            
            nameDescriptor.setLabelProvider(new ILabelProvider(){

				@Override
				public void addListener(ILabelProviderListener listener) 
				{
					
				}

				@Override
				public void dispose() 
				{
	
				}

				@Override
				public boolean isLabelProperty(Object element, String property) 
				{
					return false;
				}

				@Override
				public void removeListener(ILabelProviderListener listener) 
				{
					
				}

				@Override
				public Image getImage(Object element) 
				{
					return null;
				}

				@Override
				public String getText(Object element) 
				{
					return null;
				}});
            
            PropertyDescriptor moleculeDescriptor = new PropertyDescriptor(PROPERTY_EXPRESSION, "Molecule Expression");
			moleculeDescriptor.setCategory("Details");
			propertyDescriptors.add(moleculeDescriptor);
			
			PropertyDescriptor compartmentDescriptor = new PropertyDescriptor(PROPERTY_COMPARTMENT, "Containing Compartment");
			moleculeDescriptor.setCategory("Details");
			propertyDescriptors.add(compartmentDescriptor);
			
            PropertyDescriptor compprop = null;
            
            if(m_components != null)
            {
	            for(int i = 0; i < m_components.size(); i++)
	            {
	            	compprop = new PropertyDescriptor(PROPERTY_COMPONENTS_PREFIX+"_"+i, "Name");
	            	
	            	compprop.setCategory("Components");
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
		if(id.equals(PROPERTY_NAME))
		{
			return m_name;
		}
		else if(id.equals(PROPERTY_COMPARTMENT))
		{
			return m_compartment;
		}
		else if(id.equals(PROPERTY_EXPRESSION))
		{
			return m_expression;
		}
		else if (id instanceof String && ((String) id).contains(PROPERTY_COMPONENTS_PREFIX))
		{
			String sid = (String) id;
			int num = Integer.parseInt(sid.substring(sid.indexOf("_")+1));
			
			return m_components.get(num);
		}
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
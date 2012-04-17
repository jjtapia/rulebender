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

/**
 * This class represents a Molecule when it is selected in the contact map.  
 * An instance of this object is passed through the ISelectionService to in 
 * order to give this information to any listening parts.
 * 
 * Implements IPropertySource so that the PropertiesView can display this
 * information.
 * 
 * Implements IBNGLLinkedElement so that the text representation can be utilized 
 * by the BNGLEditor.
 * @author adammatthewsmith
 *
 */

public class MoleculePropertySource implements IPropertySource, IBNGLLinkedElement 
{

	// Each of these strings defines a property of the selected element.
	private static final String PROPERTY_NAME = "rulebender.contactmap.properties.molecule";
	private static final String PROPERTY_EXPRESSION = "rulebender.contactmap.properties.molecule.expression";
	private static final String PROPERTY_COMPARTMENT = "rulebender.contactmap.properties.molecule.compartment";
	private static final String PROPERTY_COMPONENTS_PREFIX = "rulebender.contactmap.properties.molecule.component_";
	
	// The name of the molecule
	private String m_name;
	
	// The expressino of the molecule
	private String m_expression;
	
	// The containing compartment.
	private String m_compartment;

	// The components that are in the molecule.
	private ArrayList<String> m_components;
	
	// For the PropertiesVies 
    private IPropertyDescriptor[] m_propertyDescriptors;
	
    // The source path for the bngl file.
    private String m_sourcePath;
    
    /**
     * Constructor: Takes a visual item and a source path and builds this
     * object with the contained information.
     * 
     * @param item
     * @param sourcePath
     */
	
	public MoleculePropertySource(VisualItem item, String sourcePath) 
	{
		// Set all of the strings by extracting them from the VisualItem.
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

	/**
	 * Returns an array of IPropertyDescriptor objects for the PropertiesView
	 */
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

	/**
	 * Returns a specific property value given its id.
	 */
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

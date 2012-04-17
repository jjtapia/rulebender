package rulebender.contactmap.properties;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import prefuse.visual.VisualItem;
import rulebender.core.prefuse.networkviewer.contactmap.VisualRule;
import rulebender.editors.bngl.IBNGLLinkedElement;

/**
 * This class represents a Component when it is selected in the contact map.  
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

public class ComponentPropertySource implements IPropertySource, IBNGLLinkedElement 
{

	// Each of these strings defines a property of the selected element.
	private static final String PROPERTY_NAME = "rulebender.contactmap.properties.component";
	private static final String PROPERTY_MOLECULE = "rulebender.contactmap.properties.component.molecule";
	//private static final String PROPERTY_COMPARTMENT = "rulebender.contactmap.properties.component.compartment";
	private static final String PROPERTY_STATES_PREFIX = "rulebender.contactmap.properties.rule";
	
	private String m_name;
	private String m_molecule;
	//private String m_compartment;
	
	private String m_sourcePath;

	private ArrayList<String> m_states;
	
    private IPropertyDescriptor[] m_propertyDescriptors;
    
    /**
     * Constructor: Takes a visual item and a source path and builds this
     * object with the contained information.
     * 
     * @param item
     * @param sourcePath
     */
	
	public ComponentPropertySource(VisualItem item, String sourcePath) 
	{
		m_sourcePath = sourcePath;
		m_name = ((String) item.get(VisualItem.LABEL)).trim();
		m_molecule = item.getString("molecule_expression").trim();
		//m_compartment = item.getString("").trim();
		
		m_states = (ArrayList) item.get("states");
	}

	@Override
	public Object getEditableValue() {
		// TODO Auto-generated method stub
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
			PropertyDescriptor nameDescriptor = new PropertyDescriptor(PROPERTY_NAME, "Name");
            nameDescriptor.setCategory("Details");
            propertyDescriptors.add(nameDescriptor);
            
            PropertyDescriptor moleculeDescriptor = new PropertyDescriptor(PROPERTY_MOLECULE, "Molecule");
			moleculeDescriptor.setCategory("Details");
			propertyDescriptors.add(moleculeDescriptor);
            
         	//nameDescriptor.setCategory("Label");
            
            PropertyDescriptor stateprop = null;
            
            if(m_states != null)
            {
	            for(int i = 0; i < m_states.size(); i++)
	            {
	            	stateprop = new PropertyDescriptor(PROPERTY_STATES_PREFIX+"_"+i, "Name");
	            	
	            	stateprop.setCategory("States");
	            	propertyDescriptors.add(stateprop);
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
		/*else if(id.equals(PROPERTY_COMPONENT))
		{
			return m_component;
		}*/
		else if(id.equals(PROPERTY_MOLECULE))
		{
			return m_molecule;
		}
		else if (id instanceof String && ((String) id).contains(PROPERTY_STATES_PREFIX))
		{
			String sid = (String) id;
			int num = Integer.parseInt(sid.substring(sid.indexOf("_")+1));
			
			return m_states.get(num);
		}
		return null;
	}

	@Override
	public boolean isPropertySet(Object id) 
	{
		if(id.equals(PROPERTY_NAME) && !m_name.equals(""))
		{
			return true;
		}
		else if(id.equals(PROPERTY_MOLECULE) && !m_molecule.equals(""))
		{
			return true;
		}
		else if (id instanceof String && ((String) id).contains(PROPERTY_STATES_PREFIX))
		{
			String sid = (String) id;
			int num = Integer.parseInt(sid.substring(sid.indexOf("_")+1));
			
			if(m_states.get(num) != null)
			{
				return true;
			}
		}
	
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
		// only the component from the correct molecule.
		String moleculeStripped = m_molecule.substring(0, m_molecule.indexOf("("));
		
		// if it has all of the molecule information, then strip it. 
		if(m_molecule.contains("("));
		{
			m_molecule.substring(0, m_molecule.indexOf("("));
		}
		
		// Return the formed regular expression.
		return (moleculeStripped +"\\s*\\((\\s|[^\\)])*"+ m_name + "([^\\)]|\\s)*\\)");
		
	}
}

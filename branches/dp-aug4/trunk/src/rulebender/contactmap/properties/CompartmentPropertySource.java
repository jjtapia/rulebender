package rulebender.contactmap.properties;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import prefuse.visual.VisualItem;
import rulebender.core.prefuse.networkviewer.contactmap.VisualRule;
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

public class CompartmentPropertySource implements IPropertySource, IBNGLLinkedElement 
{

	// Each of these strings defines a property of the selected element.
	private static final String PROPERTY_NAME = "rulebender.contactmap.properties.compartment";
	private static final String PROPERTY_MOLECULES_PREFIX = "rulebender.contactmap.properties.compartment.molecule_";

	// Path to the BNGL File
	private String m_sourcePath;
	
	// Name of the compartment
	private String m_name;

	// A list of molecules that are in the compartment.
	private ArrayList<String> m_molecules;
	
	// For the PropertiesView
    private IPropertyDescriptor[] m_propertyDescriptors;
    
    /**
     * Constructor: Takes a visual item and a source path and builds this
     * object with the contained information.
     * 
     * @param item
     * @param sourcePath
     */
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

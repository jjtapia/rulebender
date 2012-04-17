package rulebender.contactmap.properties;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import prefuse.visual.VisualItem;
import rulebender.core.prefuse.networkviewer.contactmap.VisualRule;
import rulebender.editors.bngl.IBNGLLinkedElement;

/**
 * This class represents a state when a state is selected in the contact map.  
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
public class StatePropertySource implements IPropertySource, IBNGLLinkedElement 
{

	// Each of these strings defines a property of the selected element.
	public static final String PROPERTY_NAME = "rulebender.contactmap.properties.state";
	public static final String PROPERTY_COMPONENT = "rulebender.contactmap.properties.state.component";
	public static final String PROPERTY_MOLECULE = "rulebender.contactmap.properties.state.molecule";
	public static final String PROPERTY_RULES_PREFIX = "rulebender.contactmap.properties.state.rule";
	
	// The name of the state
	private String m_name;
	
	// The containing component.
	private String m_component;
	
	// The containing molecule.
	private String m_molecule;

	// An arraylist of the rules (VisualRule) that can change this state.
	private ArrayList<VisualRule> m_rules;
	
	// The path the the BNGL file for the current contact map. 
	private String m_BNGLPath; 
	
	// For use with the PropertyView.
    private IPropertyDescriptor[] m_propertyDescriptors;
    
    /**
     * Constructor: Takes a visual item and a source path and builds this
     * object with the contained information.
     * 
     * @param item
     * @param sourcePath
     */
	public StatePropertySource(VisualItem item, String sourcePath)//, String bnglPath) 
	{
		setName(((String) item.get(VisualItem.LABEL)).trim());
		setComponent(item.getString("component").trim());
		setMolecule(item.getString("molecule"));
		setRules((ArrayList<VisualRule>) item.get("rules"));
		m_BNGLPath = sourcePath;
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
            
            // Add the rules to the properties.
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

	/**
	 * Returns a specific property given it's id. 
	 */
	@Override
	public Object getPropertyValue(Object id) 
	{
		if(id.equals(PROPERTY_NAME))
		{
			return getName();
		}
		else if(id.equals(PROPERTY_COMPONENT))
		{
			return getComponent();
		}
		else if(id.equals(PROPERTY_MOLECULE))
		{
			return getMolecule();
		}
		else if (id instanceof String && ((String) id).contains(PROPERTY_RULES_PREFIX))
		{
			String sid = (String) id;
			int num = Integer.parseInt(sid.substring(sid.indexOf("_")+1));
			
			return getRules().get(num).getExpression();
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

	public String getName() 
	{
		getPropertyDescriptors();
		return m_name;
	}

	private void setName(String m_name) {
		this.m_name = m_name;
	}

	public String getComponent() 
	{
		getPropertyDescriptors();
		return m_component;
	}

	private void setComponent(String m_component) {
		this.m_component = m_component;
	}

	public String getMolecule() 
	{
		getPropertyDescriptors();
		return m_molecule;
	}

	private void setMolecule(String m_molecule) {
		this.m_molecule = m_molecule;
	}

	public ArrayList<VisualRule> getRules() 
	{
		getPropertyDescriptors();
		return m_rules;
	}

	private void setRules(ArrayList<VisualRule> m_rules) 
	{
		this.m_rules = m_rules;
	}

	@Override
	public String getLinkedBNGLPath() 
	{
		return m_BNGLPath;
	}

	@Override
	public String getRegex() {
		return m_component+"~"+m_name;
	}
}

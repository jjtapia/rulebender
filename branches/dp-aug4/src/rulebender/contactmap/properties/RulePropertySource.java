package rulebender.contactmap.properties;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import rulebender.core.prefuse.networkviewer.contactmap.VisualRule;
import rulebender.editors.bngl.IBNGLLinkedElement;

/**
 * This class represents a Rule when a Rule is selected in the contact map.  
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
public class RulePropertySource implements IPropertySource, IBNGLLinkedElement 
{

	// Each of these strings defines a property of the selected element.
	private static final String PROPERTY_LABEL = "rulebender.contactmap.properties.rule";
	private static final String PROPERTY_EXPRESSION = "rulebender.contactmap.properties.rule.expression";
	//private static final String PROPERTY_CENTER_PREFIX = "rulebender.contactmap.properties.rule.center_";
	//private static final String PROPERTY_CONTEXT_PREFIX = "rulebender.contactmap.properties.rule.context_";
	
	// The String label for the Rule
	private String m_label;
	
	// The expression of the rule.
	private String m_expression;
	
	// The source path of the bngl file. 
	private String m_sourcePath;

	//private ArrayList<String> m_context;
	//private ArrayList<String> m_center;

	// For use with the PropertyView.
    private IPropertyDescriptor[] m_propertyDescriptors;
    
    /**
     * Constructor: Takes a visual item and a source path and builds this
     * object with the contained information.
     * 
     * @param item
     * @param sourcePath
     */
	public RulePropertySource(VisualRule sourceRule, String sourcePath) 
	{
		m_label = sourceRule.getLabel();
		m_expression = sourceRule.getExpression();
		
		m_sourcePath = sourcePath;
		
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

	/**
	 * Returns an array of IPropertyDescriptor objects for the PropertiesView
	 */
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

	/**
	 * Returns a specific property value given its id.
	 */
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

	@Override
	public String getLinkedBNGLPath() 
	{
		return m_sourcePath;
	}

	@Override
	public String getRegex()
	{
		String regex = "";
		
		if(m_expression.contains(")"));
		regex = m_expression.substring(0, m_expression.lastIndexOf(")")+1);
	
		String delimiter = System.getProperty("line.separator");
		
		if(regex.contains(delimiter));
		{
			regex = regex.replace(delimiter, "");
		}
		
		// put an optional pair of backslashes between every character.
		// This has to happen before the other special characters are escaped.
		regex = regex.replace("", "\\s*\\\\?\\s*"+delimiter+"?");
		
		// This makes the rule match for either forward or bidirectional.
		//regex = regex.replace("<", "<?");
		
		// Escape the parentheses
		regex = regex.replace("(", "\\(");
		regex = regex.replace(")", "\\)");
		
		// Escape the +
		regex = regex.replace("+", "\\+");
		
		// Escape the !
		regex = regex.replace("!", "\\!");
		
		// Escape the ~
		//regex = regex.replace("~", "\\~");
		
		// Escape the .
		regex = regex.replace(".", "\\.");
		
		return regex;
	}
}

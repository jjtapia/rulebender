package rulebender.contactmap.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import prefuse.data.Edge;
import prefuse.visual.VisualItem;
import rulebender.core.prefuse.networkviewer.contactmap.VisualRule;
import rulebender.editors.bngl.IBNGLLinkedElement;
import rulebender.editors.bngl.IBNGLLinkedElementCollection;

public class EdgePropertySource implements IPropertySource, IBNGLLinkedElementCollection 
{

	private static final String PROPERTY_RULES_PREFIX = "rulebender.contactmap.properties.edge.rule";

	private ArrayList<VisualRule> m_rules;
	private ArrayList<IBNGLLinkedElement> m_bnglPropRules;
	private String m_sourcePath; 
	
    private IPropertyDescriptor[] m_propertyDescriptors;
    
	public EdgePropertySource(VisualItem item, String sourcePath)
	{
		m_sourcePath = sourcePath;
		
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
			
			return m_rules.get(num).getExpression();
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
	public List<IBNGLLinkedElement> getCollection() 
	{
		if(m_bnglPropRules == null)
		{
			m_bnglPropRules = new ArrayList<IBNGLLinkedElement>();

			for(final VisualRule rule : m_rules)
			{
				m_bnglPropRules.add(new IBNGLLinkedElement(){

					@Override
					public String getLinkedBNGLPath() 
					{	
						return m_sourcePath;
					}

					@Override
					public String getRegex() 
					{
						return produceRegexFromRuleText(rule.getExpression());
					}

					private String produceRegexFromRuleText(String expression) 
					{
						String delimiter = System.getProperty("line.separator");
						
						String regex = "";
						
						if(expression.contains(")"));
						{
							regex = expression.substring(0, expression.lastIndexOf(")")+1);
						}
					
						if(regex.contains("\n"));
						{
							regex = regex.replace("\n", "");
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
					}});
			}
		}
		
		return m_bnglPropRules;
	}
}

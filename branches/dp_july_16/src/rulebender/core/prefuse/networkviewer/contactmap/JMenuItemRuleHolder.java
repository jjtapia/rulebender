package rulebender.core.prefuse.networkviewer.contactmap;

import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class JMenuItemRuleHolder extends JMenuItem 
{
	private VisualRule rule;
	
	public JMenuItemRuleHolder(VisualRule ruleIn)
	{
		super(ruleIn.getExpression());
		rule = ruleIn;
	}
	
	public VisualRule getRule()
	{
		return rule;
	}
}

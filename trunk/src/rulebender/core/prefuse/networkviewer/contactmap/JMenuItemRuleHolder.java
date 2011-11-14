package rulebender.core.prefuse.networkviewer.contactmap;

import javax.swing.JMenuItem;

public class JMenuItemRuleHolder extends JMenuItem 
{
	private VisualRule rule;
	
	public JMenuItemRuleHolder(VisualRule ruleIn)
	{
		super(ruleIn.getName());
		rule = ruleIn;
	}
	
	public VisualRule getRule()
	{
		return rule;
	}
}

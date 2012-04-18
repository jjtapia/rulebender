package networkviewer;

import javax.swing.JMenuItem;

import editor.contactmap.Rule;

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

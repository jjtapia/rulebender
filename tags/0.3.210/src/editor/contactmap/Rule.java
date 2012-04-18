package editor.contactmap;

import java.util.ArrayList;

public class Rule 
{
	String name;
	boolean bidirection;
	String rate1;
	String rate2;
	ArrayList<RulePattern> reactantpatterns = new ArrayList<RulePattern>();
	ArrayList<RulePattern> productpatterns = new ArrayList<RulePattern>();
	ArrayList<BondAction> bondactions = new ArrayList<BondAction>();
	//TODO: molecule actions, Non-trivial, refer to BNG bible to see different cases
	
	Rule()
	{
		rate1 = null;
		rate2 = null;
	}
	
	public String getName()
	{
		return name;
	}
}

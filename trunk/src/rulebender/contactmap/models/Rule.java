package rulebender.contactmap.models;

import java.util.ArrayList;

public class Rule 
{
	private String m_label;  // label of the rule, could be empty
	private String m_expression; // expression of rule, including rates
	private boolean bidirection;
	private String rate1;
	private String rate2;
	
	private ArrayList<RulePattern> reactantPatterns;
	private ArrayList<RulePattern> productPatterns;
	private ArrayList<BondAction> bondActions;
	
	//TODO: molecule actions, Non-trivial, refer to BNG bible to see different cases
	
	public Rule()
	{
		setRate1(null);
		setRate2(null);
		
		reactantPatterns = new ArrayList<RulePattern>();
		productPatterns = new ArrayList<RulePattern>();
		bondActions = new ArrayList<BondAction>();
	}
	
	
	public String getLabel() {
		return m_label;
	}


	public void setLabel(String label) {
		this.m_label = label;
	}


	public String getExpression()
	{
		return m_expression;
	}

	public void setExpression(String name) 
	{
		this.m_expression = name;
	}

	public void setBidirection(boolean bidirection) {
		this.bidirection = bidirection;
	}

	public boolean isBidirection() {
		return bidirection;
	}

	public void setRate2(String rate2) {
		this.rate2 = rate2;
	}

	public String getRate2() {
		return rate2;
	}

	public void setRate1(String rate1) {
		this.rate1 = rate1;
	}

	public String getRate1() {
		return rate1;
	}

	
	public void addReactantPattern(RulePattern rulePattern)
	{	
		reactantPatterns.add(rulePattern);
	}
	
	public void addProductPattern(RulePattern rulePattern)
	{
		productPatterns.add(rulePattern);
	}
	
	public void addBondAction(BondAction bondAction)
	{
		bondActions.add(bondAction);
	}
	
	public ArrayList<RulePattern> getReactantpatterns() 
	{
		return reactantPatterns;
	}
	
	public ArrayList<RulePattern> getProductpatterns() 
	{
		return productPatterns;
	}

	public ArrayList<BondAction> getBondactions() 
	{
		return bondActions;
	}
}

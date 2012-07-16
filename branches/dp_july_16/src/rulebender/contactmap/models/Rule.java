package rulebender.contactmap.models;

import java.util.ArrayList;

/**
 * This class represents all of the information for a rule.  
 */
public class Rule 
{
	// label of the rule, could be empty.  (Defined in the bng file as the 
	// text before the colon in a rule.  e.g. Rule_Label : A() -> B())
	private String m_label;  
	
	// expression of rule, including rates
	private String m_expression; 
	
	// Whether or not the rule is bidirectional (-> vs <->)
	private boolean bidirection;
	
	// Forward Rate
	private String rate1;
	
	// Reverse Rate.
	private String rate2;
	
	// The RulePattern (molecules and bonds) that are in the reactants
	private ArrayList<RulePattern> reactantPatterns;

	// The RulePattern (molecules and bonds) that are in the products 
	private ArrayList<RulePattern> productPatterns;
	
	// Bonds that are created or destroyed by the Rule.
	private ArrayList<BondAction> bondActions;
	
	//TODO: molecule actions.  Molecule level actions are the generation 
	// or degredation of molecules, which should be relatively straight-forward
	// to implement as some kind of diff between the reactants and the products. 
	// Right now a lot of the work that needs to be done for this detection is 
	// done in the ContactMapVisual class and it should not be there.  
	
	/**
	 * Basic constructur: just instantiate data structures. 
	 */
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

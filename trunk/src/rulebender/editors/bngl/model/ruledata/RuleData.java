package rulebender.editors.bngl.model.ruledata;

import java.util.ArrayList;

public class RuleData 
{
	private String expression;
	private String name;
	private String rate;
	
	// Each pattern can be 1 or many molecules (if they are bound)
	private ArrayList<RulePatternData> reactantPatternData;
	private ArrayList<RulePatternData> productPatternData;
	private ArrayList<BondActionData> bondActionData;
	
	public RuleData(String nameIn)
	{
		setName(nameIn);
		
		reactantPatternData = new ArrayList<RulePatternData>();
		productPatternData = new ArrayList<RulePatternData>();
		bondActionData = new ArrayList<BondActionData>();
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addReactantPatternData(RulePatternData rpd)
	{
		reactantPatternData.add(rpd);
	}

	public void addProductPatternData(RulePatternData rpd)
	{
		productPatternData.add(rpd);
	}
	
	public void addBondData(String sourceMolIn, String sourceCompIn, int compID1, String sourceStateIn, 
			String targetMolIn, String targetCompIn, int compID2, String targetStateIn, int action)
	{	
		bondActionData.add(new BondActionData(new BondData(sourceMolIn, sourceCompIn, compID1, sourceStateIn, 
				   targetMolIn, targetCompIn, compID2, targetStateIn), action));
	}
	
	public ArrayList<RulePatternData> getProductPatternData() {
		return productPatternData;
	}

	public ArrayList<RulePatternData> getReactantPatternData() 
	{
		return reactantPatternData;
	}

	public ArrayList<BondActionData> getBondActions() 
	{
		
		return bondActionData;
	}	
}

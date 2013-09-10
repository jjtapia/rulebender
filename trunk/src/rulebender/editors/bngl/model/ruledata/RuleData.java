package rulebender.editors.bngl.model.ruledata;

import java.util.ArrayList;

public class RuleData {
	private String m_ruleID;
	private String m_label;
	private String m_rate;

	// Each pattern can be 1 or many molecules (if they are bound)
	private final ArrayList<RulePatternData> m_reactantPatternData;
	private final ArrayList<RulePatternData> m_productPatternData;
	private final ArrayList<BondActionData> m_bondActionData;

	public RuleData(String nameIn) {
		setLabel(nameIn);

		m_reactantPatternData = new ArrayList<RulePatternData>();
		m_productPatternData = new ArrayList<RulePatternData>();
		m_bondActionData = new ArrayList<BondActionData>();
	}

	public String getRate() {
		return m_rate;
	}

	public void setRate(String rate) {
		this.m_rate = rate;
	}

	public String getRuleID() {
		return m_ruleID;
	}

	public void setRuleID(String ruleID) {
		this.m_ruleID = ruleID;
	}

	public String getLabel() {
		return m_label;
	}

	public void setLabel(String name) {
		this.m_label = name;
	}

	public void addReactantPatternData(RulePatternData rpd) {
		m_reactantPatternData.add(rpd);
	}

	public void addProductPatternData(RulePatternData rpd) {
		m_productPatternData.add(rpd);
	}

	public void addBondData(String sourceMolIn, String sourceCompIn, int compID1,
	    String sourceStateIn, String targetMolIn, String targetCompIn,
	    int compID2, String targetStateIn, String action) {
		m_bondActionData.add(new BondActionData(new BondData(sourceMolIn,
		    sourceCompIn, compID1, sourceStateIn, targetMolIn, targetCompIn,
		    compID2, targetStateIn), action));
	}

	public ArrayList<RulePatternData> getProductPatternData() {
		return m_productPatternData;
	}

	public ArrayList<RulePatternData> getReactantPatternData() {
		return m_reactantPatternData;
	}

	public ArrayList<BondActionData> getBondActions() {
		return m_bondActionData;
	}
}

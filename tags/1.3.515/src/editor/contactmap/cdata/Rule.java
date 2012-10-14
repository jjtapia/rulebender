package editor.contactmap.cdata;

import java.util.ArrayList;

public class Rule {
	private String label; // label of the rule, could be empty
	private String name; // expression of rule, including rates
	private boolean bidirection;
	private String rate1;
	private String rate2;
	private ArrayList<RulePattern> reactantpatterns = new ArrayList<RulePattern>();
	private ArrayList<RulePattern> productpatterns = new ArrayList<RulePattern>();
	private ArrayList<BondAction> bondactions = new ArrayList<BondAction>();

	// TODO: molecule actions, Non-trivial, refer to BNG bible to see different
	// cases

	public Rule() {
		setRate1(null);
		setRate2(null);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public void setReactantpatterns(ArrayList<RulePattern> reactantpatterns) {
		this.reactantpatterns = reactantpatterns;
	}

	public ArrayList<RulePattern> getReactantpatterns() {
		return reactantpatterns;
	}

	public void setProductpatterns(ArrayList<RulePattern> productpatterns) {
		this.productpatterns = productpatterns;
	}

	public ArrayList<RulePattern> getProductpatterns() {
		return productpatterns;
	}

	public void setBondactions(ArrayList<BondAction> bondactions) {
		this.bondactions = bondactions;
	}

	public ArrayList<BondAction> getBondactions() {
		return bondactions;
	}
}

package editor.influencegraph.idata;

import java.util.ArrayList;

public class IRule {
	private String label; // label of the rule, could be empty
	private String name; // expression of rule, including rates
	private boolean bidirection;
	private String rate1;
	private String rate2;
	private ArrayList<IRulePattern> reactantpatterns = new ArrayList<IRulePattern>();
	private ArrayList<IRulePattern> productpatterns = new ArrayList<IRulePattern>();
	private ArrayList<IBondAction> bondactions = new ArrayList<IBondAction>();

	// TODO: molecule actions, Non-trivial, refer to BNG bible to see different
	// cases
	public IRule() {
		setRate1(null);
		setRate2(null);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
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

	public void setReactantpatterns(ArrayList<IRulePattern> reactantpatterns) {
		this.reactantpatterns = reactantpatterns;
	}

	public ArrayList<IRulePattern> getReactantpatterns() {
		return reactantpatterns;
	}

	public void setProductpatterns(ArrayList<IRulePattern> productpatterns) {
		this.productpatterns = productpatterns;
	}

	public ArrayList<IRulePattern> getProductpatterns() {
		return productpatterns;
	}

	public void setBondactions(ArrayList<IBondAction> bondactions) {
		this.bondactions = bondactions;
	}

	public ArrayList<IBondAction> getBondactions() {
		return bondactions;
	}
}
package rulebender.editors.bngl.model.ruledata;

public class BondActionData {
	private BondData bondData;
	private String action; // + for add, - for del

	public BondActionData(BondData bondDataIn, String actionIn) {
		setBondData(bondDataIn);
		setAction(actionIn);
	}

	public BondData getBondData() {
		return bondData;
	}

	public void setBondData(BondData bondData) {
		this.bondData = bondData;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}

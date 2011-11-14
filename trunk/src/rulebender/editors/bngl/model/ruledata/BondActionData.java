package rulebender.editors.bngl.model.ruledata;

public class BondActionData 
{
	private BondData bondData;
	private int action; // + for add, - for del
	
	public BondActionData(BondData bondDataIn, int actionIn)
	{
		setBondData(bondDataIn);
		setAction(actionIn);
	}

	public BondData getBondData() {
		return bondData;
	}

	public void setBondData(BondData bondData) {
		this.bondData = bondData;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}
}

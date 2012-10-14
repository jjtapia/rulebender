package editor.contactmap.cdata;

public class BondAction {
	private int bondindex;// start from 0
	private int action;// negative means delete, positive means add

	public void setAction(int action) {
		this.action = action;
	}

	public int getAction() {
		return action;
	}

	public void setBondindex(int bondindex) {
		this.bondindex = bondindex;
	}

	public int getBondindex() {
		return bondindex;
	}
}

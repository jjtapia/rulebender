package rulebender.contactmap.models;

/**
 * The Action class defines whether a bond or a molecule is being created or
 * destroyed.
 */
public class Action {
	// The index of the bond/molecule in the Bond/Molecule list for the model
	private int index;

	// the action performed
	private String action;

	public Action(int index, String actionIn) {
		setAction(actionIn);
		setindex(index);
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAction() {
		return action;
	}

	public void setindex(int index) {
		this.index = index;
	}

	public int getindex() {
		return index;
	}
}
